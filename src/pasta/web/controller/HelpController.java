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
		model.addAttribute("usermeta", UnitTest.BB_META_FILENAME);
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
