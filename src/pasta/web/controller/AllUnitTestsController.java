/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pasta.domain.UserPermissionLevel;
import pasta.domain.form.NewUnitTestForm;
import pasta.domain.template.UnitTest;
import pasta.domain.user.PASTAUser;
import pasta.service.UnitTestManager;
import pasta.web.WebUtils;

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
public class AllUnitTestsController {

	public AllUnitTestsController() {
	}

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private UnitTestManager unitTestManager;


	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newUnitTest")
	public NewUnitTestForm returnNewUnitTestModel() {
		return new NewUnitTestForm();
	}

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// UNIT TEST //
	// ///////////////////////////////////////////////////////////////////////////

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
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		model.addAttribute("allUnitTests", unitTestManager.getUnitTestList());
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
	 * 	<li>Otherwise, add using {@link pasta.service.UnitTestManager#addUnitTest(NewUnitTestForm)}</li>
	 * 	<li>Redirect back using $PASTAUrl$/mirror/</li>
	 * </ul>
	 * 
	 * @param form the new unit test form
	 * @param result the binding result used for feedback
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String newUnitTest(
			@Valid @ModelAttribute(value = "newUnitTest") NewUnitTestForm form, BindingResult result,
			RedirectAttributes attr, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		if(result.hasErrors()) {
			attr.addFlashAttribute("newUnitTest", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.newUnitTest", result);
			return "redirect:.";
		}

		UnitTest newTest = unitTestManager.addUnitTest(form);
		return "redirect:" + newTest.getId() + "/";
	}
}