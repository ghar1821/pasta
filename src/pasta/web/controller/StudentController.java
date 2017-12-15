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

package pasta.web.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import pasta.domain.FileTreeNode;
import pasta.domain.UserPermissionLevel;
import pasta.domain.form.Submission;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.GroupManager;
import pasta.service.PASTAOptions;
import pasta.service.ResultManager;
import pasta.service.SubmissionManager;
import pasta.service.UserManager;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;
import pasta.web.WebUtils;

/**
 * Controller class for the student viewing functions.
 * <p>
 * 
 * @author Martin McGrane
 * @version 1.0
 * @since 23 Sep 2016
 *
 */
@Controller
@RequestMapping("/")
public class StudentController {

	public StudentController() {
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
	private GroupManager groupManager;

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("submission")
	public Submission returnSubmissionModel() {
		return new Submission();
	}

	@ModelAttribute("assessmentResult")
	public AssessmentResult returnAssessmentResultModel() {
		return new AssessmentResult();
	}

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// VIEW //
	// ///////////////////////////////////////////////////////////////////////////
	/**
	 * $PASTAUrl$/student/{studentUsername}/home/
	 * <p>
	 * View the home page for a given user.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>viewedUser</td>
	 * <td>{@link pasta.domain.user.PASTAUser} for the viewed user</td>
	 * </tr>
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
	 * <li>user/studentHome</li>
	 * </ul>
	 * 
	 * @param studentUsername
	 *            the name of the user you are looking at
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/studentHome"
	 */
	@RequestMapping(value = "student/{studentUsername}/home/")
	public String viewStudent(@ModelAttribute("user") PASTAUser user, @PathVariable("studentUsername") String username, Model model) {
		// check if tutor or student
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}
		
		model.addAttribute("viewedUser", viewedUser);
		model.addAttribute("results", resultManager.getLatestResultsIncludingGroups(viewedUser));
		
		Map<String, Set<Assessment>> allAssessments = assessmentManager.getAllAssessmentsByCategory(user.isTutor());
		Iterator<String> itCategories = allAssessments.keySet().iterator();
		while(itCategories.hasNext()) {
			String category = itCategories.next();
			Iterator<Assessment> itAssessments = allAssessments.get(category).iterator();
			while(itAssessments.hasNext()) {
				Assessment a = itAssessments.next();
				if(!a.isReleasedTo(viewedUser)) {
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
		Map<Long, Boolean> released = new HashMap<>();
		Map<Long, Boolean> closed = new HashMap<>();
		Map<Long, Boolean> hasGroupWork = new HashMap<>();
		Map<Long, Boolean> allGroupWork = new HashMap<>();
		for(Assessment assessment : assessmentManager.getAssessmentList()) {
			Date due = userManager.getDueDate(viewedUser, assessment);
			String formatted = PASTAUtil.formatDateReadable(due);
			dueDates.put(assessment.getId(), formatted);
			released.put(assessment.getId(), assessment.isReleasedTo(viewedUser));
			Date extension = userManager.getExtension(viewedUser, assessment);
			hasExtension.put(assessment.getId(), extension != null);
			closed.put(assessment.getId(), assessment.isClosedFor(viewedUser, extension));
			boolean userHasGroup = groupManager.getGroup(viewedUser, assessment) != null;
			boolean assessmentHasGroupWork = userHasGroup && assessmentManager.hasGroupWork(assessment);
			hasGroupWork.put(assessment.getId(), assessmentHasGroupWork);
			allGroupWork.put(assessment.getId(), assessmentHasGroupWork && assessmentManager.isAllGroupWork(assessment));
		}
		model.addAttribute("dueDates", dueDates);
		model.addAttribute("hasExtension", hasExtension);
		model.addAttribute("released", released);
		model.addAttribute("closed", closed);
		model.addAttribute("hasGroupWork", hasGroupWork);
		model.addAttribute("allGroupWork", allGroupWork);
		
		model.addAttribute("individualDeclaration", PASTAOptions.instance().get("submission.individual.text"));
		model.addAttribute("groupDeclaration", PASTAOptions.instance().get("submission.group.text"));
		
		return "user/studentHome";
	}

	/**
	 * $PASTAUrl$/student/{studentUsername}/info/{assessmentId}/
	 * <p>
	 * View the details and history for an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>viewedUser</td>
	 * <td>{@link pasta.domain.user.PASTAUser} of the user being viewed</td>
	 * </tr>
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
	 * @param studentUsername
	 *            the username for the user you are viewing
	 * @param assessmentId
	 *            the id of the assessment
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/viewAssessment"
	 */
	@RequestMapping(value = "student/{studentUsername}/info/{assessmentId}/")
	public String viewAssessmentInfo(@PathVariable("studentUsername") String username,
			@PathVariable("assessmentId") long assessmentId, Model model) {

		WebUtils.ensureAccess( UserPermissionLevel.TUTOR);
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(assessment == null) {
			return "redirect:/home/";
		}
		
		model.addAttribute("viewedUser", viewedUser);
		model.addAttribute("assessment", assessment);
		model.addAttribute("closed", assessment.isClosedFor(viewedUser, userManager.getExtension(viewedUser, assessment)));
		model.addAttribute("extension", userManager.getExtension(viewedUser, assessment));
		model.addAttribute("history", resultManager.getAssessmentHistory(viewedUser, assessmentId));
		
		Map<String, FileTreeNode> nodes = PASTAUtil.generateFileTree(viewedUser, assessmentId);
		PASTAUser group = groupManager.getGroup(viewedUser, assessmentId);
		if(group != null) {
			nodes.putAll(PASTAUtil.generateFileTree(group, assessmentId));
		}
		model.addAttribute("nodeList", nodes);

		return "user/viewAssessment";
	}

	/**
	 * $PASTAUrl$/download/{studentUsername}/{assessmentId}/{assessmentDate}/
	 * <p>
	 * Serve the zip file that contains the submission for the username for a
	 * given assessment at a given time.
	 * 
	 * If the user has not authenticated or is not a tutor: do nothing
	 * 
	 * Otherwise create the zip file and serve the zip file. The file name is
	 * $username$-$assessmentId$-$assessmentDate$.zip
	 * 
	 * @param studentUsername
	 *            the user which you want to download the submission for
	 * @param assessmentId
	 *            the id of the assessment
	 * @param assessmentDate
	 *            the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model
	 *            the model being used
	 * @param response
	 *            the http response being used to serve the zip file.
	 */
	@RequestMapping(value = "download/{studentUsername}/{assessmentId}/{assessmentDate}/")
	public void downloadAssessment(@PathVariable("studentUsername") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate, Model model, HttpServletResponse response) {

		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=\"" + username + "-" + assessmentId
				+ "-" + assessmentDate + ".zip\"");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outStream);
		try {
			PASTAUtil.zip(zip, new File(ProjectProperties.getInstance().getSubmissionsLocation() + username
					+ "/assessments/" + assessmentId + "/" + assessmentDate + "/submission/"),
					ProjectProperties.getInstance().getSubmissionsLocation() + username + "/assessments/"
							+ assessmentId + "/" + assessmentDate + "/submission/");
			zip.closeEntry();
			zip.close();
			IOUtils.copy(new ByteArrayInputStream(outStream.toByteArray()), response.getOutputStream());
			response.flushBuffer();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * $PASTAUrl$/runAssessment/{studentUsername}/{assessmentId}/{assessmentDate}/
	 * <p>
	 * Run an assessment again.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * Otherwise: run assessment using
	 * {@link pasta.service.SubmissionManager#runAssessment(String, long, String)}
	 * 
	 * @param studentUsername
	 *            the user which you want to download the submission for
	 * @param assessmentId
	 *            the id of the assessment
	 * @param assessmentDate
	 *            the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model
	 *            the model being used
	 * @param request
	 *            the http request being used for redirecting.
	 * @return "redirect:/login/" or "redirect:/home/" or redirect back to the
	 *         referrer
	 */
	@RequestMapping(value = "runAssessment/{studentUsername}/{assessmentId}/{assessmentDate}/")
	public String runAssessment(@PathVariable("studentUsername") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate, Model model, HttpServletRequest request) {

		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}

		AssessmentResult result = resultManager.loadAssessmentResult(viewedUser, assessmentId,
				assessmentDate);
		manager.runAssessment(result.getUser(), assessmentId, assessmentDate, result);
		return "redirect:" + request.getHeader("Referer");
	}

	/**
	 * $PASTAUrl$/student/{studentUsername}/extension/{assessmentId}/{extension}/
	 * <p>
	 * Give an extension to a user for an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * If the user is an instructor: give the extension using
	 * {@link pasta.service.UserManager#giveExtension(String, long, Date)} then
	 * redirect to the referrer page.
	 * 
	 * @param studentUsername
	 *            the user which you want to download the submission for
	 * @param assessmentId
	 *            the id of the assessment
	 * @param extension
	 *            the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model
	 *            the model being used
	 * @param request
	 *            the request begin used to redirect back to the referer.
	 * @return "redirect:/login/" or "redirect:/home/" or redirect back to the
	 *         referrer
	 */
	@RequestMapping(value = "student/{studentUsername}/extension/{assessmentId}/{extension}/")
	public String giveExtension(@ModelAttribute("user") PASTAUser user, @PathVariable("studentUsername") String username,
			@PathVariable("assessmentId") long assessmentId, @PathVariable("extension") String extension,
			Model model, HttpServletRequest request) {
		// check if tutor or student
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if (assessment != null && user.isInstructor()) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			try {
				userManager.giveExtension(viewedUser, assessment, sdf.parse(extension));
			} catch (ParseException e) {
				logger.error("Parse Exception");
			}
		}

		return "redirect:" + request.getHeader("Referer");
	}
}
