package pasta.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 19 Aug 2015
 *
 */
@Controller
@RequestMapping("/")
public class RootController {
	/**
	 * $PASTAUrl$/mirror/
	 * <p>
	 * Redirect back to the referrer. This is mainly used to get rid of the
	 * resubmitting a form when refreshing a page.
	 * 
	 * @param request
	 *            the http request used for redirection
	 * @param session
	 *            the http session that is never used here directly, but is
	 *            passed to other methods to ensure that the binding result
	 *            information is kept when using the mirror to stop the
	 *            refreshing may re-submit form.
	 * @return redirect to the referrer.
	 */
	@RequestMapping(value = "mirror/")
	public String goBack(HttpServletRequest request) {
		return "redirect:" + request.getHeader("Referer");
	}

	@RequestMapping(value = "")
	public String root() {
		return "redirect:/login/";
	}
}
