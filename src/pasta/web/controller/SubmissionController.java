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

package pasta.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pasta.domain.FileTreeNode;
import pasta.domain.UserPermissionLevel;
import pasta.domain.form.NewUnitTestForm;
import pasta.domain.form.Submission;
import pasta.domain.form.validate.SubmissionValidator;
import pasta.domain.ratings.AssessmentRating;
import pasta.domain.ratings.RatingForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.scheduler.AssessmentJob;
import pasta.scheduler.ExecutionEstimator;
import pasta.scheduler.ExecutionScheduler;
import pasta.service.AssessmentManager;
import pasta.service.GroupManager;
import pasta.service.RatingManager;
import pasta.service.ResultManager;
import pasta.service.SubmissionManager;
import pasta.service.UserManager;
import pasta.service.ValidationManager;
import pasta.service.validation.SubmissionValidationResult;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;
import pasta.web.WebUtils;

/**
 * Controller class for the submission (pretty much other, I just dump
 * everything that didn't fit in the other controllers in here) functions.
 * <p>
 * Handles mappings of $PASTAUrl$/...
 * <p>
 * Both students and teaching staff can access this url.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
@Controller
@RequestMapping("/")
public class SubmissionController {

	public SubmissionController() {
	}

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private SubmissionManager manager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentManager assessmentManager;
	@Autowired
	private ResultManager resultManager;
	@Autowired
	private RatingManager ratingManager;
	@Autowired
	private GroupManager groupManager;
	@Autowired
	private ExecutionScheduler scheduler;
	@Autowired
	private ValidationManager validationManager;
	
	@Autowired
	private SubmissionValidator submissionValidator;

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newUnitTestModel")
	public NewUnitTestForm returnNewUnitTestModel() {
		return new NewUnitTestForm();
	}

	@ModelAttribute("submission")
	public Submission returnSubmissionModel() {
		return new Submission();
	}

	@ModelAttribute("assessmentResult")
	public AssessmentResult returnAssessmentResultModel() {
		return new AssessmentResult();
	}
	
	@ModelAttribute("ratingForms")
	public Map<Long, RatingForm> loadRatingForms() {
		Map<Long, RatingForm> forms = new HashMap<Long, RatingForm>();
		
		PASTAUser user = WebUtils.getUser();
		if(user == null) {
			return null;
		}
		
		for(Assessment assessment : assessmentManager.getAssessmentList()) {
			AssessmentRating rating = ratingManager.getRating(assessment, user);
			if(rating == null) {
				rating = new AssessmentRating(assessment, user);
			}
			forms.put(assessment.getId(), new RatingForm(rating));
		}
		
		return forms;
	}
	
	@ModelAttribute("ratingForm")
	public RatingForm loadRatingForm() {
		return new RatingForm();
	}
	
	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HOME //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/home/
	 * <p>
	 * The home screen.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * ATTRIBTUES:
	 * <table>
	 * <tr>
	 * <td>assessments</td>
	 * <td>map of all assessments by category (key of category: String, value of
	 * list of {@link pasta.domain.template.Assessment}</td>
	 * </tr>
	 * <tr>
	 * <td>results</td>
	 * <td>The results as a map (key of id of assessment: Long, value of
	 * {@link pasta.domain.result.AssessmentResult}</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>user/tutorHome - if the user is a tutor</li>
	 * <li>user/studentHome - if the user is a student</li>
	 * </ul>
	 * 
	 * @param model
	 *            the model being used
	 * @param session
	 *            the http session that may hold the binding result information.
	 * @return "redirect:/login/" or "user/tutorHome" or "user/studentHome"
	 */
	@RequestMapping(value = "home/")
	public String home(@ModelAttribute("user")PASTAUser user, Model model, HttpServletRequest request, HttpSession session) {
		WebUtils.ensureLoggedIn(request);

		model.addAttribute("results", resultManager.getLatestResultsIncludingGroups(user));
		
		Map<String, Set<Assessment>> allAssessments = assessmentManager.getAllAssessmentsByCategory(user.isTutor());
		Iterator<String> itCategories = allAssessments.keySet().iterator();
		while(itCategories.hasNext()) {
			String category = itCategories.next();
			Iterator<Assessment> itAssessments = allAssessments.get(category).iterator();
			while(itAssessments.hasNext()) {
				Assessment a = itAssessments.next();
				if(!a.isReleasedTo(user)) {
					itAssessments.remove();
				}
			}
			if(allAssessments.get(category).isEmpty()) {
				itCategories.remove();
			}
		}
		model.addAttribute("assessments", allAssessments);
		
		
		Map<Long, String> dueDates = new HashMap<Long, String>();
		Map<Long, Boolean> hasExtension = new HashMap<>();
		Map<Long, Boolean> closed = new HashMap<>();
		Map<Long, Boolean> hasGroupWork = new HashMap<>();
		Map<Long, Boolean> allGroupWork = new HashMap<>();
		for(Assessment assessment : assessmentManager.getAssessmentList()) {
			Date due = userManager.getDueDate(user, assessment);
			String formatted = PASTAUtil.formatDateReadable(due);
			dueDates.put(assessment.getId(), formatted);
			Date extension = userManager.getExtension(user, assessment);
			hasExtension.put(assessment.getId(), extension != null);
			closed.put(assessment.getId(), assessment.isClosedFor(user, extension));
			boolean userHasGroup = groupManager.getGroup(user, assessment) != null;
			boolean assessmentHasGroupWork = userHasGroup && assessmentManager.hasGroupWork(assessment);
			hasGroupWork.put(assessment.getId(), assessmentHasGroupWork);
			allGroupWork.put(assessment.getId(), assessmentHasGroupWork && assessmentManager.isAllGroupWork(assessment));
		}
		model.addAttribute("dueDates", dueDates);
		model.addAttribute("hasExtension", hasExtension);
		model.addAttribute("closed", closed);
		model.addAttribute("hasGroupWork", hasGroupWork);
		model.addAttribute("allGroupWork", allGroupWork);
		
		if (user.isTutor()) {
			return "user/tutorHome";
		} else {
			return "user/studentHome";
		}
	}

	/**
	 * $PASTAUrl/home/ - POST
	 * <p>
	 * Submit an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * FAIL CONDITIONS:
	 * <table>
	 * <tr>
	 * <td>Submission.NoFile</td>
	 * <td>The form doesn't contain a file</td>
	 * <tr>
	 * <td>Submission.AfterClosingDate</td>
	 * <td>The submission is past the due date</td>
	 * </tr>
	 * <tr>
	 * <td>Submission.NoAttempts</td>
	 * <td>There are no attempts left to this user</td>
	 * </tr>
	 * </table>
	 * 
	 * If the submission is validated correctly and doesn't encounter any of the
	 * fail conditions, it's submitted using
	 * {@link pasta.service.SubmissionManager#submit(String, Submission)}
	 * 
	 * Then it's redirected to $PASTAUrl$/mirror/ to clear the form submission
	 * buffer such that pressing reload has no chance of resubmitting your form.
	 * 
	 * @param form
	 *            the submission form holding the code
	 * @param result
	 *            the binding result used for feedback
	 * @param model
	 *            the model being used
	 * @param session
	 *            the http session used for allowing passing the binding result
	 *            along while using $PASTAUrl$/mirror/ to redirect and such that
	 *            you will not resubmit the form if you refresh the page
	 * @return "redirect:/login/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "home/", method = RequestMethod.POST)
	public String submitAssessment(@ModelAttribute("user")PASTAUser user, @ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model, RedirectAttributes attr, HttpSession session) {
		return doSubmitAssessment(user, null, form, result, model, attr, session);
	}
	
	private String doSubmitAssessment(@ModelAttribute("user")PASTAUser user, String submitForUsername, Submission form, BindingResult result, Model model, RedirectAttributes attr, HttpSession session) {
		form.setSubmittingUser(user);
		
		Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		form.setSubmissionDate(now.getTime());
		
		PASTAUser forUser = user;
		if(submitForUsername != null) {
			if(!user.isTutor()) {
				return "redirect:/home/";
			}
			forUser = userManager.getUser(submitForUsername);
			if(forUser == null) {
				return "redirect:/home/";
			}
		}
		
		if(form.isGroupSubmission()) {
			PASTAGroup group = groupManager.getGroup(forUser, form.getAssessment());
			if(group == null) {
				result.reject("NoGroup");
			} else {
				forUser = group;
			}
		}
		
		submissionValidator.validate(forUser, form, result);
		if(result.hasErrors()) {
			attr.addFlashAttribute("submission", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.submission", result);
			return "redirect:.";
		}
		
		Assessment assessment = ProjectProperties.getInstance().getAssessmentDAO()
					.getAssessment(form.getAssessment());
		
		SubmissionValidationResult validationResult = validationManager.validate(forUser, assessment, form);
		session.setAttribute("validationResults", validationResult);
		if(validationResult != null && validationResult.hasErrors()) {
			return "redirect:/mirror/";
		}
		
		logger.info(assessment.getName()
				+ " submitted for " + forUser.getUsername() + " by " + user.getUsername());
		manager.submit(forUser, form);
		
		return "redirect:/mirror/";
	}
	
	@RequestMapping("**/home/clearValidationResults/") 
	@ResponseBody
	public String clearValidationResults(@RequestParam(value="part", required=false) String part, HttpSession session) {
		SubmissionValidationResult result = (SubmissionValidationResult) session.getAttribute("validationResults");
		if(result == null) {
			return "SUCCESS";
		}
		boolean success = false;
		if(part.equals("error")) {
			result.setErrors(null);
			success = true;
		} else if(part.equals("feedback")) {
			result.setFeedback(null);
			success = true;
		} 
		if(part == null || (!result.hasErrors() && !result.hasFeedback())) {
			session.removeAttribute("validationResults");
			success = true;
		}
		return success ? "SUCCESS" : "";
	}
	
	@RequestMapping("checkJobQueue/{assessmentId}/") 
	@ResponseBody
	public String checkJobQueue(@ModelAttribute("user")PASTAUser user, @PathVariable("assessmentId") long assessmentId) {
		return doCheckJobQueue(user, assessmentId);
	}
	
	@RequestMapping("student/{studentUsername}/checkJobQueue/{assessmentId}/") 
	@ResponseBody
	public String checkJobQueue(@PathVariable("assessmentId") long assessmentId, 
			@PathVariable("studentUsername") String username) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser forUser = userManager.getUser(username);
		if(forUser == null) {
			return "error";
		}
		return doCheckJobQueue(forUser, assessmentId);
	}

	private String doCheckJobQueue(PASTAUser forUser, long assessmentId) {
		List<AssessmentJob> jobs = scheduler.getAssessmentQueue();
		if(jobs == null || jobs.isEmpty()) {
			return "";
		}
		PASTAGroup userGroup = groupManager.getGroup(forUser, assessmentId);
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();
		
		long totalTime = 0;
		ArrayNode positions = mapper.createArrayNode();
		for(int i = 0; i < jobs.size(); i++) {
			AssessmentJob job = jobs.get(i);
			if(i == 0) {
				result.put("current", job.getId());
			}
			totalTime += ExecutionEstimator.estimateTime(job);
			if(job.getAssessmentId() == assessmentId
					&& (job.getUser().equals(forUser) ||
							(userGroup != null && job.getUser().equals(userGroup)))) {
				ObjectNode positionNode = mapper.createObjectNode();
				positionNode.put("position", i+1);
				positionNode.put("estimatedComplete", totalTime);
				positionNode.put("running", job.isRunning());
				positions.add(positionNode);
			}
		}
		
		result.set("positions", positions);
		return result.toString();
	}
	
	@RequestMapping("utResults/{assessmentId}/") 
	public String loadUtResults(Model model,
			@ModelAttribute("user")PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			@RequestParam("detailsLink") String detailsLink,
			@RequestParam("summary") boolean summary,
			@RequestParam("separateGroup") boolean separateGroup) {
		return doLoadUtResults(model, user, assessmentId, detailsLink, summary, separateGroup);
	}
	
	@RequestMapping("student/{studentUsername}/utResults/{assessmentId}/") 
	public String loadUtResults(Model model,
			@PathVariable("assessmentId") long assessmentId, 
			@PathVariable("studentUsername") String username,
			@RequestParam("detailsLink") String detailsLink,
			@RequestParam("summary") boolean summary,
			@RequestParam("separateGroup") boolean separateGroup) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser forUser = userManager.getUser(username);
		if(forUser == null) {
			return "error";
		}
		return doLoadUtResults(model, forUser, assessmentId, detailsLink, summary, separateGroup);
	}
	
	private String doLoadUtResults(Model model, PASTAUser forUser, long assessmentId, String detailsLink, boolean summary, boolean separateGroup) {
		model.addAttribute("results", resultManager.getLatestResultIncludingGroup(forUser, assessmentId));
		model.addAttribute("detailsLink", detailsLink);
		model.addAttribute("summary", summary);
		model.addAttribute("separateGroup", separateGroup);
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		Date extension = userManager.getExtension(forUser, assessment);
		model.addAttribute("closedAssessment", assessment.isClosedFor(forUser, extension));
		return "assessment/results/resultsPartial";
	}
	
	@RequestMapping("latestMark/{assessmentId}/") 
	@ResponseBody
	public String checkLatestMark(@ModelAttribute("user")PASTAUser user, @PathVariable("assessmentId") long assessmentId) {
		return getLatestMark(user, assessmentId, !user.isTutor());
	}
	
	@RequestMapping("student/{studentUsername}/latestMark/{assessmentId}/") 
	@ResponseBody
	public String checkLatestMark(@PathVariable("assessmentId") long assessmentId, 
			@PathVariable("studentUsername") String username) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser forUser = userManager.getUser(username);
		if(forUser == null) {
			return "error";
		}
		return getLatestMark(forUser, assessmentId, true);
	}
	
	private String getLatestMark(PASTAUser user, long assessmentId, boolean hide) {
		AssessmentResult results = resultManager.getLatestResultIncludingGroup(user, assessmentId);
		if(results == null) {
			return "0";
		}
		if(hide && (!results.isFinishedHandMarking() || 
				(results.getAssessment().isClosed() && !results.getAssessment().getSecretUnitTests().isEmpty()))) {
			return "???";
		}
		double marks = results.getMarks();
		if(marks % 1 == 0) {
			return String.valueOf((int)marks);
		}
		return String.valueOf(Math.round(results.getMarks() * 1000.0) / 1000.0);
	}

	/**
	 * $PASTAUrl$/info/{assessmentId}/
	 * <p>
	 * View the details and history for an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>assessment</td>
	 * <td>{@link pasta.domain.template.Assessment} the assessment</td>
	 * </tr>
	 * <tr>
	 * <td>history</td>
	 * <td>Collection of {@link pasta.domain.result.AssessmentResult} for all of
	 * the submissions</td>
	 * </tr>
	 * <tr>
	 * <td>nodeList</td>
	 * <td>Map of {@link pasta.domain.FileTreeNode} with the key as the date of
	 * the submission and the value being the root node of the code that was
	 * submitted.</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>user/viewAssessment</li>
	 * </ul>
	 * 
	 * @param assessmentId
	 *            the id of the assessment
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "user/viewAssessment"
	 */
	@RequestMapping(value = "info/{assessmentId}/")
	public String viewAssessmentInfo(@ModelAttribute("user") PASTAUser user, @PathVariable("assessmentId") long assessmentId, Model model) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(assessment == null) {
			return "redirect:/home/";
		}

		model.addAttribute("assessment", assessment);
		model.addAttribute("closed", assessment.isClosedFor(user, userManager.getExtension(user, assessment)));
		model.addAttribute("extension", userManager.getExtension(user, assessment));
		
		List<AssessmentResult> history = resultManager.getAssessmentHistory(user, assessmentId);
		model.addAttribute("history", history);
		
		Map<Long, String> lateString = new HashMap<Long, String>();
		for(AssessmentResult result : history) {
			Date due = result.getAssessment().getDueDate();
			Date submitted = result.getSubmissionDate();
			if(submitted.after(due)) {
				lateString.put(result.getId(), " (" + PASTAUtil.dateDiff(due, submitted) + " late)");
			}
		}
		model.addAttribute("lateString", lateString);
		
		Map<String, FileTreeNode> nodes = PASTAUtil.generateFileTree(user, assessmentId);
		PASTAUser group = groupManager.getGroup(user, assessmentId);
		if(group != null) {
			nodes.putAll(PASTAUtil.generateFileTree(group, assessmentId));
		}
		model.addAttribute("nodeList", nodes);

		return "user/viewAssessment";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// VIEW //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/student/{studentUsername}/info/{assessmentId}/updateComment/ - POST
	 * <p>
	 * Update the comment for a given user for a given assessment for a given
	 * submission using
	 * {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * 
	 * @param newComment
	 *            the new comment
	 * @param assessmentDate
	 *            the assessment date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param studentUsername
	 *            the name of the user
	 * @param assessmentId
	 *            the id of the assessment
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../"
	 */
	@RequestMapping(value = "student/{studentUsername}/info/{assessmentId}/updateComment/", method = RequestMethod.POST)
	public String updateComment(@RequestParam("newComment") String newComment,
			@RequestParam("resultId") long resultId, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		resultManager.updateComment(resultId, newComment);
		return "redirect:../";
	}

	/**
	 * $PASTAUrl$/student/{studentUsername}/home/
	 * <p>
	 * Submit an assessment for another user.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * FAIL CONDITIONS:
	 * <table>
	 * <tr>
	 * <td>Submission.NoFile</td>
	 * <td>The form doesn't contain a file</td>
	 * </tr>
	 * </table>
	 * 
	 * If the submission is validated correctly and doesn't encounter any of the
	 * fail conditions, it's submitted using
	 * {@link pasta.service.SubmissionManager#submit(String, Submission)}
	 * 
	 * Then it's redirected to $PASTAUrl$/mirror/ to clear the form submission
	 * buffer such that pressing reload has no chance of resubmitting your form.
	 * 
	 * @param form
	 *            the submission form holding the code
	 * @param result
	 *            the binding result used for feedback
	 * @param model
	 *            the model being used
	 * @param session
	 *            the http session used for allowing passing the binding result
	 *            along while using $PASTAUrl$/mirror/ to redirect and such that
	 *            you will not resubmit the form if you refresh the page
	 * @return "redirect:/login/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "student/{studentUsername}/home/", method = RequestMethod.POST)
	public String submitAssessment(@ModelAttribute("user") PASTAUser user, @PathVariable("studentUsername") String username,
			@ModelAttribute(value = "submission") Submission form, BindingResult result, Model model,
			RedirectAttributes attr, HttpSession session) {
		if(username == null || username.isEmpty()) {
			return "redirect:/home/";
		}
		return doSubmitAssessment(user, username, form, result, model, attr, session);
	}
}
