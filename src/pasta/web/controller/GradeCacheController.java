package pasta.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
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

import pasta.domain.UserPermissionLevel;
import pasta.domain.result.AssessmentResultSummaryCalculator;
import pasta.domain.user.PASTAUser;
import pasta.service.ResultManager;
import pasta.service.UserManager;
import pasta.web.WebUtils;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 11 Nov 2016
 */
@Controller
@RequestMapping("gradecache/")
@SessionAttributes("gradeCache")
public class GradeCacheController {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResultManager resultManager;
	
	@ModelAttribute("gradeCache")
	public AssessmentResultSummaryCalculator getGradeCache() {
		return new AssessmentResultSummaryCalculator();
	}

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}
	
	@RequestMapping(value="recalculate/", method=RequestMethod.GET)
	public String viewConvert(
			@ModelAttribute("gradeCache") AssessmentResultSummaryCalculator gradeCache, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		if(gradeCache.isStarted()) {
			model.addAttribute("started", true);
		}
		return "user/gradeCache";
	}
	
	@RequestMapping(value="recalculate/", method=RequestMethod.POST)
	@ResponseBody
	public String startConvert(
			@ModelAttribute("gradeCache") final AssessmentResultSummaryCalculator gradeCache) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		if(gradeCache.isStarted()) {
			return "ALREADY STARTED";
		}
		TaskExecutor executor = new SimpleAsyncTaskExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				gradeCache.recacheResultSummaries(userManager, resultManager);
			}
		});
		return "";
	}
	
	@RequestMapping(value="recalculate/status/", method=RequestMethod.POST)
	@ResponseBody
	public String checkConvert(@ModelAttribute("gradeCache") AssessmentResultSummaryCalculator gradeCache, SessionStatus status) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		if(!gradeCache.isStarted()) {
			return "NOT STARTED";
		} else if(gradeCache.isDone() && !gradeCache.hasOutput()) {
			status.setComplete();
			return "DONE";
		}
		List<String> output = gradeCache.getOutputSinceLastCall();
		StringBuilder sb = new StringBuilder();
		for(String out : output) {
			sb.append(out).append(System.lineSeparator());
		}
		return sb.toString();
	}
}
