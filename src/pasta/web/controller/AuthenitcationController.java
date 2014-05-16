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

import pasta.domain.PASTAUser;
import pasta.domain.form.LoginForm;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
@Controller
@RequestMapping("login/")
public class AuthenitcationController {


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
	 * @return
	 */
	public PASTAUser getOrCreateUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return getOrCreateUser(username);
	}
	
	public PASTAUser getOrCreateUser(String username) {
		if (username != null) {
			return userManager.getOrCreateUser(username);
		}
		return null;
	}

	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return getUser(username);
	}
	
	public PASTAUser getUser(String username) {
		if (username != null) {
			return userManager.getUser(username);
		}
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// LOGIN //
	// ///////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String get(ModelMap model) {
		if(getUser() != null){
			return "redirect:/home/";
		}
		
		model.addAttribute("LOGINFORM", new LoginForm());
		// Because we're not specifying a logical view name, the
		// DispatcherServlet's DefaultRequestToViewNameTranslator kicks in.
		return "login";
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	public String index(@ModelAttribute(value = "LOGINFORM") LoginForm userMsg,
			BindingResult result) {

		userMsg.setUnikey(userMsg.getUnikey().trim());
		
		ProjectProperties.getInstance().getAuthenticationValidator().validate(userMsg, result);
		if(!ProjectProperties.getInstance().getCreateAccountOnSuccessfulLogin() && 
				userManager.getUser(userMsg.getUnikey()) == null){
			result.rejectValue("password", "NotAvailable.loginForm.password");
		}
		if (result.hasErrors()) {
			return "login";
		}

		RequestContextHolder.currentRequestAttributes().setAttribute("user",
				userMsg.getUnikey(), RequestAttributes.SCOPE_SESSION);

		// Use the redirect-after-post pattern to reduce double-submits.
		return "redirect:/home/";
	}

	@RequestMapping("exit")
	public String logout() {
		RequestContextHolder.currentRequestAttributes().removeAttribute("user",
				RequestAttributes.SCOPE_SESSION);
		return "redirect:../";
	}

}