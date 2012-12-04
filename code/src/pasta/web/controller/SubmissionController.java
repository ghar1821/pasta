package pasta.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import pasta.domain.template.Assessment;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;
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

	@ModelAttribute("newUnitTestModel")
	public NewUnitTest returnNewUnitTestModel() {
		return new NewUnitTest();
	}

	@ModelAttribute("submission")
	public Submission returnNewSubmissionModel() {
		return new Submission();
	}

	@ModelAttribute("assessment")
	public Assessment returnNewAssessmentModel() {
		return new Assessment();
	}

	/**
	 * Get the currently logged in user.
	 * 
	 * @return
	 */
	public String getUser() {
		return (String) RequestContextHolder.currentRequestAttributes()
				.getAttribute("user", RequestAttributes.SCOPE_SESSION);
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

		form.setName(assessmentName);
		manager.addAssessment(form);
		return "redirect:.";
	}

	// view an assessment
	@RequestMapping(value = "assessments/{assessmentName}/")
	public String viewAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {

		Assessment currAssessment = manager.getAssessmentNew(assessmentName);
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

		model.addAttribute("otherUnitTests", otherUnitTetsts);
		return "assessment/view/assessment";
	}

	// view an assessment
	@RequestMapping(value = "assessments/")
	public String viewAllAssessment(Model model) {

		model.addAttribute("allAssessments", manager.getAssessmentListNew());
		return "assessment/viewAll/assessment";
	}

	// view an assessment
	@RequestMapping(value = "assessments/", method = RequestMethod.POST)
	public String newAssessmentAssessment(
			@ModelAttribute(value = "assessment") Assessment form,
			BindingResult result, Model model) {

		if (form.getName() == null || form.getName().isEmpty()) {
			result.reject("Assessment.new.noname");
		} else {
			manager.addAssessment(form);
		}
		return "redirect:.";
	}

	// release a unit test
	@RequestMapping(value = "assessments/release/{assessmentName}/")
	public String releaseAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		if (manager.getAssessmentNew(assessmentName) != null) {
			manager.getAssessmentNew(assessmentName).setReleased(true);
		}
		return "redirect:../../";
	}

	// delete a unit test
	@RequestMapping(value = "assessments/delete/{assessmentName}/")
	public String deleteAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		manager.removeAssessment(assessmentName);
		return "redirect:../../";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HAND MARKING //
	// ///////////////////////////////////////////////////////////////////////////

	// view a handmarking
	@RequestMapping(value = "handmarking/{handMarkingName}/")
	public String viewHandMarking(
			@PathVariable("handMarkingName") String handMarkingName, Model model) {

		// model.addAttribute("data",
		// manager.getHandMarking(handMarkingName).getData());
		model.addAttribute("handMarking",
				manager.getHandMarking(handMarkingName));
		return "assessment/view/handMarks";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// UNIT TEST //
	// ///////////////////////////////////////////////////////////////////////////

	// view a unit test
	@RequestMapping(value = "unitTest/{testName}/")
	public String viewUnitTest(@PathVariable("testName") String testName,
			Model model) {

		model.addAttribute("unitTest", manager.getUnitTest(testName));
		model.addAttribute(
				"latestResult",
				manager.getUnitTestResult(manager.getUnitTest(testName)
						.getFileLocation() + "/test"));

		return "assessment/view/unitTest";
	}

	// view a unit test
	@RequestMapping(value = "unitTest/{testName}/", method = RequestMethod.POST)
	public String uploadTestCode(@PathVariable("testName") String testName,
			@ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model) {

		// if submission exists
		if (form.getFile() != null && !form.getFile().isEmpty()) {
			// upload submission
			manager.testUnitTest(form, testName);
		}

		return "redirect:.";
	}

	// view all unit tests
	@RequestMapping(value = "unitTest/")
	public String viewUnitTest(Model model) {

		model.addAttribute("allUnitTests", manager.getUnitTestList());
		return "assessment/viewAll/unitTest";
	}

	// delete a unit test
	@RequestMapping(value = "unitTest/delete/{testName}/")
	public String deleteUnitTest(@PathVariable("testName") String testName,
			Model model) {
		manager.removeUnitTest(testName);
		return "redirect:../../";
	}

	// a unit test is marked as tested
	@RequestMapping(value = "unitTest/tested/{testName}/")
	public String testedUnitTest(@PathVariable("testName") String testName,
			Model model) {
		manager.getUnitTest(testName).setTested(true);
		manager.saveUnitTest(manager.getUnitTest(testName));
		return "redirect:../../" + testName + "/";
	}

	@RequestMapping(value = "unitTest/update/{testName}/", method = RequestMethod.POST)
	// after submission of an assessment
	public String updateUnitTest(
			@ModelAttribute(value = "newUnitTestModel") NewUnitTest form,
			BindingResult result, Model model) {

		// add it.
		if (!result.hasErrors()) {
			manager.addUnitTest(form);
		}

		return "redirect:../../" + form.getTestName() + "/";
	}

	@RequestMapping(value = "unitTest/", method = RequestMethod.POST)
	// after submission of a unit test
	public String home(
			@ModelAttribute(value = "newUnitTestModel") NewUnitTest form,
			BindingResult result, Model model) {

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

		return "redirect:.";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HOME //
	// ///////////////////////////////////////////////////////////////////////////

	// home page
	@RequestMapping(value = "home/")
	public String home(Model model) {
		// check if tutor or student TODO
		String username = "arad0726";// getUser();
		if (username != null) {
			model.addAttribute("unikey", username);
			model.addAttribute("results", manager.getStudentResults(username));
			return "user/studentHome";
		}
		return null;
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
		// accept the submission
		String submitting = "arad0726";
		String submittingFor = submitting;

		logger.info(form.getAssessment() + " submitted for " + submittingFor
				+ " by " + submitting);
		manager.submit(submittingFor, form);

		return "redirect:.";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// LOGIN //
	// ///////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "login", method = RequestMethod.GET)
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