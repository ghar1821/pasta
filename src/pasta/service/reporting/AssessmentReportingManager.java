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
import pasta.domain.result.UnitTestCaseResult;
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

	@Autowired
	private AssessmentDAO assDao;
	@Autowired
	private ResultDAO resultDAO;
	
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
		marksSummaryNode.put("numTests", assessment.getAllTestNames().size());
		
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
	
	public ObjectNode getTestCaseCountsSummaryJSON(Assessment assessment, PASTAUser user) {
		Collection<PASTAUser> students = userManager.getStudentList();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode passCountSummaryNode = mapper.createObjectNode();
		passCountSummaryNode.put("numTests", assessment.getAllTestNames().size());
		
		Set<PASTAUser> tutoredStudents = new TreeSet<>();
		if(user != null && user.isTutor()) {
			tutoredStudents.addAll(userManager.getTutoredStudents(user));
		}
		
		Long yourPassCount = null;
		ArrayNode passCountsNode = mapper.createArrayNode();
		ArrayNode classPassCountsNode = mapper.createArrayNode();
		for(PASTAUser student : students) {
			AssessmentResult result = resultDAO.getLatestIndividualResult(student, assessment.getId());
			long passCount = -1;
			if(result != null) {
				passCount = result.getUnitTests().stream()
						.flatMap(utr -> utr.getTestCases().stream())
						.filter(UnitTestCaseResult::isPass)
						.count();
			}
			if(student.equals(user)) {
				yourPassCount = passCount;
			}
			(tutoredStudents.contains(student) ? classPassCountsNode : passCountsNode).add(passCount);
		}
		passCountSummaryNode.set("passCounts", passCountsNode);
		if(!tutoredStudents.isEmpty()) {
			passCountSummaryNode.set("classPassCounts", classPassCountsNode);
		}
		if(yourPassCount != null) {
			passCountSummaryNode.put("yourPassCount", yourPassCount);
		}
		return passCountSummaryNode;
	}

	public ObjectNode getTestCaseDifficultyJSON(Assessment assessment, PASTAUser user) {
		Collection<PASTAUser> students = userManager.getStudentList();
		List<String> testNames = assessment.getAllTestNames();
		
		Map<String, int[]> otherCounts = new HashMap<>();
		Map<String, int[]> classCounts = new HashMap<>();
		for(String testName : testNames) {
			otherCounts.put(testName, new int[3]);
			classCounts.put(testName, new int[3]);
		}
		
		Set<PASTAUser> tutoredStudents = new TreeSet<>();
		if(user != null && user.isTutor()) {
			tutoredStudents.addAll(userManager.getTutoredStudents(user));
		}
		
		for(PASTAUser student : students) {
			AssessmentResult result = resultDAO.getLatestIndividualResult(student, assessment.getId());
			if(result == null) {
				continue;
			}
			
			Map<String, int[]> counts = tutoredStudents.contains(student) ? classCounts : otherCounts;
			result.getUnitTests().stream()
			.flatMap(utr -> utr.getTestCases().stream())
			.forEach(utcr -> {
				int[] c = counts.get(utcr.getTestName());
				if(c == null) return;
				c[utcr.isPass() ? 0 : (utcr.isFailure() ? 1 : 2)]++;
			});
		}
		
		Collections.sort(testNames, (a, b) -> {
			int[] c1a = otherCounts.get(a);
			int[] c2a = classCounts.get(a);
			int[] c1b = otherCounts.get(b);
			int[] c2b = classCounts.get(b);
			int p1 = (c1a[0] + c2a[0]) - (c1a[1] + c2a[1]) - (c1a[2] + c2a[2]);
			int p2 = (c1b[0] + c2b[0]) - (c1b[1] + c2b[1]) - (c1b[2] + c2b[2]);
			if(p1 != p2) {
				return p1 - p2;
			}
			return a.compareToIgnoreCase(b);
		});
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode testsSummaryNode = mapper.createObjectNode();
		
		ArrayNode testsNode = mapper.createArrayNode();
		for(String testName : testNames) {
			testsNode.add(testName);
		}
		testsSummaryNode.set("tests", testsNode);
		
		ArrayNode testResultsNode = mapper.createArrayNode();
		for(String testName : testNames) {
			ObjectNode testNode = mapper.createObjectNode();
			testNode.put("testName", testName);
			ObjectNode countsNode = mapper.createObjectNode();
			int[] counts = otherCounts.get(testName);
			countsNode.put("pass", counts[0]);
			countsNode.put("fail", counts[1]);
			countsNode.put("error", counts[2]);
			testNode.set("counts", countsNode);
			if(!tutoredStudents.isEmpty()) {
				countsNode = mapper.createObjectNode();
				counts = classCounts.get(testName);
				countsNode.put("pass", counts[0]);
				countsNode.put("fail", counts[1]);
				countsNode.put("error", counts[2]);
				testNode.set("classCounts", countsNode);
			}
			testResultsNode.add(testNode);
		}
		
		testsSummaryNode.set("testResults", testResultsNode);
		return testsSummaryNode;
	}
 }
