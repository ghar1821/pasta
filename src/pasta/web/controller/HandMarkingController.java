package pasta.web.controller;


import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.domain.template.HandMarking;
import pasta.domain.upload.NewHandMarking;
import pasta.service.HandMarkingManager;
import pasta.service.SubmissionManager;
import pasta.service.UserManager;

@Controller
@RequestMapping("handMarking/")
/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
public class HandMarkingController {

	public HandMarkingController() {
		codeStyle = new HashMap<String, String>();
		codeStyle.put("c", "ccode");
		codeStyle.put("cpp", "cppcode");
		codeStyle.put("h", "cppcode");
		codeStyle.put("cs", "csharpcode");
		codeStyle.put("css", "csscode");
		codeStyle.put("html", "htmlcode");
		codeStyle.put("java", "javacode");
		codeStyle.put("js", "javascriptcode");
		codeStyle.put("pl", "perlcode");
		codeStyle.put("pm", "perlcode");
		codeStyle.put("php", "phpcode");
		codeStyle.put("py", "pythoncode");
		codeStyle.put("rb", "rubycode");
		codeStyle.put("sql", "sqlcode");
		codeStyle.put("xml", "xmlcode");

	}

	protected final Log logger = LogFactory.getLog(getClass());

	private UserManager userManager;
	private HandMarkingManager handMarkingManager;
	private HashMap<String, String> codeStyle;

	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
	}
	
	@Autowired
	public void setMyService(HandMarkingManager myService) {
		this.handMarkingManager = myService;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newHandMarkingModel")
	public NewHandMarking returnNewHandMakingModel() {
		return new NewHandMarking();
	}

	@ModelAttribute("handMarking")
	public HandMarking returnHandMarkingModel() {
		return new HandMarking();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Get the currently logged in user.
	 * 
	 * @return
	 */

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
	// HAND MARKING //
	// ///////////////////////////////////////////////////////////////////////////

	// view a handmarking
	@RequestMapping(value = "{handMarkingName}/")
	public String viewHandMarking(
			@PathVariable("handMarkingName") String handMarkingName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("handMarking",
				handMarkingManager.getHandMarking(handMarkingName));
		model.addAttribute("unikey", user);
		return "assessment/view/handMarks";
	}

	// update a handmarking
	@RequestMapping(value = "{handMarkingName}/", method = RequestMethod.POST)
	public String updateHandMarking(
			@ModelAttribute(value = "handMarking") HandMarking form,
			BindingResult result,
			@PathVariable("handMarkingName") String handMarkingName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {
			form.setName(handMarkingName);
			handMarkingManager.updateHandMarking(form);
		}
		return "redirect:.";
	}

	// view a handmarking
	@RequestMapping(value = "")
	public String viewAllHandMarking(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("allHandMarking", handMarkingManager.getAllHandMarking());
		model.addAttribute("unikey", user);
		return "assessment/viewAll/handMarks";
	}

	// new handmarking
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String newHandMarking(
			@ModelAttribute(value = "newHandMarkingModel") NewHandMarking form,
			BindingResult result, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		// add it to the system
		if (getUser().isInstructor()) {
			handMarkingManager.newHandMarking(form);
			return "redirect:./" + form.getShortName() + "/";
		}
		return "redirect:.";
	}

	// delete a unit test
	@RequestMapping(value = "delete/{handMarkingName}/")
	public String deleteHandMarking(
			@PathVariable("handMarkingName") String handMarkingName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {
			handMarkingManager.removeHandMarking(handMarkingName);
		}
		return "redirect:../../";
	}

}