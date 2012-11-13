package pasta.service;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import org.springframework.validation.Errors;

import pasta.domain.AllStudentAssessmentData;
import pasta.domain.Assessment2;
import pasta.domain.Execution;
import pasta.domain.Submission;
import pasta.domain.User;
import pasta.repository.AssessmentDAOold;
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
	
	@Autowired
	private ApplicationContext context;
	
	// Validator for the submission
	private SubmissionValidator subVal = new SubmissionValidator();
	
	public static final Logger logger = Logger.getLogger(SubmissionManager.class);
	
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
	
	public void validateSubmission(Submission sub, Errors errors){
		subVal.validate(sub, errors);
	}
	
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
	
	@Scheduled(fixedDelay=120000)
	/**
	 * Method to execute all remaining jobs.
	 * 
	 * It's set of a fixed delay. It will run 2 minutes after
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