/**
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
	
	// view a unit test
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
			unitTestManager.updateUnitTestCode(form);
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