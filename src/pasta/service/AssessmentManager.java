/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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
import pasta.repository.HandMarkingDAO;
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
	
	@Autowired private AssessmentDAO assDao;
	
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
	@Autowired
	private HandMarkingDAO handMarkingDAO;
	

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
		
		assDao.saveOrUpdate(assessment);
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
		
		// Update weighted unit test details
		for (WeightedUnitTest test : assessment.getAllUnitTests()) {
			for (WeightedUnitTest formTest : form.getSelectedUnitTests()) {
				if(formTest.getId() == test.getId()) {
					test.setGroupWork(formTest.isGroupWork());
					test.setSecret(formTest.isSecret());
					test.setWeight(formTest.getWeight());
					break;
				}
			}
		}
		
		// Update weighted hand marking details
		for (WeightedHandMarking hm : assessment.getHandMarking()) {
			for (WeightedHandMarking formHm : form.getSelectedHandMarking()) {
				if(formHm.getId() == hm.getId()) {
					hm.setGroupWork(formHm.isGroupWork());
					hm.setWeight(formHm.getWeight());
					break;
				}
			}
		}
		
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
			HandMarking realTemplate = handMarkingDAO.getHandMarking(handMarking.getHandMarking().getId());
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
		
		assDao.merge(assessment);
	}

	public boolean hasGroupWork(Assessment assessment) {
		return !assessment.isOnlyIndividualWork();
	}

	public boolean isAllGroupWork(Assessment assessment) {
		return assessment.isOnlyGroupWork();
	}
}
