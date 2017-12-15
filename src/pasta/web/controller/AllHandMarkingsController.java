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

import java.util.Map;

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
import pasta.domain.form.NewHandMarkingForm;
import pasta.domain.template.HandMarking;
import pasta.domain.user.PASTAUser;
import pasta.service.HandMarkingManager;
import pasta.web.WebUtils;

/**
 * Controller class for Hand marking functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/handMarking/...
 * <p>
 * Only teaching staff can access this url.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-08-15
 *
 */
@Controller
@RequestMapping("handMarking/")
public class AllHandMarkingsController {

	public AllHandMarkingsController() {
	}

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired private HandMarkingManager handMarkingManager;


	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newHandMarkingModel")
	public NewHandMarkingForm returnNewHandMakingModel() {
		return new NewHandMarkingForm();
	}

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// HAND MARKING //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/handMarking/
	 * <p>
	 * View the list of all hand marking templates in the system.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * ATTRIBTUES:
	 * <table>
	 * 	<tr><td>allHandMarking</td><td>a collection of all of the hand marking templates</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/viewAll/handMarks</li></ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/viewAll/handMarks"
	 */
	@RequestMapping(value = "")
	public String viewAllHandMarking(Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		model.addAttribute("allHandMarking", handMarkingManager.getHandMarkingList());
		return "assessment/viewAll/handMarks";
	}

	/**
	 * $PASTAUrl$/handMarking/ - POST
	 * <p>
	 * Add a new hand marking template
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor, add the hand marking template
	 * using {@link pasta.service.HandMarkingManager#newHandMarking(NewHandMarkingForm)}
	 * 
	 * @param form the new hand marking form
	 * @param result the binding result used for feedback
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:." or "redirect:/mirror/"
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String newHandMarking(
			@Valid @ModelAttribute(value = "newHandMarkingModel") NewHandMarkingForm form, BindingResult result,
			RedirectAttributes attr, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		if(result.hasErrors()) {
			attr.addFlashAttribute("newHandMarkingModel", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.newHandMarkingModel", result);
			return "redirect:.";
		}

		HandMarking template = handMarkingManager.newHandMarking(form);
		return "redirect:./" + template.getId() + "/";
	}
}