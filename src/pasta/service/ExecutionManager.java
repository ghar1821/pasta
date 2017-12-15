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

package pasta.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.docker.ExecutionContainer;
import pasta.docker.LanguageManager;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.AssessmentResultSummary;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.user.PASTAUser;
import pasta.repository.AssessmentDAO;
import pasta.scheduler.AssessmentJob;
import pasta.scheduler.AssessmentJobExecutor;
import pasta.scheduler.ExecutionScheduler;
import pasta.testing.task.DirectoryCopyTask;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Execution Manager
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-31
 *
 */
@Service("executionManager")
@Repository
public class ExecutionManager {

	@Autowired private AssessmentDAO assDao;
	
	@Autowired private UnitTestManager unitTestManager;
	@Autowired private ResultManager resultManager;
	@Autowired private GroupManager groupManager;

	@Autowired private ExecutionScheduler scheduler;
	@Autowired private AssessmentJobExecutor executor;
	
	public static final Logger logger = Logger
			.getLogger(ExecutionManager.class);

	public void executeNormalJob(AssessmentJob job) {
		PASTAUser user = job.getUser();
		boolean userIsGroup = user.isGroup();
		
		Assessment assessment = assDao.getAssessment(job.getAssessmentId());
		
		logger.info("Running " + assessment.getName() + " unit tests for " + user.getUsername());
		
		String submissionLabel = user.getUsername() + "_" + job.getAssessmentId() + 
				"_" + PASTAUtil.formatDate(job.getRunDate());
		
		File sandboxRoot = new File(ProjectProperties.getInstance().getSandboxLocation() + submissionLabel);	
		
		// Set up location where test will be run
		try {
			if (sandboxRoot.exists()) {
				logger.debug("Deleting existing sandbox location " + sandboxRoot);
				FileUtils.deleteDirectory(sandboxRoot);
			}
		} catch (IOException e) {
			logger.error("Could not delete existing test.", e);
			return;
		}
		logger.debug("Making directories to " + sandboxRoot);
		sandboxRoot.mkdirs();
		
		File submissionLoc = job.getSubmissionRoot();
		
		Set<Long> validTestIds = new HashSet<>();
		for(WeightedUnitTest weightedTest : assessment.getAllUnitTests()) {
			if(weightedTest.isGroupWork() != userIsGroup) {
				continue;
			}
			validTestIds.add(weightedTest.getTest().getId());
		}
		
		for(WeightedUnitTest weightedTest : assessment.getAllUnitTests()) {
			if(weightedTest.isGroupWork() != userIsGroup) {
				continue;
			}
			
			UnitTest test = weightedTest.getTest();
			File sandboxTop = new File(sandboxRoot, test.getFileAppropriateName());
			File sandboxSrc = new File(sandboxTop, "src/");
			File sandboxOut = new File(sandboxTop, "out/");
			sandboxSrc.mkdirs();
			sandboxOut.mkdirs();
			
			String executionLabel = submissionLabel + "_" + test.getFileAppropriateName();
			ExecutionContainer container = new ExecutionContainer(executionLabel, sandboxSrc, sandboxOut);
			
			// Check if test has been run before, and remove previous results if so
			List<UnitTestResult> staleResults = new LinkedList<>();
			for(UnitTestResult existingResult : job.getResults().getUnitTests()) {
				if(existingResult == null) {
					continue;
				}
				if(existingResult.getTest() == null || 
						existingResult.getTest().getId() == test.getId() ||
						!validTestIds.contains(existingResult.getTest().getId())) {
					staleResults.add(existingResult);
				}
			}
			for(UnitTestResult staleResult : staleResults) {
				staleResult.clearValidationErrors();
				staleResult.removeAllTestCases();
				job.getResults().removeUnitTest(staleResult);
			}
			
			// Create the new results object that will be used
			UnitTestResult utResults = new UnitTestResult();
			utResults.setWeightedUnitTest(weightedTest);
			job.getResults().addUnitTest(utResults);
			
			// Code we are interested in testing
			File importantCode = test.getSubmissionCodeLocation(submissionLoc);
			logger.debug("Copying " + importantCode + " to " + sandboxSrc);
			new DirectoryCopyTask(importantCode, sandboxSrc).go();
			
			List<String> context = null;
			if(assessment.isAllowed(LanguageManager.getInstance().getLanguage("java"))) {
				// Get a list of files submitted for tracking later
				context = new LinkedList<String>();
				if(assessment.getShortSolutionName() != null && !assessment.getShortSolutionName().isEmpty()) {
					// Add solutionName.java just in case this is a Java 
					// submission, as that will be the most important file
					String shortName = assessment.getShortSolutionName();
					context.add(shortName + "." + LanguageManager.getInstance().getLanguage("java").getExtensions().get(0));
				}
				context.addAll(Arrays.asList(PASTAUtil.listDirectoryContents(importantCode, true)));
			}
			
			// Run any black box tests
			if(test.hasBlackBoxTests()) {
				String solutionName = assessment.getSolutionName();
				if(solutionName == null || solutionName.isEmpty()) {
					utResults.addValidationError("Assessment setup error: contact administrator.");
					logger.error("No solution name set for " + assessment.getName());
					continue;
				}
				unitTestManager.runBlackBoxTests(test, solutionName, utResults, importantCode, context, container);
			}
			
			String mainClass = test.getMainClassName();
			if(test.hasCode() && mainClass != null && !mainClass.isEmpty()) {
				unitTestManager.runJUnitTests(test, utResults, mainClass, context, container);
			}
		}
		
		logger.debug("Deleting final sandbox location " + sandboxRoot);
		try {
			FileUtils.deleteDirectory(sandboxRoot);
		} catch (IOException e) {
			logger.error("Error deleting sandbox test at " + sandboxRoot);
		}
		// Update the assessment summaries for user/group members if this is the latest submission
		AssessmentResult latest = resultManager.getLatestResultIncludingGroup(
				user,
				job.getResults().getAssessment().getId());
		if (latest.getSubmissionDate().equals(job.getResults().getSubmissionDate())) {
			resultManager.saveOrUpdate(new AssessmentResultSummary(
					user,
					job.getResults().getAssessment(),
					job.getResults().getPercentage()));

			if (job.getUser().isGroup()) {
				for (PASTAUser groupMember : groupManager.getGroup(user.getId()).getMembers()) {
					resultManager.saveOrUpdate(new AssessmentResultSummary(
							groupMember,
							job.getResults().getAssessment(),
							job.getResults().getPercentage()));
				}
			}
		}
	}

	/**
	 * Get outstanding assessment jobs
	 * <p>
	 * This method runs on a fixed delay (currently 10 sec). The system waits
	 * x ms between the end of the method and calling it again.
	 * 
	 * This queries the database for outstanding jobs, get the list, process them
	 * all, then check the database again. When a check to the database is empty,
	 * the system goes back to waiting.
	 */
	@Scheduled(fixedDelay = 5000)
	public void executeRemainingAssessmentJobs() {
		synchronized (scheduler) {
			List<AssessmentJob> outstandingJobs = scheduler.getOutstandingAssessmentJobs();
			for(AssessmentJob job : outstandingJobs) {
				executor.offer(job);
			}
		}
	}
	
	@Scheduled(fixedDelay = 3600000)
	public void fixWaitingJobs() {
		synchronized (scheduler) {
			List<AssessmentResult> waitingResults = resultManager.getWaitingResults();
			List<AssessmentJob> outstandingJobs = scheduler.getOutstandingAssessmentJobs();
			HashSet<Long> resultsInQueue = new HashSet<Long>();
			for(AssessmentJob job : outstandingJobs) {
				resultsInQueue.add(job.getResults().getId());
			}
			for(AssessmentResult waiting : waitingResults) {
				if(!resultsInQueue.contains(waiting.getId())) {
					logger.info("Scheduled Task: Found test that should be in the job queue (#" + waiting.getId() + ")");
					waiting.setWaitingToRun(false);
					scheduler.scheduleJob(waiting.getUser(), waiting.getAssessment().getId(), waiting, waiting.getSubmissionDate());
				}
			}
		}
	}
	
	public void forceSubmissionRefresh() {
		synchronized (scheduler) {
			executor.clearAllTasks();
		}
	}
	
	public List<String> getExecutingTaskDetails() {
		List<String> results = new LinkedList<String>();
		int pos = 1;
		for(AssessmentJob job : scheduler.getOutstandingAssessmentJobs()) {
			String details = pos++ + ": " + job.toString();
			results.add(details);
		}
		return results;
	}
}
