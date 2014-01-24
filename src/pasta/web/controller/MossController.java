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
import pasta.service.SubmissionManager;

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
	private SubmissionManager manager;
	private MossManager mossManager;

	@Autowired
	public void setMyService(SubmissionManager myService) {
		this.manager = myService;
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
			return manager.getUser(username);
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
	
		model.addAttribute("mossResults", mossManager.getMossRun(assessment, date));
		return "moss/view";
	}
}
