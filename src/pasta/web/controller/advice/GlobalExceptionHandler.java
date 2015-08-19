package pasta.web.controller.advice;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import pasta.domain.user.PASTAUser;
import pasta.web.WebUtils;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 17 Aug 2015
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static Logger logger = Logger.getLogger(GlobalExceptionHandler.class);
	
	@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="You are not authorised to view this page.")  // 401
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public void handleInsufficientAccess(HttpServletRequest request, Exception e) {
		PASTAUser user = WebUtils.getUser();
		String username = user == null ? "unknown" : user.getUsername();
		logger.error("User '" + username + "' prevented from viewing unauthorised material at " + request.getRequestURL());
    }
	
	@ExceptionHandler(value = SessionAuthenticationException.class)
	public String handleNeedToLoginException() {
		return "redirect:/login/";
	}
	
	@ExceptionHandler(value = Exception.class)
    public String defaultErrorHandler(HttpServletRequest request, Exception e) throws Exception {
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
            throw e;

        logger.error("Exception thrown at " + request.getRequestURL(), e);
        return "redirect:/home/";
        
        // TODO Should redirect to error page like this:
//        ModelAndView mav = new ModelAndView();
//        mav.addObject("exception", e);
//        mav.addObject("url", reqest.getRequestURL());
//        mav.setViewName("error");
//        return mav;
    }

}
