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

import java.beans.PropertyEditorSupport;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedField;
import pasta.domain.upload.UpdateHandMarkingForm;
import pasta.service.HandMarkingManager;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

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
public class HandMarkingController {

	/**
	 * Initializes the codeStyle tag mapping of file endings to 
	 * javascript tag requirements for syntax highlighting.
	 */
	public HandMarkingController() {
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

	private UserManager userManager;
	private HandMarkingManager handMarkingManager;
	private Map<String, String> codeStyle;

	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
	}
	
	@Autowired
	public void setMyService(HandMarkingManager myService) {
		this.handMarkingManager = myService;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("handMarking")
	public HandMarking loadHandMarking(@PathVariable("handMarkingId") long handMarkingId) {
		return handMarkingManager.getHandMarking(handMarkingId);
	}
	
	@ModelAttribute("updateHandMarkingForm")
	public UpdateHandMarkingForm returnUpdateForm(@PathVariable("handMarkingId") long handMarkingId) {
		return new UpdateHandMarkingForm(
				handMarkingManager.getHandMarking(handMarkingId));
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Get the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in or user isn't registered.
	 */
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
	// HAND MARKING //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/handMarking/{handMarkingId}/
	 * <p>
	 * View the hand marking template.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>the user object for the currently logged in user</td></tr>
	 * 	<tr><td>handMarking</td><td>the hand marking object</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/view/handMarks</li></ul>
	 * 
	 * @param handMarkingId the id of the hand marking template
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/view/handMarks"
	 */
	@RequestMapping(value = "{handMarkingId}/")
	public String viewHandMarking(
			@PathVariable("handMarkingId") Long handMarkingId,
			@ModelAttribute("handMarking") HandMarking template, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("unikey", user);
		return "assessment/view/handMarks";
	}

	/**
	 * $PASTAUrl/handMarking/{handMarkingId}/ - POST
	 * <p>
	 * Update the hand marking template
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor, update the hand marking object using
	 * {@link pasta.service.HandMarkingManager#updateHandMarking(HandMarking)}
	 * 
	 * @param form the updated hand marking object
	 * @param result the binding result used for feedback
	 * @param handMarkingId the id of the hand marking template
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:."
	 */
	@RequestMapping(value = "{handMarkingId}/", method = RequestMethod.POST)
	public String updateHandMarking(
			@ModelAttribute(value = "handMarking") HandMarking template,
			@ModelAttribute(value = "updateHandMarkingForm") UpdateHandMarkingForm form,
			BindingResult result,
			@PathVariable("handMarkingId") Long handMarkingId, Model model,HttpServletRequest request) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {
			handMarkingManager.updateHandMarking(template, form);
		}
		return "redirect:.";
	}
	
	/**
	 * $PASTAUrl$/handMarking/delete/{handMarkingId}/
	 * <p>
	 * Delete a hand marking template.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor: remove the hand marking template
	 * using {@link pasta.service.HandMarkingManager#removeHandMarking(String)}
	 * then redirect to $PASTAUrl$/handMarking/
	 * 
	 * @param handMarkingId the id of the hand marking template
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../"
	 */
	@RequestMapping(value = "delete/{handMarkingId}/")
	public String deleteHandMarking(
			@PathVariable("handMarkingId") Long handMarkingId, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (getUser().isInstructor()) {
			handMarkingManager.removeHandMarking(handMarkingId);
		}
		return "redirect:../../";
	}
	
	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
	    // when passing a column/row as an id, convert that id into an actual WeightedField object
		binder.registerCustomEditor(WeightedField.class, new PropertyEditorSupport() {
		    @Override
		    public void setAsText(String text) {
		    	WeightedField field = ProjectProperties.getInstance().getHandMarkingDAO()
		    			.getWeightedField(Long.parseLong(text));
		    	if(field == null) {
		    		field = new WeightedField();
		    		field.setId(Long.parseLong(text));
		    	}
		    	setValue(field);
		    }
	    });
	}

}