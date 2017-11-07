package pasta.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pasta.domain.UserPermissionLevel;
import pasta.domain.reporting.Report;
import pasta.domain.reporting.ReportPermission;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.repository.AssessmentDAO;
import pasta.repository.ReportingDAO;
import pasta.util.ProjectProperties;

@Service("reportingManager")
@Repository
public class ReportingManager implements InitializingBean {
	public static final Logger logger = Logger
			.getLogger(ReportingManager.class);
	
	private ReportingDAO reportingDAO = ProjectProperties.getInstance().getReportingDAO();
	
	@Autowired
	private AssessmentDAO assessmentDAO;
	
	@Autowired
	private AssessmentManager assessmentManager;
	
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
		List<ReportPermission> permissions = reportingDAO.getPermissions(report, user.getPermissionLevel());
		boolean allFalse = permissions.size() > 0;
		for (ReportPermission permission : permissions) {
			if(Boolean.TRUE.equals(permission.getAllow())) {
				return true;
			}
			if(!Boolean.FALSE.equals(permission.getAllow())) {
				allFalse = false;
			}
		}
		if(allFalse) {
			return false;
		}
		return report.getDefaultPermissionLevels().contains(user.getPermissionLevel());
	}
	
	public Set<Assessment> getAssessmentsForReport(PASTAUser user, Report report) {
		Set<Assessment> allowed = new HashSet<>();
		Set<Assessment> disallowed = new HashSet<>();
		
		List<ReportPermission> permissions = reportingDAO.getPermissions(report, user.getPermissionLevel());
		for (ReportPermission permission : permissions) {
			if(Boolean.FALSE.equals(permission.getAllow())) {
				disallowed.add(permission.getAssessment());
			} else if(Boolean.TRUE.equals(permission.getAllow())) {
				allowed.add(permission.getAssessment());
			}
		}
		
		Set<Assessment> base;
		if(report.getDefaultPermissionLevels().contains(user.getPermissionLevel())) {
			base = new HashSet<>(assessmentManager.getReleasedAssessments(user));
		} else {
			base = new HashSet<>();
		}
		base.addAll(allowed);
		base.removeAll(disallowed);
		return base;
	}
	
	public void saveOrUpdate(Report report) {
		reportingDAO.saveOrUpdate(report);
	}
	
	public ObjectNode getReportPermissions(String reportId) {
		ObjectMapper mapper = new ObjectMapper();
		Report report = reportingDAO.getReportById(reportId);
		Map<String, Set<Assessment>> allAssessments = assessmentDAO.getAllAssessmentsByCategory();
		UserPermissionLevel[] validPermissions = UserPermissionLevel.validReportValues();
		Set<ReportPermission> permissions = report.getPermissions();
		Set<Long> seenAssessments = new HashSet<>();
		
		ArrayNode categoriesNode = mapper.createArrayNode();
		for(Map.Entry<String, Set<Assessment>> entry : allAssessments.entrySet()) {
			ObjectNode categoryNode = mapper.createObjectNode();
			categoryNode.put("category", entry.getKey());
			ArrayNode assessmentsNode = mapper.createArrayNode();
			for(Assessment assessment : entry.getValue()) {
				ObjectNode assessmentNode = mapper.createObjectNode();
				assessmentNode.put("id", assessment.getId());
				assessmentNode.put("name", assessment.getName());
				if(seenAssessments.contains(assessment.getId())) {
					assessmentNode.put("duplicate", true);
				} else {
					seenAssessments.add(assessment.getId());
				}
				ObjectNode permissionNode = mapper.createObjectNode();
				for(ReportPermission permission : permissions) {
					if(permission.getAssessment().equals(assessment)) {
						if(permission.getAllow() != null) {
							permissionNode.put(permission.getPermissionLevel().name(), permission.getAllow());
						}
					}
				}
				assessmentNode.set("permissions", permissionNode);
				assessmentsNode.add(assessmentNode);
			}
			categoryNode.set("assessments", assessmentsNode);
			categoriesNode.add(categoryNode);
		}
		
		ObjectNode results = mapper.createObjectNode();
		results.set("assessment-permissions", categoriesNode);
		
		ArrayNode permissionTypesNode = mapper.createArrayNode();
		for(UserPermissionLevel level : validPermissions) {
			ObjectNode permNode = mapper.createObjectNode();
			permNode.put("name", level.name());
			permNode.put("description", level.getDescription());
			permissionTypesNode.add(permNode);
		}
		results.set("valid-permissions", permissionTypesNode);
		
		ObjectNode defaultPermissions = mapper.createObjectNode();
		for (UserPermissionLevel level : validPermissions) {
			boolean contains = report.getDefaultPermissionLevels().contains(level);
			defaultPermissions.put(level.name(), contains);
		}
		results.set("default-permissions", defaultPermissions);
		
		return results;
	}

	public void setAssessmentPermissions(Report report, Set<ReportPermission> assessmentPermissions) {
		Set<ReportPermission> curPermissions = report.getPermissions();
		for (ReportPermission permission : assessmentPermissions) {
			if(permission.getAllow() == null) {
				report.removePermission(permission);
			} else if(curPermissions.contains(permission)) {
				report.removePermission(permission);
				report.addPermission(permission);
			} else {
				report.addPermission(permission);
			}
		}
	}
}
