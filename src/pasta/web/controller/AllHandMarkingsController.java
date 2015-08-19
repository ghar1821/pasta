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

import java.util.Map;
import java.util.TreeMap;

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

	/**
	 * Initializes the codeStyle tag mapping of file endings to 
	 * javascript tag requirements for syntax highlighting.
	 */
	public AllHandMarkingsController() {
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

	@Autowired private HandMarkingManager handMarkingManager;
	private Map<String, String> codeStyle;


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
	public String newHandMarking(@ModelAttribute("user") PASTAUser user, 
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