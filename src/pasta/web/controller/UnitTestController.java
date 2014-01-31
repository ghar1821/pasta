package pasta.web.controller;


import java.util.Collection;
import java.util.HashMap;

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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.domain.template.UnitTest;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.service.SubmissionManager;
import pasta.service.UnitTestManager;
import pasta.service.UserManager;
import pasta.util.PASTAUtil;

@Controller
@RequestMapping("unitTest/")
/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
public class UnitTestController {

	public UnitTestController() {
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
	private UnitTestManager unitTestManager;
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
	public void setMyService(UnitTestManager myService) {
		this.unitTestManager = myService;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newUnitTestModel")
	public NewUnitTest returnNewUnitTestModel() {
		return new NewUnitTest();
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
		if (username != null) {
			return userManager.getOrCreateUser(username);
		}
		return null;
	}

	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		if (username != null) {
			return userManager.getUser(username);
		}
		return null;
	}


	// ///////////////////////////////////////////////////////////////////////////
	// UNIT TEST //
	// ///////////////////////////////////////////////////////////////////////////

	// view a unit test
	@RequestMapping(value = "{testName}/")
	public String viewUnitTest(@PathVariable("testName") String testName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("unitTest", unitTestManager.getUnitTest(testName));
		model.addAttribute(
				"latestResult",
				unitTestManager.getUnitTestResult(unitTestManager.getUnitTest(testName)
						.getFileLocation() + "/test"));
		model.addAttribute(
				"node",
				PASTAUtil.generateFileTree(unitTestManager.getUnitTest(testName)
						.getFileLocation() + "/code"));
		model.addAttribute("unikey", user);
		return "assessment/view/unitTest";
	}
	
	// test a unit test
	@RequestMapping(value = "{testName}/", method = RequestMethod.POST)
	public String updateTestCode(@PathVariable("testName") String testName,
			@ModelAttribute(value = "newUnitTestModel") NewUnitTest form,
			@ModelAttribute(value = "submission") Submission subForm,
			BindingResult result, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		// if submission exists
		if (form != null && form.getTestName() != null && form.getFile() != null && 
				!form.getFile().isEmpty() && getUser().isInstructor()) {
			// upload submission
			unitTestManager.updateUpdateUnitTest(form);
		}
		
		// if submission exists
		if (subForm != null && subForm.getAssessment() != null
				&& subForm.getFile() != null && 
				!subForm.getFile().isEmpty() && getUser().isInstructor()) {
			// upload submission
			unitTestManager.testUnitTest(subForm, testName);
		}

		return "redirect:.";
	}

	// view all unit tests
	@RequestMapping(value = "")
	public String viewUnitTest(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("allUnitTests", unitTestManager.getUnitTestList());
		model.addAttribute("unikey", user);
		return "assessment/viewAll/unitTest";
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST)
	// after submission of a unit test
	public String home(
			@ModelAttribute(value = "newUnitTestModel") NewUnitTest form,
			BindingResult result, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		if (getUser().isInstructor()) {

			// check if the name is unique
			Collection<UnitTest> allUnitTests = unitTestManager.getUnitTestList();

			for (UnitTest test : allUnitTests) {
				if (test.getName()
						.toLowerCase()
						.replace(" ", "")
						.equals(form.getTestName().toLowerCase()
								.replace(" ", ""))) {
					result.reject("UnitTest.New.NameNotUnique");
				}
			}

			// add it.
			if (!result.hasErrors()) {
				unitTestManager.addUnitTest(form);
			}
		}

		return "redirect:/mirror/";
	}

	// delete a unit test
	@RequestMapping(value = "delete/{testName}/")
	public String deleteUnitTest(@PathVariable("testName") String testName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {
			unitTestManager.removeUnitTest(testName);
		}
		return "redirect:../../";
	}

	// a unit test is marked as tested
	@RequestMapping(value = "tested/{testName}/")
	public String testedUnitTest(@PathVariable("testName") String testName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {
			unitTestManager.getUnitTest(testName).setTested(true);
			unitTestManager.saveUnitTest(unitTestManager.getUnitTest(testName));
		}
		return "redirect:../../" + testName + "/";
	}

	

}