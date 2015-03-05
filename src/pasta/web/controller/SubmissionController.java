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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;

import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.upload.NewCompetition;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.service.AssessmentManager;
import pasta.service.HandMarkingManager;
import pasta.service.SubmissionManager;
import pasta.service.UserManager;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;
import pasta.view.ExcelAutoMarkView;
import pasta.view.ExcelMarkView;

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

	/**
	 * Initializes the codeStyle tag mapping of file endings to 
	 * javascript tag requirements for syntax highlighting.
	 */
	public SubmissionController() {
		codeStyle = new TreeMap<String, String>();
		codeStyle.put("c", "ccode");
		codeStyle.put("cpp", "cppcode");
		codeStyle.put("h", "cppcode");
		codeStyle.put("cs", "csharpcode");
		codeStyle.put("css", "csscode");
		codeStyle.put("html", "htmlcode");
		codeStyle.put("java", "javacode");
		codeStyle.put("js", "javascriptcode");
		codeStyle.put("pl", "perlcode");
		codeStyle.put("pm", "perlcode");
		codeStyle.put("php", "phpcode");
		codeStyle.put("py", "pythoncode");
		codeStyle.put("rb", "rubycode");
		codeStyle.put("sql", "sqlcode");
		codeStyle.put("xml", "xmlcode");

	}

	protected final Log logger = LogFactory.getLog(getClass());

	private SubmissionManager manager;
	private UserManager userManager;
	private AssessmentManager assessmentManager;
	private HandMarkingManager handMarkingManager;
	private Map<String, String> codeStyle;

	@Autowired
	public void setMyService(SubmissionManager myService) {
		this.manager = myService;
	}

	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
	}

	@Autowired
	public void setMyService(AssessmentManager myService) {
		this.assessmentManager = myService;
	}

	@Autowired
	public void setMyService(HandMarkingManager myService) {
		this.handMarkingManager = myService;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newUnitTestModel")
	public NewUnitTest returnNewUnitTestModel() {
		return new NewUnitTest();
	}

	@ModelAttribute("newCompetitionModel")
	public NewCompetition returnNewCompetitionModel() {
		return new NewCompetition();
	}

	@ModelAttribute("submission")
	public Submission returnSubmissionModel() {
		return new Submission();
	}

	@ModelAttribute("competition")
	public Competition returnCompetitionModel() {
		return new Competition();
	}

	@ModelAttribute("assessmentResult")
	public AssessmentResult returnAssessmentResultModel() {
		return new AssessmentResult();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Get or create the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in.
	 */
	public PASTAUser getOrCreateUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return getOrCreateUser(username);
	}
	
	/**
	 * Get or create the user given a username
	 * 
	 * @param username the username of the user
	 * @return the user, null if the username is null.
	 */
	public PASTAUser getOrCreateUser(String username) {
		if (username != null) {
			return userManager.getOrCreateUser(username);
		}
		return null;
	}

	/**
	 * Get the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in or user isn't registered.
	 */
	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return getUser(username);
	}
	
	/**
	 * Get the user given a username
	 * 
	 * @param username the username of the user
	 * @return the user, null if the username is null or user isn't registered.
	 */
	public PASTAUser getUser(String username) {
		if (username != null) {
			return userManager.getUser(username);
		}
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HOME //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/mirror/
	 * <p>
	 * Redirect back to the referrer.
	 * This is mainly used to get rid of the resubmitting a form when
	 * refreshing a page.
	 * 
	 * @param request the http request used for redirection
	 * @param session the http session that is never used here directly, but is passed
	 * to other methods to ensure that the binding result information is kept when using
	 * the mirror to stop the refreshing may re-submit form.
	 * @return redirect to the referrer.
	 */
	@RequestMapping(value = "mirror/")
	public String goBack(HttpServletRequest request, HttpSession session) {
		return "redirect:" + request.getHeader("Referer");
	}

	/**
	 * $PASTAUrl$/home/
	 * <p>
	 * The home screen.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * ATTRIBTUES:
	 * <table>
	 * 	<tr><td>unikey</td><td>the user object for the currently logged in user</td></tr>
	 * 	<tr><td>assessments</td><td>map of all assessments by category (key of category: String, value of list of 
	 * 	{@link pasta.domain.template.Assessment}</td></tr>
	 * 	<tr><td>results</td><td>The results as a map (key of id of assessment: Long, value
	 * 	of {@link pasta.domain.result.AssessmentResult}</td></tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * 	<li>user/tutorHome - if the user is a tutor</li>
	 * 	<li>user/studentHome - if the user is a student</li>
	 * </ul>
	 * 
	 * @param model the model being used
	 * @param session the http session that may hold the binding result information.
	 * @return "redirect:/login/" or "user/tutorHome" or "user/studentHome"
	 */
	@RequestMapping(value = "home/")
	public String home(Model model, HttpSession session) {
		// check if tutor or student
		PASTAUser user = getOrCreateUser();
		if (user == null) {
			return "redirect:/login/";
		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("assessments",
				assessmentManager.getAllAssessmentsByCategory());
		model.addAttribute("results",
				manager.getLatestResultsForUser(user.getUsername()));

		if(session.getAttribute("binding")!= null){
			model.addAttribute("org.springframework.validation.BindingResult.submission", session.getAttribute("binding"));
			session.removeAttribute("binding");
		}
		if (user.isTutor()) {
			return "user/tutorHome";
		} else {
			return "user/studentHome";
		}
	}

	/**
	 * $PSATAUrl/home/ - POST
	 * <p>
	 * Submit an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * FAIL CONDITIONS:
	 * <table>
	 * 	<tr><td>Submission.NoFile</td><td>The form doesn't contain a file</td></tr>
	 * 	<tr><td>Submission.NotZip</td><td>The file is not a zip file (extension of .zip)</td></tr>
	 * 	<tr><td>Submission.AfterClosingDate</td><td>The submission is past the due date</td></tr>
	 * 	<tr><td>Submission.NoAttempts</td><td>There are no attempts left to this user</td></tr>
	 * </table>
	 * 
	 * If the submission is validated correctly and doesn't encounter any
	 * of the fail conditions, it's submitted using 
	 * {@link pasta.service.SubmissionManager#submit(String, Submission)}
	 * 
	 * Then it's redirected to $PASTAUrl$/mirror/ to clear the form submission buffer such
	 * that pressing reload has no chance of resubmitting your form.
	 * 
	 * @param form the submission form holding the code
	 * @param result the binding result used for feedback
	 * @param model the model being used
	 * @param session the http session used for allowing passing the binding result
	 * along while using $PASTAUrl$/mirror/ to redirect and such that you will not
	 * resubmit the form if you refresh the page 
	 * @return "redirect:/login/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "home/", method = RequestMethod.POST)
	public String submitAssessment(
			@ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model,
			HttpSession session) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		// check if the submission is valid
		if (form.getFile() == null || form.getFile().isEmpty()) {
			result.rejectValue("file", "Submission.NoFile");
		}

		logger.warn("Form submission:");
		logger.warn(form);
		logger.warn(form.getFile());
		logger.warn(form.getFile().getOriginalFilename());
		
		if (!form.getFile().getOriginalFilename().endsWith(".zip")) {
			result.rejectValue("file", "Submission.NotZip");
		}
		Date now = new Date();
		if (assessmentManager.getAssessment(form.getAssessment()).isClosed()
				&& (user.getExtensions() == null // no extension
						|| user.getExtensions().get(form.getAssessment()) == null || user
						.getExtensions().get(form.getAssessment()).before(now))
				&& (!user.isTutor())) {
			result.rejectValue("file", "Submission.AfterClosingDate");
		}
		if ((!user.isTutor())
				&& manager.getLatestResultsForUser(user.getUsername()) != null
				&& manager.getLatestResultsForUser(user.getUsername()).get(form.getAssessment()) != null
				&& assessmentManager.getAssessment(form.getAssessment()).getNumSubmissionsAllowed() != 0
				&& manager.getLatestResultsForUser(user.getUsername()).get(form.getAssessment()).getSubmissionsMade() >= assessmentManager.getAssessment(form.getAssessment()).getNumSubmissionsAllowed()) {
			result.rejectValue("file", "Submission.NoAttempts");
		}
		if (!result.hasErrors()) {
			// accept the submission
			logger.info(ProjectProperties.getInstance().getAssessmentDAO()
					.getAssessment(form.getAssessment()).getName() 
					+ " submitted by " + user.getUsername());
			manager.submit(user.getUsername(), form);
		}
		session.setAttribute("binding", result);
		return "redirect:/mirror/";
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
	 * 	<tr><td>unikey</td><td>{@link pasta.domain.PASTAUser} of the user logged in</td></tr>
	 * 	<tr><td>assessment</td><td>{@link pasta.domain.template.Assessment} the assessment</td></tr>
	 * 	<tr><td>history</td><td>Collection of {@link pasta.domain.result.AssessmentResult} for all of the submissions</td></tr>
	 * 	<tr><td>nodeList</td><td>Map of {@link pasta.domain.FileTreeNode} with the key as the date of the
	 * submission and the value being the root node of the code that was submitted.</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>user/viewAssessment</li></ul>
	 * 
	 * @param assessmentId the id of the assessment
	 * @param model the model being used
	 * @return "redirect:/login/" or "user/viewAssessment"
	 */
	@RequestMapping(value = "info/{assessmentId}/")
	public String viewAssessmentInfo(
			@PathVariable("assessmentId") long assessmentId, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("assessment",
				assessmentManager.getAssessment(assessmentId));
		model.addAttribute("history", assessmentManager.getAssessmentHistory(
				user.getUsername(), assessmentId));
		model.addAttribute("nodeList",
				PASTAUtil.genereateFileTree(user.getUsername(), assessmentId));

		return "user/viewAssessment";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// VIEW //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/downloadMarks/
	 * <p>
	 * Download the marks as an excel sheet.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * @param request the http request that is kinda not used.
	 * @param response also not really used
	 * @return the model and view (which is actually a {@link pasta.view.ExcelMarkView})
	 */
	@RequestMapping(value = "downloadMarks/")
	public ModelAndView viewExcel(HttpServletRequest request,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		ModelAndView model = new ModelAndView();

		if (user == null) {
			model.setViewName("redirect:/login/");
			return model;
		}
		if (!user.isTutor()) {
			model.setViewName("redirect:/home/");
			return model;
		}
		Map<String, Object> data = new TreeMap<String, Object>();

		data.put("assessmentList", assessmentManager.getAssessmentList());
		data.put("userList", userManager.getUserList());
		data.put("latestResults",
				assessmentManager.getLatestResults(userManager.getUserList()));

		return new ModelAndView(new ExcelMarkView(), data);
	}

	/**
	 * $PASTAUrl$/downloadAutoMarks/
	 * <p>
	 * Download the only the automated marks (all but hand marking) as an excel sheet.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * @param request the http request that is kinda not used.
	 * @param response also not really used
	 * @return the model and view (which is actually a {@link pasta.view.ExcelMarkView})
	 */
	@RequestMapping(value = "downloadAutoMarks/")
	public ModelAndView viewAutoExcel(HttpServletRequest request,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		ModelAndView model = new ModelAndView();

		if (user == null) {
			model.setViewName("redirect:/login/");
			return model;
		}
		if (!user.isTutor()) {
			model.setViewName("redirect:/home/");
			return model;
		}
		Map<String, Object> data = new TreeMap<String, Object>();

		data.put("assessmentList", assessmentManager.getAssessmentList());
		data.put("userList", userManager.getUserList());
		data.put("latestResults",
				assessmentManager.getLatestResults(userManager.getUserList()));

		return new ModelAndView(new ExcelAutoMarkView(), data);
	}

	/**
	 * $PASTAUrl$/student/{username}/info/{assessmentId}/updateComment/ - POST
	 * <p>
	 * Update the comment for a given user for a given assessment for a
	 * given submission using 
	 * {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * 
	 * @param newComment the new comment
	 * @param assessmentDate the assessment date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param username the name of the user
	 * @param assessmentId the id of the assessment
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../"
	 */
	@RequestMapping(value = "student/{username}/info/{assessmentId}/updateComment/", method = RequestMethod.POST)
	public String updateComment(@RequestParam("newComment") String newComment,
			@RequestParam("assessmentDate") String assessmentDate,
			@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		handMarkingManager.saveComment(username, assessmentId,
				assessmentDate, newComment);
		return "redirect:../";
	}

	/**
	 * $PASTAUrl$/viewFile/loadFile - GET
	 * <p>
	 * View a file.
	 * 
	 * If the user has authenticated and is a tutor, 
	 * serve up the document, otherwise do nothing.
	 * 
	 * <b>Not sure if it's actually being used</b>
	 * 
	 * @param fileName the path to the file.
	 * @param response the http response being used to serve the content
	 */
	@RequestMapping(value = "viewFile/loadFile", method = RequestMethod.GET)
	public void getFile(@RequestParam("file_name") String fileName,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user != null && user.isTutor()) {
			if (!codeStyle.containsKey(fileName.substring(fileName
					.lastIndexOf(".") + 1))) {
				try {
					// get your file as InputStream
					InputStream is = new FileInputStream(fileName.replace("\"",
							""));
					// copy it to response's OutputStream
					IOUtils.copy(is, response.getOutputStream());
					response.flushBuffer();
					is.close();
				} catch (IOException ex) {
					throw new RuntimeException(
							"IOError writing file to output stream");
				}
			}
		}
	}

	/**
	 * $PASTAUrl$/downloadFile
	 * <p>
	 * Download a file
	 * 
	 * If the user has authenticated and is a tutor, 
	 * serve up the document, otherwise do nothing.
	 * 
	 * @param fileName the path to the file.
	 * @param response the http response being used to serve the content
	 */
	@RequestMapping(value = "downloadFile", method = RequestMethod.GET)
	public void downloadFile(@RequestParam("file_name") String fileName,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user != null && user.isTutor()) {
			if (!codeStyle.containsKey(fileName.substring(fileName
					.lastIndexOf(".") + 1))) {
				try {
					// get your file as InputStream
					InputStream is = new FileInputStream(fileName.replace("\"",
							""));
					// copy it to response's OutputStream
					response.setContentType("application/octet-stream;");
					response.setHeader(
							"Content-Disposition",
							"attachment; filename="
									+ fileName.replace("\"", "").substring(
											fileName.replace("\"", "")
													.replace("\\", "/")
													.lastIndexOf("/") + 1));
					IOUtils.copy(is, response.getOutputStream());
					response.flushBuffer();
					is.close();
				} catch (IOException ex) {
					throw new RuntimeException(
							"IOError writing file to output stream");
				}
			}
		}
	}

	/**
	 * $PASTAUrl$/viewFile/loadFile - GET
	 * <p>
	 * View a file.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>{@link pasta.domain.PASTAUser} of the currently logged in user</td></tr>
	 * 	<tr><td>location</td><td>The location of the disk of the file</td></tr>
	 * 	<tr><td>codeStyle</td><td>The map of coding styles. Map<string, string></td></tr>
	 * 	<tr><td>fileEnding</td><td>The file ending of the file you're viewing</td></tr>
	 * 	<tr><td>fileContents</td><td>The contents of the file, with the &gt; and &lt; escaped </td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/mark/viewFile</li></ul>
	 * 
	 * @param location the path to the file location
	 * @param model the model being used
	 * @param response the response that's not really being used
	 * @return "redirect:/login/" or "redirect:/home/"
	 */
	@RequestMapping(value = "viewFile/", method = RequestMethod.POST)
	public String viewFile(@RequestParam("location") String location,
			Model model, HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("location", location);
		model.addAttribute("codeStyle", codeStyle);
		model.addAttribute("fileEnding",
				location.substring(location.lastIndexOf(".") + 1));

		if (codeStyle
				.containsKey(location.substring(location.lastIndexOf(".") + 1))) {
			model.addAttribute("fileContents", PASTAUtil.scrapeFile(location)
					.replace(">", "&gt;").replace("<", "&lt;"));

		}
		return "assessment/mark/viewFile";
	}

	/**
	 * $PASTAUrl$/student/{username}/home/
	 * <p>
	 * View the home page for a given user.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>{@link pasta.domain.PASTAUser} for the currently logged in user</td></tr>
	 * 	<tr><td>viewedUser</td><td>{@link pasta.domain.PASTAUser} for the viewed user</td></tr>
	 * 	<tr><td>assessments</td><td>map of all assessments by category (key of category: String, value of list of 
	 * 	{@link pasta.domain.template.Assessment}</td></tr>
	 * 	<tr><td>results</td><td>The results as a map (key of id of assessment: Long, value
	 * 	of {@link pasta.domain.result.AssessmentResult}</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>user/studentHome</li></ul>
	 * 
	 * @param username the name of the user you are looking at
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/studentHome"
	 */
	@RequestMapping(value = "student/{username}/home/")
	public String viewStudent(@PathVariable("username") String username,
			Model model) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		PASTAUser viewedUser = getOrCreateUser(username);
		model.addAttribute("unikey", user);
		model.addAttribute("viewedUser", viewedUser);
		model.addAttribute("assessments",
				assessmentManager.getAllAssessmentsByCategory());
		model.addAttribute("results",
				manager.getLatestResultsForUser(viewedUser.getUsername()));
		return "user/studentHome";
	}

	/**
	 * $PASTAUrl$/student/{username}/home/
	 * <p>
	 * Submit an assessment for another user.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * FAIL CONDITIONS:
	 * <table>
	 * 	<tr><td>Submission.NoFile</td><td>The form doesn't contain a file</td></tr>
	 * 	<tr><td>Submission.NotZip</td><td>The file is not a zip file (extension of .zip)</td></tr>
	 * </table>
	 * 
	 * If the submission is validated correctly and doesn't encounter any
	 * of the fail conditions, it's submitted using 
	 * {@link pasta.service.SubmissionManager#submit(String, Submission)}
	 * 
	 * Then it's redirected to $PASTAUrl$/mirror/ to clear the form submission buffer such
	 * that pressing reload has no chance of resubmitting your form.
	 * 
	 * @param form the submission form holding the code
	 * @param result the binding result used for feedback
	 * @param model the model being used
	 * @param session the http session used for allowing passing the binding result
	 * along while using $PASTAUrl$/mirror/ to redirect and such that you will not
	 * resubmit the form if you refresh the page 
	 * @return "redirect:/login/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "student/{username}/home/", method = RequestMethod.POST)
	public String submitAssessment(@PathVariable("username") String username,
			@ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model, HttpSession session) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		// check if the submission is valid
		if (form.getFile() == null || form.getFile().isEmpty()) {
			result.reject("Submission.NoFile");
		}
		if (!form.getFile().getOriginalFilename().endsWith(".zip")) {
			result.rejectValue("file", "Submission.NotZip");
		}
		if (!result.hasErrors()) {
			// accept the submission
			logger.info(ProjectProperties.getInstance().getAssessmentDAO()
					.getAssessment(form.getAssessment()).getName() + " submitted for " 
					+ username + " by " + user.getUsername());
			manager.submit(username, form);
		}
		session.setAttribute("binding", result);
		return "redirect:/mirror/";
	}
	
	/**
	 * $PASTAUrl$/student/{username}/info/{assessmentId}/
	 * <p>
	 * View the details and history for an assessment. 
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>{@link pasta.domain.PASTAUser} of the user logged in</td></tr>
	 * 	<tr><td>viewedUser</td><td>{@link pasta.domain.PASTAUser} of the user being viewed</td></tr>
	 * 	<tr><td>assessment</td><td>{@link pasta.domain.template.Assessment} the assessment</td></tr>
	 * 	<tr><td>history</td><td>Collection of {@link pasta.domain.result.AssessmentResult} for all of the submissions</td></tr>
	 * 	<tr><td>nodeList</td><td>Map of {@link pasta.domain.FileTreeNode} with the key as the date of the
	 * submission and the value being the root node of the code that was submitted.</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>user/viewAssessment</li></ul>
	 * 
	 * @param username the username for the user you are viewing
	 * @param assessmentId the id of the assessment
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/viewAssessment"
	 */
	@RequestMapping(value = "student/{username}/info/{assessmentId}/")
	public String viewAssessmentInfo(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("viewedUser", getUser(username));
		model.addAttribute("assessment",
				assessmentManager.getAssessment(assessmentId));
		model.addAttribute("history", assessmentManager.getAssessmentHistory(
				username, assessmentId));
		model.addAttribute("nodeList",
				PASTAUtil.genereateFileTree(username, assessmentId));

		return "user/viewAssessment";
	}

	/**
	 * $PASTAUrl$/download/{username}/{assessmentId}/{assessmentDate}/
	 * <p>
	 * Serve the zip file that contains the submission for the username for a 
	 * given assessment at a given time.
	 * 
	 * If the user has not authenticated or is not a tutor: do nothing
	 * 
	 * Otherwise create the zip file and serve the zip file. The file name is
	 * $username$-$assessmentId$-$assessmentDate$.zip
	 * 
	 * @param username the user which you want to download the submission for
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model the model being used
	 * @param response the http response being used to serve the zip file.
	 */
	@RequestMapping(value = "download/{username}/{assessmentId}/{assessmentDate}/")
	public void downloadAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate, Model model,
			HttpServletResponse response) {
		
		PASTAUser user = getUser();
		if (user == null || !user.isTutor()) {
			return;
		}
		
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=\""
				+ username + "-" + assessmentId + "-" + assessmentDate
				+ ".zip\"");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outStream);
		try {
			PASTAUtil.zip(zip, new File(ProjectProperties.getInstance()
					.getSubmissionsLocation()
					+ username
					+ "/assessments/"
					+ assessmentId
					+ "/"
					+ assessmentDate
					+ "/submission/"), ProjectProperties.getInstance()
					.getSubmissionsLocation()
					+ username
					+ "/assessments/"
					+ assessmentId
					+ "/"
					+ assessmentDate
					+ "/submission/");
			zip.closeEntry();
			zip.close();
			IOUtils.copy(new ByteArrayInputStream(outStream.toByteArray()),
					response.getOutputStream());
			response.flushBuffer();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * $PASTAUrl$/runAssessment/{username}/{assessmentId}/{assessmentDate}/
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
	 * @param username the user which you want to download the submission for
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model the model being used
	 * @param request the http request being used for redirecting.
	 * @return "redirect:/login/" or "redirect:/home/" or redirect back to the referrer
	 */
	@RequestMapping(value = "runAssessment/{username}/{assessmentId}/{assessmentDate}/")
	public String runAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate, Model model,
			HttpServletRequest request) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		manager.runAssessment(username, assessmentId, assessmentDate);
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/student/{username}/extension/{assessmentId}/{extension}/
	 * <p>
	 * Give an extension to a user for an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * If the user is an instructor: give the extension using
	 * {@link pasta.service.UserManager#giveExtension(String, long, Date)}
	 * then redirect to the referrer page.
	 * 
	 * @param username the user which you want to download the submission for
	 * @param assessmentId the id of the assessment
	 * @param extension the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model the model being used
	 * @param request the request begin used to redirect back to the referer.
	 * @return "redirect:/login/" or "redirect:/home/" or redirect back to the referrer
	 */
	@RequestMapping(value = "student/{username}/extension/{assessmentId}/{extension}/")
	public String giveExtension(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("extension") String extension, Model model,
			HttpServletRequest request) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (user.isInstructor()) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			try {
				userManager.giveExtension(username, assessmentId,
						sdf.parse(extension));
			} catch (ParseException e) {
				logger.error("Parse Exception");
			}
		}
		
		return "redirect:" + request.getHeader("Referer");
	}

	/**
	 * $PASTAUrl$/mark/{username}/{assessmentId}/{assessmentDate}/
	 * <p>
	 * Mark the submission for a student.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>{@link pasta.domain.PASTAUser} the user logged in</td></tr>
	 * 	<tr><td>student</td><td>{@link pasta.domain.PASTAUser} the user being viewed</td></tr>
	 * 	<tr><td>assessmentId</td><td>the id of the assessment</td></tr>
	 * 	<tr><td>node</td><td>The root {@link pasta.domain.FileTreeNode} for the root of the submitted code</td></tr>
	 * 	<tr><td>assessmentResult</td><td>The {@link pasta.domain.result.AssessmentResult} for this assessment</td></tr>
	 * 	<tr><td>handMarkingList</td><td>The list of {@link pasta.domain.template.WeightedHandMarking}</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>assessment/mark/handMark</li></ul>
	 * 
	 * @param username the user which you want to download the submission for
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/mark/handMark"
	 */
	@RequestMapping(value = "mark/{username}/{assessmentId}/{assessmentDate}/")
	public String handMarkAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		model.addAttribute("unikey", user);
		model.addAttribute("student", username);
		model.addAttribute("assessmentId", assessmentId);

		AssessmentResult result = assessmentManager.getAssessmentResult(
				username, assessmentId, assessmentDate);

		model.addAttribute("node", PASTAUtil.generateFileTree(username,
				assessmentId, assessmentDate));
		model.addAttribute("assessmentResult", result);
		model.addAttribute("handMarkingList", result.getAssessment()
				.getHandMarking());

		return "assessment/mark/handMark";
	}

	/**
	 * $PASTAUrl$/mark/{username}/{assessmentId}/{assessmentDate}/ - POST
	 * <p>
	 * Save the hand marking for a submission for a student
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * Otherwise: update the hand marking results and comments using
	 * {@link pasta.service.HandMarkingManager#saveHandMarkingResults(String, long, String, List)}
	 * and 
	 * {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * 
	 * @param username the user which you want to download the submission for
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param form the assessment result object
	 * @param result the binding result used for feedback
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "mark/{username}/{assessmentId}/{assessmentDate}/", method = RequestMethod.POST)
	public String saveHandMarkAssessment(
			@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form,
			BindingResult result, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		// rebinding hand marking results with their hand marking templates
		List<HandMarkingResult> results = form.getHandMarkingResults();
		for (HandMarkingResult currResult : results) {
			currResult.setMarkingTemplate(handMarkingManager
					.getHandMarking(currResult.getId()));
		}
		handMarkingManager.saveHandMarkingResults(username, assessmentId,
				assessmentDate, form.getHandMarkingResults());
		handMarkingManager.saveComment(username, assessmentId,
				assessmentDate, form.getComments());

		return "redirect:/mirror/";
	}

	/**
	 * $PASTAUrl$/mark/{assessmentId}/{studentIndex}/ - POST and GET
	 * <p>
	 * Used for the batch marking of the students in your class(es).
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * POST: Save the {@link pasta.domain.result.AssessmentResult} form for the
	 * last user you marked using 
	 * {@link pasta.service.HandMarkingManager#saveHandMarkingResults(String, long, String, List)}
	 * and {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * 
	 * GET: Takes you to the hand marking template(s) to the user.
	 * 
	 * If the studentIndex is pointing to a student that has no submissions, 
	 * go to the next one.
	 * 
	 * If the index is past the end of the array, go back to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>{@link pasta.domain.PASTAUser} of the user logged in</td></tr>
	 * 	<tr><td>assessmentId</td><td>the id of the assessment</td></tr>
	 * 	<tr><td>student</td><td>{@link pasta.domain.PASTAUser} of the student being viewed</td></tr>
	 * 	<tr><td>node</td><td>The root {@link pasta.domain.FileTreeNode} of the submitted files</td></tr>
	 * 	<tr><td>assessmentResult</td><td>The {@link pasta.domain.result.AssessmentResult} for this assessment</td></tr>
	 * 	<tr><td>handMarkingList</td><td>The list of {@link pasta.domain.template.WeightedHandMarking}</td></tr>
	 * 	<tr><td>savingStudentIndex</td><td>A string representation of the index of the student you are saving</td></tr>
	 * 	<tr><td>hasSubmission</td><td>Flag to say whether the current student has a submission</td></tr>
	 * 	<tr><td>completedMarking</td><td>Flag to say whether the marking is complete for this student</td></tr>
	 * 	<tr><td>myStudents</td><td>The list of {@link pasta.domain.PASTAUser} of the users that belong in your class(es) </td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/mark/handMarkBatch</li></ul>
	 * 
	 * @param studentIndex the index of the student being viewed
	 * @param assessmentId the id of the assessment
	 * @param student the student that you have just marked and is getting saved
	 * @param form the assessment result form to be saved
	 * @param request the http request that's not being used
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/mark/handMarkBatch"
	 */
	@RequestMapping(value = "mark/{assessmentId}/{studentIndex}/", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String handMarkAssessmentBatch(
			@PathVariable("studentIndex") String studentIndex,
			@PathVariable("assessmentId") long assessmentId,
			@RequestParam(value = "student", required = false) String student,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form,
			HttpServletRequest request, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("assessmentId", assessmentId);

		Collection<PASTAUser> myUsers = new LinkedList<PASTAUser>();
		for (String tutorial : user.getTutorClasses()) {
			myUsers.addAll(userManager.getUserListByTutorial(tutorial));
		}
		PASTAUser[] myStudents = myUsers.toArray(new PASTAUser[0]);

		// if submitted
		if (form != null && student != null && getUser(student) != null) {

			// save changes
			List<HandMarkingResult> results = form.getHandMarkingResults();
			for (HandMarkingResult currResult : results) {
				currResult.setMarkingTemplate(handMarkingManager
						.getHandMarking(currResult.getId()));
			}
			handMarkingManager.saveHandMarkingResults(student, assessmentId,
					manager.getLatestResultsForUser(student)
							.get(assessmentId).getFormattedSubmissionDate(),
					form.getHandMarkingResults());
			handMarkingManager.saveComment(student, assessmentId, manager
					.getLatestResultsForUser(student).get(assessmentId)
					.getFormattedSubmissionDate(), form.getComments());
		}

		boolean[] hasSubmission = new boolean[myStudents.length];
		boolean[] completedMarking = new boolean[myStudents.length];
		for (int i = 0; i < myStudents.length; ++i) {

			hasSubmission[i] = (myStudents[i] != null
					&& manager.getLatestResultsForUser(myStudents[i]
							.getUsername()) != null && manager
					.getLatestResultsForUser(myStudents[i].getUsername()).get(
							assessmentId) != null);
			if (hasSubmission[i]) {
				completedMarking[i] = manager
						.getLatestResultsForUser(myStudents[i].getUsername())
						.get(assessmentId).isFinishedHandMarking();
			}
		}

		// get the current student's submission
		try {
			int i_studentIndex = Integer.parseInt(studentIndex);
			if (i_studentIndex >= 0 && i_studentIndex < myStudents.length
					&& myStudents[i_studentIndex] != null
					&& hasSubmission[i_studentIndex]) {

				PASTAUser currStudent = myStudents[i_studentIndex];

				model.addAttribute("student", currStudent.getUsername());

				AssessmentResult result = manager.getLatestResultsForUser(
						currStudent.getUsername()).get(assessmentId);
				model.addAttribute("node", PASTAUtil.generateFileTree(
						currStudent.getUsername(), assessmentId,
						result.getFormattedSubmissionDate()));
				model.addAttribute("assessmentResult", result);
				model.addAttribute("handMarkingList", result.getAssessment()
						.getHandMarking());

				model.addAttribute("savingStudentIndex", i_studentIndex);
				model.addAttribute("hasSubmission", hasSubmission);
				model.addAttribute("completedMarking", completedMarking);
				model.addAttribute("myStudents", myStudents);

			} else {
				return "redirect:/home/";
			}

		} catch (NumberFormatException e) {
			return "redirect:/home/";
		}

		return "assessment/mark/handMarkBatch";
	}
	
  @RequestMapping(value = "mark/{assessmentId}/", method = RequestMethod.GET)
  public String handMarkAssessmentBatchStart(
      @PathVariable("assessmentId") long assessmentId,
      HttpServletRequest request, Model model) {

    PASTAUser user = getUser();
    if (user == null) {
      return "redirect:/login/";
    }
    if (!user.isTutor()) {
      return "redirect:/home/.";
    }

    model.addAttribute("unikey", user);
    model.addAttribute("assessmentId", assessmentId);

    if (assessmentManager.getAssessment(assessmentId) != null) {
      // find the first student with a submission
      int i = 0;
      for (String tutorial : user.getTutorClasses()) {
        for (PASTAUser student : userManager.getUserListByTutorial(tutorial)) {
          if (manager.getLatestResultsForUser(student.getUsername()) != null
              && manager.getLatestResultsForUser(student.getUsername()).get(
            		  assessmentId) != null) {
            return "redirect:" + request.getServletPath() + i + "/";
          }
          ++i;
        }
      }
    }
    return "redirect:/home/.";
  }

	// ///////////////////////////////////////////////////////////////////////////
	// GRADE CENTRE //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/gradeCentre/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center.
	 * 
	 * If the user has not authenticated or is not a tutor: return nothing.
	 * Otherwise use {@link SubmissionController#generateJSON(PASTAUser[])}
	 * for all users.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "gradeCentre/DATA/")
	public @ResponseBody
	String viewGradeCentreData() {
		PASTAUser currentUser = getUser();
		if (currentUser == null) {
			return "";
		}
		if (!currentUser.isTutor()) {
			return "";
		}
		
		return generateJSON(userManager.getUserList().toArray(new PASTAUser[0]));
	}

	/**
	 * $PASTAUrl$/stream/{streamName}/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center for a stream.
	 * 
	 * If the user has not authenticated or is not a tutor: return nothing.
	 * Otherwise use {@link SubmissionController#generateJSON(PASTAUser[])}
	 * for all users in the given stream. Return nothing if the stream
	 * doesn't exist.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "stream/{streamName}/DATA/")
	public @ResponseBody
	String viewStreamData(@PathVariable("streamName") String streamName) {
		PASTAUser currentUser = getUser();
		if (currentUser == null) {
			return "";
		}
		if (!currentUser.isTutor()) {
			return "";
		}

		// latestResults[user.username][assessment.shortName].marks
		if (userManager.getUserListByStream(streamName) == null) {
			return "";
		}
		return generateJSON(userManager.getUserListByStream(streamName)
				.toArray(new PASTAUser[0]));
	}

	/**
	 * $PASTAUrl$/tutorial/{className}/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center for a tutorial class.
	 * 
	 * If the user has not authenticated or is not a tutor: return nothing.
	 * Otherwise use {@link SubmissionController#generateJSON(PASTAUser[])}
	 * for all users in the given tutorial class. Return nothing if the tutorial
	 * class doesn't exist.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "tutorial/{className}/DATA/")
	public @ResponseBody
	String viewTutorialData(@PathVariable("className") String className) {
		PASTAUser currentUser = getUser();
		if (currentUser == null) {
			return "";
		}
		if (!currentUser.isTutor()) {
			return "";
		}

		// latestResults[user.username][assessment.shortName].marks
		if (userManager.getUserListByTutorial(className) == null) {
			return "";
		}

		return generateJSON(userManager.getUserListByTutorial(className)
				.toArray(new PASTAUser[0]));
	}

	/**
	 * $PASTAUrl$/myTutorial/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center for the user's
	 * tutorial class.
	 * 
	 * If the user has not authenticated or is not a tutor: return nothing.
	 * Otherwise use {@link SubmissionController#generateJSON(PASTAUser[])}
	 * for all users in the user's tutorial class.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "myTutorials/DATA/")
	public @ResponseBody
	String viewMyTutorialData() {
		PASTAUser currentUser = getUser();
		if (currentUser == null) {
			return "";
		}
		if (!currentUser.isTutor()) {
			return "";
		}

		Collection<PASTAUser> myUsers = new LinkedList<PASTAUser>();
		for (String tutorial : currentUser.getTutorClasses()) {
			myUsers.addAll(userManager.getUserListByTutorial(tutorial));
		}

		return generateJSON(myUsers.toArray(new PASTAUser[0]));
	}

	/**
	 * Generate the JSON
	 * <p>
	 * format:
	 * <pre>{@code {
	"data": 
	[
		{
			"name": "$username$",
			"stream": "$stream$",
			"class": "$class$",
			"$assessmentId$": {
				"mark": "######.###",
				"percentage": "double",
				"assessmentid": "$assessmentId$"
				"assessmentname": "$assessmentName$"
			},
			...
			"$assessmentId$": {
				"mark": "######.###",
				"percentage": "double",
				"assessmentid": "$assessmentId$"
				"assessmentname": "$assessmentName$"
			}
		},
		...
		{
			"name": "$username$",
			"stream": "$stream$",
			"class": "$class$",
			"$assessmentId$": {
				"mark": "######.###",
				"percentage": "double",
				"assessmentid": "$assessmentId$"
				"assessmentname": "$assessmentName$"
			},
			...
			"$assessmentId$": {
				"mark": "######.###",
				"percentage": "double",
				"assessmentid": "$assessmentId$"
				"assessmentname": "$assessmentName$"
			}
		}
	]
}}</pre>

	 * If there is no submission, mark and percentage will be "".
	 * Percentage is [1.0,0.0]. Mark is displayed to 3 decimal places.
	 * 
	 * @param allUsers the users for which to generate the JSON
	 * @return the JSON string
	 */
	private String generateJSON(PASTAUser[] allUsers) {
		DecimalFormat df = new DecimalFormat("#.###");

		String data = "{\r\n  \"data\": [\r\n";

		Assessment[] allAssessments = assessmentManager.getAssessmentList()
				.toArray(new Assessment[0]);
		for (int i = 0; i < allUsers.length; ++i) {
			PASTAUser user = allUsers[i];

			String userData = "    {\r\n";

			// name
			userData += "      \"name\": \"" + user.getUsername() + "\",\r\n";
			// stream
			userData += "      \"stream\": \"" + user.getStream() + "\",\r\n";
			// class
			userData += "      \"class\": \"" + user.getTutorial() + "\",\r\n";

			// marks
			for (int j = 0; j < allAssessments.length; j++) {
				// assessment mark
				Assessment currAssessment = allAssessments[j];
				userData += "      \"" + currAssessment.getId()
						+ "\": {\r\n";
				String mark = "";
				String percentage = "";

				if (assessmentManager.getLatestResultsForUser(user
						.getUsername()) != null
						&& assessmentManager.getLatestResultsForUser(
								user.getUsername()).get(
								currAssessment.getId()) != null) {
					mark = df.format(assessmentManager
							.getLatestResultsForUser(user.getUsername())
							.get(currAssessment.getId()).getMarks());
					percentage = ""
							+ assessmentManager
									.getLatestResultsForUser(user.getUsername())
									.get(currAssessment.getId())
									.getPercentage();
				}
				userData += "        \"mark\": \"" + mark + "\",\r\n";
				userData += "        \"percentage\": \"" + percentage
						+ "\",\r\n";
				userData += "        \"assessmentid\": \""
						+ currAssessment.getId() + "\"\r\n";
				userData += "        \"assessmentname\": \""
						+ currAssessment.getName() + "\"\r\n";
				userData += "      }";

				if (j < allAssessments.length - 1) {
					userData += ",";
				}
				userData += "\r\n";
			}

			userData += "    }";
			if (i < allUsers.length - 1) {
				userData += ",";
			}
			userData += "\r\n";

			data += userData;
		}
		data += "  ]\r\n}";
		return data;
	}

	/**
	 * $PASTAUrl$/gradeCentre/
	 * <p>
	 * Display the grade center.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>The {@link pasta.domain.PASTAUser} of the currently logged in user</td></tr>
	 * 	<tr><td>assessmentList</td><td>The list of all {@link pasta.domain.template.Assessment} on the system</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>user/viewAll2</li></ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/viewAll2"
	 */
	@RequestMapping(value = "gradeCentre/")
	public String viewGradeCentre2(Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewAll2";
	}

	/**
	 * $PASTAUrl$/tutorial/{className}/
	 * <p>
	 * Display the grade center for a given class.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>The {@link pasta.domain.PASTAUser} of the currently logged in user</td></tr>
	 * 	<tr><td>assessmentList</td><td>The list of all {@link pasta.domain.template.Assessment} on the system</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>user/viewSome</li></ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/viewSome"
	 */
	@RequestMapping(value = "tutorial/{className}/")
	public String viewClass(@PathVariable("className") String className,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewSome";
	}

	/**
	 * $PASTAUrl$/stream/{streamName}/
	 * <p>
	 * Display the grade center for a given stream.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>The {@link pasta.domain.PASTAUser} of the currently logged in user</td></tr>
	 * 	<tr><td>assessmentList</td><td>The list of all {@link pasta.domain.template.Assessment} on the system</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>user/viewSome</li></ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/viewSome"
	 */
	@RequestMapping(value = "stream/{streamName}/")
	public String viewStream(@PathVariable("streamName") String streamName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewSome";
	}

	/**
	 * $PASTAUrl$/myTutorials/
	 * <p>
	 * Display the grade center for the user's tutorial(s).
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>The {@link pasta.domain.PASTAUser} of the currently logged in user</td></tr>
	 * 	<tr><td>assessmentList</td><td>The list of all {@link pasta.domain.template.Assessment} on the system</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>user/viewAll2</li></ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/viewAll2"
	 */
	@RequestMapping(value = "myTutorials/")
	public String viewMyTutorials(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewAll2";
	}

}

