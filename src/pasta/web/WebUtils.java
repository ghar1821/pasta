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
		if(user == null || (level == UserPermissionLevel.TUTOR && !user.isTutor()) ||
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
