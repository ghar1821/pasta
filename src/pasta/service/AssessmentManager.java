/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.PASTAUser;
import pasta.domain.form.ReleaseForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.Job;
import pasta.util.ProjectProperties;

/**
 * Assessment manager.
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-31
 *
 */
@Service("assessmentManager")
@Repository
public class AssessmentManager {
	
	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	
	private ExecutionScheduler scheduler;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
	}
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(AssessmentManager.class);
	
	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.AssessmentDAO#getAssessmentList()
	 * @return collection of all assessments
	 */
	public Collection<Assessment> getAssessmentList() {
		return assDao.getAssessmentList();
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.AssessmentDAO#getAssessment(String)
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @return the assessment (null if it does not exist)
	 */
	public Assessment getAssessment(String assessmentName) {
		return assDao.getAssessment(assessmentName);
	}
	

	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.ResultDAO#getAssessmentHistory(String, Assessment)
	 * @param username the name of the user
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @return the collection of submission history for a user for a given assessment
	 */
	public Collection<AssessmentResult> getAssessmentHistory(String username, String assessmentName){
		return resultDAO.getAssessmentHistory(username, getAssessment(assessmentName));
	}

	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.AssessmentDAO#releaseAssessment(String, ReleaseForm)
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @param releaseForm {@link pasta.domain.form.ReleaseForm} 
	 */
	public void releaseAssessment(String assessmentName, ReleaseForm releaseForm)
	{
		assDao.releaseAssessment(assessmentName,releaseForm);
		
	}

	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.AssessmentDAO#removeAssessment(String)
	 * @param assessment the short name (no whitespace) of the assessment
	 */
	public void removeAssessment(String assessment) {
		assDao.removeAssessment(assessment);
	}
	
	/**
	 * Add a new assessment
	 * <p>
	 * Performs the re-linking with unit tests, secret unit tests, hand marking
	 * templates, competitions.
	 * 
	 * Toggles competition to live if they did not previously have a competition
	 * connected with them. Adds the arenas to the execution queue.
	 * 
	 * @param assessmentToAdd the assessment to add.
	 */
	public void addAssessment(Assessment assessmentToAdd) {
		try {

			// unit Tests
			for (WeightedUnitTest test : assessmentToAdd.getUnitTests()) {
				if (assDao.getAllUnitTests().get(test.getUnitTestName().replace(" ", "")) != null) {
					test.setTest(assDao.getAllUnitTests().get(test.getUnitTestName().replace(
							" ", "")));
				}
			}

			// secret unit tests
			for (WeightedUnitTest test : assessmentToAdd.getSecretUnitTests()) {
				if (assDao.getAllUnitTests().get(test.getUnitTestName().replace(" ", "")) != null) {
					test.setTest(assDao.getAllUnitTests().get(test.getUnitTestName().replace(
							" ", "")));
				}
			}
			
			// hand marking
			for (WeightedHandMarking handMarking : assessmentToAdd.getHandMarking()) {
				if (assDao.getHandMarking(handMarking.getHandMarkingName().replace(" ", "")) != null) {
					handMarking.setHandMarking(assDao.getHandMarking(handMarking.getHandMarkingName().replace(
							" ", "")));
				}
			}
			
			// competitions
			for (WeightedCompetition competition : assessmentToAdd.getCompetitions()) {
				if (assDao.getCompetition(competition.getCompName().replace(" ", "")) != null) {
					competition.setCompetition(assDao.getCompetition(competition.getCompName().replace(
							" ", "")));
					
					// if the competition is not already live, add comp/arenas to the scheduler
					if(!assDao.getCompetition(competition.getCompName().replace(" ", "")).isLive()){
						if(assDao.getCompetition(competition.getCompName().replace(" ", "")).isCalculated()){
							// add competition 
							scheduler.save(new Job("PASTACompetitionRunner", 
									assDao.getCompetition(competition.getCompName().replace(" ", "")).getShortName(), 
									assDao.getCompetition(competition.getCompName().replace(" ", "")).getNextRunDate()));
						}
						else{
							// add arenas
							// official
							if(assDao.getCompetition(competition.getCompName().replace(" ", "")).getOfficialArena() != null){
								Arena arena = assDao.getCompetition(competition.getCompName().replace(" ", "")).getOfficialArena();
								scheduler.save(new Job("PASTACompetitionRunner", 
										assDao.getCompetition(competition.getCompName().replace(" ", "")).getShortName()+"#PASTAArena#"+arena.getName(), 
										arena.getNextRunDate()));
							}
							// outstanding
							if(assDao.getCompetition(competition.getCompName().replace(" ", "")).getOutstandingArenas() != null){
								for(Arena arena : assDao.getCompetition(competition.getCompName().replace(" ", "")).getOutstandingArenas()){
									scheduler.save(new Job("PASTACompetitionRunner", 
											assDao.getCompetition(competition.getCompName().replace(" ", "")).getShortName()+"#PASTAArena#"+arena.getName(), 
											arena.getNextRunDate()));
								}
							}
						}
					}
					if (assDao.getAssessment(assessmentToAdd.getShortName()) == null) {
						assDao.getCompetition(
								competition.getCompName().replace(" ", ""))
								.addAssessment(assessmentToAdd);
					} else {
						assDao.getCompetition(
								competition.getCompName().replace(" ", ""))
								.addAssessment(
										assDao.getAssessment(assessmentToAdd
												.getShortName()));
					}
				}
			}

			// add it to the directory structure
			File location = new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/template/assessment/"
					+ assessmentToAdd.getName().replace(" ", ""));
			location.mkdirs();

			PrintStream out = new PrintStream(location.getAbsolutePath()
					+ "/assessmentProperties.xml");
			out.print(assessmentToAdd);
			out.close();

			PrintStream descriptionOut = new PrintStream(
					location.getAbsolutePath() + "/description.html");
			descriptionOut.print(assessmentToAdd.getDescription());
			descriptionOut.close();

			// add it to the list.
			assDao.addAssessment(assessmentToAdd);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Helper method
	 * 
	 * @see pasta.repository.ResultDAO#getLatestResults(String)
	 * @param username the name of the user
	 * @return all of the cached assessment results.
	 */
	public Map<String, AssessmentResult> getLatestResultsForUser(String username){
		return resultDAO.getLatestResults(username);
	}
	
	/**
	 * Get the latest result for the collection of users.
	 * <p>
	 * Gets all of the cached assessment results for every assessment on the system, for the
	 * collection of users given.
	 * 
	 * @param allUsers the collection of {@link pasta.domain.PASTAUser} that are being queried
	 * @return the map (String username , String assessmentName, {@link pasta.domain.result.AssessmentResult} assessmentResults) 
	 */
	public Map<String, Map<String, AssessmentResult>> getLatestResults(Collection<PASTAUser> allUsers){
		Map<String, Map<String, AssessmentResult>> results = new TreeMap<String, Map<String, AssessmentResult>>();
		
		for(PASTAUser user: allUsers){
			Map<String, AssessmentResult> currResultMap = resultDAO.getLatestResults(user.getUsername());
			results.put(user.getUsername(), currResultMap);
		}
		
		return results;
	}
	
	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.ResultDAO#getAsssessmentResult(String, Assessment, String)
	 * @param username the name of the user
	 * @param assessmentName the short name (no whitespace) of the assessment 
	 * @param assessmentDate the date (formatted "yyyy-MM-dd'T'hh-mm-ss"
	 * @return the queried assessment result or null if not available.
	 */
	public AssessmentResult getAssessmentResult(String username, String assessmentName,
			String assessmentDate) {
		return resultDAO.getAsssessmentResult(username, assDao.getAssessment(assessmentName), assessmentDate);
	}
	
	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.AssessmentDAO#getAllAssessmentsByCategory()
	 * @return a map of the categories and the list of all assessments belonging to that category.
	 */
	public Map<String, List<Assessment>> getAllAssessmentsByCategory() {
		return assDao.getAllAssessmentsByCategory();
	}
}
