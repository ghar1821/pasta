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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.WeightedUnitTest;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.Job;
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
	private void executeCalculatedCompetitionJob(Job job) {

		Competition comp = assDao.getCompetition(job.getAssessmentName());
		if (comp != null) {

			// if dead, remove from the list and do nothing
			if (!comp.isLive()) {
				scheduler.delete(job);
				return;
			}

			// create folder
			String competitionLocation = ProjectProperties.getInstance()
					.getCompetitionsLocation()
					+ comp.getShortName()
					+ "/competition/"
					+ PASTAUtil.formatDate(job.getRunDate());
			(new File(competitionLocation)).mkdirs();

			// copy across
			try {
				FileUtils.copyDirectory(new File(comp.getFileLocation()
						+ "/code"), new File(competitionLocation));

				// compile
				File buildFile = new File(competitionLocation + "/build.xml");

				ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
				Project project = new Project();

				project.setUserProperty("ant.file", buildFile.getAbsolutePath());
				project.init();

				project.addReference("ant.projectHelper", projectHelper);
				projectHelper.parse(project, buildFile);

				try {
					project.executeTarget("build");
					project.executeTarget("compete");
					project.executeTarget("mark");
					project.executeTarget("clean");
				} catch (BuildException e) {
					// TODO
					logger.error("Could run competition " + comp.getName()
							+ " - " + e);
				}

				// delete everything else
				String[] allFiles = (new File(competitionLocation)).list();
				for (String file : allFiles) {
					File actualFile = new File(competitionLocation + "/" + file);
					if (actualFile.isDirectory()) {
						FileUtils.deleteDirectory(actualFile);
					} else {
						if (!file.equals("marks.csv")
								&& !file.equals("results.csv")) {
							FileUtils.forceDelete(actualFile);
						}
					}
				}
				// delete it
				scheduler.delete(job);

				// update resultDAO
				resultDAO.updateCalculatedCompetitionResults(job
						.getAssessmentName());

				// check if still live and ready
				if (comp.isLive()) {
					scheduler.save(new Job("PASTACompetitionRunner", comp
							.getShortName(), comp.getNextRunDate()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Execute a calculated competition
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
	private void executeArenaCompetitionJob(Job job) {
		Competition comp = assDao.getCompetition(job.getAssessmentName().split(
				"#PASTAArena#")[0]);
		Arena arena = null;
		if (comp != null) {
			arena = comp
					.getArena(job.getAssessmentName().split("#PASTAArena#")[1]);
		}
		if (comp != null && comp.isLive() && arena != null) {

			logger.info("Executing " + job.getAssessmentName());

			Map<String, String> playerLocations = new TreeMap<String, String>();

			// copy across players
			for (Entry<String, Set<String>> players : arena.getPlayers()
					.entrySet()) {
				String username = players.getKey();
				for (String playerName : players.getValue()) {

					String playerLocation = null;

					// check if player is active
					if (new File(ProjectProperties.getInstance()
							.getSubmissionsLocation()
							+ username
							+ "/competitions/"
							+ comp.getShortName()
							+ "/"
							+ playerName + "/active").exists()) {
						// player is active
						playerLocation = ProjectProperties.getInstance()
								.getSubmissionsLocation()
								+ username
								+ "/competitions/"
								+ comp.getShortName()
								+ "/"
								+ playerName
								+ "/active/code";
					} else {
						// player is not
						String[] retiredList = new File(ProjectProperties
								.getInstance().getSubmissionsLocation()
								+ username
								+ "/competitions/"
								+ comp.getShortName()
								+ "/"
								+ playerName
								+ "/retired/").list();

						if (retiredList == null || retiredList.length == 0) {
							break;
						}

						Arrays.sort(retiredList);

						playerLocation = ProjectProperties.getInstance()
								.getSubmissionsLocation()
								+ username
								+ "/competitions/"
								+ comp.getShortName()
								+ "/"
								+ playerName
								+ "/retired/"
								+ retiredList[retiredList.length - 1] + "/code";
					}

					if (playerLocation != null) {
						// copy player across
						playerLocations.put(username + "." + playerName,
								playerLocation);

						// make folder
						(new File(comp.getFileLocation() + "/arenas/"
								+ arena.getName() + "/"
								+ PASTAUtil.formatDate(job.getRunDate())
								+ "/players/" + username)).mkdirs();

						// copy code across
						try {
							FileUtils.copyDirectory(
									new File(playerLocation),
									new File(comp.getFileLocation()
											+ "/arenas/"
											+ arena.getName()
											+ "/"
											+ PASTAUtil.formatDate(job
													.getRunDate())
											+ "/players/" + username + "/"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			// copy across results if they are there
			try {
				FileUtils.copyFile(
						new File(comp.getFileLocation() + "/arenas/"
								+ arena.getName() + "/results.csv"),
						new File(comp.getFileLocation() + "/arenas/"
								+ arena.getName() + "/"
								+ PASTAUtil.formatDate(job.getRunDate())
								+ "/results.csv"));
			} catch (IOException e1) {
				// do nothing
			}

			// copy across base code
			try {
				FileUtils.copyDirectory(
						new File(comp.getFileLocation() + "/code/"),
						new File(comp.getFileLocation() + "/arenas/"
								+ arena.getName() + "/"
								+ PASTAUtil.formatDate(job.getRunDate())));

				// compile
				File buildFile = new File(comp.getFileLocation() + "/arenas/"
						+ arena.getName() + "/"
						+ PASTAUtil.formatDate(job.getRunDate()) + "/build.xml");

				ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
				Project project = new Project();

				project.setUserProperty("ant.file", buildFile.getAbsolutePath());
				project.setBasedir(comp.getFileLocation() + "/arenas/"
						+ arena.getName() + "/"
						+ PASTAUtil.formatDate(job.getRunDate()));
				project.init();

				project.addReference("ant.projectHelper", projectHelper);
				projectHelper.parse(project, buildFile);

				// execute targets
				try {
					logger.info("Building " + job.getAssessmentName());
					project.executeTarget("build");
					logger.info("Competing " + job.getAssessmentName());
					project.executeTarget("compete");
					logger.info("Marking " + job.getAssessmentName());
					project.executeTarget("mark");
					logger.info("Cleaning " + job.getAssessmentName());
					project.executeTarget("clean");
				} catch (BuildException e) {
					logger.error("Could not complete execution of arena "
							+ arena.getName() + " for competition "
							+ comp.getName() + " : " + e);
					PrintStream compileErrors = new PrintStream(
							comp.getFileLocation() + "/arenas/"
									+ arena.getName() + "/compile.errors");
					compileErrors.print(e.toString());
					compileErrors.close();
				}

				// remove job from queue
				scheduler.delete(job);

				if (arena.isRepeatable()) {
					scheduler.save(new Job(job.getUsername(), job
							.getAssessmentName(), arena.getNextRunDate()));
				} else {
					comp.completeArena(arena);
				}

				// clean up
				boolean worked = true;
				try {
					FileUtils.copyFile(
							new File(comp.getFileLocation() + "/arenas/"
									+ arena.getName() + "/"
									+ PASTAUtil.formatDate(job.getRunDate())
									+ "/results.csv"),
							new File(comp.getFileLocation() + "/arenas/"
									+ arena.getName() + "/results.csv"));
				} catch (IOException e1) {
					logger.error("Execution of arena " + arena.getName()
							+ " of competition " + comp.getName()
							+ " did not create a results.csv file");
					worked = false;
				}
				try {
					FileUtils.copyFile(
							new File(comp.getFileLocation() + "/arenas/"
									+ arena.getName() + "/"
									+ PASTAUtil.formatDate(job.getRunDate())
									+ "/marks.csv"),
							new File(comp.getFileLocation() + "/arenas/"
									+ arena.getName() + "/marks.csv"));
				} catch (IOException e1) {
					logger.error("Execution of arena " + arena.getName()
							+ " of competition " + comp.getName()
							+ " did not create a marks.csv file");
					worked = false;
				}

				if (worked) {

					// send updates to players
					Scanner resultsIn = new Scanner(new File(
							comp.getFileLocation() + "/arenas/"
									+ arena.getName() + "/results.csv"));

					String ending = "/../unofficial.stats";
					if (arena.getName().replace(" ", "").toLowerCase()
							.equals("officialarena")) {
						ending = "/../official.stats";
						resultDAO.updateArenaCompetitionResults(job
								.getAssessmentName());
					}

					while (resultsIn.hasNextLine()) {
						String[] data = resultsIn.nextLine().split(",");
						// ensure a player with the name exists.
						String[] userData = data[0].split("\\.");
						if (arena.getPlayers().containsKey(userData[0])) {
							// write out to the correct location.
							String location = playerLocations.get(userData[0]
									+ "." + userData[userData.length - 1])
									+ ending;

							if (location != null) {

								try {
									PrintWriter out = new PrintWriter(
											new BufferedWriter(new FileWriter(
													location, true)));
									out.println(data[1] + "," + data[2] + ","
											+ data[3] + "," + data[4] + ","
											+ data[5]);
									out.close();
								} catch (IOException e1) {
									// do nothing
								}
							}
						}
					}

					resultsIn.close();

					// delete everything else
					String[] allFiles = (new File(comp.getFileLocation()
							+ "/arenas/" + arena.getName() + "/"
							+ PASTAUtil.formatDate(job.getRunDate()))).list();
					for (String file : allFiles) {
						File actualFile = new File(comp.getFileLocation()
								+ "/arenas/" + arena.getName() + "/"
								+ PASTAUtil.formatDate(job.getRunDate()) + "/"
								+ file);
						if (actualFile.isDirectory()) {
							FileUtils.deleteDirectory(actualFile);
						} else {
							if (!file.equals("marks.csv")
									&& !file.equals("results.csv")) {
								FileUtils.forceDelete(actualFile);
							}
						}
					}
				}

			} catch (IOException e) {
				logger.error(e.toString());
			}

		} else {
			scheduler.delete(job);
		}
	}

	/**
	 * Execute a normal job (not competition)
	 * <p>
	 * <ol>
	 * 	<li>Create a folder where this will run $location$/submissions/$username$/assessments/$assessmentName$/$date$/unitTests/$unitTestName$</li>
	 * 	<li>Copy unit test code (if there are multiple unit tests, run them all sequentially)</li>
	 * 	<li>Run ant: 
	 * 		<ol>
	 * 			<li>build</li>
	 * 			<li>test</li>
	 * 			<li>clean</li>
	 * 		</ol>
	 * 	</li>
	 * 	<li>Deletes all except result.xml, compile.errors, run.errors</li>
	 * 	<li>Delete the job from the queue</li>
	 * 	<li>Update the cached results</li>
	 * </ol>
	 * 
	 * @param job the job to run
	 */
	private void executeNormalJob(Job job) {
		// do it
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		String location = ProjectProperties.getInstance().getSubmissionsLocation() + job.getUsername() + "/assessments/"
				+ job.getAssessmentName() + "/" + sdf.format(job.getRunDate())
				+ "/submission";

		Assessment currAssessment = assDao.getAssessment(job
				.getAssessmentName());

		try {

			logger.info("Running unit test " + currAssessment.getName()
					+ " for " + job.getUsername() + " with ExecutionScheduler - " + this);

			String unitTestsLocation = ProjectProperties.getInstance()
					.getSubmissionsLocation()
					+ job.getUsername()
					+ "/assessments/"
					+ job.getAssessmentName()
					+ "/"
					+ sdf.format(job.getRunDate()) + "/unitTests";

			if (new File(unitTestsLocation).exists()) {
				FileUtils.deleteDirectory(new File(unitTestsLocation));
			}

			PrintStream runErrors = null;
			// run unit tests
			for (WeightedUnitTest test : currAssessment.getAllUnitTests()) {
				try {
					// create folder
					(new File(unitTestsLocation + "/"
							+ test.getTest().getShortName())).mkdirs();

					// copy over submission
					FileUtils.copyDirectory(new File(location), new File(
							unitTestsLocation + "/"
									+ test.getTest().getShortName()));
					
					// copy over unit test
					FileUtils.copyDirectory(new File(test.getTest()
							.getFileLocation() + "/code/"), new File(
							unitTestsLocation + "/"
									+ test.getTest().getShortName()));

					// compile
					File buildFile = new File(unitTestsLocation + "/"
							+ test.getTest().getShortName() + "/build.xml");

					ProjectHelper projectHelper = ProjectHelper
							.getProjectHelper();
					Project project = new Project();

					project.setUserProperty("ant.file",
							buildFile.getAbsolutePath());
					project.setBasedir(unitTestsLocation + "/"
							+ test.getTest().getShortName());
					DefaultLogger consoleLogger = new DefaultLogger();
					runErrors = new PrintStream(unitTestsLocation + "/"
							+ test.getTest().getShortName() + "/run.errors");
					consoleLogger.setOutputPrintStream(runErrors);
					consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
					project.addBuildListener(consoleLogger);
					project.init();

					project.addReference("ant.projectHelper", projectHelper);
					projectHelper.parse(project, buildFile);
					
					boolean failedToCompile = false;

					try {
						project.executeTarget("build");
						project.executeTarget("test");
						project.executeTarget("clean");
					} catch (BuildException e) {
						logger.error("Could not compile " + job.getUsername()
								+ " - " + currAssessment.getName() + " - "
								+ test.getTest().getName() + e);
						failedToCompile = true;
					}
					
					runErrors.close();
					
					if(failedToCompile){
						PrintStream compileErrors = new PrintStream(
								unitTestsLocation + "/"
										+ test.getTest().getShortName()
										+ "/compile.errors");
						
						Scanner in = new Scanner (new File(unitTestsLocation + "/"
								+ test.getTest().getShortName() + "/run.errors"));
						
						boolean print = false;
						
						while(in.hasNextLine()){
							String line = in.nextLine();
							
							if(line.contains("[javac] Files to be compiled:")){
								print = true;
							}
							
							if(print && line.contains("[javac]")){
								compileErrors.println(line.replace("[javac]", "").replaceAll(
										".*" + unitTestsLocation + "/"
												+ test.getTest().getShortName() + "/",
										"folder "));
							}
						}
						
						compileErrors.close();
						in.close();
					}

					// delete everything else
					String[] allFiles = (new File(unitTestsLocation + "/"
							+ test.getTest().getShortName())).list();
					for (String file : allFiles) {
						File actualFile = new File(unitTestsLocation + "/"
								+ test.getTest().getShortName() + "/" + file);
						if (actualFile.isDirectory()) {
							FileUtils.deleteDirectory(actualFile);
						} else {
							if (!file.equals("result.xml")
									&& !file.equals("compile.errors")
									&& !file.equals("run.errors")) {
								FileUtils.forceDelete(actualFile);
							}
						}
					}

				} catch (IOException e) {
					logger.error("Unable to compile unit test "
							+ currAssessment.getName() + " for "
							+ job.getUsername()
							+ System.getProperty("line.separator") + e);
				}
			}

			// delete it
			scheduler.delete(job);

			// update resultDAO
			resultDAO.updateUnitTestResults(job.getUsername(), currAssessment,
					job.getRunDate());
		} catch (Exception e) {
			logger.error("Execution error for " + job.getUsername() + " - "
					+ job.getAssessmentName() + "   " + e);
		}
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
		List<Job> outstandingJobs = scheduler.getOutstandingAssessmentJobs();
		while (outstandingJobs != null && !outstandingJobs.isEmpty()) {
			for (Job job : outstandingJobs) {
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
		List<Job> outstandingJobs = scheduler.getOutstandingCompetitionJobs();
		while (outstandingJobs != null && !outstandingJobs.isEmpty()) {
			for (Job job : outstandingJobs) {
				if (job.getAssessmentName().contains("#PASTAArena#")) {
					executeArenaCompetitionJob(job);
				} else {
					executeCalculatedCompetitionJob(job);
				}
			}
			outstandingJobs = scheduler.getOutstandingCompetitionJobs();
		}
	}

}
