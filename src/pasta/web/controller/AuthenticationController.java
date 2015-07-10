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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.form.LoginForm;
import pasta.domain.user.PASTAUser;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

/**
 * Controller class for Authentication functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/login/...
 * <p>
 * All users can access this url equally.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-08-15
 *
 */
@Controller
@RequestMapping("login/")
public class AuthenticationController {


	protected final Log logger = LogFactory.getLog(getClass());
	private UserManager userManager;

	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
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
		PASTAUser user = (PASTAUser) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return user;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// LOGIN //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/login/ - GET
	 * <p>
	 * Serves up the login page.
	 * 
	 * If the user is logged in, redirect to home.
	 * 
	 * Attributes:
	 * <table>
	 * 	<tr><td>LOGINFORM</td><td> the login form </td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>login</li></ul>
	 * 
	 * @param model the model used
	 * @return "redirect:/home/" or "login"
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String get(ModelMap model) {
		if(getUser() != null){
			return "redirect:/home/";
		}
		
		model.addAttribute("LOGINFORM", new LoginForm());
		return "login";
	}

	/**
	 * $PASTAUrl$/login/ - POST
	 * <p>
	 * Processes the submitted login form and returns errors if necessary.
	 * 
	 * <ol>
	 * 	<li>Performs a trim on the username.</li>
	 * 	<li>Validate using the correct authenticator</li>
	 * 	<li>If the system is set to only allow logging in if the user is registered already, 
	 * give correct error code (NotAvailable.loginForm.password)</li>
	 * 	<li>If the authentication has errors or the system is restricting logging 
	 * in to those who are registered and the user is not registerd, return them to the login form</li>
	 * 	<li>If everything went fine, set the session attribute with name "user" to the username and redirect to home</li>
	 * </ol>
	 * 
	 * @param loginForm the login form
	 * @param result the binding result used for feedback
	 * @return "redirect:/home/" or "login"
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String index(@ModelAttribute(value = "LOGINFORM") LoginForm loginForm,
			BindingResult result) {

		loginForm.setUnikey(loginForm.getUnikey().trim());
		
		ProjectProperties.getInstance().getAuthenticationValidator().validate(loginForm, result);
		PASTAUser user = userManager.getUser(loginForm.getUnikey());
		if(user == null || !user.isActive()) {
			if(ProjectProperties.getInstance().getCreateAccountOnSuccessfulLogin()) {
				user = new PASTAUser();
				user.setUsername(loginForm.getUnikey());
				user = userManager.getOrCreateUser(user);
			} else {
				result.rejectValue("password", "NotAvailable.loginForm.password");
			}
		}
		
		if (result.hasErrors()) {
			return "login";
		}
		
		RequestContextHolder.currentRequestAttributes().setAttribute("user",
				user, RequestAttributes.SCOPE_SESSION);

		// Use the redirect-after-post pattern to reduce double-submits.
		return "redirect:/home/";
	}

	/**
	 * $PASTAUrl$/login/exit/
	 * <p>
	 * Log out. Remove the session attribute with the name "user"
	 * 
	 * @return "redirect:../"
	 */
	@RequestMapping("exit")
	public String logout() {
		RequestContextHolder.currentRequestAttributes().removeAttribute("user",
				RequestAttributes.SCOPE_SESSION);
		return "redirect:../";
	}

}