package pasta.web.controller;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import pasta.domain.UserPermissionLevel;
import pasta.domain.template.UnitTest;
import pasta.domain.user.PASTAUser;
import pasta.util.PASTAUtil;
import pasta.web.WebUtils;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 24 Jul 2015
 *
 */
@Controller
@RequestMapping("help/")
public class HelpController {

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}
	
	@RequestMapping("unitTests/")
	public String unitTestHelp(Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		model.addAttribute("userout", UnitTest.BB_OUTPUT_FILENAME);
		try {
			model.addAttribute("PASTAJUnitTest", PASTAUtil.scrapeFile(PASTAUtil.getTemplateResource("help_templates/PASTAJUnitTest.java")));
			model.addAttribute("HelloWorldTest", PASTAUtil.scrapeFile(PASTAUtil.getTemplateResource("help_templates/HelloWorldTest.java")));
			model.addAttribute("SampleCustomTest", PASTAUtil.scrapeFile(PASTAUtil.getTemplateResource("help_templates/SampleCustomTest.java")));
		} catch (FileNotFoundException e) {}
		return "help/unitTestHelp";
	}
	
	@RequestMapping("customValidation/")
	public String customValidationHelp(Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		try {
			model.addAttribute("PASTASubmissionValidator", PASTAUtil.scrapeFile(PASTAUtil.getTemplateResource("help_templates/PASTASubmissionValidator.java")));
			model.addAttribute("SampleCustomValidator", PASTAUtil.scrapeFile(PASTAUtil.getTemplateResource("help_templates/SampleCustomValidator.java")));
			model.addAttribute("ValidationFeedback", PASTAUtil.scrapeFile(PASTAUtil.getTemplateResource("help_templates/ValidationFeedback.java")));
		} catch (FileNotFoundException e) {}
		return "help/customValidationHelp";
	}
}
