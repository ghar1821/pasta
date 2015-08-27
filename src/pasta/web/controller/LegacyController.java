package pasta.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import pasta.archive.convert.AssessmentConverter;
import pasta.domain.UserPermissionLevel;
import pasta.domain.user.PASTAUser;
import pasta.web.WebUtils;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 24 Jul 2015
 */
@Controller
@RequestMapping("legacy/")
@SessionAttributes("assessmentConverter")
public class LegacyController {
	
	@ModelAttribute("assessmentConverter")
	public AssessmentConverter getConverter() {
		return new AssessmentConverter();
	}

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}
	
	@RequestMapping(value="convert/", method=RequestMethod.GET)
	public String viewConvert(
			@ModelAttribute("assessmentConverter") AssessmentConverter converter, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		if(converter.isStarted()) {
			model.addAttribute("started", true);
		}
		return "legacy/convert";
	}
	
	@RequestMapping(value="convert/", method=RequestMethod.POST)
	@ResponseBody
	public String startConvert(
			@ModelAttribute("assessmentConverter") final AssessmentConverter converter) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		if(converter.isStarted()) {
			return "ALREADY STARTED";
		}
		TaskExecutor executor = new SimpleAsyncTaskExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				converter.convertLegacyContent();
			}
		});
		return "";
	}
	
	@RequestMapping(value="convert/status/", method=RequestMethod.POST)
	@ResponseBody
	public String checkConvert(@ModelAttribute("assessmentConverter") AssessmentConverter converter, SessionStatus status) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		if(!converter.isStarted()) {
			return "NOT STARTED";
		} else if(converter.isDone() && !converter.hasOutput()) {
			status.setComplete();
			return "DONE";
		}
		List<String> output = converter.getOutputSinceLastCall();
		StringBuilder sb = new StringBuilder();
		for(String out : output) {
			sb.append(out).append(System.lineSeparator());
		}
		return sb.toString();
	}
}
