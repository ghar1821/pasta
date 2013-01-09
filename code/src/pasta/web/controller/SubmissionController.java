package pasta.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.LoginForm;
import pasta.domain.PASTAUser;
import pasta.domain.template.Assessment;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewHandMarking;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.login.AuthValidator;
import pasta.service.SubmissionManager;

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
	protected final Log logger = LogFactory.getLog(getClass());

	private SubmissionManager manager;
	private AuthValidator validator = new AuthValidator();

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

	@ModelAttribute("submission")
	public Submission returnSubmissionModel() {
		return new Submission();
	}

	@ModelAttribute("assessment")
	public Assessment returnAssessmentModel() {
		return new Assessment();
	}
	
	@ModelAttribute("handMarking")
	public HandMarking returnHandMarkingModel() {
		return new HandMarking();
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
	@RequestMapping(value = "assessments/{assessmentName}/")
	public String viewAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {

		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}
		
		Assessment currAssessment = manager.getAssessment(assessmentName);
		model.addAttribute("assessment", currAssessment);

		List<WeightedUnitTest> otherUnitTetsts = new ArrayList<WeightedUnitTest>();

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
		
		List<WeightedHandMarking> otherHandMarking = new ArrayList<WeightedHandMarking>();

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

		model.addAttribute("otherUnitTests", otherUnitTetsts);
		model.addAttribute("otherHandMarking", otherHandMarking);
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

	// delete a unit test
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
			model.addAttribute("results", manager.getStudentResults(user.getUsername()));
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
		model.addAttribute("unikey", getOrCreateUser());

		return "user/viewAssessment";
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// VIEW //
	// ///////////////////////////////////////////////////////////////////////////
	
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
				model.addAttribute("results", manager.getStudentResults(viewedUser.getUsername()));
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
	
	// ///////////////////////////////////////////////////////////////////////////
	// GRADE CENTRE //
	// ///////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "gradeCentre/")
	public String viewGradeCentre(Model model) {
		
		if(getUser() == null || !getUser().isTutor()){
			return "redirect:/home/.";
		}

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
				
				// TODO must fix - too expensive for now.
//				// get statistics -- assessmet, statistic (min,max,median, UQ, LQ)
//				HashMap<String, HashMap<String, Double>> statistics = new HashMap<String, HashMap<String, Double>>();
//				for(Assessment ass: manager.getAssessmentList()){
//					LinkedList<Double> allMarks = new LinkedList<Double>();
//					for(PASTAUser currUser: manager.getUserListByTutorial(className)){
//						allMarks.add(manager.getLatestResults().get(currUser.getUsername()).get(ass.getShortName()).getMarks());
//					}
//					HashMap<String, Double> currStats = null;
//					if(allMarks.size() > 0){
//						Collections.sort(allMarks);
//						currStats = new HashMap<String, Double>();
//						currStats.put("max", allMarks.get(0));
//						currStats.put("min", allMarks.get(allMarks.size()-1));
//						currStats.put("median", allMarks.get(allMarks.size()/2));
//						currStats.put("UQ", allMarks.get((allMarks.size()/4)*3));
//						currStats.put("LQ", allMarks.get(allMarks.size()/4));
//					}
//					statistics.put(ass.getShortName(), currStats);
//				}
//				
//				model.addAttribute("statistics", statistics);
				
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

		validator.validate(userMsg, result);
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