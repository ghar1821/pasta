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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import pasta.domain.ratings.AssessmentRating;
import pasta.domain.ratings.RatingForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.upload.NewCompetitionForm;
import pasta.domain.upload.NewUnitTestForm;
import pasta.domain.upload.Submission;
import pasta.scheduler.AssessmentJob;
import pasta.scheduler.ExecutionScheduler;
import pasta.service.AssessmentManager;
import pasta.service.HandMarkingManager;
import pasta.service.RatingManager;
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
	 * Initializes the codeStyle tag mapping of file endings to javascript tag
	 * requirements for syntax highlighting.
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
	private RatingManager ratingManager;
	@Autowired
	private ExecutionScheduler scheduler;

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
	public NewUnitTestForm returnNewUnitTestModel() {
		return new NewUnitTestForm();
	}

	@ModelAttribute("newCompetitionModel")
	public NewCompetitionForm returnNewCompetitionModel() {
		return new NewCompetitionForm();
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
	
	@ModelAttribute("ratingForms")
	public Map<Long, RatingForm> loadRatingForms() {
		Map<Long, RatingForm> forms = new HashMap<Long, RatingForm>();
		
		PASTAUser user = getUser();
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

	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Get the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in or user
	 *         isn't registered.
	 */
	public PASTAUser getUser() {
		PASTAUser user = (PASTAUser) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return user;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HOME //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/mirror/
	 * <p>
	 * Redirect back to the referrer. This is mainly used to get rid of the
	 * resubmitting a form when refreshing a page.
	 * 
	 * @param request
	 *            the http request used for redirection
	 * @param session
	 *            the http session that is never used here directly, but is
	 *            passed to other methods to ensure that the binding result
	 *            information is kept when using the mirror to stop the
	 *            refreshing may re-submit form.
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>the user object for the currently logged in user</td>
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
	public String home(Model model, HttpSession session) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("results", manager.getLatestResultsForUser(user));
		
		Map<String, Set<Assessment>> allAssessments = assessmentManager.getAllAssessmentsByCategory();
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
		Map<Long, Boolean> closed = new HashMap<>();
		for(Assessment assessment : assessmentManager.getAssessmentList()) {
			Date due = UserManager.getDueDate(user, assessment);
			String formatted = PASTAUtil.formatDateReadable(due);
			dueDates.put(assessment.getId(), formatted);
			closed.put(assessment.getId(), assessment.isClosedFor(user));
		}
		model.addAttribute("dueDates", dueDates);
		model.addAttribute("closed", closed);

		if (session.getAttribute("binding") != null) {
			model.addAttribute("org.springframework.validation.BindingResult.submission",
					session.getAttribute("binding"));
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
	public String submitAssessment(@ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model, HttpSession session) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		// check if the submission is valid
		if (form.getFile() == null || form.getFile().isEmpty()) {
			result.rejectValue("file", "Submission.NoFile");
		}

		Date now = new Date();
		if (assessmentManager.getAssessment(form.getAssessment()).isClosed()
				&& (user.getExtensions() == null // no extension
						|| user.getExtensions().get(form.getAssessment()) == null || user.getExtensions()
						.get(form.getAssessment()).before(now)) && (!user.isTutor())) {
			result.rejectValue("file", "Submission.AfterClosingDate");
		}
		AssessmentResult latestResult = manager.getLatestResultsForUserAssessment(user,
				form.getAssessment());
		int maxSubmissions = assessmentManager.getAssessment(form.getAssessment()).getNumSubmissionsAllowed();
		if ((!user.isTutor()) && latestResult != null && maxSubmissions != 0
				&& latestResult.getSubmissionsMade() >= maxSubmissions) {
			result.rejectValue("file", "Submission.NoAttempts");
		}
		if (!result.hasErrors()) {
			// accept the submission
			logger.info(ProjectProperties.getInstance().getAssessmentDAO()
					.getAssessment(form.getAssessment()).getName()
					+ " submitted by " + user.getUsername());
			manager.submit(user, form);
		}
		session.setAttribute("binding", result);
		return "redirect:/mirror/";
	}
	
	@RequestMapping("checkJobQueue/{assessmentId}/") 
	@ResponseBody
	public String checkJobQueue(@PathVariable("assessmentId") long assessmentId) {
		
		PASTAUser user = getUser();
		if (user == null) {
			return "";
		}
		
		List<AssessmentJob> jobs = scheduler.getAssessmentQueue();
		if(jobs == null || jobs.isEmpty()) {
			return "";
		}
		
		StringBuilder positions = new StringBuilder();
		int subCount = 0;
		for(int i = 0; i < jobs.size(); i++) {
			AssessmentJob job = jobs.get(i);
			if(job.getUser().equals(user) && job.getAssessmentId() == assessmentId) {
				if(subCount > 0) {
					positions.append(", ");
				}
				positions.append(i+1);
				subCount++;
			}
		}
		
		if(subCount == 0) {
			return "";
		} else if(subCount == 1) {
			return "Your submission is currently at position " + positions.toString() + " in the testing queue.";
		} else {
			int pos = positions.lastIndexOf(",");
			positions.replace(pos, pos+1, " and");
			return "Your submissions are currently at positions " + positions.toString() + " in the testing queue.";
		}
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
	 * <td>unikey</td>
	 * <td>{@link pasta.domain.PASTAUser} of the user logged in</td>
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
	 * @param assessmentId
	 *            the id of the assessment
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "user/viewAssessment"
	 */
	@RequestMapping(value = "info/{assessmentId}/")
	public String viewAssessmentInfo(@PathVariable("assessmentId") long assessmentId, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("assessment", assessmentManager.getAssessment(assessmentId));
		model.addAttribute("history",
				assessmentManager.getAssessmentHistory(user, assessmentId));
		model.addAttribute("nodeList", PASTAUtil.genereateFileTree(user, assessmentId));

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
	 * @param request
	 *            the http request that is kinda not used.
	 * @param response
	 *            also not really used
	 * @return the model and view (which is actually a
	 *         {@link pasta.view.ExcelMarkView})
	 */
	@RequestMapping(value = "downloadMarks/")
	public ModelAndView viewExcel(HttpServletRequest request, HttpServletResponse response) {
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
		data.put("latestResults", assessmentManager.getLatestResults(userManager.getUserList()));

		return new ModelAndView(new ExcelMarkView(), data);
	}

	/**
	 * $PASTAUrl$/downloadAutoMarks/
	 * <p>
	 * Download the only the automated marks (all but hand marking) as an excel
	 * sheet.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * @param request
	 *            the http request that is kinda not used.
	 * @param response
	 *            also not really used
	 * @return the model and view (which is actually a
	 *         {@link pasta.view.ExcelMarkView})
	 */
	@RequestMapping(value = "downloadAutoMarks/")
	public ModelAndView viewAutoExcel(HttpServletRequest request, HttpServletResponse response) {
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
		data.put("latestResults", assessmentManager.getLatestResults(userManager.getUserList()));

		return new ModelAndView(new ExcelAutoMarkView(), data);
	}

	/**
	 * $PASTAUrl$/student/{username}/info/{assessmentId}/updateComment/ - POST
	 * <p>
	 * Update the comment for a given user for a given assessment for a given
	 * submission using
	 * {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * 
	 * @param newComment
	 *            the new comment
	 * @param assessmentDate
	 *            the assessment date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param username
	 *            the name of the user
	 * @param assessmentId
	 *            the id of the assessment
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../"
	 */
	@RequestMapping(value = "student/{username}/info/{assessmentId}/updateComment/", method = RequestMethod.POST)
	public String updateComment(@RequestParam("newComment") String newComment,
			@RequestParam("resultId") long resultId, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		assessmentManager.updateComment(resultId, newComment);
		return "redirect:../";
	}

	/**
	 * $PASTAUrl$/viewFile/loadFile - GET
	 * <p>
	 * View a file.
	 * 
	 * If the user has authenticated and is a tutor, serve up the document,
	 * otherwise do nothing.
	 * 
	 * <b>Not sure if it's actually being used</b>
	 * 
	 * @param fileName
	 *            the path to the file.
	 * @param response
	 *            the http response being used to serve the content
	 */
	@RequestMapping(value = "viewFile/loadFile", method = RequestMethod.GET)
	public void getFile(@RequestParam("file_name") String fileName, HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user != null && user.isTutor()) {
			if (!codeStyle.containsKey(fileName.substring(fileName.lastIndexOf(".") + 1))) {
				try {
					// get your file as InputStream
					InputStream is = new FileInputStream(fileName.replace("\"", ""));
					// copy it to response's OutputStream
					IOUtils.copy(is, response.getOutputStream());
					response.flushBuffer();
					is.close();
				} catch (IOException ex) {
					throw new RuntimeException("IOError writing file to output stream");
				}
			}
		}
	}

	/**
	 * $PASTAUrl$/downloadFile
	 * <p>
	 * Download a file
	 * 
	 * If the user has authenticated and is a tutor, serve up the document,
	 * otherwise do nothing.
	 * 
	 * @param fileName
	 *            the path to the file.
	 * @param response
	 *            the http response being used to serve the content
	 */
	@RequestMapping(value = "downloadFile", method = RequestMethod.GET)
	public void downloadFile(@RequestParam("file_name") String fileName, HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user != null && user.isTutor()) {
			if (!codeStyle.containsKey(fileName.substring(fileName.lastIndexOf(".") + 1))) {
				try {
					// get your file as InputStream
					InputStream is = new FileInputStream(fileName.replace("\"", ""));
					// copy it to response's OutputStream
					response.setContentType("application/octet-stream;");
					response.setHeader(
							"Content-Disposition",
							"attachment; filename="
									+ fileName.replace("\"", "")
											.substring(
													fileName.replace("\"", "").replace("\\", "/")
															.lastIndexOf("/") + 1));
					IOUtils.copy(is, response.getOutputStream());
					response.flushBuffer();
					is.close();
				} catch (IOException ex) {
					throw new RuntimeException("IOError writing file to output stream");
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>{@link pasta.domain.PASTAUser} of the currently logged in user</td>
	 * </tr>
	 * <tr>
	 * <td>location</td>
	 * <td>The location of the disk of the file</td>
	 * </tr>
	 * <tr>
	 * <td>codeStyle</td>
	 * <td>The map of coding styles. Map<string, string></td>
	 * </tr>
	 * <tr>
	 * <td>fileEnding</td>
	 * <td>The file ending of the file you're viewing</td>
	 * </tr>
	 * <tr>
	 * <td>fileContents</td>
	 * <td>The contents of the file, with the &gt; and &lt; escaped</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>assessment/mark/viewFile</li>
	 * </ul>
	 * 
	 * @param location
	 *            the path to the file location
	 * @param model
	 *            the model being used
	 * @param response
	 *            the response that's not really being used
	 * @return "redirect:/login/" or "redirect:/home/"
	 */
	@RequestMapping(value = "viewFile/", method = RequestMethod.POST)
	public String viewFile(@RequestParam("location") String location, Model model,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		String fileEnding = location.substring(location.lastIndexOf(".") + 1);
//		if(fileEnding.equalsIgnoreCase("pdf")) {
			//TODO: figure out a way to redirect to pdfs
//			logger.warn("Redirecting to: redirect:" + location);
//			return "redirect:" + location;
//		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("location", location);
		model.addAttribute("codeStyle", codeStyle);
		model.addAttribute("fileEnding", fileEnding.toLowerCase());
		

		if (codeStyle.containsKey(location.substring(location.lastIndexOf(".") + 1))) {
			model.addAttribute("fileContents",
					PASTAUtil.scrapeFile(location).replace(">", "&gt;").replace("<", "&lt;"));
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>{@link pasta.domain.PASTAUser} for the currently logged in user</td>
	 * </tr>
	 * <tr>
	 * <td>viewedUser</td>
	 * <td>{@link pasta.domain.PASTAUser} for the viewed user</td>
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
	 * @param username
	 *            the name of the user you are looking at
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/studentHome"
	 */
	@RequestMapping(value = "student/{username}/home/")
	public String viewStudent(@PathVariable("username") String username, Model model) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("viewedUser", viewedUser);
		model.addAttribute("results", manager.getLatestResultsForUser(viewedUser));
		
		Map<String, Set<Assessment>> allAssessments = assessmentManager.getAllAssessmentsByCategory();
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
		Map<Long, Boolean> released = new HashMap<>();
		Map<Long, Boolean> closed = new HashMap<>();
		for(Assessment assessment : assessmentManager.getAssessmentList()) {
			Date due = UserManager.getDueDate(viewedUser, assessment);
			String formatted = PASTAUtil.formatDateReadable(due);
			dueDates.put(assessment.getId(), formatted);
			released.put(assessment.getId(), assessment.isReleasedTo(viewedUser));
			closed.put(assessment.getId(), assessment.isClosedFor(viewedUser));
		}
		model.addAttribute("dueDates", dueDates);
		model.addAttribute("released", released);
		model.addAttribute("closed", closed);
		
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
	@RequestMapping(value = "student/{username}/home/", method = RequestMethod.POST)
	public String submitAssessment(@PathVariable("username") String username,
			@ModelAttribute(value = "submission") Submission form, BindingResult result, Model model,
			HttpSession session) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}
		
		// check if the submission is valid
		if (form.getFile() == null || form.getFile().isEmpty()) {
			result.reject("Submission.NoFile");
		}
		if (!result.hasErrors()) {
			// accept the submission
			logger.info(ProjectProperties.getInstance().getAssessmentDAO()
					.getAssessment(form.getAssessment()).getName()
					+ " submitted for " + viewedUser.getUsername() + " by " + user.getUsername());
			manager.submit(viewedUser, form);
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>{@link pasta.domain.PASTAUser} of the user logged in</td>
	 * </tr>
	 * <tr>
	 * <td>viewedUser</td>
	 * <td>{@link pasta.domain.PASTAUser} of the user being viewed</td>
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
	 * @param username
	 *            the username for the user you are viewing
	 * @param assessmentId
	 *            the id of the assessment
	 * @param model
	 *            the model being used
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
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("viewedUser", viewedUser);
		model.addAttribute("assessment", assessmentManager.getAssessment(assessmentId));
		model.addAttribute("history", assessmentManager.getAssessmentHistory(viewedUser, assessmentId));
		model.addAttribute("nodeList", PASTAUtil.genereateFileTree(viewedUser, assessmentId));

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
	 * @param username
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
	@RequestMapping(value = "download/{username}/{assessmentId}/{assessmentDate}/")
	public void downloadAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate, Model model, HttpServletResponse response) {

		PASTAUser user = getUser();
		if (user == null || !user.isTutor()) {
			return;
		}

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
	 * @param username
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
	@RequestMapping(value = "runAssessment/{username}/{assessmentId}/{assessmentDate}/")
	public String runAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate, Model model, HttpServletRequest request) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}

		AssessmentResult result = assessmentManager.loadAssessmentResult(viewedUser, assessmentId,
				assessmentDate);
		manager.runAssessment(viewedUser, assessmentId, assessmentDate, result);
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
	 * {@link pasta.service.UserManager#giveExtension(String, long, Date)} then
	 * redirect to the referrer page.
	 * 
	 * @param username
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
	@RequestMapping(value = "student/{username}/extension/{assessmentId}/{extension}/")
	public String giveExtension(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId, @PathVariable("extension") String extension,
			Model model, HttpServletRequest request) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}
		if (user.isInstructor()) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			try {
				userManager.giveExtension(viewedUser, assessmentId, sdf.parse(extension));
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>{@link pasta.domain.PASTAUser} the user logged in</td>
	 * </tr>
	 * <tr>
	 * <td>student</td>
	 * <td>{@link pasta.domain.PASTAUser} the user being viewed</td>
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
	 * <td>The {@link pasta.domain.result.AssessmentResult} for this assessment</td>
	 * </tr>
	 * <tr>
	 * <td>handMarkingList</td>
	 * <td>The list of {@link pasta.domain.template.WeightedHandMarking}</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>assessment/mark/handMark</li>
	 * </ul>
	 * 
	 * @param username
	 *            the user which you want to download the submission for
	 * @param assessmentId
	 *            the id of the assessment
	 * @param assessmentDate
	 *            the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or
	 *         "assessment/mark/handMark"
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
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("student", username);

		String assessmentName = ProjectProperties.getInstance().getAssessmentDAO()
				.getAssessment(assessmentId).getName();
		model.addAttribute("assessmentName", assessmentName);

		// TODO this object's handmarkingresults are not being got from a passed
		// ID rom the JSP

		AssessmentResult result = assessmentManager.loadAssessmentResult(viewedUser, assessmentId,
				assessmentDate);

		logger.warn("Loading result: " + result.getId());
		logger.warn("hand marks: " + result.getHandMarkingResults().size());

		model.addAttribute("node", PASTAUtil.generateFileTree(viewedUser, assessmentId, assessmentDate));
		model.addAttribute("assessmentResult", result);
		model.addAttribute("handMarkingList", result.getAssessment().getHandMarking());
		model.addAttribute("handMarkingResultList", result.getHandMarkingResults());

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
	 * @param username
	 *            the user which you want to download the submission for
	 * @param assessmentId
	 *            the id of the assessment
	 * @param assessmentDate
	 *            the date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param form
	 *            the assessment result object
	 * @param result
	 *            the binding result used for feedback
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "mark/{username}/{assessmentId}/{assessmentDate}/", method = RequestMethod.POST)
	public String saveHandMarkAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("assessmentDate") String assessmentDate,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form, BindingResult result,
			Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		PASTAUser viewedUser = userManager.getUser(username);
		if(viewedUser == null) {
			return "redirect:/home/";
		}

		AssessmentResult assessResult = assessmentManager.loadAssessmentResult(viewedUser, assessmentId,
				assessmentDate);

		// rebinding hand marking results with their hand marking templates
		List<HandMarkingResult> results = form.getHandMarkingResults();
		for (HandMarkingResult currResult : results) {
			currResult.setWeightedHandMarking(handMarkingManager.getWeightedHandMarking(currResult
					.getWeightedHandMarking().getId()));
		}

		assessResult.setComments(form.getComments());
		assessResult.setHandMarkingResults(results);
		assessmentManager.updateAssessmentResults(assessResult);

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
	 * and
	 * {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * 
	 * GET: Takes you to the hand marking template(s) to the user.
	 * 
	 * If the studentIndex is pointing to a student that has no submissions, go
	 * to the next one.
	 * 
	 * If the index is past the end of the array, go back to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>unikey</td>
	 * <td>{@link pasta.domain.PASTAUser} of the user logged in</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentId</td>
	 * <td>the id of the assessment</td>
	 * </tr>
	 * <tr>
	 * <td>student</td>
	 * <td>{@link pasta.domain.PASTAUser} of the student being viewed</td>
	 * </tr>
	 * <tr>
	 * <td>node</td>
	 * <td>The root {@link pasta.domain.FileTreeNode} of the submitted files</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentResult</td>
	 * <td>The {@link pasta.domain.result.AssessmentResult} for this assessment</td>
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
	 * <td>The list of {@link pasta.domain.PASTAUser} of the users that belong
	 * in your class(es)</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>assessment/mark/handMarkBatch</li>
	 * </ul>
	 * 
	 * @param studentIndex
	 *            the index of the student being viewed
	 * @param assessmentId
	 *            the id of the assessment
	 * @param student
	 *            the student that you have just marked and is getting saved
	 * @param form
	 *            the assessment result form to be saved
	 * @param request
	 *            the http request that's not being used
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or
	 *         "assessment/mark/handMarkBatch"
	 */
	@RequestMapping(value = "mark/{assessmentId}/{studentIndex}/", method = { RequestMethod.POST,
			RequestMethod.GET })
	public String handMarkAssessmentBatch(@PathVariable("studentIndex") String studentIndex,
			@PathVariable("assessmentId") long assessmentId,
			@RequestParam(value = "student", required = false) String student,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form, HttpServletRequest request,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		PASTAUser viewedUser = userManager.getUser(student);

		model.addAttribute("unikey", user);
		model.addAttribute("assessmentId", assessmentId);
		
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(assessment == null) {
			return "redirect:/home/.";
		}
		model.addAttribute("assessmentName", assessment.getName());

		Collection<PASTAUser> myUsers = userManager.getTutoredStudents(user);
		PASTAUser[] myStudents = myUsers.toArray(new PASTAUser[0]);

		// if submitted
		if (form != null && viewedUser != null) {

			// save changes
			List<HandMarkingResult> results = form.getHandMarkingResults();
			for (HandMarkingResult currResult : results) {
				currResult.setWeightedHandMarking(handMarkingManager.getWeightedHandMarking(currResult
						.getWeightedHandMarking().getId()));
			}

			AssessmentResult assessResult = assessmentManager
					.getLatestAssessmentResult(viewedUser, assessmentId);
			assessResult.setComments(form.getComments());
			assessResult.setHandMarkingResults(results);
			assessmentManager.updateAssessmentResults(assessResult);
		}

		boolean[] hasSubmission = new boolean[myStudents.length];
		boolean[] completedMarking = new boolean[myStudents.length];
		int lastIndex = -1;
		for (int i = 0; i < myStudents.length; ++i) {
			AssessmentResult latestResult = manager.getLatestResultsForUserAssessment(
					myStudents[i], assessmentId);
			hasSubmission[i] = (myStudents[i] != null && latestResult != null);
			if(hasSubmission[i]) {
				lastIndex = i;
			}
			if (hasSubmission[i]) {
				completedMarking[i] = latestResult.isFinishedHandMarking();
			}
		}

		// get the current student's submission
		try {
			int i_studentIndex = Integer.parseInt(studentIndex);
			if (i_studentIndex >= 0 && i_studentIndex < myStudents.length
					&& myStudents[i_studentIndex] != null && hasSubmission[i_studentIndex]) {

				PASTAUser currStudent = myStudents[i_studentIndex];

				model.addAttribute("student", currStudent.getUsername());

				AssessmentResult result = manager.getLatestResultsForUserAssessment(
						currStudent, assessmentId);
				model.addAttribute(
						"node",
						PASTAUtil.generateFileTree(currStudent, assessmentId,
								result.getFormattedSubmissionDate()));
				model.addAttribute("assessmentResult", result);
				model.addAttribute("handMarkingList", result.getAssessment().getHandMarking());
				model.addAttribute("handMarkingResultList", result.getHandMarkingResults());
				model.addAttribute("savingStudentIndex", i_studentIndex);
				model.addAttribute("hasSubmission", hasSubmission);
				model.addAttribute("completedMarking", completedMarking);
				model.addAttribute("myStudents", myStudents);
				
				if(i_studentIndex == lastIndex) {
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

	@RequestMapping(value = "mark/{assessmentId}/", method = RequestMethod.GET)
	public String handMarkAssessmentBatchStart(@PathVariable("assessmentId") long assessmentId,
			HttpServletRequest request, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("unikey", user);

		// find the first student with a submission
		int i = 0;
		for (PASTAUser student : userManager.getTutoredStudents(user)) {
			if (assessmentManager.getLatestAssessmentResult(student, assessmentId) != null) {
				return "redirect:" + request.getServletPath() + i + "/";
			}
			++i;
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
	 * Otherwise use {@link SubmissionController#generateJSON(PASTAUser[])} for
	 * all users.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "gradeCentre/DATA/")
	public @ResponseBody String viewGradeCentreData() {
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
	 * Otherwise use {@link SubmissionController#generateJSON(PASTAUser[])} for
	 * all users in the given stream. Return nothing if the stream doesn't
	 * exist.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "stream/{streamName}/DATA/")
	public @ResponseBody String viewStreamData(@PathVariable("streamName") String streamName) {
		PASTAUser currentUser = getUser();
		if (currentUser == null) {
			return "";
		}
		if (!currentUser.isTutor()) {
			return "";
		}

		if (userManager.getUserListByStream(streamName) == null) {
			return "";
		}
		return generateJSON(userManager.getUserListByStream(streamName).toArray(new PASTAUser[0]));
	}

	/**
	 * $PASTAUrl$/tutorial/{className}/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center for a tutorial
	 * class.
	 * 
	 * If the user has not authenticated or is not a tutor: return nothing.
	 * Otherwise use {@link SubmissionController#generateJSON(PASTAUser[])} for
	 * all users in the given tutorial class. Return nothing if the tutorial
	 * class doesn't exist.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "tutorial/{className}/DATA/")
	public @ResponseBody String viewTutorialData(@PathVariable("className") String className) {
		PASTAUser currentUser = getUser();
		if (currentUser == null) {
			return "";
		}
		if (!currentUser.isTutor()) {
			return "";
		}

		if (userManager.getUserListByTutorial(className) == null) {
			return "";
		}

		return generateJSON(userManager.getUserListByTutorial(className).toArray(new PASTAUser[0]));
	}

	/**
	 * $PASTAUrl$/myTutorial/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center for the user's
	 * tutorial class.
	 * 
	 * If the user has not authenticated or is not a tutor: return nothing.
	 * Otherwise use {@link SubmissionController#generateJSON(PASTAUser[])} for
	 * all users in the user's tutorial class.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "myTutorials/DATA/")
	public @ResponseBody String viewMyTutorialData() {
		PASTAUser currentUser = getUser();
		if (currentUser == null) {
			return "";
		}
		if (!currentUser.isTutor()) {
			return "";
		}

		Collection<PASTAUser> myUsers = userManager.getTutoredStudents(currentUser);
		return generateJSON(myUsers.toArray(new PASTAUser[0]));
	}

	/**
	 * Generate the JSON
	 * <p>
	 * format:
	 * 
	 * <pre>
	 * {@code {
	 * 	"data": 
	 * 	[
	 * 		{
	 * 			"name": "$username$",
	 * 			"stream": "$stream$",
	 * 			"class": "$class$",
	 * 			"$assessmentId$": {
	 * 				"mark": "######.###",
	 * 				"percentage": "double",
	 * 				"assessmentid": "$assessmentId$"
	 * 				"assessmentname": "$assessmentName$"
	 * 			},
	 * 			...
	 * 			"$assessmentId$": {
	 * 				"mark": "######.###",
	 * 				"percentage": "double",
	 * 				"assessmentid": "$assessmentId$"
	 * 				"assessmentname": "$assessmentName$"
	 * 			}
	 * 		},
	 * 		...
	 * 		{
	 * 			"name": "$username$",
	 * 			"stream": "$stream$",
	 * 			"class": "$class$",
	 * 			"$assessmentId$": {
	 * 				"mark": "######.###",
	 * 				"percentage": "double",
	 * 				"assessmentid": "$assessmentId$"
	 * 				"assessmentname": "$assessmentName$"
	 * 			},
	 * 			...
	 * 			"$assessmentId$": {
	 * 				"mark": "######.###",
	 * 				"percentage": "double",
	 * 				"assessmentid": "$assessmentId$"
	 * 				"assessmentname": "$assessmentName$"
	 * 			}
	 * 		}
	 * 	]
	 * }}
	 * </pre>
	 * 
	 * If there is no submission, mark and percentage will be "". Percentage is
	 * [1.0,0.0]. Mark is displayed to 3 decimal places.
	 * 
	 * @param allUsers
	 *            the users for which to generate the JSON
	 * @return the JSON string
	 */
	private String generateJSON(PASTAUser[] allUsers) {
		DecimalFormat df = new DecimalFormat("#.###");

		String data = "{\r\n  \"data\": [\r\n";

		Assessment[] allAssessments = assessmentManager.getAssessmentList().toArray(new Assessment[0]);
		for (int i = 0; i < allUsers.length; ++i) {
			PASTAUser user = allUsers[i];

			String userData = "    {\r\n";

			// name
			userData += "      \"name\": \"" + user.getUsername() + "\",\r\n";
			// stream
			userData += "      \"stream\": \"" + user.getStream() + "\",\r\n";
			// class
			userData += "      \"class\": \"" + user.getTutorial() + "\"";
					
			if(allAssessments.length > 0) {
				userData += ",";
			}
			userData += "\r\n";

			// marks
			for (int j = 0; j < allAssessments.length; j++) {
				// assessment mark
				Assessment currAssessment = allAssessments[j];
				userData += "      \"" + currAssessment.getId() + "\": {\r\n";
				String mark = "";
				String percentage = "";

				AssessmentResult latestResult = assessmentManager.getLatestAssessmentResult(
						user, currAssessment.getId());
				if (latestResult != null) {
					mark = df.format(latestResult.getMarks());
					percentage = String.valueOf(latestResult.getPercentage());
				}
				userData += "        \"mark\": \"" + mark + "\",\r\n";
				userData += "        \"percentage\": \"" + percentage + "\",\r\n";
				userData += "        \"assessmentid\": \"" + currAssessment.getId() + "\",\r\n";
				userData += "        \"assessmentname\": \"" + currAssessment.getName() + "\"\r\n";
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>The {@link pasta.domain.PASTAUser} of the currently logged in user</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentList</td>
	 * <td>The list of all {@link pasta.domain.template.Assessment} on the
	 * system</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>user/viewAll2</li>
	 * </ul>
	 * 
	 * @param model
	 *            the model being used
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

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>The {@link pasta.domain.PASTAUser} of the currently logged in user</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentList</td>
	 * <td>The list of all {@link pasta.domain.template.Assessment} on the
	 * system</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>user/viewSome</li>
	 * </ul>
	 * 
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/viewSome"
	 */
	@RequestMapping(value = "tutorial/{className}/")
	public String viewClass(@PathVariable("className") String className, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>The {@link pasta.domain.PASTAUser} of the currently logged in user</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentList</td>
	 * <td>The list of all {@link pasta.domain.template.Assessment} on the
	 * system</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>user/viewSome</li>
	 * </ul>
	 * 
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/viewSome"
	 */
	@RequestMapping(value = "stream/{streamName}/")
	public String viewStream(@PathVariable("streamName") String streamName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
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
	 * <tr>
	 * <td>unikey</td>
	 * <td>The {@link pasta.domain.PASTAUser} of the currently logged in user</td>
	 * </tr>
	 * <tr>
	 * <td>assessmentList</td>
	 * <td>The list of all {@link pasta.domain.template.Assessment} on the
	 * system</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>user/viewAll2</li>
	 * </ul>
	 * 
	 * @param model
	 *            the model being used
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

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewAll2";
	}

}
