package pasta.service.reporting;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.template.Assessment;
import pasta.repository.AssessmentDAO;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service("assessmentReportingManager")
@Repository
public class AssessmentReportingManager {

	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	
	public String getAllAssessments() {
		Map<String, Set<Assessment>> allAssessments = assDao.getAllAssessmentsByCategory();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode root = mapper.createArrayNode();
		for(Map.Entry<String, Set<Assessment>> entry : allAssessments.entrySet()) {
			ObjectNode categoryNode = mapper.createObjectNode();
			String category = entry.getKey();
			categoryNode.put("category", category);
			ArrayNode assessmentsNode = mapper.createArrayNode();
			for(Assessment assessment : entry.getValue()) {
				ObjectNode assessmentNode = mapper.createObjectNode();
				assessmentNode.put("id", assessment.getId());
				assessmentNode.put("name", assessment.getName());
				assessmentNode.put("dueDate", PASTAUtil.formatDateReadable(assessment.getDueDate()));
				assessmentNode.put("marks", assessment.getMarks());
				assessmentsNode.add(assessmentNode);
			}
			categoryNode.set("assessments", assessmentsNode);
			root.add(categoryNode);
		}
		return root.toString();
	}
	
}
