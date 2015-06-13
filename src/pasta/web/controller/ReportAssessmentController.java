package pasta.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import pasta.service.AssessmentManager;
import pasta.service.reporting.AssessmentReportingManager;
import pasta.service.reporting.UnitTestReportingManager;

@Controller
@RequestMapping("api/report/assessment/")
public class ReportAssessmentController {
	
	@Autowired
	private AssessmentManager assessmentManager;
	@Autowired
	private AssessmentReportingManager assessmentReportManager;
	@Autowired
	private UnitTestReportingManager unitTestReportManager;
	
	@RequestMapping("all/")
	@ResponseBody
	public String getAll() {
		return assessmentReportManager.getAllAssessments();
	}
	
	@RequestMapping("{assessmentId}/")
	@ResponseBody
	public String getAssessment(@PathVariable("assessmentId") long assessmentId) {
		return assessmentReportManager.getAssessment(assessmentManager.getAssessment(assessmentId));
	}
	
	@RequestMapping("{assessmentId}/marksSummary/")
	@ResponseBody
	public String getMarksSummary(@PathVariable("assessmentId") long assessmentId) {
		return assessmentReportManager.getMarksSummary(assessmentManager.getAssessment(assessmentId));
	}
	
	@RequestMapping("{assessmentId}/unitTests/")
	@ResponseBody
	public String getUnitTestSummary(@PathVariable("assessmentId") long assessmentId) {
		return unitTestReportManager.getAllTestsSummary(assessmentManager.getAssessment(assessmentId));
	}
	
	@RequestMapping("{assessmentId}/ratings/")
	@ResponseBody
	public String getRatings(@PathVariable("assessmentId") long assessmentId) {
		return assessmentReportManager.getAssessmentRatings(assessmentManager.getAssessment(assessmentId));
	}
}
