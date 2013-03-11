package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

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

import pasta.domain.FileTreeNode;
import pasta.domain.PASTAUser;
import pasta.domain.ReleaseForm;
import pasta.domain.UserPermissionLevel;
import pasta.domain.result.ArenaResult;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.CompetitionResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewCompetition;
import pasta.domain.upload.NewHandMarking;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.repository.AssessmentDAO;
import pasta.repository.LoginDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UserDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.Job;
import pasta.util.ProjectProperties;

@Service("submissionManager")
@Repository
/**
 * Submission amnager.
 * 
 * Manages interaction between controller and data.
 * 
 * @author Alex
 *
 */
public class SubmissionManager {
	
	private AssessmentDAO assDao = new AssessmentDAO();
	private ResultDAO resultDAO = new ResultDAO(assDao);
	
	private ExecutionScheduler scheduler;
	private UserDAO userDao;
	private LoginDAO loginDao;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
		
		// start up competitions
		for(Competition comp: assDao.getCompetitionList()){
			if(comp.isLive()){
				scheduler.save(new Job("PASTACompetitionRunner", comp.getShortName(), comp.getNextRunDate()));
			}
		}
	}
	
	@Autowired
	public void setMyUserDAO(UserDAO myUserDao) {
		this.userDao = myUserDao;
	}
	
	@Autowired
	public void setMyLoginDAO(LoginDAO myLoginDao) {
		this.loginDao = myLoginDao;
		ProjectProperties.getInstance().setDBDao(myLoginDao);
	}

	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(SubmissionManager.class);
	
	public void submit(String username, Submission form) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		Date now = new Date();
		String currDate = sdf.format(now);
		String location = ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/" + username + "/assessments/"
				+ form.getAssessment() + "/" + currDate + "/submission";
		
		Assessment currAssessment = assDao.getAssessment(form.getAssessment());
		boolean compiled = true;

		(new File(location)).mkdirs();
		try {
			form.getFile().transferTo(new File(location+"/"+form.getFile().getOriginalFilename()));
			if(form.getFile().getOriginalFilename().endsWith(".zip")){
				ProjectProperties.extractFolder(location+"/"+form.getFile().getOriginalFilename());
				(new File(location+"/"+form.getFile().getOriginalFilename())).delete();
			}
			
			String unitTestsLocation = ProjectProperties.getInstance().getProjectLocation()
					+ "/submissions/" + username + "/assessments/"
					+ form.getAssessment() + "/" + currDate + "/unitTests";
			// ensure all unit tests compile
			for(WeightedUnitTest test: currAssessment.getUnitTests()){
				try {
					// create folder
					(new File(unitTestsLocation + "/" + test.getTest().getShortName())).mkdirs();

					// copy over unit test
					FileUtils.copyDirectory(new File(test.getTest().getFileLocation()
							+ "/code/"),
							new File(unitTestsLocation + "/" + test.getTest().getShortName()));
					// copy over submission
					FileUtils.copyDirectory(new File(location),
							new File(unitTestsLocation + "/" + test.getTest().getShortName()));
					
					// compile
					File buildFile = new File(unitTestsLocation + "/" + test.getTest().getShortName()
							+ "/build.xml");

					ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
					Project project = new Project();

					project.setUserProperty("ant.file", buildFile.getAbsolutePath());
					DefaultLogger consoleLogger = new DefaultLogger();
					PrintStream runErrors = new PrintStream(
							unitTestsLocation + "/" + test.getTest().getShortName()
							+ "/run.errors");
					consoleLogger.setOutputPrintStream(runErrors);
					consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
					project.addBuildListener(consoleLogger);
					project.init();

					project.addReference("ant.projectHelper", projectHelper);
					projectHelper.parse(project, buildFile);
					
					try {
						project.executeTarget("build");
						project.executeTarget("clean");
					} catch (BuildException e) {
						compiled = false;
						logger.error("Could not compile " + username + " - "
								+ currAssessment.getName() + " - "
								+ test.getTest().getName() + e);
					}

					runErrors.flush();
					runErrors.close();
					
					// scrape compiler errors from run.errors
					try{
						Scanner in = new Scanner (new File(unitTestsLocation + "/" + test.getTest().getShortName()
								+ "/run.errors"));
						boolean containsError = false;
						boolean importantData = false;
						String output = "";
						while(in.hasNextLine()){
							String line = in.nextLine();
							if(line.contains(": error:")){
								containsError = true;
							}
							if(line.contains("[javac] Files to be compiled:")){
								importantData = true;
							}
							if(importantData){
								output += line.replace("[javac]", "").replaceAll(".*unitTests","") + System.getProperty("line.separator");
							}
						}
						in.close();
						
						if(containsError){
							PrintStream compileErrors = new PrintStream(
									unitTestsLocation + "/" + test.getTest().getShortName()
									+ "/compile.errors");
							compileErrors.print(output);
							compileErrors.close();
						}
					}
					catch (Exception e){
						// do nothing
					}
					
					// delete everything else
					String[] allFiles = (new File(unitTestsLocation + "/" + test.getTest().getShortName()))
							.list();
					for (String file : allFiles) {
						File actualFile = new File(unitTestsLocation + "/" + test.getTest().getShortName()
								+ "/" + file);
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
					
					if(compiled){
						FileUtils.forceDelete(new File(unitTestsLocation + "/" + test.getTest().getShortName()
								+ "/compile.errors"));
					}
					
				} catch (IOException e) {
					logger.error("Unable to compile unit test "
							+ currAssessment.getName() + " for " + username
							+ System.getProperty("line.separator") + e);
				}
			}
			
			resultDAO.updateUnitTestResults(username, currAssessment, now);
			
			// add to scheduler
			if(compiled){
				scheduler.save(new Job(username, form.getAssessment(), now));
			}
		} catch (Exception e) {
			logger.error("Submission error for " + username + " - " + form + "   " + e);
		}
	}
	
	private void executeCompetitionJob(Job job){
		
		Competition comp = assDao.getCompetition(job.getAssessmentName());
		if(comp != null){
			
			// if dead, remove from the list and do nothing
			if(!comp.isLive()){
				scheduler.delete(job);
				return;
			}
			
			// create folder
			String competitionLocation = ProjectProperties.getInstance().getProjectLocation()
					+ "/competitions/" + comp.getShortName() + "/competition/" 
					+ ProjectProperties.formatDate(job.getRunDate());
			(new File(competitionLocation)).mkdirs();
			
			// copy across
			try {
				FileUtils.copyDirectory(new File(comp.getFileLocation()+"/code"),
						new File(competitionLocation));
				
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
					if(comp.isCalculated()){
						project.executeTarget("compete");
					}
					project.executeTarget("mark");
					project.executeTarget("clean");
				} catch (BuildException e) {
					// TODO
					logger.error("Could run competition " + comp.getName() + " - "
							+ e);
				}

				// delete everything else
				String[] allFiles = (new File(competitionLocation))
						.list();
				for (String file : allFiles) {
					File actualFile = new File(competitionLocation
							+ "/" + file);
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
				resultDAO.updateCompetitionResults(job.getAssessmentName());
				
				// check if still live and readd
				if(comp.isLive()){
					scheduler.save(new Job("PASTACompetitionRunner", comp.getShortName(), comp.getNextRunDate()));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void executeArenaJob(Job job){
		// TODO
	}
	
	private void executeNormalJob(Job job){
		// do it
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		String location = ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/" + job.getUsername() + "/assessments/"
				+ job.getAssessmentName() + "/" + sdf.format(job.getRunDate()) + "/submission";
		
		Assessment currAssessment = assDao.getAssessment(job.getAssessmentName());

		try {
			
			String unitTestsLocation = ProjectProperties.getInstance().getProjectLocation()
					+ "/submissions/" + job.getUsername() + "/assessments/"
					+ job.getAssessmentName() + "/" + sdf.format(job.getRunDate()) + "/unitTests";
			// run unit tests
			for(WeightedUnitTest test: currAssessment.getUnitTests()){
				try {
					// create folder
					(new File(unitTestsLocation + "/" + test.getTest().getShortName())).mkdirs();

					// copy over unit test
					FileUtils.copyDirectory(new File(test.getTest().getFileLocation()
							+ "/code/"),
							new File(unitTestsLocation + "/" + test.getTest().getShortName()));
					// copy over submission
					FileUtils.copyDirectory(new File(location),
							new File(unitTestsLocation + "/" + test.getTest().getShortName()));
					
					// compile
					File buildFile = new File(unitTestsLocation + "/" + test.getTest().getShortName()
							+ "/build.xml");

					ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
					Project project = new Project();

					project.setUserProperty("ant.file", buildFile.getAbsolutePath());
					project.init();

					project.addReference("ant.projectHelper", projectHelper);
					projectHelper.parse(project, buildFile);
					
					try {
						project.executeTarget("build");
						project.executeTarget("test");
						project.executeTarget("clean");
					} catch (BuildException e) {
						logger.error("Could not compile " + job.getUsername() + " - "
								+ currAssessment.getName() + " - "
								+ test.getTest().getName() + e);
					}

					// delete everything else
					String[] allFiles = (new File(unitTestsLocation + "/" + test.getTest().getShortName()))
							.list();
					for (String file : allFiles) {
						File actualFile = new File(unitTestsLocation + "/" + test.getTest().getShortName()
								+ "/" + file);
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
							+ currAssessment.getName() + " for " + job.getUsername()
							+ System.getProperty("line.separator") + e);
				}
			}
			
			// delete it
			scheduler.delete(job);
			
			// update resultDAO
			resultDAO.updateUnitTestResults(job.getUsername(), currAssessment, job.getRunDate());
		} catch (Exception e) {
			logger.error("Execution error for " + job.getUsername() + " - " + job.getAssessmentName() + "   " + e);
		}
	}
	
	@Scheduled(fixedDelay = 30000)
	public void executeRemainingJobs(){
		List<Job> outstandingJobs = scheduler.getOutstandingJobs();
		while(outstandingJobs != null && !outstandingJobs.isEmpty()){
			for(Job job: outstandingJobs){
				if(job.getUsername().equals("PASTACompetitionRunner")){
					if(job.getAssessmentName().contains("#PASTAArena#")){
						executeArenaJob(job);
					}else{
						executeCompetitionJob(job);
					}
				}
				else{
					executeNormalJob(job);
				}
			}
			outstandingJobs = scheduler.getOutstandingJobs();
		}
		
		logger.info("Finished executing all jobs");
	}

	public UnitTestResult getUnitTestResult(String location) {
		return resultDAO.getUnitTestResult(location);
	}

	// new
	public Collection<UnitTest> getUnitTestList() {
		return assDao.getAllUnitTests().values();
	}

	// new
	public UnitTest getUnitTest(String name) {
		return assDao.getAllUnitTests().get(name);
	}

	// new
	public Collection<Assessment> getAssessmentList() {
		return assDao.getAssessmentList();
	}
	
	// new
	public Collection<HandMarking> getHandMarkingList() {
		return assDao.getHandMarkingList();
	}

	// new
	public HandMarking getHandMarking(String handMarkingName) {
		return assDao.getHandMarking(handMarkingName);
	}
	
	// new
	public Collection<HandMarking> getAllHandMarking() {
		return assDao.getHandMarkingList();
	}

	// new
	public Assessment getAssessment(String assessmentName) {
		return assDao.getAssessment(assessmentName);
	}
	
	// new
	public Collection<AssessmentResult> getAssessmentHistory(String username, String assessmentName){
		return resultDAO.getAssessmentHistory(username, getAssessment(assessmentName));
	}

	public void saveUnitTest(UnitTest thisTest) {
		try {

			// create space on the file system.
			(new File(thisTest.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
			PrintStream out = new PrintStream(thisTest.getFileLocation()
					+ "/unitTestProperties.xml");
			out.print(thisTest);
			out.close();

		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName()
					+ " could not be saved successfully!"
					+ System.getProperty("line.separator") + e);
		}
	}

	// new - unit test is guaranteed to have a unique name
	public void addUnitTest(NewUnitTest newTest) {
		UnitTest thisTest = new UnitTest(newTest.getTestName(), false);

		try {

			// create space on the file system.
			(new File(thisTest.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
			PrintStream out = new PrintStream(thisTest.getFileLocation()
					+ "/unitTestProperties.xml");
			out.print(thisTest);
			out.close();

			// unzip the uploaded code into the code folder. (if exists)
			if (newTest.getFile() != null && !newTest.getFile().isEmpty()) {
				// unpack
				newTest.getFile().transferTo(
						new File(thisTest.getFileLocation() + "/code/"
								+ newTest.getFile().getOriginalFilename()));
				ProjectProperties.extractFolder(thisTest.getFileLocation()
						+ "/code/" + newTest.getFile().getOriginalFilename());
				newTest.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisTest.getFileLocation()
						+ "/code/" + newTest.getFile().getOriginalFilename()));
			}

			FileUtils.deleteDirectory((new File(thisTest.getFileLocation()
					+ "/test/")));

			assDao.addUnitTest(thisTest);
		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName()
					+ " could not be created successfully!"
					+ System.getProperty("line.separator") + e);
		}
	}
	
	// new - unit test is guaranteed to have a unique name
	public void addCompetition(NewCompetition form) {
		Competition thisComp = new Competition();
		thisComp.setName(form.getTestName());
		thisComp.setTested(false);
		thisComp.setFirstStartDate(form.getFirstStartDate());
		thisComp.setFrequency(form.getFrequency());
		if(form.getType().equalsIgnoreCase("arena")){
			thisComp.setArenas(new LinkedList<Arena>());
		}

		try {

			// create space on the file system.
			(new File(thisComp.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
			PrintStream out = new PrintStream(thisComp.getFileLocation()
					+ "/competitionProperties.xml");
			out.print(thisComp);
			out.close();

			// unzip the uploaded code into the code folder. (if exists)
			if (form.getFile() != null && !form.getFile().isEmpty()) {
				// unpack
				form.getFile().transferTo(
						new File(thisComp.getFileLocation() + "/code/"
								+ form.getFile().getOriginalFilename()));
				ProjectProperties.extractFolder(thisComp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename());
				form.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisComp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename()));
			}

			FileUtils.deleteDirectory((new File(thisComp.getFileLocation()
					+ "/test/")));

			assDao.addCompetition(thisComp);
		} catch (Exception e) {
			(new File(thisComp.getFileLocation())).delete();
			logger.error("Competition " + thisComp.getName()
					+ " could not be created successfully!"
					+ System.getProperty("line.separator") + e);
		}
	}
	
	public void updateCompetition(NewCompetition form) {
		Competition thisComp = getCompetition(form.getTestName().replace(" ", ""));
		if (thisComp == null){
			thisComp = new Competition();
		}
		thisComp.setName(form.getTestName());
		try {

			// create space on the file system.
			(new File(thisComp.getFileLocation() + "/code/")).mkdirs();

			// generate competitionProperties
			PrintStream out = new PrintStream(thisComp.getFileLocation()
					+ "/competitionProperties.xml");
			out.print(thisComp);
			out.close();

			// unzip the uploaded code into the code folder. (if exists)
			if (form.getFile() != null && !form.getFile().isEmpty()) {
				// unpack
				form.getFile().transferTo(
						new File(thisComp.getFileLocation() + "/code/"
								+ form.getFile().getOriginalFilename()));
				ProjectProperties.extractFolder(thisComp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename());
				form.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisComp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename()));
			}

			FileUtils.deleteDirectory((new File(thisComp.getFileLocation()
					+ "/test/")));

			getCompetition(thisComp.getShortName()).setTested(false);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			(new File(thisComp.getFileLocation())).delete();
			logger.error("Competition " + thisComp.getName()
					+ " could not be created successfully!"
					+ System.getProperty("line.separator") + sw.toString());
		}
	}
	
	public void addCompetition(Competition form) {
		try {

			// create space on the file system.
			(new File(form.getFileLocation() + "/code/")).mkdirs();

			assDao.addCompetition(form);

			// generate unitTestProperties
			PrintStream out = new PrintStream(form.getFileLocation()
					+ "/competitionProperties.xml");
			out.print(getCompetition(form.getShortName()));
			out.close();
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			(new File(form.getFileLocation())).delete();
			logger.error("Competition " + form.getName()
					+ " could not be updated successfully!"
					+ System.getProperty("line.separator") + pw);
		}
	}

	public void releaseAssessment(String AssessmentName, ReleaseForm released)
	{
		assDao.releaseAssessment(AssessmentName,released);
		
	}
	// new - unit test is guaranteed to have a unique name
	public void removeUnitTest(String testName) {
		assDao.removeUnitTest(testName);
	}

	public void removeAssessment(String assessment) {
		assDao.removeAssessment(assessment);
	}
	
	public void removeCompetition(String competitionName) {
		assDao.removeCompetition(competitionName);
	}

	// new - test submission
	public void testUnitTest(Submission submission, String testName) {
		PrintStream compileErrors = null;
		PrintStream runErrors = null;
		try {
			UnitTest thisTest = getUnitTest(testName);
			// delete old submission if exists
			FileUtils.deleteDirectory(new File(thisTest.getFileLocation()
					+ "/test/"));

			// create folder
			(new File(thisTest.getFileLocation() + "/test/")).mkdirs();
			// extract submission
			submission.getFile().transferTo(
					new File(thisTest.getFileLocation() + "/test/"
							+ submission.getFile().getOriginalFilename()));
			ProjectProperties.extractFolder(thisTest.getFileLocation()
					+ "/test/" + submission.getFile().getOriginalFilename());
			FileUtils.forceDelete(new File(thisTest.getFileLocation()
					+ "/test/" + submission.getFile().getOriginalFilename()));

			// copy over unit test
			FileUtils.copyDirectory(new File(thisTest.getFileLocation()
					+ "/code/"),
					new File(thisTest.getFileLocation() + "/test/"));

			// compile
			File buildFile = new File(thisTest.getFileLocation()
					+ "/test/build.xml");

			ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
			Project project = new Project();

			project.setUserProperty("ant.file", buildFile.getAbsolutePath());
			DefaultLogger consoleLogger = new DefaultLogger();
			 runErrors = new PrintStream(thisTest.getFileLocation()
					  + "/test/run.errors");
			consoleLogger.setOutputPrintStream(runErrors);
			consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
			project.addBuildListener(consoleLogger);
			project.init();

			project.addReference("ant.projectHelper", projectHelper);
			projectHelper.parse(project, buildFile);
			try {
				project.executeTarget("build");
				project.executeTarget("test");
				project.executeTarget("clean");
			} catch (BuildException e) {
				throw new RuntimeException(String.format(
						"Run %s [%s] failed: %s", buildFile, "everything",
						e.getMessage()), e);
			}

			runErrors.close();
			
			// scrape compiler errors from run.errors
			try{
				Scanner in = new Scanner (new File(thisTest.getFileLocation() + "/test/" + "/run.errors"));
				boolean containsError = false;
				boolean importantData = false;
				String output = "";
				while(in.hasNextLine()){
					String line = in.nextLine();
					if(line.contains(": error:")){
						containsError = true;
					}
					if(line.contains("[javac] Files to be compiled:")){
						importantData = true;
					}
					if(importantData){
						output += line.replace("[javac]", "").replaceAll(".*unitTests","") + System.getProperty("line.separator");
					}
				}
				in.close();
				
				if(containsError){
					compileErrors = new PrintStream(
							thisTest.getFileLocation() + "/test/" + "/compile.errors");
					compileErrors.print(output);
					compileErrors.close();
					
				}
			}
			catch (Exception e){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.error("Something went wrong: " + sw.toString());
			}

//			// delete everything else
//			String[] allFiles = (new File(thisTest.getFileLocation() + "/test/"))
//					.list();
//			for (String file : allFiles) {
//				File actualFile = new File(thisTest.getFileLocation()
//						+ "/test/" + file);
//				if (actualFile.isDirectory()) {
//					FileUtils.deleteDirectory(actualFile);
//				} else {
//					if (!file.equals("result.xml")
//							&& !file.equals("compile.errors")
//							&& !file.equals("run.errors")) {
//						FileUtils.forceDelete(actualFile);
//					}
//				}
//			}

		} catch (IOException e) {
			logger.error("Unable to test unit test "
					+ getUnitTest(testName).getName()
					+ System.getProperty("line.separator") + e);
		} catch (Exception e){
			// catch the rest of the exceptions
		}
		
		
		// ensure everything is closed
		if(runErrors != null){
			runErrors.close();
		}
		if(compileErrors != null){
			compileErrors.close();
		}
	}
	// release an assignment ? 
	public void releaseAssesment(String assessmentName)
	{
		
	}
	// new add assessment
	public void addAssessment(Assessment assessmentToAdd) {
		try {

			// unit Tests
			for (WeightedUnitTest test : assessmentToAdd.getUnitTests()) {
				if (getUnitTest(test.getUnitTestName().replace(" ", "")) != null) {
					test.setTest(getUnitTest(test.getUnitTestName().replace(
							" ", "")));
				}
			}

			// secret unit tests
			for (WeightedUnitTest test : assessmentToAdd.getSecretUnitTests()) {
				if (getUnitTest(test.getUnitTestName().replace(" ", "")) != null) {
					test.setTest(getUnitTest(test.getUnitTestName().replace(
							" ", "")));
				}
			}
			
			// hand marking
			for (WeightedHandMarking test : assessmentToAdd.getHandMarking()) {
				if (getHandMarking(test.getHandMarkingName().replace(" ", "")) != null) {
					test.setHandMarking(getHandMarking(test.getHandMarkingName().replace(
							" ", "")));
				}
			}
			
			// competitions
			for (WeightedCompetition test : assessmentToAdd.getCompetitions()) {
				if (getCompetition(test.getCompName().replace(" ", "")) != null) {
					test.setTest(getCompetition(test.getCompName().replace(
							" ", "")));
					getCompetition(test.getCompName().replace(" ", "")).addAssessment(assessmentToAdd);
				}
			}

			// add it to the directory structure
			File location = new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/template/assessment/"
					+ assessmentToAdd.getName().replace(" ", ""));
			location.mkdirs();

			PrintStream out = new PrintStream(location.getAbsolutePath()
					+ "/assessmentProperties.xml");
			out.print(assessmentToAdd);
			out.close();

			PrintStream descriptionOut = new PrintStream(
					location.getAbsolutePath() + "/description.html");
			descriptionOut.print(assessmentToAdd.getDescription());
			descriptionOut.close();

			// add it to the list.
			boolean reRunEverything = false;
			Assessment currAss = assDao.getAssessment(assessmentToAdd.getShortName());
			if(currAss != null 
					&& !currAss.getUnitTests().equals(assessmentToAdd.getUnitTests())
					&& !currAss.getSecretUnitTests().equals(assessmentToAdd.getSecretUnitTests())){
				reRunEverything = true;
			}
			assDao.addAssessment(assessmentToAdd);
			
			if(reRunEverything){
				runAssessment(assessmentToAdd);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PASTAUser getUser(String username) {
		return userDao.getUser(username);
	}
	
	public Collection<PASTAUser> getUserList() {
		return userDao.getUserList();
	}
	
	public HashMap<String, Collection<String>> getTutorialByStream(){
		return userDao.getTutorialByStream();
	}
	
	public Collection<PASTAUser> getUserListByTutorial(String className) {
		return userDao.getUserListByTutorial(className);
	}
	
	public Collection<PASTAUser> getUserListByStream(String stream) {
		return userDao.getUserListByStream(stream);
	}
	
	public PASTAUser getOrCreateUser(String username) {
		PASTAUser user = userDao.getUser(username);
		if(user == null){
			user = new PASTAUser();
			user.setUsername(username);
			user.setStream("");
			user.setTutorial("");
			user.setPermissionLevel(UserPermissionLevel.STUDENT);
			
			userDao.add(user);
		}
		return user;
	}
	
	public HashMap<String, AssessmentResult> getLatestResultsForUser(String username){
		return resultDAO.getLatestResults(username);
	}
	
	public HashMap<String, HashMap<String, AssessmentResult>> getLatestResults(){
		HashMap<String, HashMap<String, AssessmentResult>> results = new HashMap<String, HashMap<String, AssessmentResult>>();
		Collection<PASTAUser> allUsers = getUserList();
		
		for(PASTAUser user: allUsers){
//			HashMap<String, AssessmentResult> currResultMap = new HashMap<String, AssessmentResult>();
//			Collection<AssessmentResult> currentResults = getStudentResults(user.getUsername());
//			if(currentResults == null){
//				currentResults = new ArrayList<AssessmentResult>();
//			}
//			
//			for(AssessmentResult result: currentResults){
//				currResultMap.put(result.getAssessment().getName(), result);
//			}
			HashMap<String, AssessmentResult> currResultMap = resultDAO.getLatestResults(user.getUsername());
			results.put(user.getUsername(), currResultMap);
		}
		
		return results;
	}
	
	
	public void updateHandMarking(HandMarking marking){
		assDao.updateHandMarking(marking);
	}

	public void newHandMarking(NewHandMarking newMarking){
		assDao.newHandMarking(newMarking);
	}
	
	public void runAssessment(String username, String assessmentName, String assessmentDate){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		try {
			scheduler.save(new Job(username, assessmentName, sdf.parse(assessmentDate)));
		} catch (ParseException e) {
			logger.error("Unable to re-run assessment "
					+ assessmentName + " for " + username
					+ System.getProperty("line.separator") + e);
		}
	}
	
	public void runAssessment(Assessment assessment){
		// scan to see all who made a submission
		for(PASTAUser user: userDao.getUserList()){
			// add them to the queue
			AssessmentResult currResult = resultDAO.getLatestResults(user.getUsername()).get(assessment.getShortName());
			if(currResult != null){
				scheduler.save(new Job(user.getUsername(), 
						assessment.getShortName(), 
						currResult.getSubmissionDate()));
			}
		}
	}

	public void saveHandMarkingResults(String username, String assessmentName,
			String assessmentDate, List<HandMarkingResult> handMarkingResults) {
		AssessmentResult result = resultDAO.getAsssessmentResult(username, assDao.getAssessment(assessmentName), assessmentDate);
		// save to memory
		if(result != null){
			result.setHandMarkingResults(handMarkingResults);
			// save to file
			resultDAO.saveHandMarkingToFile(username, assessmentName, assessmentDate, handMarkingResults);
		}
	}

	public AssessmentResult getAssessmentResult(String username, String assessmentName,
			String assessmentDate) {
		return resultDAO.getAsssessmentResult(username, assDao.getAssessment(assessmentName), assessmentDate);
	}

	public void removeHandMarking(String handMarkingName) {
		assDao.removeHandMarking(handMarkingName);
		// delete file
		try {
			FileUtils.deleteDirectory(new File(ProjectProperties.getInstance().getProjectLocation()
					+ "/template/handMarking/"
					+ handMarkingName));
		} catch (IOException e) {}
	}

	public FileTreeNode generateFileTree(String username,
			String assessmentName, String assessmentDate) {
		return generateFileTree(ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/"
				+ username
				+ "/assessments/"
				+ assessmentName
				+ "/"
				+ assessmentDate
				+ "/submission");
	}
	
	public FileTreeNode generateFileTree(String location){
		File[] subDirectories = new File(location).listFiles();
		if(subDirectories == null || subDirectories.length == 0){
			FileTreeNode current = new FileTreeNode(location, null);
			if(new File(location).isDirectory()){
				current.setLeaf(false);
			}
			return current;
		}
		List<FileTreeNode> children = new LinkedList<FileTreeNode>();
		for(File subDirectory: subDirectories){
			children.add(generateFileTree(subDirectory.getAbsolutePath()));
		}
		return new FileTreeNode(location, children);
	}

	public String scrapeFile(String location) {
		String file = "";
		try {
			Scanner in = new Scanner(new File(location));
			while(in.hasNextLine()){
				file+=in.nextLine() + System.getProperty("line.separator");
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return file;
	}

	public HashMap<String, FileTreeNode> genereateFileTree(String username, String assessmentName) {
		HashMap<String, FileTreeNode> allsubmissions = new HashMap<String, FileTreeNode>();
		
		String[] allSubs = (new File(ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/"
				+ username
				+ "/assessments/"
				+ assessmentName).list());
		if(allSubs != null && allSubs.length > 0){
			for(String submission : allSubs){
				if(submission.matches("\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d-\\d\\d-\\d\\d")){
					allsubmissions.put(submission, generateFileTree(username, assessmentName, submission));
				}
			}
		}
		
		return allsubmissions;
	}

	public void saveComment(String username, String assessmentName,
			String assessmentDate, String comments) {
		// make that better
		resultDAO.saveHandMarkingComments(username, assessmentName, assessmentDate, comments);
	}

	public Collection<Competition> getCompeitionList() {
		return assDao.getCompetitionList();
	}

	public Competition getCompetition(String competitionName) {
		return assDao.getCompetition(competitionName);
	}

	public CompetitionResult getCompetitionResult(String competitionName) {
		return resultDAO.getCompetitionResult(competitionName);
	}
	
	public ArenaResult getCalculatedCompetitionResult(String competitionName){
		return resultDAO.getCalculatedCompetitionResult(competitionName);
	}

	public void giveExtension(String username, String assessmentName, Date extension) {
		PASTAUser user = getUser(username);
		user.getExtensions().put(assessmentName, extension);
		
		// update the files
		try {
			PrintWriter out = new PrintWriter(new File(ProjectProperties.getInstance().getProjectLocation() + "/submissions/" +
					username + "/user.extensions"));
			for (Entry<String, Date> ex : user.getExtensions().entrySet()) {
				out.println(ex.getKey() + ">" + ProjectProperties.formatDate(ex.getValue()));
			}
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not save extension information for " + username);
		}
	}

	public LoginDAO getLoginDao() {
		return loginDao;
	}
	
	public Map<String, List<Assessment>> getAllAssessmentsByCategory() {
		return assDao.getAllAssessmentsByCategory();
	}

	public void updateComment(String username, String assessmentName,
			String newComment) {
		// TODO Auto-generated method stub
		
	}
	
}
