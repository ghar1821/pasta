/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.service.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.result.AssessmentResult;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.user.PASTAUser;
import pasta.repository.ResultDAO;
import pasta.service.UserManager;

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

	@Autowired
	private ResultDAO resultDAO;
	
	@Autowired
	private UserManager userManager;
	
	public ObjectNode getAllTestsSummaryJSON(Assessment assessment, PASTAUser user) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = mapper.createObjectNode();
		
		if(assessment.getAllUnitTests().isEmpty()) {
			return root;
		}
		
		Collection<PASTAUser> students = userManager.getStudentList();
		Map<PASTAUser, List<AssessmentResult>> allResults = new LinkedHashMap<>();
		for(PASTAUser student : students) {
			List<AssessmentResult> studentResults = resultDAO.getAllResults(student, assessment.getId(), false, false);
			allResults.put(student, studentResults);
		}
		
		List<String> testNames = assessment.getAllTestNames();
		
		ArrayNode nameIndexNode = mapper.createArrayNode();
		for(String testName : testNames) {
			nameIndexNode.add(testName);
		}
		root.set("testNames", nameIndexNode);
		
//		int[] sum = new int[testNames.size()];
//		int[] count = new int[testNames.size()];
//		int[] sumCompleted = new int[testNames.size()];
//		int[] countCompleted = new int[testNames.size()];
		
		int testCount = testNames.size();
		AttemptCountSummary mainSummary = new AttemptCountSummary("", "", testCount);
		Map<String, AttemptCountSummary> allSummaries = new HashMap<>();
		
		ArrayNode studentResultsNode = mapper.createArrayNode();
		for(Map.Entry<PASTAUser, List<AssessmentResult>> studentEntry : allResults.entrySet()) {
			ObjectNode studentNode = mapper.createObjectNode();
			PASTAUser student = studentEntry.getKey();
			
			String stream = Optional.ofNullable(student.getStream()).orElse("");
			String tutorial = Optional.ofNullable(student.getTutorial()).orElse("");
			
			AttemptCountSummary streamSummary = null;
			if(!stream.isEmpty()) {
				streamSummary = allSummaries.get(stream);
				if(streamSummary == null) {
					streamSummary = new AttemptCountSummary(stream, "", testCount);
					allSummaries.put(stream, streamSummary);
				}
			}
			
			AttemptCountSummary tutorialSummary = null;
			if(!tutorial.isEmpty()) {
				tutorialSummary = allSummaries.get(stream + "." + tutorial);
				if(tutorialSummary == null) {
					tutorialSummary = new AttemptCountSummary(stream, tutorial, testCount);
					allSummaries.put(stream + "." + tutorial, tutorialSummary);
				}
			}
			
			studentNode.put("username", student.getUsername());
			studentNode.put("stream", stream);
			studentNode.put("class", tutorial);
			int[] attempts = getAttemptsUntilCorrect(studentEntry.getValue(), testNames);
			
			int subCount = studentEntry.getValue().size();
			mainSummary.registerAttempts(attempts, subCount);
			if(streamSummary != null) streamSummary.registerAttempts(attempts, subCount);
			if(tutorialSummary != null) tutorialSummary.registerAttempts(attempts, subCount);
			
			ArrayNode attemptsNode = mapper.createArrayNode();
			for(int i = 0; i < attempts.length; i++) {
				String attempt = (attempts[i] < 0 ? "" : (attempts[i] == 0 ? "-" : String.valueOf(attempts[i])));
				attemptsNode.add(attempt);
			}
			studentNode.set("attempts", attemptsNode);
			if(user == null || user.isTutor() || user.equals(student)) {
				studentResultsNode.add(studentNode);
			}
		}
		root.set("studentResults", studentResultsNode);
		
		root.set("mainSummary", mainSummary.getAsNode());
		
		if(user == null || user.isTutor()) {
			ArrayNode summaries = mapper.createArrayNode();
			for(AttemptCountSummary summary : allSummaries.values()) {
				summaries.add(summary.getAsNode());
			}
			root.set("summaries", summaries);
		}
		
		return root;
	}
	
	public String getAllTestsSummary(Assessment assessment, PASTAUser user) {
		return getAllTestsSummaryJSON(assessment, user).toString();
	}
	
	public String getAllTestsSummary(Assessment assessment) {
		return getAllTestsSummaryJSON(assessment, null).toString();
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
	
	private class AttemptCountSummary {
		String stream;
		String tutorial;
		int[] sum;
		int[] count;
		int[] sumCompleted;
		int[] countCompleted;
		
		public AttemptCountSummary(String stream, String tutorial, int testCount) {
			this.stream = stream;
			this.tutorial = tutorial;
			sum = new int[testCount];
			count = new int[testCount];
			sumCompleted = new int[testCount];
			countCompleted = new int[testCount];
		}
		
		void registerAttempts(int[] attempts, int subCount) {
			for(int i = 0; i < attempts.length; i++) {
				if(attempts[i] >= 0) {
					if(attempts[i] == 0) {
						sum[i] += (subCount + 1);
					} else {
						sum[i] += attempts[i];
						sumCompleted[i] += attempts[i];
						countCompleted[i]++;
					}
					count[i]++;
				}
			}
		}
		
		String getStream() {
			return stream.isEmpty() ? "ANY" : stream;
		}
		String getTutorial() {
			return tutorial.isEmpty() ? "ANY" : tutorial;
		}
		
		double[] means() {
			double[] means = new double[sum.length];
			for(int i = 0; i < means.length; i++) {
				means[i] = count[i] > 0 ? sum[i] / (double)count[i] : -1;
			}
			return means;
		}
		
		double[] percentComplete() {
			double[] pc = new double[sum.length];
			for(int i = 0; i < pc.length; i++) {
				pc[i] = count[i] > 0 ? countCompleted[i] / (double)count[i] : -1;
			}
			return pc;
		}
		
		double[] meansComplete() {
			double[] mc = new double[sum.length];
			for(int i = 0; i < mc.length; i++) {
				mc[i] = countCompleted[i] > 0 ? sumCompleted[i] / (double)countCompleted[i] : -1;
			}
			return mc;
		}
		
		ObjectNode getAsNode() {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode node = mapper.createObjectNode();
			ArrayNode meansNode = mapper.createArrayNode();
			for(double m : means()) {
				meansNode.add(m);
			}
			ArrayNode meansCompletedNode = mapper.createArrayNode();
			for(double m : meansComplete()) {
				meansCompletedNode.add(m);
			}
			ArrayNode percentCompleteNode = mapper.createArrayNode();
			for(double m : percentComplete()) {
				percentCompleteNode.add(m);
			}
			node.set("testMeans", meansNode);
			node.set("testMeansCompleted", meansCompletedNode);
			node.set("testPercentComplete", percentCompleteNode);
			node.put("class", getTutorial());
			node.put("stream", getStream());
			return node;
		}
	}

	public CSVReport getAllUnitTestAttemptsReport(int pageSize) {
		List<Object[]> attempts = resultDAO.getAllTestCaseDetails();
		String[] header = {
				"submission_id", "test_case", "result", "test_case_weight", 
		};
		return new CSVReport(header, attempts, pageSize);
	}
	
	public CSVReport getAllSubmissionsReport(int pageSize) {
		List<Object[]> attempts = resultDAO.getAllSubmissionDetails();
		String[] header = {
				"submission_id", "assessment_id", "assessment_name", "assessment_release_date",
				"assessment_due_date", "auto_mark_weighted_percentage", "submission_date",
				"user", "permission_level", "submitted_by", "group_members"
		};
		return new CSVReport(header, attempts, pageSize);
	}
}
