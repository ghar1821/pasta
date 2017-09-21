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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.form.NewAssessmentForm;
import pasta.domain.form.UpdateAssessmentForm;
import pasta.domain.template.Assessment;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.user.PASTAUser;
import pasta.repository.AssessmentDAO;
import pasta.repository.UnitTestDAO;
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
	private RatingManager ratingManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ReleaseManager releaseManager;
	
	@Autowired
	private UnitTestDAO unitTestDAO;
	

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
	
	public Collection<Assessment> getReleasedAssessments(PASTAUser user) {
		List<Assessment> allAssessments = new LinkedList<>(assDao.getAssessmentList());
		return allAssessments.stream()
				.filter(assessment -> assessment.isReleasedTo(user))
				.collect(Collectors.toList());
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
	public boolean removeAssessment(long assessmentId) {
		if(releaseManager.isAssessmentLinked(assessmentId)) {
			//TODO explain to user that you can't delete an assessment that is used in a release rule
			return false;
		}
		groupManager.deleteAllAssessmentGroups(assessmentId);
		ratingManager.deleteAllRatingsForAssessment(assessmentId);
		resultManager.deleteAllResultsForAssessment(assessmentId);
		userManager.deleteAllExtensionsForAssessment(assessmentId);
		
		assDao.removeAssessment(assessmentId);
		return true;
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
		assessment.setLateDate(form.getLateDate());
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
