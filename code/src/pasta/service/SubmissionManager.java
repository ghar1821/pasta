package pasta.service;


import java.io.BufferedReader;
import java.io.File;
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
import pasta.domain.template.UnitTest;
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
	
	public UnitTestResult getUnitTestResult(String location){
		return resultDAO.getUnitTestResult(location);
	}
	
	// new
	public Collection<UnitTest> getUnitTestList(){
		return assDaoNew.getAllUnitTests().values();
	}
	
	// new
	public UnitTest getUnitTest(String name){
		return assDaoNew.getAllUnitTests().get(name);
	}
	
	// new
	public Collection<Assessment> getAssessmentListNew(){
		return assDaoNew.getAssessmentList();
	}
	
	// new
	public Assessment getAssessmentNew(String assessmentName){
		return assDaoNew.getAssessment(assessmentName);
	}
	
	//new - unit test is guaranteed to have a unique name
	public void addUnitTest(NewUnitTest newTest){
		UnitTest thisTest = new UnitTest(newTest.getTestName(), false);

		try {
			
			// create space on the file system.
			(new File(thisTest.getFileLocation()+"/code/")).mkdirs();
			
			// generate unitTestProperties
			PrintStream out = new PrintStream(thisTest.getFileLocation()+"/unitTestProperties.xml");
			out.print(thisTest);
			out.close();
			
			// unzip the uploaded code into the code folder. (if exists)
			if(newTest.getFile() != null && !newTest.getFile().isEmpty() ){
				// unpack
				newTest.getFile().transferTo(new File(thisTest.getFileLocation()+"/code/"+newTest.getFile().getOriginalFilename()));
				ProjectProperties.extractFolder(thisTest.getFileLocation()+"/code/"+newTest.getFile().getOriginalFilename());
				newTest.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisTest.getFileLocation()+"/code/"+newTest.getFile().getOriginalFilename()));
			}
			
			assDaoNew.addUnitTest(thisTest);
		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST "+thisTest.getName()+" could not be created successfully!\r\n "+e);
		}
	}
	
	//new - unit test is guaranteed to have a unique name
	public void removeUnitTest(String testName){
		assDaoNew.removeUnitTest(testName);
	}
	
	//new - test submission
	public void testUnitTest(Submission submission, String testName){
		try {
			UnitTest thisTest = getUnitTest(testName);
			// delete old submission if exists
			FileUtils.deleteDirectory(new File(thisTest.getFileLocation()+"/test/"));
			
			// create folder
			(new File(thisTest.getFileLocation()+"/test/")).mkdirs();
			// extract submission
			submission.getFile().transferTo(new File(thisTest.getFileLocation()+"/test/"+submission.getFile().getOriginalFilename()));
			ProjectProperties.extractFolder(thisTest.getFileLocation()+"/test/"+submission.getFile().getOriginalFilename());
			FileUtils.forceDelete(new File(thisTest.getFileLocation()+"/test/"+submission.getFile().getOriginalFilename()));
				
			// copy over unit test
			FileUtils.copyDirectory(new File(thisTest.getFileLocation()+"/code/"), new File(thisTest.getFileLocation()+"/test/"));
			
			// compile
			ProcessBuilder compiler = new ProcessBuilder("ant", "build clean");
			compiler.redirectErrorStream(true);
			compiler.directory(new File(thisTest.getFileLocation()+"/test/"));
			compiler.redirectErrorStream(true);
			Process compile = compiler.start();

			BufferedReader compileIn = new BufferedReader(new InputStreamReader(compile.getInputStream()));
			String line;
			String compileMessage = "Compiler Errors:\r\n";
			while ((line = compileIn.readLine()) != null) {
				compileMessage += line + "\r\n";
			}
			compileMessage += "\r\n\r\n ERROR CODE: " + compile.waitFor();
			
			// if errors, return errors, dump ant output to compile.errors
			if (!compileMessage.contains("BUILD SUCCESSFUL")) {
				PrintWriter compileErrors = new PrintWriter(thisTest.getFileLocation()+"/test/compile.errors");
				compileErrors.println(compileMessage);
				compileErrors.close();
			}
			compile.destroy();
			
			// run
			ProcessBuilder tester = new ProcessBuilder("ant", "clean build test clean");
			tester.redirectErrorStream(true);
			tester.directory(new File(thisTest.getFileLocation()+"/test/"));
			tester.redirectErrorStream(true);
			Process test = tester.start();

			BufferedReader testIn = new BufferedReader(new InputStreamReader(test.getInputStream()));
			String testLine="";
			String testMessage = "Compiler Errors:\r\n";
			while ((line = testIn.readLine()) != null) {
				testMessage += testLine + "\r\n";
			}
			testMessage += "\r\n\r\n ERROR CODE: " + test.waitFor();
			
			// if errors, return errors, dump ant output to run.errors
			if (!testMessage.contains("BUILD SUCCESSFUL")) {
				PrintWriter runErrors = new PrintWriter(thisTest.getFileLocation()+"/test/run.errors");
				runErrors.println(testMessage);
				runErrors.close();
			}
			test.destroy();
			
			// delete everything else
			String[] allFiles = (new File(thisTest.getFileLocation()+"/test/")).list();
			for(String file: allFiles){
				File actualFile = new File(thisTest.getFileLocation()+"/test/"+file);
				if(actualFile.isDirectory()){
					FileUtils.deleteDirectory(actualFile);
				}
				else{
					if(!file.equals("result.xml") && !file.equals("compile.errors") && !file.equals("run.errors")){
						FileUtils.forceDelete(actualFile);
					}
				}
			}
			
		} catch (IOException e) {
			logger.error("Unable to test unit test "+getUnitTest(testName).getName()+"\r\n"+e);
		} catch (InterruptedException e) {
			logger.error("Unable to execute unit test "+getUnitTest(testName).getName()+"\r\n"+e);
		}
	}
	
	public User getUser(String unikey){
		return userDao.getUser(unikey);
	}
	
	public String[] getUserList(){
		return userDao.getUserList();
	}
	
	public List<String> getAssessmentList(){
		return assDao.getAssessmentList();
	}
	
	public Map<String, Assessment2> getAssessments(String unikey){
		return assDao.getAssessments(unikey);
	}
	
	public List<Assessment2> getAssessmentHistory(String unikey, String assessmentName){
		return assDao.getAssessmentHistory(unikey, assessmentName);
	}
	
	public Assessment2 getAssessment(String unikey, String assessmentName){
		return assDao.getAssessment(assessmentName, unikey);
	}
	
//	public void validateSubmission(Submission sub, Errors errors){
//		subVal.validate(sub, errors);
//	}
	
	@Scheduled(fixedDelay=600000)
	public void fixPermissions(){
		try {
			Runtime.getRuntime().exec("chmod g+w -R "+ProjectProperties.getInstance().getSubmissionsLocation());
			Runtime.getRuntime().exec("chgrp -R mark1103 "+ProjectProperties.getInstance().getSubmissionsLocation());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Scheduled(fixedDelay=30000)
	/**
	 * Method to execute all remaining jobs.
	 * 
	 * It's set of a fixed delay. It will run 30 seconds after
	 * the previous execution.
	 */
	public void executeRemainingJobs(){
		Execution exec = ExecutionScheduler.getInstance().nextExecution();
		String UOS = "";
		if(context != null && context.getMessage("UOS", null, Locale.getDefault()) != null){
			UOS = context.getMessage("UOS", null, Locale.getDefault()) + ": ";
		}
		while(exec != null){
			// execute jobs
			try {
				logger.info(UOS + "executing " + exec.getAssessmentName() + " for " + exec.getUnikey());
				File latest = new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/"
						+ exec.getUnikey() + "/" + exec.getAssessmentName() + "/latest");

				 // copy testing code across
				FileUtils.copyDirectory(new File(ProjectProperties.getInstance().getTemplateLocation()+
						"/"+exec.getAssessmentName()+"/code"), latest);
				
				// run
				ProcessBuilder compiler = new ProcessBuilder("bash", "-c", "ant clean build test clean");
				if(ProjectProperties.getInstance().getJava6Location() != null){
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
				logger.info("[PASTA ERROR] " + exec.getUnikey() + " - " + exec.getAssessmentName() + ":\r\n" + e.getMessage());
			}
			// get next
			exec = ExecutionScheduler.getInstance().nextExecution();
		}
		
		logger.info(UOS + "finished executing all jobs");
	}
	
}
