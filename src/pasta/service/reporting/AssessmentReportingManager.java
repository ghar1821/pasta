package pasta.service.reporting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import pasta.service.ResultManager;
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
	@Autowired
	private ResultManager resultManager;
	
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
				assessmentsNode.add(getAssessmentJSON(assessment));
			}
			categoryNode.set("assessments", assessmentsNode);
			root.add(categoryNode);
		}
		return root.toString();
	}
	
	public String getAssessment(Assessment assessment) {
		return getAssessmentJSON(assessment).toString();
	}
	
	public ObjectNode getAssessmentJSON(Assessment assessment) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode assessmentNode = mapper.createObjectNode();
		assessmentNode.put("id", assessment.getId());
		assessmentNode.put("name", assessment.getName());
		assessmentNode.put("dueDate", PASTAUtil.formatDateReadable(assessment.getDueDate()));
		assessmentNode.put("marks", assessment.getMarks());
		return assessmentNode;
	}

	public String getMarksSummary(Assessment assessment) {
		return getMarksSummaryJSON(assessment).toString();
	}
	
	public ObjectNode getMarksSummaryJSON(Assessment assessment) {
		return getMarksSummaryJSON(assessment, null);
	}
	
	public ObjectNode getMarksSummaryJSON(Assessment assessment, PASTAUser user) {
		Collection<PASTAUser> students = userManager.getStudentList();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode marksSummaryNode = mapper.createObjectNode();
		marksSummaryNode.put("maxMark", assessment.getMarks());
		
		Set<PASTAUser> tutoredStudents = new TreeSet<>();
		if(user != null && user.isTutor()) {
			tutoredStudents.addAll(userManager.getTutoredStudents(user));
		}
		
		Double yourMark = null;
		ArrayNode marksNode = mapper.createArrayNode();
		ArrayNode classMarksNode = mapper.createArrayNode();
		for(PASTAUser student : students) {
			AssessmentResult result = resultDAO.getLatestIndividualResult(student, assessment.getId());
			double mark = -1;
			if(result != null) {
				mark = result.getMarks();
			}
			if(student.equals(user)) {
				yourMark = mark;
			}
			(tutoredStudents.contains(student) ? classMarksNode : marksNode).add(mark);
		}
		marksSummaryNode.set("marks", marksNode);
		if(!tutoredStudents.isEmpty()) {
			marksSummaryNode.set("classMarks", classMarksNode);
		}
		if(yourMark != null) {
			marksSummaryNode.put("yourMark", yourMark);
		}
		return marksSummaryNode;
	}
	
	public String getAssessmentRatings(Assessment assessment) {
		return getAssessmentRatingsJSON(assessment).toString();
	}
	
	public ObjectNode getAssessmentRatingsJSON(Assessment assessment) {
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
		return ratingsNode;
	}
	
	public ObjectNode getAssessmentSubmissionsJSON(Assessment assessment) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = mapper.createObjectNode();
		
		Collection<PASTAUser> students = userManager.getStudentList();
		root.put("studentCount", students.size());
		
		Set<Date> allSubmissions = new TreeSet<>();
		Set<PASTAUser> noSubmissions = new TreeSet<>();
		
		HashMap<Date, Integer> submissionCounts = new HashMap<>();
		HashMap<Date, Integer> startedCount = new HashMap<>();
		
		for(PASTAUser user : students) {
			List<Date> submissionHistory = resultManager.getSubmissionDates(user, assessment.getId());
			
			boolean first = true;
			for(Date date : submissionHistory) {
				Date roundDate = getDay(date);
				if(!allSubmissions.contains(date)) {
					Integer count = submissionCounts.get(roundDate);
					if(count == null) {
						count = 0;
					}
					submissionCounts.put(roundDate, count+1);
				}
				if(first) {
					Integer count = startedCount.get(roundDate);
					if(count == null) {
						count = 0;
					}
					startedCount.put(roundDate, count+1);
				}
				first = false;
			}
			
			if(submissionHistory.isEmpty()) {
				noSubmissions.add(user);
			} else {
				allSubmissions.addAll(submissionHistory);
			}
		}
		
		ArrayNode noSubmissionNode = mapper.createArrayNode();
		for(PASTAUser user : noSubmissions) {
			noSubmissionNode.add(user.getUsername());
		}
		root.set("noSubmission", noSubmissionNode);
		
		TreeSet<Date> dates = new TreeSet<>(submissionCounts.keySet());
		if(!dates.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(getDay(dates.first()));
			Date last = getDay(dates.last());
			if(last.before(assessment.getDueDate())) {
				last = getDay(assessment.getDueDate());
			}
			Date today = getDay(new Date());
			if(last.after(today)) {
				last = today;
			}
			while(!cal.getTime().after(last)) {
				dates.add(cal.getTime());
				cal.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		ArrayNode datesNode = mapper.createArrayNode();
		for(Date date : dates) {
			datesNode.add(sdf.format(date));
		}
		root.set("dates", datesNode);
		
		ArrayNode submissionCountsNode = mapper.createArrayNode();
		for(Date date : dates) {
			Integer count = submissionCounts.get(date);
			if(count == null) {
				count = 0;
			}
			submissionCountsNode.add(count);
		}
		root.set("submissionCounts", submissionCountsNode);
		
		ArrayNode startedCountsNode = mapper.createArrayNode();
		int totalCount = 0;
		for(Date date : dates) {
			Integer count = startedCount.get(date);
			if(count == null) {
				count = 0;
			}
			totalCount += count;
			startedCountsNode.add(totalCount);
		}
		root.set("startedCounts", startedCountsNode);
		
		return root;
	}
	private Date getDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
 }
