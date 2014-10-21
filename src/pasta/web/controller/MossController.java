/**
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.service.MossManager;
import pasta.service.UserManager;

/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
@Controller
@RequestMapping("moss/")
public class MossController {


	protected final Log logger = LogFactory.getLog(getClass());
	private UserManager userManager;
	private MossManager mossManager;

	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
	}
	
	@Autowired
	public void setMyService(MossManager myService) {
		this.mossManager = myService;
	}
	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

//	@ModelAttribute("changePasswordForm")
//	public ChangePasswordForm returnNewUnitTestModel() {
//		return new ChangePasswordForm();
//	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////
	
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
	// MOSS //
	// ///////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "/run/{assessmentName}/")
	public String runMoss(ModelMap model, HttpServletRequest request,
			@PathVariable("assessmentName") String assessment) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
	
		if(user.isTutor()){
			// run Moss
			mossManager.runMoss(assessment);
		}
				
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/view/{assessmentName}/")
	public String viewMoss(ModelMap model, HttpServletRequest request,
			@PathVariable("assessmentName") String assessment) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
		model.addAttribute("unikey", user);
		model.addAttribute("assessmentName", assessment);
	
		model.addAttribute("mossList", mossManager.getMossList(assessment));
		return "moss/list";
	}
	
	@RequestMapping(value = "/view/{assessmentName}/{date}/")
	public String viewMoss(ModelMap model, HttpServletRequest request,
			@PathVariable("assessmentName") String assessment,
			@PathVariable("date") String date) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
		model.addAttribute("unikey", user);
	
		model.addAttribute("mossResults", mossManager.getMossRun(assessment, date));
		return "moss/view";
	}
}
