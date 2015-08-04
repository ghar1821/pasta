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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.form.NewAssessmentForm;
import pasta.domain.form.UpdateAssessmentForm;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.repository.AssessmentDAO;
import pasta.repository.UnitTestDAO;
import pasta.scheduler.ExecutionScheduler;
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
	
	@Autowired
	private ResultManager resultManager;
	@Autowired
	private GroupManager groupManager;
	
	@Autowired
	private UnitTestDAO unitTestDAO;
	@Autowired
	private ExecutionScheduler scheduler;
	
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
	
	public List<Long> getAssessmentIDList() {
		return assDao.getAssessmentIDList();
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
	 * Gets all assessments by category, including only the assessment
	 * categories that can be viewed by the user.
	 * 
	 * @param includeTutorOnly
	 *            whether to include categories that are marked as being
	 *            tutor-only assessments.
	 * @return a map of the categories and the list of all assessments belonging
	 *         to that category.
	 */
	public Map<String, Set<Assessment>> getAllAssessmentsByCategory(boolean includeTutorOnly) {
		Map<String, Set<Assessment>> allCategories = assDao.getAllAssessmentsByCategory();
		Map<String, String> keyReplacements = new HashMap<>();
		for(String key : allCategories.keySet()) {
			if(key.startsWith(Assessment.TUTOR_CATEGORY_PREFIX)) {
				keyReplacements.put(key, key.substring(Assessment.TUTOR_CATEGORY_PREFIX.length()));
			}
		}
		for(Map.Entry<String, String> replaceKey : keyReplacements.entrySet()) {
			String oldKey = replaceKey.getKey();
			String newKey = replaceKey.getValue();
			Set<Assessment> oldSet = allCategories.remove(oldKey);
			if(includeTutorOnly) {
				if(allCategories.containsKey(newKey)) {
					allCategories.get(newKey).addAll(oldSet);
				} else {
					allCategories.put(newKey, oldSet);
				}
			}
		}
		return allCategories;
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
		assessment.setSolutionName(form.getSolutionName());
		
		assessment.setSubmissionLanguages(form.getLanguages());
		
		if(form.getGroupCount() != -1) {
			int groupCount = groupManager.getGroupCount(assessment);
			if(groupCount > form.getGroupCount()) {
				groupManager.removeExtraGroups(assessment, groupCount - form.getGroupCount());
			}
		}
		
		assessment.setGroupLockDate(form.getGroupLockDate());
		assessment.setGroupCount(form.getGroupCount());
		assessment.setGroupSize(form.getGroupSize());
		assessment.setStudentsManageGroups(form.isStudentsManageGroups());
		
		// unlink any unnecessary unit tests
		{
			Collection<WeightedUnitTest> toRemove = CollectionUtils.subtract(assessment.getAllUnitTests(), form.getSelectedUnitTests());
			Collection<WeightedUnitTest> toAdd = CollectionUtils.subtract(form.getSelectedUnitTests(), assessment.getAllUnitTests());	
			assessment.removeUnitTests(toRemove);
			assessment.addUnitTests(toAdd);
		}
		
		// unlink any unnecessary hand marking templates
		{
			Collection<WeightedHandMarking> toRemove = CollectionUtils.subtract(assessment.getHandMarking(), form.getSelectedHandMarking());	
			Collection<WeightedHandMarking> toAdd = CollectionUtils.subtract(form.getSelectedHandMarking(), assessment.getHandMarking());
			assessment.removeHandMarkings(toRemove);
			assessment.addHandMarkings(toAdd);
		}
		
		// unlink any unnecessary competitions
		{
			Collection<WeightedCompetition> toRemove = CollectionUtils.subtract(assessment.getCompetitions(), form.getSelectedCompetitions());	
			Collection<WeightedCompetition> toAdd = CollectionUtils.subtract(form.getSelectedCompetitions(), assessment.getCompetitions());
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
		
		if(form.getValidatorFile() != null && !form.getValidatorFile().isEmpty()) {
			String location = ProjectProperties.getInstance().getAssessmentValidatorLocation() + assessment.getId() + "/";
			File unzipTo = new File(location);
			if(unzipTo.exists()) {
				FileUtils.deleteQuietly(unzipTo);
			}
			unzipTo.mkdirs();
			String filename = form.getValidatorFile().getOriginalFilename();
			try {
				File newLocation = new File(unzipTo, filename);
				form.getValidatorFile().transferTo(newLocation);
				assessment.setCustomValidatorName(filename);
			} catch (IllegalStateException | IOException e) {
				logger.error("Cannot save validator to disk.", e);
			}
		}
		
		ProjectProperties.getInstance().getAssessmentDAO().saveOrUpdate(assessment);
	}

	public boolean hasGroupWork(Assessment assessment) {
		return !assessment.isOnlyIndividualWork();
	}

	public boolean isAllGroupWork(Assessment assessment) {
		return assessment.isOnlyGroupWork();
	}
}
