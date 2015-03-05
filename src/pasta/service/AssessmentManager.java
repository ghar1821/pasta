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
import java.util.Set;
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
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UnitTestDAO;
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
	
	@Autowired
	private UnitTestDAO unitTestDAO;
	
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
	 * @see pasta.repository.AssessmentDAO#getAssessment(long)
	 * @param assessmentId the id of the assessment
	 * @return the assessment (null if it does not exist)
	 */
	public Assessment getAssessment(long assessmentId) {
		return assDao.getAssessment(assessmentId);
	}
	
	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.ResultDAO#getAssessmentHistory(String, Assessment)
	 * @param username the name of the user
	 * @param assessmentId the id of the assessment
	 * @return the collection of submission history for a user for a given assessment
	 */
	public Collection<AssessmentResult> getAssessmentHistory(String username, long assessmentId){
		return resultDAO.getAssessmentHistory(username, getAssessment(assessmentId));
	}
	
	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.AssessmentDAO#releaseAssessment(long, ReleaseForm)
	 * @param assessmentId the id of the assessment
	 * @param releaseForm {@link pasta.domain.form.ReleaseForm} 
	 */
	public void releaseAssessment(long assessmentId, ReleaseForm releaseForm)
	{
		assDao.releaseAssessment(assessmentId,releaseForm);
	}
	
	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.AssessmentDAO#removeAssessment(long)
	 * @param assessmentId the id of the assessment
	 */
	public void removeAssessment(long assessmentId) {
		assDao.removeAssessment(assessmentId);
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
		
		// unit Tests
		for (WeightedUnitTest test : assessmentToAdd.getUnitTests()) {
			test.setTest(unitTestDAO.getUnitTest(test.getTest().getId()));
			test.setAssessment(assessmentToAdd);
		}
		
		// secret unit tests
		for (WeightedUnitTest test : assessmentToAdd.getSecretUnitTests()) {
			test.setTest(unitTestDAO.getUnitTest(test.getTest().getId()));
			test.setAssessment(assessmentToAdd);
		}
	
		// hand marking
		for (WeightedHandMarking handMarking : assessmentToAdd.getHandMarking()) {
			HandMarking realTemplate = ProjectProperties.getInstance().getHandMarkingDAO()
					.getHandMarking(handMarking.getHandMarking().getId());
			handMarking.setHandMarking(realTemplate);
			handMarking.setAssessment(assessmentToAdd);
		}
		
		ProjectProperties.getInstance().getAssessmentDAO().merge(assessmentToAdd);
//		if(assessmentToAdd.getId() == 0) {
//			ProjectProperties.getInstance().getAssessmentDAO().save(assessmentToAdd);
//		} else {
//		}
			
//			// competitions
//			for (WeightedCompetition compeition : assessmentToAdd.getCompetitions()) {
//				if (assDao.getCompetition(compeition.getCompName().replace(" ", "")) != null) {
//					compeition.setTest(assDao.getCompetition(compeition.getCompName().replace(
//							" ", "")));
//					
//					// if the competition is not already live, add comp/arenas to the scheduler
//					if(!assDao.getCompetition(compeition.getCompName().replace(" ", "")).isLive()){
//						if(assDao.getCompetition(compeition.getCompName().replace(" ", "")).isCalculated()){
//							// add competition 
//							scheduler.save(new Job("PASTACompetitionRunner", 
//									assDao.getCompetition(compeition.getCompName().replace(" ", "")).getShortName(), 
//									assDao.getCompetition(compeition.getCompName().replace(" ", "")).getNextRunDate()));
//						}
//						else{
//							// add arenas
//							// official
//							if(assDao.getCompetition(compeition.getCompName().replace(" ", "")).getOfficialArena() != null){
//								Arena arena = assDao.getCompetition(compeition.getCompName().replace(" ", "")).getOfficialArena();
//								scheduler.save(new Job("PASTACompetitionRunner", 
//										assDao.getCompetition(compeition.getCompName().replace(" ", "")).getShortName()+"#PASTAArena#"+arena.getName(), 
//										arena.getNextRunDate()));
//							}
//							// outstanding
//							if(assDao.getCompetition(compeition.getCompName().replace(" ", "")).getOutstandingArenas() != null){
//								for(Arena arena : assDao.getCompetition(compeition.getCompName().replace(" ", "")).getOutstandingArenas()){
//									scheduler.save(new Job("PASTACompetitionRunner", 
//											assDao.getCompetition(compeition.getCompName().replace(" ", "")).getShortName()+"#PASTAArena#"+arena.getName(), 
//											arena.getNextRunDate()));
//								}
//							}
//						}
//					}
//					if (assDao.getAssessment(assessmentToAdd.getId()) == null) {
//						assDao.getCompetition(
//								compeition.getCompName().replace(" ", ""))
//								.addAssessment(assessmentToAdd);
//					} else {
//						assDao.getCompetition(
//								compeition.getCompName().replace(" ", ""))
//								.addAssessment(
//										assDao.getAssessment(assessmentToAdd
//												.getId()));
//					}
//				}
//			}

//			// add it to the directory structure
//			File location = new File(ProjectProperties.getInstance()
//					.getProjectLocation()
//					+ "/template/assessment/"
//					+ assessmentToAdd.getName().replace(" ", ""));
//			location.mkdirs();
//
//			PrintStream out = new PrintStream(location.getAbsolutePath()
//					+ "/assessmentProperties.xml");
//			out.print(assessmentToAdd);
//			out.close();
//
//			PrintStream descriptionOut = new PrintStream(
//					location.getAbsolutePath() + "/description.html");
//			descriptionOut.print(assessmentToAdd.getDescription());
//			descriptionOut.close();
	}

	/**
	 * Helper method
	 * 
	 * @see pasta.repository.ResultDAO#getLatestResults(String)
	 * @param username the name of the user
	 * @return all of the cached assessment results.
	 */
	public Map<Long, AssessmentResult> getLatestResultsForUser(String username){
		return resultDAO.getLatestResults(username);
	}
	
	/**
	 * Get the latest result for the collection of users.
	 * <p>
	 * Gets all of the cached assessment results for every assessment on the system, for the
	 * collection of users given.
	 * 
	 * @param allUsers the collection of {@link pasta.domain.PASTAUser} that are being queried
	 * @return the map (String username , Long assessmentId, {@link pasta.domain.result.AssessmentResult} assessmentResults) 
	 */
	public Map<String, Map<Long, AssessmentResult>> getLatestResults(Collection<PASTAUser> allUsers){
		Map<String, Map<Long, AssessmentResult>> results = new TreeMap<String, Map<Long, AssessmentResult>>();
		
		for(PASTAUser user: allUsers){
			Map<Long, AssessmentResult> currResultMap = resultDAO.getLatestResults(user.getUsername());
			results.put(user.getUsername(), currResultMap);
		}
		
		return results;
	}
	
	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.ResultDAO#getAsssessmentResult(String, Assessment, String)
	 * @param username the name of the user
	 * @param assessmentId the id of the assessment 
	 * @param assessmentDate the date (formatted "yyyy-MM-dd'T'hh-mm-ss"
	 * @return the queried assessment result or null if not available.
	 */
	public AssessmentResult getAssessmentResult(String username, long assessmentId,
			String assessmentDate) {
		return resultDAO.getAsssessmentResult(username, assDao.getAssessment(assessmentId), assessmentDate);
	}
	
	/**
	 * Helper method.
	 * 
	 * @see pasta.repository.AssessmentDAO#getAllAssessmentsByCategory()
	 * @return a map of the categories and the list of all assessments belonging to that category.
	 */
	public Map<String, Set<Assessment>> getAllAssessmentsByCategory() {
		return assDao.getAllAssessmentsByCategory();
	}
}
