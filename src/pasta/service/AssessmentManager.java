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

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewAssessmentForm;
import pasta.domain.upload.UpdateAssessmentForm;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UnitTestDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.util.PASTAUtil;
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
	 * @param user the user
	 * @param assessmentId the id of the assessment
	 * @return the collection of submission history for a user for a given assessment
	 */
	public Collection<AssessmentResult> getAssessmentHistory(PASTAUser user, long assessmentId){
		return resultDAO.getAllResultsForUserAssessment(user, assessmentId);
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
	 * @deprecated use updateAssessment(assessment, form) instead
	 */
	@Deprecated
	public void addAssessment(Assessment assessmentToAdd) {
		
		// unlink any unnecessary unit tests, hand marking templates or competitions
		Assessment previousAssessment = ProjectProperties.getInstance().getAssessmentDAO()
				.getAssessment(assessmentToAdd.getId());
		boolean removed = false;
		if(previousAssessment != null) {
			for(WeightedUnitTest test : previousAssessment.getAllUnitTests()) {
				boolean keep = false;
				for(WeightedUnitTest newTest : assessmentToAdd.getAllUnitTests()) {
					if(newTest.getId() == test.getId()) {
						keep = true;
						break;
					}
				}
				if(!keep) {
					test.setAssessment(null);
					test.setTest(null);
					removed = true;
				}
			}
			for(WeightedHandMarking handMarking : previousAssessment.getHandMarking()) {
				boolean keep = false;
				for(WeightedHandMarking newHandMarking : assessmentToAdd.getHandMarking()) {
					if(newHandMarking.getId() == handMarking.getId()) {
						keep = true;
						break;
					}
				}
				if(!keep) {
					handMarking.setAssessment(null);
					handMarking.setHandMarking(null);
					removed = true;
				}
			}
			for(WeightedCompetition competition : previousAssessment.getCompetitions()) {
				boolean keep = false;
				for(WeightedCompetition newCompetition : assessmentToAdd.getCompetitions()) {
					if(newCompetition.getId() == competition.getId()) {
						keep = true;
						break;
					}
				}
				if(!keep) {
					competition.setAssessment(null);
					competition.setCompetition(null);
					removed = true;
				}
			}
			
			if(removed) {
				ProjectProperties.getInstance().getAssessmentDAO().merge(previousAssessment);
			}
		}
		
		// unit Tests
		for (WeightedUnitTest test : assessmentToAdd.getAllUnitTests()) {
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
		
		// competitions
		for (WeightedCompetition competition : assessmentToAdd.getCompetitions()) {
			Competition realComp = ProjectProperties.getInstance().getCompetitionDAO()
					.getCompetition(competition.getCompetition().getId());
			
			competition.setCompetition(realComp);
			competition.setAssessment(assessmentToAdd);
			
			if(ProjectProperties.getInstance().getCompetitionDAO().isRunning(realComp)) {
				if(realComp.isCalculated()) {
					scheduler.scheduleJob(realComp, realComp.getNextRunDate());
				} else {
					Arena arena = realComp.getOfficialArena();
					scheduler.scheduleJob(realComp, arena, arena.getNextRunDate());
					for(Arena outstanding : realComp.getOutstandingArenas()) {
						scheduler.scheduleJob(realComp, outstanding, outstanding.getNextRunDate());
					}
				}
			}
		}
		
		ProjectProperties.getInstance().getAssessmentDAO().merge(assessmentToAdd);
	}

	/**
	 * Helper method
	 * 
	 * @see pasta.repository.ResultDAO#getLatestResults(String)
	 * @param user the user
	 * @return all of the cached assessment results.
	 */
	public Map<Long, AssessmentResult> getLatestResultsForUser(PASTAUser user){
		return resultDAO.getLatestResults(user);
	}
	
	/**
	 * Get the latest result for the collection of users.
	 * <p>
	 * Gets all of the cached assessment results for every assessment on the system, for the
	 * collection of users given.
	 * 
	 * @param allUsers the collection of {@link pasta.domain.PASTAUser} that are being queried
	 * @return the map (Long userId , Long assessmentId, {@link pasta.domain.result.AssessmentResult} assessmentResults) 
	 */
	public Map<PASTAUser, Map<Long, AssessmentResult>> getLatestResults(Collection<PASTAUser> allUsers){
		Map<PASTAUser, Map<Long, AssessmentResult>> results = new TreeMap<>();
		
		for(PASTAUser user: allUsers){
			Map<Long, AssessmentResult> currResultMap = resultDAO.getLatestResults(user);
			results.put(user, currResultMap);
		}
		
		return results;
	}
	
	/**
	 * Gets an assessment result given a user, assessment and formatted submission date.
	 * 
	 * @param user the user
	 * @param assessmentId the id of the assessment 
	 * @param assessmentDate the date (formatted "yyyy-MM-dd'T'hh-mm-ss")
	 * @return the queried assessment result or null if not available.
	 */
	public AssessmentResult loadAssessmentResult(PASTAUser user, long assessmentId,
			String assessmentDate) {
		AssessmentResult result;
		try {
			result = resultDAO.getAssessmentResult(user, assessmentId, PASTAUtil.parseDate(assessmentDate));
		} catch (ParseException e) {
			logger.error("Error parsing date", e);
			return null;
		}
		
		return result;
	}
	
	public AssessmentResult getAssessmentResult(long id) {
		return ProjectProperties.getInstance().getResultDAO().getAssessmentResult(id);
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

	public void updateComment(long resultId, String newComment) {
		AssessmentResult result = getAssessmentResult(resultId);
		result.setComments(newComment);
		ProjectProperties.getInstance().getResultDAO().update(result);
	}

	public void updateAssessmentResults(AssessmentResult result) {
		ProjectProperties.getInstance().getResultDAO().update(result);
	}

	public AssessmentResult getLatestAssessmentResult(PASTAUser user, long assessmentId) {
		List<AssessmentResult> allResults = ProjectProperties.getInstance().getResultDAO()
				.getAllResultsForUserAssessment(user, assessmentId);
		if(allResults == null || allResults.isEmpty()) {
			return null;
		}
		return allResults.get(0);
	}

	public Assessment addAssessment(NewAssessmentForm form) {
		Assessment assessment = new Assessment();
		assessment.setName(form.getName());
		assessment.setMarks(form.getMarks());
		assessment.setNumSubmissionsAllowed(form.getMaxSubmissions());
		assessment.setDueDate(form.getDueDate());
		
		ProjectProperties.getInstance().getAssessmentDAO().saveOrUpdate(assessment);
		return assessment;
	}
	
	public void updateAssessment(Assessment assessment, UpdateAssessmentForm form) {
		assessment.setName(form.getName());
		assessment.setCategory(form.getCategory());
		assessment.setDueDate(form.getDueDate());
		assessment.setMarks(form.getMarks());
		assessment.setNumSubmissionsAllowed(form.getNumSubmissionsAllowed());
		assessment.setCountUncompilable(form.isCountUncompilable());
		assessment.setDescription(form.getDescription());
		
		for(WeightedUnitTest test : form.getNewUnitTests()) {
			test.setSecret(false);
		}
		for(WeightedUnitTest test : form.getNewSecretUnitTests()) {
			test.setSecret(true);
		}
		
		// unlink any unnecessary unit tests
		{
			Collection<WeightedUnitTest> toRemove = CollectionUtils.subtract(assessment.getAllUnitTests(), form.getAllUnitTests());
			Collection<WeightedUnitTest> toAdd = CollectionUtils.subtract(form.getAllUnitTests(), assessment.getAllUnitTests());	
			assessment.removeUnitTests(toRemove);
			assessment.addUnitTests(toAdd);
		}
		
		// unlink any unnecessary hand marking templates
		{
			Collection<WeightedHandMarking> toRemove = CollectionUtils.subtract(assessment.getHandMarking(), form.getNewHandMarking());	
			Collection<WeightedHandMarking> toAdd = CollectionUtils.subtract(form.getNewHandMarking(), assessment.getHandMarking());
			assessment.removeHandMarkings(toRemove);
			assessment.addHandMarkings(toAdd);
		}
		
		// unlink any unnecessary competitions
		{
			Collection<WeightedCompetition> toRemove = CollectionUtils.subtract(assessment.getCompetitions(), form.getNewCompetitions());	
			Collection<WeightedCompetition> toAdd = CollectionUtils.subtract(form.getNewCompetitions(), assessment.getCompetitions());
			assessment.removeCompetitions(toRemove);
			assessment.addCompetitions(toAdd);
		}
		
		// link weighted unit tests to unit test and assessment
		for (WeightedUnitTest test : assessment.getAllUnitTests()) {
			test.setTest(unitTestDAO.getUnitTest(test.getTest().getId()));
			test.setAssessment(assessment);
		}
	
		// link weighted hand marking to hand marking template and assessment
		for (WeightedHandMarking handMarking : assessment.getHandMarking()) {
			HandMarking realTemplate = ProjectProperties.getInstance().getHandMarkingDAO()
					.getHandMarking(handMarking.getHandMarking().getId());
			handMarking.setHandMarking(realTemplate);
			handMarking.setAssessment(assessment);
		}
		
		// link weighted competitions to competition and assessment
		for (WeightedCompetition competition : assessment.getCompetitions()) {
			Competition realComp = ProjectProperties.getInstance().getCompetitionDAO()
					.getCompetition(competition.getCompetition().getId());
			
			competition.setCompetition(realComp);
			competition.setAssessment(assessment);
			
			// schedule new jobs if necessary
			if(ProjectProperties.getInstance().getCompetitionDAO().isRunning(realComp)) {
				if(realComp.isCalculated()) {
					scheduler.scheduleJob(realComp, realComp.getNextRunDate());
				} else {
					Arena arena = realComp.getOfficialArena();
					scheduler.scheduleJob(realComp, arena, arena.getNextRunDate());
					for(Arena outstanding : realComp.getOutstandingArenas()) {
						scheduler.scheduleJob(realComp, outstanding, outstanding.getNextRunDate());
					}
				}
			}
		}
		
		ProjectProperties.getInstance().getAssessmentDAO().saveOrUpdate(assessment);
	}
}
