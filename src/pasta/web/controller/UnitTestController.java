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
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipOutputStream;

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
import pasta.util.ProjectProperties;

/**
 * Controller class for Unit Test functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/unitTest/...
 * <p>
 * Only teaching staff can access this url.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-08-15
 *
 */
@Controller
@RequestMapping("unitTest/")
public class UnitTestController {

	/**
	 * Initializes the codeStyle tag mapping of file endings to 
	 * javascript tag requirements for syntax highlighting.
	 */
	public UnitTestController() {
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
	private UnitTestManager unitTestManager;
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
	
	@ModelAttribute("submission")
	public Submission returnSubmissionModel() {
		return new Submission();
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
	// UNIT TEST //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/unitTest/{testName}/
	 * <p>
	 * View the details of a unit test.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>The {@link pasta.domain.PASTAUser} for the logged in user</td></tr>
	 * 	<tr><td>unitTest</td><td>The {@link pasta.domain.template.UnitTest} for this test</td></tr>
	 * 	<tr><td>latestResult</td><td>The {@link pasta.domain.result.UnitTestResult} for the execution of the test run of the unit testing code.</td></tr>
	 * 	<tr><td>node</td><td>The root {@link pasta.domain.FileTreeNode} for the root of the unit test code.</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>assessment/view/unitTest</li></ul>
	 * 
	 * @param testName the short name (no whitespace) of the test.
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/view/unitTest"
	 */
	@RequestMapping(value = "{testName}/")
	public String viewUnitTest(@PathVariable("testName") String testName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("unitTest", unitTestManager.getUnitTest(testName));
		model.addAttribute(
				"latestResult",
				unitTestManager.getUnitTestResult(unitTestManager.getUnitTest(testName)
						.getFileLocation() + "/test"));
		model.addAttribute(
				"node",
				PASTAUtil.generateFileTree(unitTestManager.getUnitTest(testName)
						.getFileLocation() + "/code"));
		return "assessment/view/unitTest";
	}
	
	/**
	 * $PASTAUrl$/unitTest/{testName}/download/
	 * <p>
	 * Download the unit test code on the machine.
	 * 
	 * If the user has not authenticated or is not a tutor: do nothing
	 * 
	 * Otherwise create a zip file with the name: $testName$.zip
	 * 
	 * @param testName the short name (no whitespace) of the unit test
	 * @param model the model being used
	 * @param response the response being used to serve the zip
	 */
	@RequestMapping(value = "{testName}/download/")
	public void downloadUnitTest(@PathVariable("testName") String testName,
			Model model,HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user == null) {
			return;
		}
		if (!user.isTutor()) {
			return;
		}
		
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=\""
				+ testName + ".zip\"");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outStream);
		try {
			PASTAUtil.zip(zip, new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/template/unitTest/"
					+ testName
					+ "/code/"), ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/template/unitTest/"
					+ testName
					+ "/code/");
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
	 * $PASTAUrl$/unitTest/{testName}/ - POST
	 * <p>
	 * Upload some code to test the unit test on the production machine or
	 * to update the unit tests on the system.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * If the user is an instructor:
	 * <ul>
	 * 	<li><b>updating code</b> - done using {@link pasta.service.UnitTestManager#updateUnitTestCode(NewUnitTest)}</li>
	 * 	<li><b>testing test code</b> - done using {@link pasta.service.UnitTestManager#testUnitTest(Submission, String)}</li> 
	 * </ul>
	 * 
	 * @param testName the short name (no whitespace) of the test
	 * @param form used for updating the unit test code
	 * @param subForm used for testing the unit test code
	 * @param result binding results used for feedback.
	 * @param model the model being used.
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/"
	 */
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
			return "redirect:/home/";
		}

		// if submission exists
		if (form != null && form.getTestName() != null && form.getFile() != null && 
				!form.getFile().isEmpty() && getUser().isInstructor()) {
			// upload submission
			unitTestManager.updateUnitTestCode(form);
		}
		
		// if submission exists
		if (subForm != null && subForm.getAssessment() != null
				&& subForm.getFile() != null && 
				!subForm.getFile().isEmpty() && getUser().isInstructor()) {
			// upload submission
			unitTestManager.testUnitTest(subForm, testName);
		}

		return "redirect:/mirror/";
	}

	/**
	 * $PASTAUrl$/unitTest/
	 * <p>
	 * List all unit tests on the system.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>The {@link pasta.domain.PASTAUser} for the currently logged in user.</td></tr>
	 * 	<tr><td>allUnitTests</td><td>A collection of all {@link pasta.domain.template.UnitTest} of all unit tests on the system.</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>assessment/viewAll/unitTest</li></ul>
	 * 
	 * @param model the model being used.
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/viewAll/unitTest"
	 */
	@RequestMapping(value = "")
	public String viewUnitTest(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("allUnitTests", unitTestManager.getUnitTestList());
		model.addAttribute("unikey", user);
		return "assessment/viewAll/unitTest";
	}
	
	/**
	 * $PASTAUrl$/unitTest/ - POST
	 * <p>
	 * Add a new unit test to the system.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * If the user is an instructor:
	 * <ul>
	 * 	<li>Check if the name is unique, if it's not reject with UnitTest.New.NameNotUnique</li>
	 * 	<li>Otherwise, add using {@link pasta.service.UnitTestManager#addUnitTest(NewUnitTest)}</li>
	 * 	<li>Redirect back using $PASTAUrl$/mirror/</li>
	 * </ul>
	 * 
	 * @param form the new unit test form
	 * @param result the binding result used for feedback
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String home(
			@ModelAttribute(value = "newUnitTestModel") NewUnitTest form,
			BindingResult result, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
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

	/**
	 * $PASTAUrl$/unitTest/delete/{testName}/
	 * <p>
	 * Delete a unit test from the system.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * If the user is an instructor: delete the unit test using
	 * {@link pasta.service.UnitTestManager#removeUnitTest(String)}
	 * then redirect to $PASTAUrl$/unitTest/
	 * 
	 * @param testName the short name (no whitespace) of the unit test.
	 * @param model the model being used.
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../"
	 */
	@RequestMapping(value = "delete/{testName}/")
	public String deleteUnitTest(@PathVariable("testName") String testName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (getUser().isInstructor()) {
			unitTestManager.removeUnitTest(testName);
		}
		return "redirect:../../";
	}

	/**
	 * $PASTAUrl$/unitTest/tested/{testName}/
	 * <p>
	 * Mark a unit test as tested.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * If the user is an instructor: mark the unit test as tested.
	 * 
	 * @param testName the short name (no whitespace) of the test
	 * @param model the model being used.
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../$testName$/"
	 */
	@RequestMapping(value = "tested/{testName}/")
	public String testedUnitTest(@PathVariable("testName") String testName,
			Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (getUser().isInstructor()) {
			unitTestManager.getUnitTest(testName).setTested(true);
			unitTestManager.saveUnitTest(unitTestManager.getUnitTest(testName));
		}
		return "redirect:../../" + testName + "/";
	}

	

}