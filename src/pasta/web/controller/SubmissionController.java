package pasta.web.controller;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		codeStyle = new HashMap<String, String>();
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
	private HashMap<String, String> codeStyle;

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
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return getOrCreateUser(username);
	}
	
	public PASTAUser getOrCreateUser(String username) {
		if (username != null) {
			return userManager.getOrCreateUser(username);
		}
		return null;
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
	@RequestMapping(value="mirror/")
	public String goBack(HttpServletRequest request){
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}
	
	// home page
	@RequestMapping(value = "home/")
	public String home(Model model) {
		// check if tutor or student
		PASTAUser user = getOrCreateUser();
		if (user != null) {
			model.addAttribute("unikey", user);
			model.addAttribute("assessments", assessmentManager.getAllAssessmentsByCategory());
			model.addAttribute("results",
					manager.getLatestResultsForUser(user.getUsername()));
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
			BindingResult result, Model model) {
		
		PASTAUser user = getUser();
		if(user == null){
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
					|| user.getExtensions().get(form.getAssessment()) == null
					|| user.getExtensions().get(form.getAssessment()).before(now))
				&& (!user.isTutor())) {
			result.rejectValue("file", "Submission.AfterClosingDate");
		}
		if((!user.isTutor()) && 
				manager.getLatestResultsForUser(user.getUsername()) != null &&
				manager.getLatestResultsForUser(user.getUsername()).get(form.getAssessment()) != null &&
					manager.getLatestResultsForUser(user.getUsername()).get(form.getAssessment()).getSubmissionsMade() >= assessmentManager.getAssessment(form.getAssessment()).getNumSubmissionsAllowed()){
			result.rejectValue("file", "Submission.NoAttempts");
		}
		if(!result.hasErrors()){
			// accept the submission
			logger.info(form.getAssessment() + " submitted for "
					+ user.getUsername() + " by "
					+ user.getUsername());
			manager.submit(user.getUsername(), form);
		}
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
		model.addAttribute("assessment", assessmentManager.getAssessment(assessmentName));
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
		Map<String, Object> data = new HashMap<String, Object>();

		data.put("assessmentList", assessmentManager.getAssessmentList());
		data.put("userList", userManager.getUserList());
		data.put("latestResults", assessmentManager.getLatestResults(userManager.getUserList()));
		
		return new ModelAndView(new ExcelMarkView(), data);
	}
	
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
		Map<String, Object> data = new HashMap<String, Object>();

		data.put("assessmentList", assessmentManager.getAssessmentList());
		data.put("userList", userManager.getUserList());
		data.put("latestResults", assessmentManager.getLatestResults(userManager.getUserList()));
		
		return new ModelAndView(new ExcelAutoMarkView(), data);
	}
	
	@RequestMapping(value = "student/{username}/info/{assessmentName}/updateComment/", method = RequestMethod.POST)
	public String updateComment(@RequestParam("newComment") String newComment,
			@RequestParam("assessmentDate") String assessmentDate,
			@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		handMarkingManager.saveComment(username, assessmentName, assessmentDate, newComment);
		return "redirect:../";
	}

	


	@RequestMapping(value = "viewFile/loadFile", method = RequestMethod.GET)
	public void getFile(@RequestParam("file_name") String fileName,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user != null && user.isTutor()) {
			if(!codeStyle.containsKey(fileName.substring(fileName.lastIndexOf(".") + 1))){
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
	
	@RequestMapping(value = "downloadFile", method = RequestMethod.GET)
	public void downloadFile(@RequestParam("file_name") String fileName,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user != null && user.isTutor()) {
			if(!codeStyle.containsKey(fileName.substring(fileName.lastIndexOf(".") + 1))){
				try {
			      // get your file as InputStream
			      InputStream is = new FileInputStream(fileName.replace("\"", ""));
			      // copy it to response's OutputStream
			      response.setContentType("application/octet-stream;");
			      response.setHeader("Content-Disposition", "attachment; filename="+fileName.replace("\"", "")
			    		  .substring(fileName.replace("\"", "").replace("\\", "/").lastIndexOf("/") + 1));
			      IOUtils.copy(is, response.getOutputStream());
			      response.flushBuffer();
			      is.close();
			    } catch (IOException ex) {
			      throw new RuntimeException("IOError writing file to output stream");
			    }
			}
		}
	}
	
	
	@RequestMapping(value = "viewFile/", method = RequestMethod.POST)
	public String viewFile(@RequestParam("location") String location,
			Model model,  
		    HttpServletResponse response) {
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
	
		if(codeStyle.containsKey(location.substring(location.lastIndexOf(".") + 1))){
			model.addAttribute("fileContents", PASTAUtil.scrapeFile(location)
					.replace(">", "&gt;").replace("<", "&lt;"));

			return "assessment/mark/viewFile";
		}
//		else{
//			try {
//			      // get your file as InputStream
//			      InputStream is = new FileInputStream(location);
//			      // copy it to response's OutputStream
//			      IOUtils.copy(is, response.getOutputStream());
//			      response.flushBuffer();
//			    } catch (IOException ex) {
//			      throw new RuntimeException("IOError writing file to output stream");
//			    }
//		}
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
		model.addAttribute("assessments", assessmentManager.getAllAssessmentsByCategory());
		model.addAttribute("results", manager
				.getLatestResultsForUser(viewedUser.getUsername()));
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
//		if (manager.getAssessment(form.getAssessment()).isClosed()) {
//			result.reject("Submission.AfterClosingDate");
//		}
		if(!result.hasErrors()){
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
		model.addAttribute("assessment", assessmentManager.getAssessment(assessmentName));
		model.addAttribute("history",
				assessmentManager.getAssessmentHistory(username, assessmentName));
		model.addAttribute("unikey", user);
		model.addAttribute("viewedUser", getUser(username));
		model.addAttribute("nodeList",
				PASTAUtil.genereateFileTree(username, assessmentName));

		return "user/viewAssessment";
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
		if(user.isInstructor()){
			manager.runAssessment(username, assessmentName, assessmentDate);
		}
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

		AssessmentResult result = assessmentManager.getAssessmentResult(username,
				assessmentName, assessmentDate);

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
			currResult.setMarkingTemplate(handMarkingManager.getHandMarking(currResult
					.getHandMarkingTemplateShortName()));
		}
		handMarkingManager.saveHandMarkingResults(username, assessmentName,
				assessmentDate, form.getHandMarkingResults());
		handMarkingManager.saveComment(username, assessmentName, assessmentDate,
				form.getComments());

		return "redirect:.";
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
		if (!request.getHeader("Referer").endsWith("/home/") && Integer.parseInt(s_currStudentIndex) > 0) {
			// get previous user
			int prevStudentIndex = Integer.parseInt(s_currStudentIndex) - 1;
			PASTAUser prevStudent = (PASTAUser) myUsers.toArray()[prevStudentIndex];

			// rebinding hand marking results with their hand marking templates
			List<HandMarkingResult> results = form.getHandMarkingResults();
			for (HandMarkingResult currResult : results) {
				currResult.setMarkingTemplate(handMarkingManager.getHandMarking(currResult
						.getHandMarkingTemplateShortName()));
			}

			AssessmentResult result = manager.getLatestResultsForUser(
					prevStudent.getUsername()).get(assessmentName);

			handMarkingManager.saveHandMarkingResults(prevStudent.getUsername(),
					assessmentName, result.getFormattedSubmissionDate(),
					form.getHandMarkingResults());
			handMarkingManager.saveComment(prevStudent.getUsername(), assessmentName,
					result.getFormattedSubmissionDate(), form.getComments());
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
						.get(assessmentName) == null) || (currStudent.getUsername() == user.getUsername())) {
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

	@RequestMapping(value = "gradeCentre/")
	public String viewGradeCentre(Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		HashMap<String, HashMap<String, AssessmentResult>> allResults = assessmentManager.getLatestResults(userManager.getUserList());
		HashMap<String, TreeMap<Integer, Integer>> submissionDistribution = new HashMap<String, TreeMap<Integer, Integer>>();
		Collection<Assessment> assessments = assessmentManager.getAssessmentList();

		int numBreaks = 10;

		HashMap<String, Integer[]> markDistribution = new HashMap<String, Integer[]>();

		for (Assessment assessment : assessments) {
			int[] currMarkDist = new int[numBreaks + 1];
			TreeMap<Integer, Integer> currSubmissionDistribution = new TreeMap<Integer, Integer>();
			for (Entry<String, HashMap<String, AssessmentResult>> entry : allResults
					.entrySet()) {
				int spot = 0;
				int numSubmissionsMade = 0;
				if (entry.getValue() != null
						&& entry.getValue().get(assessment.getShortName()) != null) {
					spot = ((int) (entry.getValue()
							.get(assessment.getShortName()).getPercentage() * 100 / (100 / numBreaks)));
					numSubmissionsMade = entry.getValue()
							.get(assessment.getShortName())
							.getSubmissionsMade();
				}
				// mark histogram
				currMarkDist[spot]++;

				// # submission distribution
				if (!currSubmissionDistribution.containsKey(numSubmissionsMade)) {
					currSubmissionDistribution.put(numSubmissionsMade, 0);
				}
				currSubmissionDistribution.put(numSubmissionsMade,
						currSubmissionDistribution.get(numSubmissionsMade) + 1);
			}

			// add to everything list
			submissionDistribution.put(assessment.getShortName(),
					currSubmissionDistribution);

			Integer[] tempCurrMarkDist = new Integer[currMarkDist.length];
			for (int i = 0; i < currMarkDist.length; ++i) {
				tempCurrMarkDist[i] = currMarkDist[i];
			}

			markDistribution.put(assessment.getShortName(), tempCurrMarkDist);
		}

		model.addAttribute("assessments", assessments);
		model.addAttribute("maxBreaks", numBreaks);
		model.addAttribute("markDistribution", markDistribution);
		model.addAttribute("submissionDistribution", submissionDistribution);

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
		model.addAttribute("userList", userManager.getUserList());
		model.addAttribute("latestResults", assessmentManager.getLatestResults(userManager.getUserList()));
		model.addAttribute("unikey", user);

		return "user/viewAll";
	}

	// home page
	@RequestMapping(value = "tutorial/{className}/")
	public String viewClass(@PathVariable("className") String className,
			Model model) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("userList",
				userManager.getUserListByTutorial(className));
		model.addAttribute("latestResults", assessmentManager.getLatestResults(userManager.getUserList()));
		model.addAttribute("unikey", user);
		model.addAttribute("classname", "Class - " + className);

		return "compound/classHome";
	}

	// home page
	@RequestMapping(value = "stream/{streamName}/")
	public String viewStream(@PathVariable("streamName") String streamName,
			Model model) {
		// check if tutor or student
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		model.addAttribute("assessmentList",
				assessmentManager.getAssessmentList());
		model.addAttribute("userList",
				userManager.getUserListByStream(streamName));
		model.addAttribute("latestResults", assessmentManager.getLatestResults(userManager.getUserList()));
		model.addAttribute("unikey", user);
		model.addAttribute("classname", "Stream - " + streamName);
	
		return "compound/classHome";
	}
	
	@RequestMapping(value = "student/{username}/extension/{assessmentName}/{extension}/")
	public String giveExtension(@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("extension") String extension,
			Model model,
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
				userManager.giveExtension(username, assessmentName, sdf.parse(extension));
			} catch (ParseException e) {
				logger.error("Parse Exception");
			}
		} 
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}

}