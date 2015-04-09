/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.PASTAPlayer;
import pasta.domain.PASTAUser;
import pasta.domain.players.PlayerHistory;
import pasta.domain.players.PlayerResult;
import pasta.domain.result.CompetitionMarks;
import pasta.domain.result.CompetitionResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.WeightedUnitTest;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.scheduler.CompetitionJob;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.AssessmentJob;
import pasta.testing.AntJob;
import pasta.testing.AntResults;
import pasta.testing.ArenaCompetitionRunner;
import pasta.testing.GenericScriptRunner;
import pasta.testing.JUnitTestRunner;
import pasta.testing.options.ScriptOptions;
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

	private AssessmentDAO assDao = ProjectProperties.getInstance()
			.getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance()
			.getResultDAO();

	private ExecutionScheduler scheduler;

	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
	}

	@Autowired
	private ApplicationContext context;

	public static final Logger logger = Logger
			.getLogger(ExecutionManager.class);

	/**
	 * Execute a calculated competition
	 * <p>
	 * <ol>
	 * 	<li>Create a folder where this will run $location$/competitions/$compShortName$/competition/$date$</li>
	 * 	<li>Copy competition code</li>
	 * 	<li>Run ant: 
	 * 		<ol>
	 * 			<li>build</li>
	 * 			<li>compete</li>
	 * 			<li>mark</li>
	 * 			<li>clean</li>
	 * 		</ol>
	 * 	</li>
	 * 	<li>Deletes all except marks.csv and results.csv</li>
	 * 	<li>Delete the job from the queue</li>
	 * 	<li>Update the cached results</li>
	 * 	<li>If the competition is still live, add a new job to the queue to run the competition</li>
	 * </ol>
	 * 
	 * @param job the calculated competition job
	 */
	private void executeCalculatedCompetitionJob(CompetitionJob job) {
		Competition comp = job.getCompetition();
		if(comp == null || !ProjectProperties.getInstance().getCompetitionDAO().isLive(comp)) {
			scheduler.delete(job);
			return;
		}
		
		logger.info("Executing competition " + comp.getName());
		
		try {
			// create folder
			File compDir = new File(ProjectProperties.getInstance().getSandboxLocation() + "competition"
					+ File.separator + comp.getFileAppropriateName());
			File thisRunDir = new File(compDir, PASTAUtil.formatDate(job.getRunDate()));
			thisRunDir.mkdirs();
			
			// copy each submission to ./submissions/<assignment>/<user>/
			File allSubmissionsDir = new File(thisRunDir, "submissions");
			for (Assessment linkedAssessment : ProjectProperties.getInstance().getCompetitionDAO()
					.getAssessmentsUsingCompetition(comp)) {
				for (PASTAUser user : ProjectProperties.getInstance().getUserDAO().getStudentList()) {
					File submissionDir = ProjectProperties.getInstance().getResultDAO()
							.getLastestSubmission(user, linkedAssessment);
					if (submissionDir == null) {
						continue;
					}
					File newLocation = new File(allSubmissionsDir, user.getUsername());
					if (newLocation.exists()) {
						logger.warn("User " + user.getUsername()
								+ " has submitted to multiple assessments running competition "
								+ comp.getName() + ". Assessment " + linkedAssessment.getName()
								+ " submission will take precedence.");
					}
					FileUtils.copyDirectory(submissionDir, newLocation);
				}
			}
		
			GenericScriptRunner runner = new GenericScriptRunner();
			ScriptOptions runOptions = new ScriptOptions();
			// TODO make generic - test on unix machine
			runOptions.scriptFilename = "calculate.sh";
			runOptions.timeout = 180000;
			runner.setRunScript(runOptions);
			
			AntJob antJob = new AntJob(thisRunDir, runner, "build", "execute", "clean");
			antJob.addDependency("execute", "build");
			
			antJob.addSetupTask(new DirectoryCopyTask(new File(comp.getFileLocation(), "code"), new File(thisRunDir, "src")));

			// clean up
			boolean worked = true;
			try {
				FileUtils.copyFile(
						new File(thisRunDir, CompetitionResult.RESULT_FILENAME),
						new File(compDir, CompetitionResult.RESULT_FILENAME));
			} catch (IOException e1) {
				logger.error("Execution of competition " + comp.getName()
						+ " did not create a " + CompetitionResult.RESULT_FILENAME + " file");
				worked = false;
			}
			
			try {
				FileUtils.copyFile(
						new File(thisRunDir, CompetitionMarks.MARKS_FILENAME),
						new File(compDir, CompetitionMarks.MARKS_FILENAME));
			} catch (IOException e1) {
				logger.error("Execution of competition " + comp.getName()
						+ " did not create a " + CompetitionMarks.MARKS_FILENAME + " file");
				worked = false;
			}

			if (worked) {
				CompetitionResult compResult = job.getResult();
				compResult.setRunDate(new Date());
				resultDAO.loadCompetitionResults(compResult, new File(compDir, CompetitionResult.RESULT_FILENAME));
				resultDAO.saveOrUpdate(compResult);
				
				CompetitionMarks compMarks = job.getMarks();
				compMarks.setRunDate(new Date());
				resultDAO.loadCompetitionMarks(compMarks, new File(compDir, CompetitionMarks.MARKS_FILENAME));
				resultDAO.saveOrUpdate(compMarks);

			}
			
			// delete everything
			FileUtils.deleteDirectory(thisRunDir);
			
			// check if still live and ready
			if (ProjectProperties.getInstance().getCompetitionDAO().isLive(comp)) {
				scheduler.scheduleJob(comp, comp.getNextRunDate());
			}
		} catch (Exception e) {
			logger.error("Error executing competition " + comp.getName(), e);
		}
		
		// delete job
		scheduler.delete(job);
	}

	/**
	 * Execute an arena competition
	 * <p>
	 * <ol>
	 * 	<li>Create a folder where this will run $compLocation$/arenas/$arenaName$/$date$</li>
	 * 	<li>Copy player code to $compLocation$/arenas/$arenaName$/$date$/player/$playerName$, look for active player, if no active players found, go for the latest retired player</li>
	 * 	<li>Copy competition code</li>
	 * 	<li>Run ant: 
	 * 		<ol>
	 * 			<li>build</li>
	 * 			<li>compete</li>
	 * 			<li>mark</li>
	 * 			<li>clean</li>
	 * 		</ol>
	 * 	</li>
	 * 	<li>Deletes all except marks.csv and results.csv</li>
	 * 	<li>Delete the job from the queue</li>
	 * 	<li>Update the cached results</li>
	 * 	<li>If the competition is still live and the arena is a repeating arena, add a new job to the queue to run the competition</li>
	 * </ol>
	 * 
	 * @param job the arena competition job
	 */
	private void executeArenaJob(CompetitionJob job) {
		Arena arena = job.getArena();
		if(arena == null) {
			scheduler.delete(job);
			return;
		}
		Competition comp = arena.getCompetition();
		if (comp == null) {
			logger.error("Missing link to competition in arena " + arena.getName());
			scheduler.delete(job);
			return;
		}
		
		if(!ProjectProperties.getInstance().getCompetitionDAO().isLive(comp)) {
			scheduler.delete(job);
			return;
		}
		
		logger.info("Executing competition " + comp.getName() + " arena " + arena.getName());
		Map<String, String> playerLocations = new TreeMap<String, String>();
		
		File arenaDir = new File(ProjectProperties.getInstance().getSandboxLocation() + "competitions/"
				+ comp.getFileAppropriateName() + "/arenas/" + arena.getFileAppropriateName());
		File thisRunDir = new File(arenaDir, PASTAUtil.formatDate(job.getRunDate()));
		
		for(PASTAPlayer player : arena.getPlayers()) {
			String playerLocation = null;
			PlayerHistory history = ProjectProperties.getInstance().getPlayerDAO().getPlayerHistory(player.getUsername(), comp.getId(), player.getPlayerName());
			if(history.getActivePlayer() != null) {
				playerLocation = comp.getFileLocation() + "players/" + player.getUsername() + "/"
						+ player.getPlayerName() + "/active/code";
			} else {
				if(history.getRetiredPlayers() == null || history.getRetiredPlayers().isEmpty()) {
					continue;
				}
				List<PlayerResult> retired = history.getRetiredPlayers();
				playerLocation = comp.getFileLocation() + "players/" + player.getUsername() + "/"
						+ player.getPlayerName() + "/retired/"
						+ retired.get(retired.size() - 1).getFirstUploaded() + "/code";
				logger.warn("DEBUGGING: MAKE SURE THIS IS MOST RECENT RETIRED PLAYER: " + playerLocation);
			}
			
			if (playerLocation != null) {
				// copy player across
				playerLocations.put(player.getUsername() + "." + player.getPlayerName(), playerLocation);
				
				File playerDir = new File(thisRunDir, "players/" + player.getUsername());
				
				// make folder
				playerDir.mkdirs();
				
				// copy code across
				try {
					FileUtils.copyDirectory(new File(playerLocation), playerDir);
				} catch (IOException e) {
					continue;
				}
			}
		}

		// copy across results if they are there
		try {
			FileUtils.copyFile(
					new File(arenaDir, CompetitionResult.RESULT_FILENAME),
					new File(thisRunDir, CompetitionResult.RESULT_FILENAME));
		} catch (IOException e1) {
			// do nothing
		}

		// copy across base code
		try {
			FileUtils.copyDirectory(new File(comp.getFileLocation() + "/code/"), thisRunDir);

			ArenaCompetitionRunner runner = new ArenaCompetitionRunner();
			runner.setCompetitionCodeLocation("src");
			//TODO make dynamic
			runner.setMainClassname("tournament.Tournament");
			runner.setRepeats(arena.isRepeatable());
			
			AntJob antJob = new AntJob(thisRunDir, runner, "build", "compete", "mark", "clean");
			antJob.addDependency("compete", "build");
			antJob.addDependency("mark", "compete");
			
			antJob.run();
			
			AntResults results = antJob.getResults();
			logger.warn(results.getFullOutput());

			// remove job from queue
			scheduler.delete(job);

			if (arena.isRepeatable()) {
				scheduler.scheduleJob(comp, arena, arena.getNextRunDate());
			} else {
				comp.completeArena(arena);
			}

			// clean up
			boolean worked = true;
			try {
				FileUtils.copyFile(
						new File(thisRunDir, CompetitionResult.RESULT_FILENAME),
						new File(arenaDir, CompetitionResult.RESULT_FILENAME));
			} catch (IOException e1) {
				logger.error("Execution of arena " + arena.getName()
						+ " of competition " + comp.getName()
						+ " did not create a " + CompetitionResult.RESULT_FILENAME + " file");
				worked = false;
			}
			
			try {
				FileUtils.copyFile(
						new File(thisRunDir, CompetitionMarks.MARKS_FILENAME),
						new File(arenaDir, CompetitionMarks.MARKS_FILENAME));
			} catch (IOException e1) {
				logger.error("Execution of arena " + arena.getName()
						+ " of competition " + comp.getName()
						+ " did not create a " + CompetitionMarks.MARKS_FILENAME + " file");
				worked = false;
			}

			if (worked) {
				CompetitionResult compResult = job.getResult();
				compResult.setRunDate(new Date());
				resultDAO.loadCompetitionResults(compResult, new File(arenaDir, CompetitionResult.RESULT_FILENAME));
				resultDAO.saveOrUpdate(compResult);
				
				CompetitionMarks compMarks = job.getMarks();
				compMarks.setRunDate(new Date());
				resultDAO.loadCompetitionMarks(compMarks, new File(arenaDir, CompetitionMarks.MARKS_FILENAME));
				resultDAO.saveOrUpdate(compMarks);
				
				
				// TODO send updates to player results objects 
				// this is what it used to do:
//				Scanner resultsIn = new Scanner(new File(arenaDir, CompetitionResult.RESULT_FILENAME));
//				while (resultsIn.hasNextLine()) {
//					String[] data = resultsIn.nextLine().split(",");
//					// ensure a player with the name exists.
//					String[] userData = data[0].split("\\.");
//					if(arena.hasUser(userData[0])) {
//						String username = userData[0];
//						String playerName = userData[userData.length - 1];
//						
//						// write out to the correct location.
//						String location = playerLocations.get(userData[0]
//								+ "." + userData[userData.length - 1])
//								+ ending;
//
//						if (location != null) {
//
//							try {
//								PrintWriter out = new PrintWriter(
//										new BufferedWriter(new FileWriter(
//												location, true)));
//								out.println(data[1] + "," + data[2] + ","
//										+ data[3] + "," + data[4] + ","
//										+ data[5]);
//								out.close();
//							} catch (IOException e1) {
//								// do nothing
//							}
//						}
//					}
//				}
//				resultsIn.close();
				

				// delete everything
				FileUtils.deleteDirectory(thisRunDir);
			}

		} catch (IOException e) {
			logger.error("Error executing arena job.", e);
		}
	}
	
	private void executeNormalJob(AssessmentJob job) {
		// TODO: remove /submission
		String submissionHome = ProjectProperties.getInstance().getSubmissionsLocation() + job.getUsername() + "/assessments/"
				+ job.getAssessmentId() + "/" + PASTAUtil.formatDate(job.getRunDate()) + "/submission";
		File submissionLoc = new File(submissionHome);
		
		String sandboxHome = ProjectProperties.getInstance().getSandboxLocation() + job.getUsername() + "/"
				+ job.getAssessmentId() + "/" + PASTAUtil.formatDate(job.getRunDate());
		File sandboxLoc = new File(sandboxHome);	
		
		Assessment currAssessment = assDao.getAssessment(job.getAssessmentId());
		
		try {
			logger.info("Running unit test " + currAssessment.getName()
					+ " for " + job.getUsername() + " with ExecutionScheduler - " + this);
			
			if (sandboxLoc.exists()) {
				FileUtils.deleteDirectory(sandboxLoc);
			}
			
			for (WeightedUnitTest test : currAssessment.getAllUnitTests()) {
				try {
					String mainClass = test.getTest().getMainClassName();
					if(mainClass == null || mainClass.isEmpty()) {
						logger.warn("No main test class for test " + test.getTest().getName());
						continue;
					}
					
					// create folder
					File testLoc = new File(sandboxLoc, test.getTest().getFileAppropriateName());
					testLoc.mkdirs();
					
					JUnitTestRunner runner = new JUnitTestRunner();
					runner.setMainTestClassname(test.getTest().getMainClassName());
					
					AntJob antJob = new AntJob(testLoc, runner, "build", "test", "clean");
					antJob.addDependency("test", "build");
					
					antJob.addSetupTask(new DirectoryCopyTask(submissionLoc, testLoc));
					antJob.addSetupTask(new DirectoryCopyTask(new File(test.getTest().getFileLocation() + "/code/"), testLoc));
					
					antJob.run();
					
					AntResults results = antJob.getResults();
					
					Scanner scn = new Scanner(results.getOutput("build"));
					StringBuilder files = new StringBuilder();
					StringBuilder compErrors = new StringBuilder();
					String line = "";
					if(scn.hasNext()) {
						while(scn.hasNextLine()) {
							line = scn.nextLine();
							if(line.contains("Files to be compiled")) {
								break;
							}
						}
					}
					if(scn.hasNextLine()) {
						while(scn.hasNextLine()) {
							line = scn.nextLine();
							if(line.contains("error")) {
								break;
							}
							files.append(line.replaceFirst("\\s*\\[javac\\]", "").trim()).append('\n');
						}
						// Chop off the trailing "\n"
						files.replace(files.length()-1, files.length(), "");
					}
					if(scn.hasNextLine()) {
						compErrors.append(line.replaceFirst("\\s*\\[javac\\]", "")).append('\n');
						while(scn.hasNextLine()) {
							line = scn.nextLine();
							compErrors.append(line.replaceFirst("\\s*\\[javac\\]", "")).append('\n');
						}
						// Chop off the trailing "\n"
						compErrors.replace(compErrors.length()-1, compErrors.length(), "");
					}
					scn.close();
					
					// Check if this test has already been run
					UnitTestResult currentResult = null;
					for(UnitTestResult existingResult : job.getResults().getUnitTests()) {
						if(existingResult == null) {
							continue;
						}
						if(existingResult.getTest().getId() == test.getTest().getId()) {
							currentResult = existingResult;
							break;
						}
					}
					
					// Get results from ant output
					UnitTestResult utResults = ProjectProperties.getInstance().getResultDAO()
							.getUnitTestResultFromDisk(testLoc.getAbsolutePath());
					if(utResults == null) {
						utResults = new UnitTestResult();
						utResults.setTest(test.getTest());
					}
					
					// If the test has been done before, update results, otherwise save new results
					if(currentResult == null) {
						currentResult = utResults;
						job.getResults().addUnitTest(currentResult);
						currentResult.setTest(test.getTest());
					} else {
						currentResult.getTestCases().clear();
						currentResult.getTestCases().addAll(utResults.getTestCases());
					}
					
					currentResult.setFilesCompiled(files.toString());
					if(!results.isSuccess("build")) {
						currentResult.setCompileErrors(compErrors.toString().replaceAll(Matcher.quoteReplacement(testLoc.getAbsolutePath()), ""));
					}
					
					currentResult.setRuntimeError(results.hasRun("test") && !results.isSuccess("test"));
					currentResult.setCleanError(!results.isSuccess("clean"));
					currentResult.setRuntimeOutput(results.getFullOutput());
					
					ProjectProperties.getInstance().getResultDAO().update(job.getResults());
				} catch(Exception e2) {
					logger.error(
							"Error executing test " + test.getTest().getName() + " for " + job.getUsername()
									+ " - " + job.getAssessmentId(), e2);
				}
			}
			
			FileUtils.deleteDirectory(sandboxLoc);
		}
		catch (Exception e) {
			logger.error("Execution error for " + job.getUsername() + " - "
					+ job.getAssessmentId(), e);
		}
		
		scheduler.delete(job);
	}

	/**
	 * Get outstanding non competition jobs
	 * <p>
	 * This method runs on a fixed delay (currently 10 sec). The system waits
	 * x ms between the end of the method and calling it again.
	 * 
	 * This queries the database for outstanding jobs, get the list, process them
	 * all, then check the database again. When a check to the database is empty,
	 * the system goes back to waiting.
	 */
	@Scheduled(fixedDelay = 10000)
	public void executeRemainingAssessmentJobs() {
		List<AssessmentJob> outstandingJobs = scheduler.getOutstandingAssessmentJobs();
		while (outstandingJobs != null && !outstandingJobs.isEmpty()) {
			for (AssessmentJob job : outstandingJobs) {
				executeNormalJob(job);
			}
			outstandingJobs = scheduler.getOutstandingAssessmentJobs();
		}
	}

	/**
	 * Get outstanding competition jobs
	 * <p>
	 * This method runs on a fixed delay (currently 10 sec). The system waits
	 * x ms between the end of the method and calling it again.
	 * 
	 * This queries the database for outstanding jobs, get the list, process them
	 * all, then check the database again. When a check to the database is empty,
	 * the system goes back to waiting.
	 */
	@Scheduled(fixedDelay = 10000)
	public void executeRemainingCompetitionJobs() {
		List<CompetitionJob> outstandingJobs = scheduler.getOutstandingCompetitionJobs();
		while (outstandingJobs != null && !outstandingJobs.isEmpty()) {
			for (CompetitionJob job : outstandingJobs) {
				executeCalculatedCompetitionJob(job);
			}
			outstandingJobs = scheduler.getOutstandingCompetitionJobs();
		}
	}
	
	/**
	 * Get outstanding competition jobs
	 * <p>
	 * This method runs on a fixed delay (currently 10 sec). The system waits
	 * x ms between the end of the method and calling it again.
	 * 
	 * This queries the database for outstanding jobs, get the list, process them
	 * all, then check the database again. When a check to the database is empty,
	 * the system goes back to waiting.
	 */
	@Scheduled(fixedDelay = 10000)
	public void executeRemainingArenaJobs() {
		List<CompetitionJob> outstandingJobs = scheduler.getOutstandingArenaJobs();
		while (outstandingJobs != null && !outstandingJobs.isEmpty()) {
			for (CompetitionJob job : outstandingJobs) {
				executeArenaJob(job);
			}
			outstandingJobs = scheduler.getOutstandingArenaJobs();
		}
	}

}
