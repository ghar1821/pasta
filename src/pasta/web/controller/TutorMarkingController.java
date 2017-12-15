package pasta.web.controller;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pasta.domain.UserPermissionLevel;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.AssessmentResultSummary;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.service.AssessmentManager;
import pasta.service.GroupManager;
import pasta.service.HandMarkingManager;
import pasta.service.ResultManager;
import pasta.service.UserManager;
import pasta.util.PASTAUtil;
import pasta.web.WebUtils;

/**
 * Controller class for marking of submission functions.
 * <p>
 * Handles mappings of $PASTAUrl$/mark/...
 * <p>
 * 
 * @author Martin McGrane
 * @version 1.0
 * @since 23 Sep 2016
 */
@Controller
@RequestMapping("mark/")
public class TutorMarkingController {

	public TutorMarkingController() {
	}

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private AssessmentDAO assessmentDAO;
	@Autowired
	private ResultDAO resultDAO;

	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentManager assessmentManager;
	@Autowired
	private ResultManager resultManager;
	@Autowired
	private HandMarkingManager handMarkingManager;
	@Autowired
	private GroupManager groupManager;

	@ModelAttribute("assessmentResult")
	public AssessmentResult returnAssessmentResultModel() {
		return new AssessmentResult();
	}
	
	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	/**
	 * $PASTAUrl$/mark/{studentUsername}/{assessmentId}/{assessmentDate}/
	 * <p>
	 * Mark the submission for a student. If the user has not authenticated:
	 * redirect to login. If the user is not a tutor: redirect to home.
	 * ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>student</td>
	 * <td>{@link pasta.domain.user.PASTAUser} the user being viewed</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentName</td>
	 * <td>the name of the assessment</td>
	 * </tr>
	 * <tr>
	 * <td>node</td>
	 * <td>The root {@link pasta.domain.FileTreeNode} for the root of the
	 * submitted code</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentResult</td>
	 * <td>The {@link pasta.domain.result.AssessmentResult} for this
	 * assessment</td>
	 * </tr>
	 * <tr>
	 * <td>handMarkingList</td>
	 * <td>The list of {@link pasta.domain.template.WeightedHandMarking}</td>
	 * </tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * <li>assessment/mark/handMark</li>
	 * </ul>
	 * 
	 * @param studentUsername the user which you want to download the submission
	 *          for
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or
	 *         "assessment/mark/handMark"
	 */
	@RequestMapping(value = "{studentUsername}/{assessmentId}/{assessmentDate}/")
	public String handMarkAssessment(@PathVariable("studentUsername") String username,
			@PathVariable("assessmentId") long assessmentId, @PathVariable("assessmentDate") String assessmentDate,
			Model model) {

		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser viewedUser = userManager.getUser(username);
		if (viewedUser == null) {
			return "redirect:/home/";
		}

		Assessment assessment = assessmentDAO.getAssessment(assessmentId);
		String assessmentName = assessment.getName();
		model.addAttribute("assessmentName", assessmentName);

		AssessmentResult result = resultManager.loadAssessmentResult(viewedUser, assessmentId, assessmentDate);
		model.addAttribute("assessmentResult", result);
		Date due = result.getAssessment().getDueDate();
		Date submitted = result.getSubmissionDate();
		if (submitted.after(due)) {
			model.addAttribute("lateString", " (" + PASTAUtil.dateDiff(due, submitted) + " late)");
		}
		model.addAttribute("handMarkingResultList", result.getHandMarkingResults());

		PASTAUser student;
		if (result.isGroupResult()) {
			student = groupManager.getGroup(viewedUser, assessmentId);
		} else {
			student = viewedUser;
		}
		if (student == null) {
			return "redirect;/home/";
		}
		model.addAttribute("student", student);
		
		if(student.isGroup()) {
			PASTAGroup group = groupManager.getGroup(student.getId());
			Map<String, String> extensions = new HashMap<>();
			for(PASTAUser member : group.getMembers()) {
				Date extension = userManager.getExtension(member, assessment);
				if(extension != null) {
					extensions.put(member.getUsername(), PASTAUtil.formatDateReadable(extension));
				}
			}
			model.addAttribute("studentsInGroup", group.getMembers());
			model.addAttribute("studentsInGroupExtensions", extensions);
		}

		model.addAttribute("node", PASTAUtil.generateFileTree(student, assessmentId, assessmentDate));

		return "assessment/mark/handMark";
	}

	/**
	 * $PASTAUrl$/mark/{studentUsername}/{assessmentId}/{assessmentDate}/ - POST
	 * <p>
	 * Save the hand marking for a submission for a student If the user has not
	 * authenticated: redirect to login. If the user is not a tutor: redirect to
	 * home. Otherwise: update the hand marking results and comments using
	 * {@link pasta.service.HandMarkingManager#saveHandMarkingResults(String, long, String, List)}
	 * and
	 * {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * 
	 * @param studentUsername the user which you want to download the submission
	 *          for
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param form the assessment result object
	 * @param result the binding result used for feedback
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "{studentUsername}/{assessmentId}/{assessmentDate}/", method = RequestMethod.POST)
	public String saveHandMarkAssessment(@PathVariable("studentUsername") String username,
			@PathVariable("assessmentId") long assessmentId, @PathVariable("assessmentDate") String assessmentDate,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form, BindingResult result, Model model) {

		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser viewedUser = userManager.getUser(username);
		if (viewedUser == null) {
			return "redirect:/home/";
		}

		AssessmentResult assessResult = resultManager.loadAssessmentResult(viewedUser, assessmentId,
				assessmentDate);

		// rebinding hand marking results with their hand marking templates
		List<HandMarkingResult> results = form.getHandMarkingResults();
		for (HandMarkingResult currResult : results) {
			currResult.setWeightedHandMarking(
					handMarkingManager.getWeightedHandMarking(currResult.getWeightedHandMarking().getId()));
		}

		assessResult.setComments(form.getComments());
		assessResult.setHandMarkingResults(results);
		resultManager.updateAssessmentResults(assessResult);
		updateResultSummary(viewedUser, assessResult);

		return "redirect:/mirror/";
	}

	/**
	 * $PASTAUrl$/mark/{assessmentId}/{studentIndex}/ - POST and GET
	 * <p>
	 * Used for the batch marking of the students in your class(es). If the user
	 * has not authenticated: redirect to login. If the user is not a tutor:
	 * redirect to home. POST: Save the
	 * {@link pasta.domain.result.AssessmentResult} form for the last user you
	 * marked using
	 * {@link pasta.service.HandMarkingManager#saveHandMarkingResults(String, long, String, List)}
	 * and
	 * {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * GET: Takes you to the hand marking template(s) to the user. If the
	 * studentIndex is pointing to a student that has no submissions, go to the
	 * next one. If the index is past the end of the array, go back to home.
	 * ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>assessmentId</td>
	 * <td>the id of the assessment</td>
	 * </tr>
	 * <tr>
	 * <td>student</td>
	 * <td>{@link pasta.domain.user.PASTAUser} of the student being viewed</td>
	 * </tr>
	 * <tr>
	 * <td>node</td>
	 * <td>The root {@link pasta.domain.FileTreeNode} of the submitted files</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentResult</td>
	 * <td>The {@link pasta.domain.result.AssessmentResult} for this
	 * assessment</td>
	 * </tr>
	 * <tr>
	 * <td>handMarkingList</td>
	 * <td>The list of {@link pasta.domain.template.WeightedHandMarking}</td>
	 * </tr>
	 * <tr>
	 * <td>savingStudentIndex</td>
	 * <td>A string representation of the index of the student you are saving</td>
	 * </tr>
	 * <tr>
	 * <td>hasSubmission</td>
	 * <td>Flag to say whether the current student has a submission</td>
	 * </tr>
	 * <tr>
	 * <td>completedMarking</td>
	 * <td>Flag to say whether the marking is complete for this student</td>
	 * </tr>
	 * <tr>
	 * <td>myStudents</td>
	 * <td>The list of {@link pasta.domain.user.PASTAUser} of the users that
	 * belong in your class(es)</td>
	 * </tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * <li>assessment/mark/handMarkBatch</li>
	 * </ul>
	 * 
	 * @param studentIndex the index of the student being viewed
	 * @param assessmentId the id of the assessment
	 * @param student the student that you have just marked and is getting saved
	 * @param form the assessment result form to be saved
	 * @param request the http request that's not being used
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or
	 *         "assessment/mark/handMarkBatch"
	 */
	@RequestMapping(value = "{assessmentId}/{studentIndex}/",
			method = { RequestMethod.POST, RequestMethod.GET })
	public String handMarkAssessmentBatch(@ModelAttribute("user") PASTAUser user,
			@PathVariable("studentIndex") String studentIndex, @PathVariable("assessmentId") long assessmentId,
			@RequestParam(value = "student", required = false) String student,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser viewedUser = userManager.getUser(student);

		model.addAttribute("assessmentId", assessmentId);

		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if (assessment == null) {
			return "redirect:/home/.";
		}
		model.addAttribute("assessmentName", assessment.getName());

		Collection<PASTAUser> myUsers = userManager.getTutoredStudents(user);
		Collection<PASTAGroup> myGroups = groupManager.getGroups(myUsers, assessmentId);
		PASTAUser[] myStudents = myUsers.toArray(new PASTAUser[0]);
		PASTAGroup[] myStudentGroups = myGroups.toArray(new PASTAGroup[0]);

		PASTAUser[] allUsers = new PASTAUser[myStudents.length + myStudentGroups.length];
		for (int i = 0; i < allUsers.length; i++) {
			allUsers[i] = i < myStudents.length ? myStudents[i] : myStudentGroups[i - myStudents.length];
		}
		model.addAttribute("allUsers", allUsers);

		// if submitted
		if (form != null && viewedUser != null) {

			// save changes
			List<HandMarkingResult> results = form.getHandMarkingResults();
			for (HandMarkingResult currResult : results) {
				currResult.setWeightedHandMarking(
						handMarkingManager.getWeightedHandMarking(currResult.getWeightedHandMarking().getId()));
			}

			AssessmentResult assessResult = resultManager.getLatestAssessmentResult(viewedUser, assessmentId);
			assessResult.setComments(form.getComments());
			assessResult.setHandMarkingResults(results);
			resultManager.updateAssessmentResults(assessResult);
			updateResultSummary(viewedUser, assessResult);
		}

		boolean[] hasSubmission = new boolean[allUsers.length];
		boolean[] completedMarking = new boolean[allUsers.length];
		int lastIndex = -1;
		for (int i = 0; i < allUsers.length; ++i) {
			PASTAUser thisUser = allUsers[i];
			AssessmentResult latestResult = resultManager.getLatestAssessmentResult(thisUser, assessmentId);
			hasSubmission[i] = (thisUser != null && latestResult != null);
			if (hasSubmission[i]) {
				lastIndex = i;
			}
			if (hasSubmission[i]) {
				completedMarking[i] = latestResult.isFinishedHandMarking();
			}
		}

		// get the current student's submission
		try {
			int i_studentIndex = Integer.parseInt(studentIndex);
			if (i_studentIndex >= 0 && i_studentIndex < allUsers.length && allUsers[i_studentIndex] != null
					&& hasSubmission[i_studentIndex]) {

				PASTAUser currStudent = allUsers[i_studentIndex];
				model.addAttribute("student", currStudent);

				AssessmentResult result = resultManager.getLatestAssessmentResult(currStudent, assessmentId);
				model.addAttribute("node",
						PASTAUtil.generateFileTree(currStudent, assessmentId, result.getFormattedSubmissionDate()));
				model.addAttribute("assessmentResult", result);
				Date due = result.getAssessment().getDueDate();
				Date submitted = result.getSubmissionDate();
				if (submitted.after(due)) {
					model.addAttribute("lateString", " (" + PASTAUtil.dateDiff(due, submitted) + " late)");
				}
				model.addAttribute("handMarkingList", result.getAssessment().getHandMarking());
				model.addAttribute("handMarkingResultList", result.getHandMarkingResults());
				model.addAttribute("savingStudentIndex", i_studentIndex);
				model.addAttribute("hasSubmission", hasSubmission);
				model.addAttribute("completedMarking", completedMarking);

				model.addAttribute("myStudents", myStudents);
				model.addAttribute("myStudentGroups", myStudentGroups);
				
				if(currStudent.isGroup()) {
					PASTAGroup group = groupManager.getGroup(currStudent.getId());
					Map<String, String> extensions = new HashMap<>();
					for(PASTAUser member : group.getMembers()) {
						Date extension = userManager.getExtension(member, assessment);
						if(extension != null) {
							extensions.put(member.getUsername(), PASTAUtil.formatDateReadable(extension));
						}
					}
					model.addAttribute("studentsInGroup", group.getMembers());
					model.addAttribute("studentsInGroupExtensions", extensions);
				}

				if (i_studentIndex == lastIndex) {
					model.addAttribute("last", "last");
				}

			} else {
				return "redirect:/home/";
			}

		} catch (NumberFormatException e) {
			return "redirect:/home/";
		}

		return "assessment/mark/handMarkBatch";
	}

	@RequestMapping(value = "{assessmentId}/", method = RequestMethod.GET)
	public String handMarkAssessmentBatchStart(@ModelAttribute("user") PASTAUser user,
			@PathVariable("assessmentId") long assessmentId, HttpServletRequest request, Model model) {

		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		// find the first student or group with a submission
		int i = 0;
		Collection<PASTAUser> myUsers = userManager.getTutoredStudents(user);
		for (PASTAUser student : myUsers) {
			if (resultManager.getLatestAssessmentResult(student, assessmentId) != null) {
				return "redirect:" + request.getServletPath() + i + "/";
			}
			++i;
		}
		Collection<PASTAGroup> groups = groupManager.getGroups(myUsers, assessmentId);
		for (PASTAGroup group : groups) {
			if (resultManager.getLatestAssessmentResult(group, assessmentId) != null) {
				return "redirect:" + request.getServletPath() + i + "/";
			}
			++i;
		}
		return "redirect:/home/.";
	}

	private void updateResultSummary(PASTAUser user, AssessmentResult assessResult) {
		// Update the assessment summaries for user/group members if this is the
		// latest submission
		AssessmentResult latest = resultDAO.getLatestIndividualResult(user, assessResult.getAssessment().getId());
		if (latest == null) {
			latest = resultDAO.getLatestGroupResult(user, assessResult.getAssessment().getId());
		}

		if (latest.getSubmissionDate().equals(assessResult.getSubmissionDate())) {
			if (assessResult.isGroupResult()) {
				logger.info("Summary update request; is Group Result");
				// Update for group ID
				resultManager.saveOrUpdate(
						new AssessmentResultSummary(groupManager.getGroup(user, assessResult.getAssessment()),
								assessResult.getAssessment(), assessResult.getPercentage()));
				// Update for all group members
				for (PASTAUser groupMember : groupManager.getGroup(user, assessResult.getAssessment()).getMembers()) {
					logger.info("Summary update request; updating for " + groupMember);
					resultManager.saveOrUpdate(new AssessmentResultSummary(groupMember, assessResult.getAssessment(),
							assessResult.getPercentage()));
				}
			} else {
				resultManager.saveOrUpdate(
						new AssessmentResultSummary(user, assessResult.getAssessment(), assessResult.getPercentage()));
			}
		}
	}
}
