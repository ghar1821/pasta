package pasta.service.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.ratings.AssessmentRating;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.service.RatingManager;
import pasta.service.UserManager;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service("assessmentReportingManager")
@Repository
public class AssessmentReportingManager {

	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RatingManager ratingManager;
	
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
				assessmentsNode.add(getAssessmentNode(assessment));
			}
			categoryNode.set("assessments", assessmentsNode);
			root.add(categoryNode);
		}
		return root.toString();
	}
	
	public String getAssessment(Assessment assessment) {
		return getAssessmentNode(assessment).toString();
	}
	
	private ObjectNode getAssessmentNode(Assessment assessment) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode assessmentNode = mapper.createObjectNode();
		assessmentNode.put("id", assessment.getId());
		assessmentNode.put("name", assessment.getName());
		assessmentNode.put("dueDate", PASTAUtil.formatDateReadable(assessment.getDueDate()));
		assessmentNode.put("marks", assessment.getMarks());
		return assessmentNode;
	}

	public String getMarksSummary(Assessment assessment) {
		Collection<PASTAUser> students = userManager.getStudentList();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode marksSummaryNode = mapper.createObjectNode();
		marksSummaryNode.put("maxMark", assessment.getMarks());
		ArrayNode marksNode = mapper.createArrayNode();
		for(PASTAUser user : students) {
			AssessmentResult result = resultDAO.getLatestIndividualResult(user, assessment.getId());
			double mark = -1;
			if(result != null) {
				mark = result.getMarks();
			}
			marksNode.add(mark);
		}
		marksSummaryNode.set("marks", marksNode);
		return marksSummaryNode.toString();
	}
	
	public String getAssessmentRatings(Assessment assessment) {
		List<AssessmentRating> ratings = ratingManager.getRatingsForAssessment(assessment);
		List<String> comments = new ArrayList<String>();
		List<Integer> ratingValues = new ArrayList<Integer>();
		int ratingCount = 0;
		for(AssessmentRating rating : ratings) {
			comments.add(rating.getComment());
			ratingValues.add(rating.getRating());
			ratingCount++;
		}
		Collections.shuffle(comments);
		Collections.shuffle(ratingValues);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode ratingsNode = mapper.createObjectNode();
		ArrayNode commentsNode = mapper.createArrayNode();
		for(String comment : comments) {
			if(comment != null && !comment.trim().isEmpty()) {
				commentsNode.add(comment);
			}
		}
		ratingsNode.set("comments", commentsNode);
		ArrayNode valuesNode = mapper.createArrayNode();
		for(Integer value : ratingValues) {
			valuesNode.add(value);
		}
		ratingsNode.set("ratings", valuesNode);
		ratingsNode.put("ratingCount", ratingCount);
		return ratingsNode.toString();
	}
}
