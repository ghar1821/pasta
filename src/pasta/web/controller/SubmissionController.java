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
import java.util.zip.ZipEntry;
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

@Controller
@RequestMapping("/")
/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
public class SubmissionController {

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
	 * Get the currently logged in user.
	 * 
	 * @return
	 */
	public PASTAUser getOrCreateUser() {
		if(ProjectProperties.getInstance().getCreateAccountOnSuccessfulLogin()){
			String username = (String) RequestContextHolder
					.currentRequestAttributes().getAttribute("user",
							RequestAttributes.SCOPE_SESSION);
			return getOrCreateUser(username);
		}
		else{
			return getUser();
		}
	}

	public PASTAUser getOrCreateUser(String username) {
		if(ProjectProperties.getInstance().getCreateAccountOnSuccessfulLogin()){
			if (username != null) {
				return userManager.getOrCreateUser(username);
			}
			return null;
		}
		else{
			return getUser(username);
		}
	}

	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return getUser(username);
	}

	public PASTAUser getUser(String username) {
		if (username != null) {
			return userManager.getUser(username);
		}
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HOME //
	// ///////////////////////////////////////////////////////////////////////////

	// redirect back
	@RequestMapping(value = "mirror/")
	public String goBack(HttpServletRequest request, HttpSession session) {
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}

	// home page
	@RequestMapping(value = "home/")
	public String home(Model model, HttpSession session) {
		// check if tutor or student
		PASTAUser user = getOrCreateUser();
		if (user != null) {
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
		return "redirect:/login/";
	}

	// home page
	@RequestMapping(value = "home/", method = RequestMethod.POST)
	public String submitAssessment(
			@ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model,
			HttpSession session) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:../login";
		}
		// check if the submission is valid
		if (form.getFile() == null || form.getFile().isEmpty()) {
			result.rejectValue("file", "Submission.NoFile");
		}

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
			logger.info(form.getAssessment() + " submitted for "
					+ user.getUsername() + " by " + user.getUsername());
			manager.submit(user.getUsername(), form);
		}
		session.setAttribute("binding", result);
		return "redirect:/mirror/";
	}

	// history
	@RequestMapping(value = "info/{assessmentName}/")
	public String viewAssessmentInfo(
			@PathVariable("assessmentName") String assessmentName, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		model.addAttribute("assessment",
				assessmentManager.getAssessment(assessmentName));
		model.addAttribute("history", assessmentManager.getAssessmentHistory(
				user.getUsername(), assessmentName));
		model.addAttribute("nodeList",
				PASTAUtil.genereateFileTree(user.getUsername(), assessmentName));
		model.addAttribute("unikey", user);

		return "user/viewAssessment";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// VIEW //
	// ///////////////////////////////////////////////////////////////////////////

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

	@RequestMapping(value = "student/{username}/info/{assessmentName}/updateComment/", method = RequestMethod.POST)
	public String updateComment(@RequestParam("newComment") String newComment,
			@RequestParam("assessmentDate") String assessmentDate,
			@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		handMarkingManager.saveComment(username, assessmentName,
				assessmentDate, newComment);
		return "redirect:../";
	}

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

	@RequestMapping(value = "viewFile/", method = RequestMethod.POST)
	public String viewFile(@RequestParam("location") String location,
			Model model, HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("location", location);
		model.addAttribute("unikey", user);
		model.addAttribute("codeStyle", codeStyle);
		model.addAttribute("fileEnding",
				location.substring(location.lastIndexOf(".") + 1));

		if (codeStyle
				.containsKey(location.substring(location.lastIndexOf(".") + 1))) {
			model.addAttribute("fileContents", PASTAUtil.scrapeFile(location)
					.replace(">", "&gt;").replace("<", "&lt;"));

			return "assessment/mark/viewFile";
		}
		// else{
		// try {
		// // get your file as InputStream
		// InputStream is = new FileInputStream(location);
		// // copy it to response's OutputStream
		// IOUtils.copy(is, response.getOutputStream());
		// response.flushBuffer();
		// } catch (IOException ex) {
		// throw new RuntimeException("IOError writing file to output stream");
		// }
		// }
		return "assessment/mark/viewFile";
	}

	// home page
	@RequestMapping(value = "student/{username}/home/")
	public String viewStudent(@PathVariable("username") String username,
			Model model) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
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

	// home page
	@RequestMapping(value = "student/{username}/home/", method = RequestMethod.POST)
	public String submitAssessment(@PathVariable("username") String username,
			@ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		// check if the submission is valid
		if (form.getFile() == null || form.getFile().isEmpty()) {
			result.reject("Submission.NoFile");
		}
		// if (manager.getAssessment(form.getAssessment()).isClosed()) {
		// result.reject("Submission.AfterClosingDate");
		// }
		if (!result.hasErrors()) {
			// accept the submission
			logger.info(form.getAssessment() + " submitted for " + username
					+ " by " + user.getUsername());
			manager.submit(username, form);
		}
		return "redirect:.";
	}

	// history
	@RequestMapping(value = "student/{username}/info/{assessmentName}/")
	public String viewAssessmentInfo(@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		model.addAttribute("assessment",
				assessmentManager.getAssessment(assessmentName));
		model.addAttribute("history", assessmentManager.getAssessmentHistory(
				username, assessmentName));
		model.addAttribute("unikey", user);
		model.addAttribute("viewedUser", getUser(username));
		model.addAttribute("nodeList",
				PASTAUtil.genereateFileTree(username, assessmentName));

		return "user/viewAssessment";
	}

	// download submission assessment
	@RequestMapping(value = "download/{username}/{assessmentName}/{assessmentDate}/")
	public void downloadAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("assessmentDate") String assessmentDate, Model model,
			HttpServletResponse response) {
		
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=\""
				+ username + "-" + assessmentName + "-" + assessmentDate
				+ ".zip\"");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outStream);
		try {
			zip(zip, new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/submissions/"
					+ username
					+ "/assessments/"
					+ assessmentName
					+ "/"
					+ assessmentDate
					+ "/submission/"), ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/submissions/"
					+ username
					+ "/assessments/"
					+ assessmentName
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

	private void zip(ZipOutputStream zip, File file, String remove) {
		byte[] buffer = new byte[1024];
		if (file.isFile()) {
			// file - zip it
			try {
				ZipEntry ze = new ZipEntry(file.getAbsolutePath().substring(remove.length()));//file.getAbsolutePath().replace(remove, ""));
				zip.putNextEntry(ze);
				FileInputStream in = new FileInputStream(file);
				int len;
				while ((len = in.read(buffer)) > 0) {
					zip.write(buffer, 0, len);
				}
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// directory - keep going
			for (File f : file.listFiles()) {
				zip(zip, f, remove);
			}
		}
	}

	// re-run assessment
	@RequestMapping(value = "runAssessment/{username}/{assessmentName}/{assessmentDate}/")
	public String runAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("assessmentDate") String assessmentDate, Model model,
			HttpServletRequest request) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		manager.runAssessment(username, assessmentName, assessmentDate);
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}

	// hand mark assessment
	@RequestMapping(value = "mark/{username}/{assessmentName}/{assessmentDate}/")
	public String handMarkAssessment(@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("assessmentDate") String assessmentDate, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		model.addAttribute("unikey", user);
		model.addAttribute("student", username);
		model.addAttribute("assessmentName", assessmentName);

		AssessmentResult result = assessmentManager.getAssessmentResult(
				username, assessmentName, assessmentDate);

		model.addAttribute("node", PASTAUtil.generateFileTree(username,
				assessmentName, assessmentDate));
		model.addAttribute("assessmentResult", result);
		model.addAttribute("handMarkingList", result.getAssessment()
				.getHandMarking());

		return "assessment/mark/handMark";
	}

	// hand mark assessment
	@RequestMapping(value = "mark/{username}/{assessmentName}/{assessmentDate}/", method = RequestMethod.POST)
	public String saveHandMarkAssessment(
			@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("assessmentDate") String assessmentDate,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form,
			BindingResult result, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		// rebinding hand marking results with their hand marking templates
		List<HandMarkingResult> results = form.getHandMarkingResults();
		for (HandMarkingResult currResult : results) {
			currResult.setMarkingTemplate(handMarkingManager
					.getHandMarking(currResult
							.getHandMarkingTemplateShortName()));
		}
		handMarkingManager.saveHandMarkingResults(username, assessmentName,
				assessmentDate, form.getHandMarkingResults());
		handMarkingManager.saveComment(username, assessmentName,
				assessmentDate, form.getComments());

		return "redirect:.";
	}

	@RequestMapping(value = "mark/{assessmentName}/{studentIndex}/", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String handMarkAssessmentBatch(
			@PathVariable("studentIndex") String studentIndex,
			@PathVariable("assessmentName") String assessmentName,
			@RequestParam(value = "student", required = false) String student,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form,
			HttpServletRequest request, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("assessmentName", assessmentName);

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
						.getHandMarking(currResult
								.getHandMarkingTemplateShortName()));
			}
			handMarkingManager.saveHandMarkingResults(student, assessmentName,
					manager.getLatestResultsForUser(student)
							.get(assessmentName).getFormattedSubmissionDate(),
					form.getHandMarkingResults());
			handMarkingManager.saveComment(student, assessmentName, manager
					.getLatestResultsForUser(student).get(assessmentName)
					.getFormattedSubmissionDate(), form.getComments());
		}

		boolean[] hasSubmission = new boolean[myStudents.length];
		boolean[] completedMarking = new boolean[myStudents.length];
		for (int i = 0; i < myStudents.length; ++i) {

			hasSubmission[i] = (myStudents[i] != null
					&& manager.getLatestResultsForUser(myStudents[i]
							.getUsername()) != null && manager
					.getLatestResultsForUser(myStudents[i].getUsername()).get(
							assessmentName) != null);
			if (hasSubmission[i]) {
				completedMarking[i] = manager
						.getLatestResultsForUser(myStudents[i].getUsername())
						.get(assessmentName).isFinishedHandMarking();
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
						currStudent.getUsername()).get(assessmentName);
				model.addAttribute("node", PASTAUtil.generateFileTree(
						currStudent.getUsername(), assessmentName,
						result.getFormattedSubmissionDate()));
				model.addAttribute("assessmentResult", result);
				model.addAttribute("handMarkingList", result.getAssessment()
						.getHandMarking());

				model.addAttribute("savingStudentIndex", i_studentIndex);
				model.addAttribute("hasSubmission", hasSubmission);
				model.addAttribute("completedMarking", completedMarking);
				model.addAttribute("myStudents", myStudents);

			} else {
				return "redirect:/home/.";
			}

		} catch (NumberFormatException e) {
			return "redirect:/home/.";
		}

		return "assessment/mark/handMarkBatch";
	}

	// hand mark assessment
	@RequestMapping(value = "mark/{assessmentName}/", method = RequestMethod.GET)
	public String handMarkAssessmentBatchStart(
			@PathVariable("assessmentName") String assessmentName,
			HttpServletRequest request, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("assessmentName", assessmentName);

		if (assessmentManager.getAssessment(assessmentName) != null) {
			// find the first student with a submission
			int i = 0;
			for (String tutorial : user.getTutorClasses()) {
				for (PASTAUser student : userManager
						.getUserListByTutorial(tutorial)) {
					if (manager.getLatestResultsForUser(student.getUsername()) != null
							&& manager.getLatestResultsForUser(
									student.getUsername()).get(assessmentName) != null) {
						return "redirect:" + request.getServletPath() + i + "/";
					}
					++i;
				}
			}
		}
		return "redirect:/home/.";
	}

	// hand mark assessment
	@RequestMapping(value = "mark/{assessmentName}/", method = RequestMethod.POST)
	public String handMarkAssessment(
			@RequestParam("currStudentIndex") String s_currStudentIndex,
			@PathVariable("assessmentName") String assessmentName,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form,
			HttpServletRequest request, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("assessmentName", assessmentName);

		Collection<PASTAUser> myUsers = new LinkedList<PASTAUser>();
		for (String tutorial : user.getTutorClasses()) {
			myUsers.addAll(userManager.getUserListByTutorial(tutorial));
		}

		// save the latest submission
		if (!request.getHeader("Referer").endsWith("/home/")
				&& Integer.parseInt(s_currStudentIndex) > 0) {
			// get previous user
			int prevStudentIndex = Integer.parseInt(s_currStudentIndex) - 1;
			PASTAUser prevStudent = (PASTAUser) myUsers.toArray()[prevStudentIndex];

			// rebinding hand marking results with their hand marking templates
			List<HandMarkingResult> results = form.getHandMarkingResults();
			for (HandMarkingResult currResult : results) {
				currResult.setMarkingTemplate(handMarkingManager
						.getHandMarking(currResult
								.getHandMarkingTemplateShortName()));
			}

			AssessmentResult result = manager.getLatestResultsForUser(
					prevStudent.getUsername()).get(assessmentName);

			handMarkingManager.saveHandMarkingResults(
					prevStudent.getUsername(), assessmentName,
					result.getFormattedSubmissionDate(),
					form.getHandMarkingResults());
			handMarkingManager.saveComment(prevStudent.getUsername(),
					assessmentName, result.getFormattedSubmissionDate(),
					form.getComments());
		}

		// get the correct new student index
		int currStudentIndex = 0;
		try {
			currStudentIndex = Integer.parseInt(s_currStudentIndex);
		} catch (Exception e) {
		}

		if (currStudentIndex >= myUsers.size()) {
			return "redirect:../../home/";
		}

		PASTAUser currStudent = (PASTAUser) myUsers.toArray()[currStudentIndex];

		// make sure the current student has work to be marked and is not you
		while (currStudentIndex < myUsers.size()
				&& (manager.getLatestResultsForUser(currStudent.getUsername()) == null || manager
						.getLatestResultsForUser(currStudent.getUsername())
						.get(assessmentName) == null)
				|| (currStudent.getUsername() == user.getUsername())) {
			currStudentIndex++;
			currStudent = (PASTAUser) myUsers.toArray()[currStudentIndex];
		}

		if (currStudentIndex >= myUsers.size()) {
			return "redirect:../../home/";
		}

		if (currStudentIndex < myUsers.size()) {
			model.addAttribute("student", currStudent.getUsername());

			AssessmentResult result = manager.getLatestResultsForUser(
					currStudent.getUsername()).get(assessmentName);
			model.addAttribute("node", PASTAUtil.generateFileTree(
					currStudent.getUsername(), assessmentName,
					result.getFormattedSubmissionDate()));
			model.addAttribute("assessmentResult", result);
			model.addAttribute("handMarkingList", result.getAssessment()
					.getHandMarking());

			model.addAttribute("currStudentIndex", currStudentIndex);
			model.addAttribute("maxStudentIndex", myUsers.size() - 1);
		}

		// check if they are the last
		int nextStudentIndex = currStudentIndex + 1;
		if (nextStudentIndex < myUsers.size()) {
			PASTAUser nextStudent = (PASTAUser) myUsers.toArray()[nextStudentIndex];
			while (nextStudentIndex < myUsers.size()
					&& (manager.getLatestResultsForUser(nextStudent
							.getUsername()) == null || manager
							.getLatestResultsForUser(nextStudent.getUsername())
							.get(assessmentName) == null)) {
				nextStudentIndex++;
				nextStudent = (PASTAUser) myUsers.toArray()[nextStudentIndex];
			}
		}

		if (nextStudentIndex >= myUsers.size()) {
			model.addAttribute("last", true);
		}

		return "assessment/mark/handMarkBatch";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// GRADE CENTRE //
	// ///////////////////////////////////////////////////////////////////////////

	// AJAX DATA
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

		DecimalFormat df = new DecimalFormat("#.###");

		String data = "{\r\n  \"data\": [\r\n";
		// latestResults[user.username][assessment.shortName].marks
		PASTAUser[] allUsers = userManager.getStudentList().toArray(
				new PASTAUser[0]);
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
				userData += "      \"" + currAssessment.getShortName()
						+ "\": {\r\n";
				String mark = "";
				String percentage = "";

				if (assessmentManager.getLatestResultsForUser(user
						.getUsername()) != null
						&& assessmentManager.getLatestResultsForUser(
								user.getUsername()).get(
								currAssessment.getShortName()) != null) {
					mark = df.format(assessmentManager
							.getLatestResultsForUser(user.getUsername())
							.get(currAssessment.getShortName()).getMarks());
					percentage = ""
							+ assessmentManager
									.getLatestResultsForUser(user.getUsername())
									.get(currAssessment.getShortName())
									.getPercentage();
				}
				userData += "        \"mark\": \"" + mark + "\",\r\n";
				userData += "        \"percentage\": \"" + percentage
						+ "\",\r\n";
				userData += "        \"assessmentname\": \""
						+ currAssessment.getShortName() + "\"\r\n";
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
				userData += "      \"" + currAssessment.getShortName()
						+ "\": {\r\n";
				String mark = "";
				String percentage = "";

				if (assessmentManager.getLatestResultsForUser(user
						.getUsername()) != null
						&& assessmentManager.getLatestResultsForUser(
								user.getUsername()).get(
								currAssessment.getShortName()) != null) {
					mark = df.format(assessmentManager
							.getLatestResultsForUser(user.getUsername())
							.get(currAssessment.getShortName()).getMarks());
					percentage = ""
							+ assessmentManager
									.getLatestResultsForUser(user.getUsername())
									.get(currAssessment.getShortName())
									.getPercentage();
				}
				userData += "        \"mark\": \"" + mark + "\",\r\n";
				userData += "        \"percentage\": \"" + percentage
						+ "\",\r\n";
				userData += "        \"assessmentname\": \""
						+ currAssessment.getShortName() + "\"\r\n";
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

	@RequestMapping(value = "gradeCentre/")
	public String viewGradeCentre2(Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewAll2";
	}

	// home page
	@RequestMapping(value = "tutorial/{className}/")
	public String viewClass(@PathVariable("className") String className,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewSome";
	}

	// home page
	@RequestMapping(value = "stream/{streamName}/")
	public String viewStream(@PathVariable("streamName") String streamName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewSome";
	}

	@RequestMapping(value = "myTutorials/")
	public String viewMyTutorials(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);

		return "user/viewAll2";
	}

	@RequestMapping(value = "student/{username}/extension/{assessmentName}/{extension}/")
	public String giveExtension(@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("extension") String extension, Model model,
			HttpServletRequest request) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (user.isInstructor()) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			try {
				userManager.giveExtension(username, assessmentName,
						sdf.parse(extension));
			} catch (ParseException e) {
				logger.error("Parse Exception");
			}
		}
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}

}