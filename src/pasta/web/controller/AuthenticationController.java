package pasta.web.controller;

import javax.servlet.http.HttpSession;

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
import pasta.web.WebUtils;

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
		if(WebUtils.getUser() != null){
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
		if (result.hasErrors()) {
			return "login";
		}
		
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

		return WebUtils.getRedirectAfterLogin();
	}

	/**
	 * $PASTAUrl$/login/exit/
	 * <p>
	 * Log out. Remove the session attribute with the name "user"
	 * 
	 * @return "redirect:../"
	 */
	@RequestMapping("exit")
	public String logout(HttpSession session) {
		for(String sessionAtt : RequestContextHolder.currentRequestAttributes()
				.getAttributeNames(RequestAttributes.SCOPE_SESSION) ) {
			RequestContextHolder.currentRequestAttributes()
			.removeAttribute(sessionAtt, RequestAttributes.SCOPE_SESSION);
		}
		return "redirect:../";
	}

}