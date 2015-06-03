package pasta.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import pasta.service.reporting.AssessmentReportingManager;

@Controller
@RequestMapping("api/report/assessment/")
public class ReportAssessmentController {
	
	@Autowired
	private AssessmentReportingManager assessmentManager;
	
	@RequestMapping("all/")
	@ResponseBody
	public String getAll() {
		return assessmentManager.getAllAssessments();
	}
}
