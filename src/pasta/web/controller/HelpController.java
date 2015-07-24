package pasta.web.controller;

import java.io.FileNotFoundException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.user.PASTAUser;
import pasta.util.PASTAUtil;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 24 Jul 2015
 *
 */
@Controller
@RequestMapping("help/")
public class HelpController {

	/**
	 * Get the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in or user isn't registered.
	 */
	public PASTAUser getUser() {
		PASTAUser user = (PASTAUser) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return user;
	}
	
	@RequestMapping("unitTests/")
	public String help(Model model) {
		PASTAUser user = getUser();
		if(user == null) {
			return "redirect:/login/"; 
		}
		if(!user.isTutor()) {
			return "redirect:/home/"; 
		}
		model.addAttribute("unikey", user);
		try {
			model.addAttribute("PASTAJUnitTest", PASTAUtil.scrapeFile(PASTAUtil.getTemplateResource("help_templates/PASTAJUnitTest.java")));
			model.addAttribute("HelloWorldTest", PASTAUtil.scrapeFile(PASTAUtil.getTemplateResource("help_templates/HelloWorldTest.java")));
		} catch (FileNotFoundException e) {}
		return "help/unitTestHelp";
	}
}
