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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import pasta.domain.UserPermissionLevel;
import pasta.domain.user.PASTAUser;
import pasta.service.MossManager;
import pasta.web.WebUtils;

/**
 * Controller class for the MOSS plagarism detection functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/moss/...
 * <p>
 * Only teaching staff can access this url.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-24
 *
 */
@Controller
@RequestMapping("moss/")
public class MossController {
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired private MossManager mossManager;

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// MOSS //
	// ///////////////////////////////////////////////////////////////////////////
	
	/**
	 * $PASTAUrl$/moss/run/{assessmentId}/
	 * <p>
	 * Run moss.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home	
	 * 
	 * Run moss using {@link pasta.service.MossManager#runMoss(String)}
	 * 
	 * @param model the mode being used
	 * @param request the http request used for redirecting back to the referrer url
	 * @param assessmentId the id of the assessment.
	 * @return "redirect:/login/" or redirect back to the referrer
	 */
	@RequestMapping(value = "/run/{assessmentId}/")
	public String runMoss(ModelMap model, HttpServletRequest request,
			@PathVariable("assessmentId") long assessmentId) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		mossManager.runMoss(assessmentId);
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/moss/view/{assessmentId}/
	 * <p>
	 * View the list of moss executions for an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>assessmentId</td><td>the id of the assessment</td></tr>
	 * 	<tr><td>mossList</td><td>the list of moss execution given by {@link pasta.service.MossManager#getMossList(String)}</td></tr>
	 * </table>
	 * 
	 * JSP: <ul><li>moss/list</li></ul>
	 * 
	 * @param model the model being used
	 * @param assessmentId the id of the assessment
	 * @return "redirect:/login/" or "redirect:/home/" or "moss/list"
	 */
	@RequestMapping(value = "/view/{assessmentId}/")
	public String viewMoss(ModelMap model,
			@PathVariable("assessmentId") long assessmentId) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		model.addAttribute("assessmentId", assessmentId);
		model.addAttribute("mossList", mossManager.getMossList(assessmentId));
		return "moss/list";
	}
	
	/**
	 * $PASTAUrl$/moss/view/{assessmentId}/{date}/
	 * <p>
	 * View the results of the moss execution.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>mossResults</td><td>the {@link pasta.domain.moss.MossResults} for the execution at this time.</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>moss/view</li></ul>
	 * 
	 * @param model the model being used
	 * @param assessment the id of the assessment
	 * @param date the date as a string in the following format: yyyy-MM-dd'T'HH-mm-ss
	 * @return "redirect:/login/" or "redirect:/home/" or "moss/view"
	 */
	@RequestMapping(value = "/view/{assessmentId}/{date}/")
	public String viewMoss(ModelMap model,
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("date") String date) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		model.addAttribute("mossResults", mossManager.getMossRun(assessmentId, date));
		return "moss/view";
	}
}
