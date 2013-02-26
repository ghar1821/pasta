package pasta.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.LoginForm;
import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewCompetition;
import pasta.domain.upload.NewHandMarking;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.service.SubmissionManager;
import pasta.util.ProjectProperties;

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
	
	public SubmissionController(){
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
	private HashMap<String, String> codeStyle;

	@Autowired
	public void setMyService(SubmissionManager myService) {
		this.manager = myService;
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newUnitTestModel")
	public NewUnitTest returnNewUnitTestModel() {
		return new NewUnitTest();
	}
	
	@ModelAttribute("newHandMarkingModel")
	public NewHandMarking returnNewHandMakingModel() {
		return new NewHandMarking();
	}
	
	@ModelAttribute("newCompetitionModel")
	public NewCompetition returnNewCompetitionModel() {
		return new NewCompetition();
	}

	@ModelAttribute("submission")
	public Submission returnSubmissionModel() {
		return new Submission();
	}

	@ModelAttribute("assessment")
	public Assessment returnAssessmentModel() {
		return new Assessment();
	}
	
	@ModelAttribute("competition")
	public Competition returnCompetitionModel() {
		return new Competition();
	}
	
	@ModelAttribute("handMarking")
	public HandMarking returnHandMarkingModel() {
		return new HandMarking();
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
		String username = (String) RequestContextHolder.currentRequestAttributes()
				.getAttribute("user", RequestAttributes.SCOPE_SESSION);
//		username = "arad0726";
		if(username != null){
			return manager.getOrCreateUser(username);
		}
		return null;
	}
	
	public PASTAUser getUser() {
		String username = (String) RequestContextHolder.currentRequestAttributes()
				.getAttribute("user", RequestAttributes.SCOPE_SESSION);
		if(username != null){
			return manager.getUser(username);
		}
		return null;
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// ASSESSMENTS //
	// ///////////////////////////////////////////////////////////////////////////

	// view an assessment
	@RequestMapping(value = "assessments/{assessmentName}/", method = RequestMethod.POST)
	public String viewAssessment(
			@PathVariable("assessmentName") String assessmentName,
			@ModelAttribute(value = "assessment") Assessment form,
			BindingResult result, Model model) {

		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			form.setName(assessmentName);
			manager.addAssessment(form);
		}
		return "redirect:.";
	}
	
	// view an assessment
	@RequestMapping(value = "assessments/{assessmentName}/run/")
	public String runAssessment(
			@PathVariable("assessmentName") String assessmentName,
			HttpServletRequest request) {

		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			manager.runAssessment(manager.getAssessment(assessmentName));
		}
		String referer = request.getHeader("Referer");
		return "redirect:"+ referer;
	}

	// view an assessment
	@RequestMapping(value = "assessments/{assessmentName}/")
	public String viewAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {

		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		Assessment currAssessment = manager.getAssessment(assessmentName);
		model.addAttribute("assessment", currAssessment);

		List<WeightedUnitTest> otherUnitTetsts = new LinkedList<WeightedUnitTest>();

		for (UnitTest test : manager.getUnitTestList()) {
			boolean contains = false;
			for (WeightedUnitTest weightedTest : currAssessment.getUnitTests()) {
				if (weightedTest.getTest() == test) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				for (WeightedUnitTest weightedTest : currAssessment
						.getSecretUnitTests()) {
					if (weightedTest.getTest() == test) {
						contains = true;
						break;
					}
				}
			}

			if (!contains) {
				WeightedUnitTest weigthedTest = new WeightedUnitTest();
				weigthedTest.setTest(test);
				weigthedTest.setWeight(0);
				otherUnitTetsts.add(weigthedTest);
			}
		}
		
		List<WeightedHandMarking> otherHandMarking = new LinkedList<WeightedHandMarking>();

		for (HandMarking test : manager.getHandMarkingList()) {
			boolean contains = false;
			for (WeightedHandMarking weightedHandMarking : currAssessment.getHandMarking()) {
				if (weightedHandMarking.getHandMarking() == test) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				WeightedHandMarking weigthedHM = new WeightedHandMarking();
				weigthedHM.setHandMarking(test);
				weigthedHM.setWeight(0);
				otherHandMarking.add(weigthedHM);
			}
		}
		
		List<WeightedCompetition> otherCompetitions = new LinkedList<WeightedCompetition>();

		for (Competition test : manager.getCompeitionList()) {
			boolean contains = false;
			for (WeightedCompetition weightedComp : currAssessment.getCompetitions()) {
				if (weightedComp.getTest() == test) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				WeightedCompetition weigthedComp = new WeightedCompetition();
				weigthedComp.setTest(test);
				weigthedComp.setWeight(0);
				otherCompetitions.add(weigthedComp);
			}
		}

		model.addAttribute("otherUnitTests", otherUnitTetsts);
		model.addAttribute("otherHandMarking", otherHandMarking);
		model.addAttribute("otherCompetitions", otherCompetitions);
		model.addAttribute("unikey", getOrCreateUser());
		return "assessment/view/assessment";
	}

	// view an assessment
	@RequestMapping(value = "assessments/")
	public String viewAllAssessment(Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		model.addAttribute("allAssessments", manager.getAssessmentList());
		model.addAttribute("unikey", getOrCreateUser());
		return "assessment/viewAll/assessment";
	}

	// view an assessment
	@RequestMapping(value = "assessments/", method = RequestMethod.POST)
	public String newAssessmentAssessment(
			@ModelAttribute(value = "assessment") Assessment form,
			BindingResult result, Model model) {

		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			if (form.getName() == null || form.getName().isEmpty()) {
				result.reject("Assessment.new.noname");
			} else {
				manager.addAssessment(form);
			}
		}
		return "redirect:.";
	}

	// release a unit test
	@RequestMapping(value = "assessments/release/{assessmentName}/")
	public String releaseAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			if (manager.getAssessment(assessmentName) != null) {
				manager.getAssessment(assessmentName).setReleased(true);
			}
		}
		return "redirect:../../";
	}

	// delete an assessment
	@RequestMapping(value = "assessments/delete/{assessmentName}/")
	public String deleteAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			manager.removeAssessment(assessmentName);
		}
		return "redirect:../../";
	}
	
	// delete a unit test
	@RequestMapping(value = "assessments/stats/{assessmentName}/")
	public String statisticsForAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isTutor()){
			HashMap<String, HashMap<String, AssessmentResult>> allResults = manager.getLatestResults();
			TreeMap<Integer, Integer>  submissionDistribution = new TreeMap<Integer, Integer>();

			int maxBreaks = 10;
			
			int[] markDistribution = new int[maxBreaks+1];
			
			for(Entry<String, HashMap<String, AssessmentResult>> entry: allResults.entrySet()){
				int spot = 0;
				int numSubmissionsMade = 0;
				if(entry.getValue() != null && entry.getValue().get(assessmentName) != null){
					spot = ((int)(entry.getValue().get(assessmentName).getPercentage()*100/(100/maxBreaks)));
					numSubmissionsMade = entry.getValue().get(assessmentName).getSubmissionsMade();
				}
				// mark histogram
				markDistribution[spot]++;
				
				// # submission distribution
				if(!submissionDistribution.containsKey(numSubmissionsMade)){
					submissionDistribution.put(numSubmissionsMade, 0);
				}
				submissionDistribution.put(numSubmissionsMade, submissionDistribution.get(numSubmissionsMade)+1);
			}
			
			model.addAttribute("assessment", manager.getAssessment(assessmentName));
			model.addAttribute("maxBreaks", maxBreaks);
			model.addAttribute("markDistribution", markDistribution);
			model.addAttribute("submissionDistribution", submissionDistribution);
			return "assessment/view/assessmentStats";
		}
		return "redirect:../../";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HAND MARKING //
	// ///////////////////////////////////////////////////////////////////////////

	// view a handmarking
	@RequestMapping(value = "handMarking/{handMarkingName}/")
	public String viewHandMarking(
			@PathVariable("handMarkingName") String handMarkingName, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}

		model.addAttribute("handMarking", manager.getHandMarking(handMarkingName));
		model.addAttribute("unikey", getOrCreateUser());
		return "assessment/view/handMarks";
	}
	
	// update a handmarking
	@RequestMapping(value = "handMarking/{handMarkingName}/", method = RequestMethod.POST)
	public String updateHandMarking(@ModelAttribute(value = "handMarking") HandMarking form,
			BindingResult result, @PathVariable("handMarkingName") String handMarkingName, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			form.setName(handMarkingName);
			manager.updateHandMarking(form);
		}
		return "redirect:.";
	}
	
	// view a handmarking
	@RequestMapping(value = "handMarking/")
	public String viewAllHandMarking( Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		model.addAttribute("allHandMarking", manager.getAllHandMarking());
		model.addAttribute("unikey", getOrCreateUser());
		return "assessment/viewAll/handMarks";
	}
	
	// new handmarking
	@RequestMapping(value = "handMarking/", method = RequestMethod.POST)
	public String newHandMarking(@ModelAttribute(value = "newHandMarkingModel") NewHandMarking form,
			BindingResult result, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		// add it to the system
		if(getUser().isInstructor()){
			manager.newHandMarking(form);
			return "redirect:./"+form.getShortName()+"/";
		}
		return "redirect:.";
	}
	
	// delete a unit test
	@RequestMapping(value = "handMarking/delete/{handMarkingName}/")
	public String deleteHandMarking(@PathVariable("handMarkingName") String handMarkingName,
			Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			manager.removeHandMarking(handMarkingName);
		}
		return "redirect:../../";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// UNIT TEST //
	// ///////////////////////////////////////////////////////////////////////////

	// view a unit test
	@RequestMapping(value = "unitTest/{testName}/")
	public String viewUnitTest(@PathVariable("testName") String testName,
			Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}

		model.addAttribute("unitTest", manager.getUnitTest(testName));
		model.addAttribute(
				"latestResult",
				manager.getUnitTestResult(manager.getUnitTest(testName)
						.getFileLocation() + "/test"));
		model.addAttribute("node", manager.generateFileTree(manager.getUnitTest(testName).getFileLocation()+"/code"));
		model.addAttribute("unikey", getOrCreateUser());
		return "assessment/view/unitTest";
	}

	// update a unit test
	@RequestMapping(value = "unitTest/{testName}/", method = RequestMethod.POST)
	public String uploadTestCode(@PathVariable("testName") String testName,
			@ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}

		// if submission exists
		if (form.getFile() != null && !form.getFile().isEmpty() && getUser().isInstructor()) {
			// upload submission
			manager.testUnitTest(form, testName);
		}

		return "redirect:.";
	}

	// view all unit tests
	@RequestMapping(value = "unitTest/")
	public String viewUnitTest(Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}

		model.addAttribute("allUnitTests", manager.getUnitTestList());
		model.addAttribute("unikey", getOrCreateUser());
		return "assessment/viewAll/unitTest";
	}

	// delete a unit test
	@RequestMapping(value = "unitTest/delete/{testName}/")
	public String deleteUnitTest(@PathVariable("testName") String testName,
			Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			manager.removeUnitTest(testName);
		}
		return "redirect:../../";
	}

	// a unit test is marked as tested
	@RequestMapping(value = "unitTest/tested/{testName}/")
	public String testedUnitTest(@PathVariable("testName") String testName,
			Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			manager.getUnitTest(testName).setTested(true);
			manager.saveUnitTest(manager.getUnitTest(testName));
		}
		return "redirect:../../" + testName + "/";
	}

	@RequestMapping(value = "unitTest/", method = RequestMethod.POST)
	// after submission of a unit test
	public String home(
			@ModelAttribute(value = "newUnitTestModel") NewUnitTest form,
			BindingResult result, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}

		if(getUser().isInstructor()){
	
			// check if the name is unique
			Collection<UnitTest> allUnitTests = manager.getUnitTestList();
	
			for (UnitTest test : allUnitTests) {
				if (test.getName().toLowerCase().replace(" ", "")
						.equals(form.getTestName().toLowerCase().replace(" ", ""))) {
					result.reject("UnitTest.New.NameNotUnique");
				}
			}
	
			// add it.
			if (!result.hasErrors()) {
				manager.addUnitTest(form);
			}
		}

		return "redirect:.";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HOME //
	// ///////////////////////////////////////////////////////////////////////////

	// home page
	@RequestMapping(value = "home/")
	public String home(Model model) {
		// check if tutor or student
		PASTAUser user = getOrCreateUser();
		if (user != null) {
			model.addAttribute("unikey", user);
			model.addAttribute("assessments", manager.getAssessmentList());
			model.addAttribute("results", manager.getLatestResultsForUser(user.getUsername()));
			if(user.isTutor()){
				return "user/tutorHome";
			}
			else{
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
		// check if the submission is valid
		if (form.getFile() == null || form.getFile().isEmpty()) {
			result.reject("Submission.NoFile");
		}
		
		if (manager.getAssessment(form.getAssessment()).isClosed()){
			result.reject("Submission.AfterClosingDate");
		}
		// accept the submission
		logger.info(form.getAssessment() + " submitted for " + getOrCreateUser().getUsername()
				+ " by " + getOrCreateUser().getUsername());
		manager.submit(getOrCreateUser().getUsername(), form);

		return "redirect:.";
	}
	
	// history
	@RequestMapping(value = "info/{assessmentName}/")
	public String viewAssessmentInfo(@PathVariable("assessmentName") String assessmentName,
			Model model) {
		
		PASTAUser user = getOrCreateUser();
		if(user == null){
			return"redirect:/login/";
		}
		model.addAttribute("assessment", manager.getAssessment(assessmentName));
		model.addAttribute("history", manager.getAssessmentHistory(user.getUsername(), assessmentName));
		model.addAttribute("nodeList", manager.genereateFileTree(user.getUsername(), assessmentName));
		model.addAttribute("unikey", getOrCreateUser());

		return "user/viewAssessment";
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// VIEW //
	// ///////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "viewFile/", method = RequestMethod.POST)
	public String viewFile(@RequestParam("location") String location, Model model){
		PASTAUser user = getOrCreateUser();
		if (user != null) {
			if(user.isTutor()){
				model.addAttribute("unikey", user);
				model.addAttribute("codeStyle", codeStyle);
				model.addAttribute("fileContents", manager.scrapeFile(location).replace(">", "&gt;").replace("<", "&lt;"));
				model.addAttribute("fileEnding", location.substring(location.lastIndexOf(".")+1));
				return "assessment/mark/viewFile";
			}
			else{
				return "redirect:/home/";
			}
		}
		return "redirect:/login/";
	}
	
	// home page
	@RequestMapping(value = "student/{username}/home/")
	public String viewStudent(@PathVariable("username") String username,
			Model model) {
		// check if tutor or student
		PASTAUser user = getOrCreateUser();
		if (user != null) {
			if(user.isTutor()){
				PASTAUser viewedUser = manager.getOrCreateUser(username);
				model.addAttribute("unikey", user);
				model.addAttribute("viewedUser", viewedUser);
				model.addAttribute("assessments", manager.getAssessmentList());
				model.addAttribute("results", manager.getLatestResultsForUser(viewedUser.getUsername()));
				return "user/studentHome";
			}
			else{
				return "redirect:/home/";
			}
		}
		return "redirect:/login/";
	}
	
	// home page
	@RequestMapping(value = "student/{username}/home/", method = RequestMethod.POST)
	public String submitAssessment(@PathVariable("username") String username,
			@ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model) {
		// check if the submission is valid
		if (form.getFile() == null || form.getFile().isEmpty()) {
			result.reject("Submission.NoFile");
		}
		
		if (manager.getAssessment(form.getAssessment()).isClosed()){
			result.reject("Submission.AfterClosingDate");
		}
		// accept the submission
		logger.info(form.getAssessment() + " submitted for " + username
				+ " by " + getOrCreateUser().getUsername());
		manager.submit(username, form);

		return "redirect:.";
	}
	
	// history
	@RequestMapping(value = "student/{username}/info/{assessmentName}/")
	public String viewAssessmentInfo(@PathVariable("username") String username, 
			@PathVariable("assessmentName") String assessmentName, Model model) {
		
		PASTAUser user = getOrCreateUser();
		if(user == null){
			return"redirect:/login/";
		}
		model.addAttribute("assessment", manager.getAssessment(assessmentName));
		model.addAttribute("history", manager.getAssessmentHistory(username, assessmentName));
		model.addAttribute("unikey", getOrCreateUser());
		model.addAttribute("viewedUser", manager.getUser(username));
		model.addAttribute("nodeList", manager.genereateFileTree(username, assessmentName));

		return "user/viewAssessment";
	}
	
	// re-run assessment
	@RequestMapping(value = "runAssessment/{username}/{assessmentName}/{assessmentDate}/")
	public String runAssessment(@PathVariable("username") String username, 
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("assessmentDate") String assessmentDate,
			Model model, HttpServletRequest request) {
		
		PASTAUser user = getOrCreateUser();
		if(user == null){
			return "redirect:/login/";
		}
		manager.runAssessment(username, assessmentName, assessmentDate);
		String referer = request.getHeader("Referer");
		return "redirect:"+ referer;
	}
	
	// hand mark assessment
	@RequestMapping(value = "mark/{username}/{assessmentName}/{assessmentDate}/")
	public String handMarkAssessment(@PathVariable("username") String username, 
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("assessmentDate") String assessmentDate,
			Model model) {
		
		PASTAUser user = getOrCreateUser();
		if(user == null){
			return "redirect:/login/";
		}
		model.addAttribute("unikey", user);
		model.addAttribute("student", username);
		model.addAttribute("assessmentName", assessmentName);
		
		AssessmentResult result = manager.getAssessmentResult(username, assessmentName, assessmentDate);
		
		model.addAttribute("node", manager.generateFileTree(username, assessmentName, assessmentDate));
		model.addAttribute("assessmentResult", result);
		model.addAttribute("handMarkingList", result.getAssessment().getHandMarking());
		
		return "assessment/mark/handMark";
	}
	
	// hand mark assessment
	@RequestMapping(value = "mark/{username}/{assessmentName}/{assessmentDate}/", method = RequestMethod.POST)
	public String saveHandMarkAssessment(@PathVariable("username") String username, 
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("assessmentDate") String assessmentDate,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form,
			BindingResult result, Model model) {
		
		PASTAUser user = getOrCreateUser();
		if(user == null){
			return "redirect:/login/";
		}
		// rebinding hand marking results with their hand marking templates
		List<HandMarkingResult> results = form.getHandMarkingResults();
		for(HandMarkingResult currResult: results){
			currResult.setMarkingTemplate(manager.getHandMarking(currResult.getHandMarkingTemplateShortName()));
		}
		manager.saveHandMarkingResults(username, assessmentName, assessmentDate, form.getHandMarkingResults());
		manager.saveComment(username, assessmentName, assessmentDate, form.getComments());
		
		return "redirect:.";
	}
	
	// hand mark assessment
	@RequestMapping(value = "mark/{assessmentName}/", method = RequestMethod.POST)
	public String handMarkAssessment(@RequestParam("currStudentIndex") String s_currStudentIndex, 
			@PathVariable("assessmentName") String assessmentName,
			@ModelAttribute(value = "assessmentResult") AssessmentResult form,
			HttpServletRequest request,
			Model model) {
		
		PASTAUser user = getOrCreateUser();
		if(user == null){
			return "redirect:/login/";
		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("assessmentName", assessmentName);
		
		Collection<PASTAUser> myUsers = new LinkedList<PASTAUser>();
		for(String tutorial: user.getTutorClasses()){
			myUsers.addAll(manager.getUserListByTutorial(tutorial));
		}
		
		// save the latest submission
		if(!request.getHeader("Referer").endsWith("/home/")){
			// get previous user
			int prevStudentIndex = Integer.parseInt(s_currStudentIndex)-1;
			PASTAUser prevStudent = (PASTAUser) myUsers.toArray()[prevStudentIndex];
			
			// rebinding hand marking results with their hand marking templates
			List<HandMarkingResult> results = form.getHandMarkingResults();
			for(HandMarkingResult currResult: results){
				currResult.setMarkingTemplate(manager.getHandMarking(currResult.getHandMarkingTemplateShortName()));
			}
			
			AssessmentResult result = manager.getLatestResultsForUser(prevStudent.getUsername()).get(assessmentName);
			
			manager.saveHandMarkingResults(prevStudent.getUsername(), assessmentName, result.getFormattedSubmissionDate(), form.getHandMarkingResults());
			manager.saveComment(prevStudent.getUsername(), assessmentName, result.getFormattedSubmissionDate(), form.getComments());
		}
		
		// get the correct new student index
		int currStudentIndex = 0;
		try{
			currStudentIndex = Integer.parseInt(s_currStudentIndex);
		}
		catch(Exception e){}
		
		if(currStudentIndex >= myUsers.size()){
			return "redirect:../../home/";
		}
		
		PASTAUser currStudent = (PASTAUser) myUsers.toArray()[currStudentIndex];
		
		// make sure the current student has work to be marked
		while( currStudentIndex < myUsers.size() && 
				(manager.getLatestResultsForUser(currStudent.getUsername()) == null || 
				manager.getLatestResultsForUser(currStudent.getUsername()).get(assessmentName) == null)){
			currStudentIndex++;
			currStudent = (PASTAUser) myUsers.toArray()[currStudentIndex];
		}
		
		if(currStudentIndex >= myUsers.size()){
			return "redirect:../../home/";
		}
		
		if(currStudentIndex < myUsers.size()){
			model.addAttribute("student", currStudent.getUsername());
			
			AssessmentResult result = manager.getLatestResultsForUser(currStudent.getUsername()).get(assessmentName);
			model.addAttribute("node", manager.generateFileTree(currStudent.getUsername(), assessmentName, result.getFormattedSubmissionDate()));
			model.addAttribute("assessmentResult", result);
			model.addAttribute("handMarkingList", result.getAssessment().getHandMarking());
			
			model.addAttribute("currStudentIndex", currStudentIndex);
			model.addAttribute("maxStudentIndex", myUsers.size() - 1);
		}
		
		// check if they are the last
		int nextStudentIndex = currStudentIndex+1;
		if(nextStudentIndex < myUsers.size()){
			PASTAUser nextStudent = (PASTAUser) myUsers.toArray()[nextStudentIndex];
			while( nextStudentIndex < myUsers.size() && 
					(manager.getLatestResultsForUser(nextStudent.getUsername()) == null || 
					manager.getLatestResultsForUser(nextStudent.getUsername()).get(assessmentName) == null)){
				nextStudentIndex++;
				nextStudent = (PASTAUser) myUsers.toArray()[nextStudentIndex];
			}
		}

		if(nextStudentIndex >= myUsers.size()){
			model.addAttribute("last", true);
		}
		
		return "assessment/mark/handMarkBatch";
	}
	
	@RequestMapping(value = "student/{username}/extension/{assessmentName}/{dueDate}")
	public String giveExtension(
			@PathVariable("username") String username,
			@PathVariable("assessmentName") String assessmentName,
			@PathVariable("dueDate") String dueDate,
			HttpServletRequest request) {

		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			try {
				manager.giveExtension(username, assessmentName, sdf.parse(dueDate));
				logger.info(getUser().getUsername() + " gave " + username + " an extension for assessment " 
				+ assessmentName + " until " + dueDate);
			} catch (ParseException e) {
				logger.error("Could not parse " + dueDate + " to give student " + username + " an extension");
			}
		}
		String referer = request.getHeader("Referer");
		return "redirect:"+ referer;
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// COMPETITIONS //
	// ///////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "competition/")
	public String viewAllCompetitions(Model model){
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		PASTAUser user = getUser();
		model.addAttribute("unikey", user);
		model.addAttribute("allCompetitions", manager.getCompeitionList());
		
		return "assessment/viewAll/competition";
	}
	
	@RequestMapping(value = "competition/", method = RequestMethod.POST)
	public String newCompetition(Model model,
			@ModelAttribute(value = "newCompetitionModel") NewCompetition form){
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		manager.addCompetition(form);
		
		return "redirect:.";
	}
	
	// delete a unit test
	@RequestMapping(value = "competition/delete/{competitionName}/")
	public String deleteCompetition(
			@PathVariable("competitionName") String competitionName, Model model) {
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		if(getUser().isInstructor()){
			manager.removeCompetition(competitionName);
		}
		return "redirect:../../";
	}
	
	
	@RequestMapping(value = "competition/{competitionName}")
	public String viewCompetition(@PathVariable("competitionName") String competitionName, Model model){
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		PASTAUser user = getUser();
		model.addAttribute("unikey", user);
		model.addAttribute("competition", manager.getCompetition(competitionName));
		model.addAttribute("node", manager.generateFileTree(manager.getCompetition(competitionName).getFileLocation()+"/code"));

		return "assessment/view/competition";
	}
	
	@RequestMapping(value = "competition/{competitionName}/", method = RequestMethod.POST)
	public String updateCompetition(@ModelAttribute(value = "newCompetitionModel") NewCompetition form,
			@ModelAttribute(value = "competition") Competition compForm,
			@PathVariable("competitionName") String competitionName, Model model){
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		
		if(form.getFile() != null && !form.getFile().isEmpty()){
			// update contents
			form.setTestName(competitionName);
			manager.updateCompetition(form);
		}
		else{
			// update competition
			compForm.setName(competitionName);
			compForm.setArenas(manager.getCompetition(competitionName).getArenas());
			manager.addCompetition(compForm);
		}
		
		return "redirect:.";
	}
	
	@RequestMapping(value = "competition/view/{competitionName}/")
	public String viewCompetitionPage(Model model,
			@PathVariable("competitionName") String competitionName){
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		Competition currComp = manager.getCompetition(competitionName);
		if(currComp == null){
			return "redirect:../../../home";
		}
		
		model.addAttribute("unikey", getUser());
		model.addAttribute("competition", currComp);
		
		if(currComp.isCalculated()){
			model.addAttribute("arenaResult", manager.getCalculatedCompetitionResult(competitionName));
			model.addAttribute("marks", manager.getCompetitionResult(competitionName));
			return "assessment/competition/calculated";
		}
		else{
			model.addAttribute("arenas", currComp.getArenas());
			return "assessment/competition/arena";
		}
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// GRADE CENTRE //
	// ///////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "gradeCentre/")
	public String viewGradeCentre(Model model) {
		
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		HashMap<String, HashMap<String, AssessmentResult>> allResults = manager.getLatestResults();
		HashMap<String, TreeMap<Integer, Integer>>  submissionDistribution = new HashMap<String,TreeMap<Integer, Integer>>();
		Collection<Assessment> assessments = manager.getAssessmentList();

		int numBreaks = 10;
		
		HashMap<String, Integer[]> markDistribution = new HashMap<String, Integer[]>();
		
		for(Assessment assessment: assessments){
			int[] currMarkDist = new int[numBreaks+1];
			TreeMap<Integer, Integer> currSubmissionDistribution = new TreeMap<Integer, Integer>();
			for(Entry<String, HashMap<String, AssessmentResult>> entry: allResults.entrySet()){
				int spot = 0;
				int numSubmissionsMade = 0;
				if(entry.getValue() != null && entry.getValue().get(assessment.getShortName()) != null){
					spot = ((int)(entry.getValue().get(assessment.getShortName()).getPercentage()*100/(100/numBreaks)));
					numSubmissionsMade = entry.getValue().get(assessment.getShortName()).getSubmissionsMade();
				}
				// mark histogram
				currMarkDist[spot]++;
				
				// # submission distribution
				if(!currSubmissionDistribution.containsKey(numSubmissionsMade)){
					currSubmissionDistribution.put(numSubmissionsMade, 0);
				}
				currSubmissionDistribution.put(numSubmissionsMade, currSubmissionDistribution.get(numSubmissionsMade)+1);
			}
			
			// add to everything list
			submissionDistribution.put(assessment.getShortName(), currSubmissionDistribution);
			
			Integer[] tempCurrMarkDist = new Integer[currMarkDist.length]; 
			for(int i=0; i< currMarkDist.length; ++i){
				tempCurrMarkDist[i] = currMarkDist[i];
			}
			
			markDistribution.put(assessment.getShortName(), tempCurrMarkDist);
		}
		
		model.addAttribute("assessments", assessments);
		model.addAttribute("maxBreaks", numBreaks);
		model.addAttribute("markDistribution", markDistribution);
		model.addAttribute("submissionDistribution", submissionDistribution);

		model.addAttribute("assessmentList", manager.getAssessmentList());
		model.addAttribute("userList", manager.getUserList());
		model.addAttribute("latestResults", manager.getLatestResults());
		model.addAttribute("unikey", getOrCreateUser());

		return "user/viewAll";
	}
	
	// home page
	@RequestMapping(value = "tutorial/{className}/")
	public String viewClass(@PathVariable("className") String className,
			Model model) {
		// check if tutor or student
		PASTAUser user = getOrCreateUser();
		if (user != null) {
			if(user.isTutor()){
				model.addAttribute("assessmentList", manager.getAssessmentList());
				model.addAttribute("userList", manager.getUserListByTutorial(className));
				model.addAttribute("latestResults", manager.getLatestResults());
				model.addAttribute("unikey", getOrCreateUser());
				model.addAttribute("classname", "Class - " + className);
				
				return "compound/classHome";
			}
			else{
				return "redirect:/home/";
			}
		}
		return "redirect:/login/";
	}
	
	// home page
	@RequestMapping(value = "stream/{streamName}/")
	public String viewStream(@PathVariable("streamName") String streamName,
			Model model) {
		// check if tutor or student
		PASTAUser user = getOrCreateUser();
		if (user != null) {
			if(user.isTutor()){
				model.addAttribute("assessmentList", manager.getAssessmentList());
				model.addAttribute("userList", manager.getUserListByStream(streamName));
				model.addAttribute("latestResults", manager.getLatestResults());
				model.addAttribute("unikey", getOrCreateUser());
				model.addAttribute("classname", "Stream - " + streamName);
				
				return "compound/classHome";
			}
			else{
				return "redirect:/home/";
			}
		}
		return "redirect:/login/";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// LOGIN //
	// ///////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "login/", method = RequestMethod.GET)
	public String get(ModelMap model) {
		model.addAttribute("LOGINFORM", new LoginForm());
		// Because we're not specifying a logical view name, the
		// DispatcherServlet's DefaultRequestToViewNameTranslator kicks in.
		return "login";
	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	public String index(@ModelAttribute(value = "LOGINFORM") LoginForm userMsg,
			BindingResult result) {

		ProjectProperties.getInstance().getAuthenticationValidator().validate(userMsg, result);
		if (result.hasErrors()) {
			return "login";
		}

		RequestContextHolder.currentRequestAttributes().setAttribute("user",
				userMsg.getUnikey(), RequestAttributes.SCOPE_SESSION);
		
		// Use the redirect-after-post pattern to reduce double-submits.
		return "redirect:/home/";
	}

	@RequestMapping("login/exit")
	public String logout() {
		RequestContextHolder.currentRequestAttributes().removeAttribute("user",
				RequestAttributes.SCOPE_SESSION);
		return "redirect:../";
	}

}