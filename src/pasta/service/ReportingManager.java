package pasta.service;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.UserPermissionLevel;
import pasta.domain.reporting.Report;
import pasta.domain.user.PASTAUser;
import pasta.repository.ReportingDAO;
import pasta.util.ProjectProperties;

@Service("reportingManager")
@Repository
public class ReportingManager implements InitializingBean {
	public static final Logger logger = Logger
			.getLogger(ReportingManager.class);
	
	private ReportingDAO reportingDAO = ProjectProperties.getInstance().getReportingDAO();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		List<Report> reports = Arrays.asList(
				new Report("mark-histograms", "Assessment Marks", 
						"View a breakdown of total marks for each assessment.",
						UserPermissionLevel.INSTRUCTOR, UserPermissionLevel.TUTOR),
				new Report("test-case-histograms", "Unit Tests - Test Case Pass Counts", 
						"How many test cases are students currently passing for each assessment?",
						UserPermissionLevel.INSTRUCTOR, UserPermissionLevel.TUTOR),
				new Report("test-case-difficulty", "Unit Tests - Test Case Difficulty", 
						"Compare the current pass/fail rate of test cases within each assessment.",
						UserPermissionLevel.INSTRUCTOR, UserPermissionLevel.TUTOR),
				new Report("unit-test-attempts", "Unit Test Attempts",
						"See how many attempts each student required before being able to complete each unit test case.",
						UserPermissionLevel.INSTRUCTOR, UserPermissionLevel.TUTOR),
				new Report("assessment-ratings", "Assessment Feedback",
						"View anonymous student feedback on assessments.",
						UserPermissionLevel.INSTRUCTOR),
				new Report("submissions-timeline", "Submissions Timeline",
						"View submission counts over time, as well as a list of students who have not yet started each assessment.",
						UserPermissionLevel.INSTRUCTOR, UserPermissionLevel.TUTOR)
		);
		for(Report report : reports) {
			Report saved = getReport(report.getId());
			if(saved == null) {
				logger.info("Creating missing report: " + report.getId());
				saveOrUpdate(report);
				continue;
			}
			if(!report.getName().equals(saved.getName()) || !report.getDescription().equals(saved.getDescription())) {
				logger.info("Updating report details: " + report.getId());
				saved.setName(report.getName());
				saved.setDescription(report.getDescription());
				saveOrUpdate(saved);
			}
		}
	}
	
	public List<Report> getAllReports(PASTAUser user) {
		List<Report> allReports = reportingDAO.getAllReports();
		if(!user.isInstructor()) {
			ListIterator<Report> lit = allReports.listIterator();
			while(lit.hasNext()) {
				if(!userCanViewReport(user, lit.next())) {
					lit.remove();
				}
			}
		}
		return allReports;
	}
	
	public Report getReport(String id) {
		return reportingDAO.getReportById(id);
	}
	
	public boolean userCanViewReport(PASTAUser user, Report report) {
		UserPermissionLevel userPermission = user.getPermissionLevel();
		Set<UserPermissionLevel> reportPermissions = report.getPermissionLevels();
		return reportPermissions.contains(userPermission);
	}
	
	public void saveOrUpdate(Report report) {
		reportingDAO.saveOrUpdate(report);
	}
}
