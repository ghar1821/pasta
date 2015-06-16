package pasta.service.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedUnitTest;
import pasta.repository.ResultDAO;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 3 Jun 2015
 */
@Service("unitTestReportingManager")
@Repository
public class UnitTestReportingManager {

	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	
	@Autowired
	private UserManager userManager;
	
	public String getAllTestsSummary(Assessment assessment) {
		if(assessment.getAllUnitTests().isEmpty()) {
			return "";
		}
		
		Collection<PASTAUser> students = userManager.getStudentList();
		Map<String, List<AssessmentResult>> allResults = new LinkedHashMap<>();
		for(PASTAUser user : students) {
			List<AssessmentResult> studentResults = resultDAO.getAllResultsForUserAssessment(user, assessment.getId(), false);
			allResults.put(user.getUsername(), studentResults);
		}
		
		List<String> testNames = new ArrayList<>();
		for(WeightedUnitTest test : assessment.getAllUnitTests()) {
			testNames.addAll(test.getTest().getAllTestNames());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = mapper.createObjectNode();
		
		ArrayNode nameIndexNode = mapper.createArrayNode();
		for(String testName : testNames) {
			nameIndexNode.add(testName);
		}
		root.set("testNames", nameIndexNode);
		
		int[] sum = new int[testNames.size()];
		int[] count = new int[testNames.size()];
		int[] sumCompleted = new int[testNames.size()];
		int[] countCompleted = new int[testNames.size()];
		
		ArrayNode studentResultsNode = mapper.createArrayNode();
		for(Map.Entry<String, List<AssessmentResult>> student : allResults.entrySet()) {
			ObjectNode studentNode = mapper.createObjectNode();
			String unikey = student.getKey();
			studentNode.put("unikey", unikey);
			int[] attempts = getAttemptsUntilCorrect(student.getValue(), testNames);
			ArrayNode attemptsNode = mapper.createArrayNode();
			for(int i = 0; i < attempts.length; i++) {
				String attempt = (attempts[i] < 0 ? "" : (attempts[i] == 0 ? "-" : String.valueOf(attempts[i])));
				attemptsNode.add(attempt);
				
				if(attempts[i] >= 0) {
					if(attempts[i] == 0) {
						sum[i] += student.getValue().size() + 1;
					} else {
						sum[i] += attempts[i];
						sumCompleted[i] += attempts[i];
						countCompleted[i]++;
					}
					count[i]++;
				}
			}
			studentNode.set("attempts", attemptsNode);
			studentResultsNode.add(studentNode);
		}
		root.set("studentResults", studentResultsNode);
		
		double[] percentComplete = new double[testNames.size()];
		ArrayNode meansNode = mapper.createArrayNode();
		ArrayNode meansCompletedNode = mapper.createArrayNode();
		ArrayNode percentCompleteNode = mapper.createArrayNode();
		for(int i = 0; i < count.length; i++) {
			double average = -1;
			if(count[i] > 0) {
				average = sum[i] / (double)count[i];
				percentComplete[i] = countCompleted[i] / (double)count[i];
			}
			meansNode.add(average);
			percentCompleteNode.add(percentComplete[i]);
			average = -1;
			if(countCompleted[i] > 0) {
				average = sumCompleted[i] / (double)countCompleted[i];
			}
			meansCompletedNode.add(average);
		}
		root.set("testMeans", meansNode);
		root.set("testMeansCompleted", meansCompletedNode);
		root.set("testPercentComplete", percentCompleteNode);
		
		return root.toString();
	}
	
	/**
	 * Finds how many attempts it has taken for a student to pass each unit test
	 * case.
	 * 
	 * @param results
	 *            the set of results to look through
	 * @param testNames
	 *            the list of valid test names
	 * @return an array of attempts for each test case before passing. 0 means
	 *         they didn't pass at all; -1 means they didn't attempt it at all.
	 *         attempts are assumed to be stored in order of test name order to
	 *         reduce resulting response size.
	 */
	private int[] getAttemptsUntilCorrect(List<AssessmentResult> results, List<String> testNames) {
		int done = 0;
		LinkedHashMap<String, Integer> attempts = new LinkedHashMap<>();
		for(String testName : testNames) {
			attempts.put(testName, -1);
		}
		if(results != null) {
			for(int i = 0; i < results.size(); i++) {
				// already seen every test passed
				if(done == testNames.size()) {
					break;
				}
				
				int attempt = i+1;
				AssessmentResult result = results.get(i);
				for(UnitTestResult utResult : result.getUnitTests()) {
					List<UnitTestCaseResult> tcResults = utResult.getTestCases();
					if(tcResults != null) {
						for(UnitTestCaseResult tcResult : tcResults) {
							String testName = tcResult.getTestName();
							Integer current = attempts.get(testName);
							if(current == null || current > 0) {
								continue;
							}
							if(tcResult.isPass()) {
								attempts.put(testName, new Integer(attempt));
								done++;
							} else if(current != 0) {
								attempts.put(testName, new Integer(0));
							}
						}
					}
				}
			}
		}
		
		int[] attemptArr = new int[testNames.size()];
		int i = 0;
		for(Map.Entry<String, Integer> entry : attempts.entrySet()) {
			attemptArr[i++] = entry.getValue();
		}
		return attemptArr;
	}
}
