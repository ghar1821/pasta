package pasta.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.UserPermissionLevel;
import pasta.domain.user.PASTAUser;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 17 Aug 2015
 *
 */
public class WebUtils {

	protected static Logger logger = Logger.getLogger(WebUtils.class);
	
	public static PASTAUser getUser() {
		PASTAUser user = (PASTAUser) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return user;
	}
	
	public static void ensureLoggedIn(HttpServletRequest request) {
		if(getUser() == null) {
			request.getSession().setAttribute("redirectAfterLogin", request.getServletPath());
			throw new SessionAuthenticationException("Please log in");
		}
	}
	
	public static void ensureAccess(UserPermissionLevel level) {
		PASTAUser user = getUser();
		if((level == UserPermissionLevel.TUTOR && !user.isTutor()) ||
				(level == UserPermissionLevel.INSTRUCTOR && !user.isInstructor())) {
			throw new InsufficientAuthenticationException("You do not have sufficient access to do that");
		}
	}
	
	public static String getRedirectAfterLogin() {
		String redirect = (String) RequestContextHolder.currentRequestAttributes()
				.getAttribute("redirectAfterLogin", RequestAttributes.SCOPE_SESSION);
		if(redirect == null) {
			return "redirect:/home/";
		}
		RequestContextHolder.currentRequestAttributes()
				.removeAttribute("redirectAfterLogin", RequestAttributes.SCOPE_SESSION);
		return "redirect:" + redirect;
	}
}
