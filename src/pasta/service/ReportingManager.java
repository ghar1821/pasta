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
				new Report("mark-histograms", "Assessment Marks", UserPermissionLevel.INSTRUCTOR, UserPermissionLevel.TUTOR),
				new Report("unit-test-attempts", "Unit Test Attempts", UserPermissionLevel.INSTRUCTOR, UserPermissionLevel.TUTOR)
		);
		for(Report report : reports) {
			Report saved = getReport(report.getId());
			if(saved == null) {
				logger.info("Creating missing report: " + report.getId());
				saveOrUpdate(report);
				continue;
			}
			if(!report.getName().equals(saved.getName())) {
				logger.info("Updating report name: " + report.getId());
				saved.setName(report.getName());
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
