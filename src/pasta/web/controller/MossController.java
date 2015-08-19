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
