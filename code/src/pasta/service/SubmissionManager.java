package pasta.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import pasta.domain.AllStudentAssessmentData;
import pasta.domain.Assessment2;
import pasta.domain.Execution;
import pasta.domain.User;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.repository.AssessmentDAO;
import pasta.repository.AssessmentDAOold;
import pasta.repository.ResultDAO;
import pasta.repository.UserDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.util.ProjectProperties;
import pasta.validation.SubmissionValidator;

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
	private UserDAO userDao = new UserDAO();
	private AssessmentDAOold assDao = new AssessmentDAOold();
	private AssessmentDAO assDaoNew = new AssessmentDAO();
	private ResultDAO resultDAO = new ResultDAO();

	@Autowired
	private ApplicationContext context;

	// Validator for the submission
	private SubmissionValidator subVal = new SubmissionValidator();

	public static final Logger logger = Logger.getLogger(SubmissionManager.class);

	public UnitTestResult getUnitTestResult(String location) {
		return resultDAO.getUnitTestResult(location);
	}

	// new
	public Collection<UnitTest> getUnitTestList() {
		return assDaoNew.getAllUnitTests().values();
	}

	// new
	public UnitTest getUnitTest(String name) {
		return assDaoNew.getAllUnitTests().get(name);
	}

	// new
	public Collection<Assessment> getAssessmentListNew() {
		return assDaoNew.getAssessmentList();
	}

	// new
	public HandMarking getHandMarking(String handMarkingName) {
		return assDaoNew.getHandMarking(handMarkingName);
	}

	// new
	public Assessment getAssessmentNew(String assessmentName) {
		return assDaoNew.getAssessment(assessmentName);
	}

	public void saveUnitTest(UnitTest thisTest) {
		try {

			// create space on the file system.
			(new File(thisTest.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
			PrintStream out = new PrintStream(thisTest.getFileLocation() + "/unitTestProperties.xml");
			out.print(thisTest);
			out.close();

		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName() + " could not be saved successfully!" + System.getProperty("line.separator") + e);
		}
	}
	
	// new - unit test is guaranteed to have a unique name
	public void addUnitTest(NewUnitTest newTest) {
		UnitTest thisTest = new UnitTest(newTest.getTestName(), false);

		try {

			// create space on the file system.
			(new File(thisTest.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
			PrintStream out = new PrintStream(thisTest.getFileLocation() + "/unitTestProperties.xml");
			out.print(thisTest);
			out.close();

			// unzip the uploaded code into the code folder. (if exists)
			if (newTest.getFile() != null && !newTest.getFile().isEmpty()) {
				// unpack
				newTest.getFile().transferTo(
						new File(thisTest.getFileLocation() + "/code/" + newTest.getFile().getOriginalFilename()));
				ProjectProperties.extractFolder(thisTest.getFileLocation() + "/code/"
						+ newTest.getFile().getOriginalFilename());
				newTest.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisTest.getFileLocation() + "/code/"
						+ newTest.getFile().getOriginalFilename()));
			}

			assDaoNew.addUnitTest(thisTest);
		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName() + " could not be created successfully!" + System.getProperty("line.separator") + e);
		}
	}

	// new - unit test is guaranteed to have a unique name
	public void removeUnitTest(String testName) {
		assDaoNew.removeUnitTest(testName);
	}
	
	// new - assessment is guaranteed to have a unique name
	public void removeAssessment(String assessment) {
		assDaoNew.removeAssessment(assessment);
	}

	// new - test submission
	public void testUnitTest(Submission submission, String testName) {
		try {
			UnitTest thisTest = getUnitTest(testName);
			// delete old submission if exists
			FileUtils.deleteDirectory(new File(thisTest.getFileLocation() + "/test/"));

			// create folder
			(new File(thisTest.getFileLocation() + "/test/")).mkdirs();
			// extract submission
			submission.getFile().transferTo(
					new File(thisTest.getFileLocation() + "/test/" + submission.getFile().getOriginalFilename()));
			ProjectProperties.extractFolder(thisTest.getFileLocation() + "/test/"
					+ submission.getFile().getOriginalFilename());
			FileUtils.forceDelete(new File(thisTest.getFileLocation() + "/test/"
					+ submission.getFile().getOriginalFilename()));

			// copy over unit test
			FileUtils.copyDirectory(new File(thisTest.getFileLocation() + "/code/"),
					new File(thisTest.getFileLocation() + "/test/"));

			// compile
			File buildFile = new File(thisTest.getFileLocation() + "/test/build.xml");

			ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
			Project project = new Project();

			project.setUserProperty("ant.file", buildFile.getAbsolutePath());
			DefaultLogger consoleLogger = new DefaultLogger();
			PrintStream compileErrors = new PrintStream(thisTest.getFileLocation() + "/test/compile.errors");
			PrintStream runErrors = new PrintStream(thisTest.getFileLocation() + "/test/run.errors");
			consoleLogger.setErrorPrintStream(compileErrors);
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
				throw new RuntimeException(String.format("Run %s [%s] failed: %s", buildFile, "everything",
						e.getMessage()), e);
			}

			// delete everything else
			String[] allFiles = (new File(thisTest.getFileLocation() + "/test/")).list();
			for (String file : allFiles) {
				File actualFile = new File(thisTest.getFileLocation() + "/test/" + file);
				if (actualFile.isDirectory()) {
					FileUtils.deleteDirectory(actualFile);
				} else {
					if (!file.equals("result.xml") && !file.equals("compile.errors") && !file.equals("run.errors")) {
						FileUtils.forceDelete(actualFile);
					}
				}
			}

		} catch (IOException e) {
			logger.error("Unable to test unit test " + getUnitTest(testName).getName()  + System.getProperty("line.separator") + e);
		}
	}
	
	// new TOOD add assessment
	public void addAssessment(Assessment assessmentToAdd){
		try {
			
			// reload unit tests
			Collection<UnitTest> tests = getUnitTestList(); 
			
			// unit Tests
			for(WeightedUnitTest test: assessmentToAdd.getUnitTests()){
				if(getUnitTest(test.getUnitTestName().replace(" ", ""))!= null){
					test.setTest(getUnitTest(test.getUnitTestName().replace(" ", "")));
				}
			}

			// secret unit tests
			for(WeightedUnitTest test: assessmentToAdd.getSecretUnitTests()){
				if(getUnitTest(test.getUnitTestName().replace(" ", ""))!= null){
					test.setTest(getUnitTest(test.getUnitTestName().replace(" ", "")));
				}
			}
				
			// add it to the directory structure
			File location = new File(ProjectProperties.getInstance().getProjectLocation()+"/template/assessment/"+assessmentToAdd.getName().replace(" ", ""));
			location.mkdirs();
			
			PrintStream out = new PrintStream(location.getAbsolutePath()+"/assessmentProperties.xml");
			out.print(assessmentToAdd);
			out.close();
			
			PrintStream descriptionOut = new PrintStream(location.getAbsolutePath() + "/description.html");
			descriptionOut.print(assessmentToAdd.getDescription());
			descriptionOut.close();
		
		// add it to the list.
		assDaoNew.addAssessment(assessmentToAdd);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public User getUser(String unikey) {
		return userDao.getUser(unikey);
	}

	public String[] getUserList() {
		return userDao.getUserList();
	}

	public List<String> getAssessmentList() {
		return assDao.getAssessmentList();
	}

	public Map<String, Assessment2> getAssessments(String unikey) {
		return assDao.getAssessments(unikey);
	}

	public List<Assessment2> getAssessmentHistory(String unikey, String assessmentName) {
		return assDao.getAssessmentHistory(unikey, assessmentName);
	}

	public Assessment2 getAssessment(String unikey, String assessmentName) {
		return assDao.getAssessment(assessmentName, unikey);
	}

	// public void validateSubmission(Submission sub, Errors errors){
	// subVal.validate(sub, errors);
	// }

	@Scheduled(fixedDelay = 600000)
	public void fixPermissions() {
		try {
			Runtime.getRuntime().exec("chmod g+w -R " + ProjectProperties.getInstance().getSubmissionsLocation());
			Runtime.getRuntime().exec("chgrp -R mark1103 " + ProjectProperties.getInstance().getSubmissionsLocation());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Scheduled(fixedDelay = 30000)
	/**
	 * Method to execute all remaining jobs.
	 * 
	 * It's set of a fixed delay. It will run 30 seconds after
	 * the previous execution.
	 */
	public void executeRemainingJobs() {
		Execution exec = ExecutionScheduler.getInstance().nextExecution();
		String UOS = "";
		if (context != null && context.getMessage("UOS", null, Locale.getDefault()) != null) {
			UOS = context.getMessage("UOS", null, Locale.getDefault()) + ": ";
		}
		while (exec != null) {
			// execute jobs
			try {
				logger.info(UOS + "executing " + exec.getAssessmentName() + " for " + exec.getUnikey());
				File latest = new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/"
						+ exec.getUnikey() + "/" + exec.getAssessmentName() + "/latest");

				// copy testing code across
				FileUtils.copyDirectory(
						new File(ProjectProperties.getInstance().getTemplateLocation() + "/" + exec.getAssessmentName()
								+ "/code"), latest);

				// run
				ProcessBuilder compiler = new ProcessBuilder("bash", "-c", "ant clean build test clean");
				if (ProjectProperties.getInstance().getJava6Location() != null) {
					compiler.environment().put("JAVA_HOME", ProjectProperties.getInstance().getJava6Location());
				}
				compiler.redirectErrorStream(true);
				compiler.directory(latest);
				compiler.redirectErrorStream(true);
				Process compile;
				compile = compiler.start();

				// take output from ant and ignore it thoroughly
				BufferedReader compileIn = new BufferedReader(new InputStreamReader(compile.getInputStream()));
				String line;
				String compileMessage = "Compiler Errors:\r\n";
				while ((line = compileIn.readLine()) != null) {
					compileMessage += line + "\r\n";
				}
				compileMessage += "\r\n\r\n ERROR CODE: " + compile.waitFor();

				// if errors, return errors, dump ant output to compile.errors
				if (!compileMessage.contains("BUILD SUCCESSFUL")) {
					PrintWriter compileErrors = new PrintWriter(latest.getAbsolutePath() + "/run.errors");
					compileErrors.println(compileMessage);
					compileErrors.close();
				}

				compile.destroy();

				// cleanup - should make this better TODO
				FileUtils.deleteDirectory(new File(latest.getAbsolutePath() + "/bin"));
				FileUtils.deleteDirectory(new File(latest.getAbsolutePath() + "/junit_jars"));
				FileUtils.deleteDirectory(new File(latest.getAbsolutePath() + "/test"));
				(new File(latest.getAbsolutePath() + "/build.xml")).delete();

				ExecutionScheduler.getInstance().completedExecution(exec);
				// update caching
				AllStudentAssessmentData.getInstance().updateStudent(exec.getUnikey(), exec.getAssessmentName());

			} catch (Exception e) {
				// remove the execution from the database.
				ExecutionScheduler.getInstance().completedExecution(exec);
				ExecutionScheduler.getInstance().scheduleExecution(exec);
				logger.info("[PASTA ERROR] " + exec.getUnikey() + " - " + exec.getAssessmentName() + ":\r\n"
						+ e.getMessage());
			}
			// get next
			exec = ExecutionScheduler.getInstance().nextExecution();
		}

		logger.info(UOS + "finished executing all jobs");
	}

}
