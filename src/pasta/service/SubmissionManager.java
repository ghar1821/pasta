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

import org.apache.commons.codec.digest.DigestUtils;
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
import pasta.domain.UserPermissionLevel;
import pasta.domain.form.ReleaseForm;
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
import pasta.util.PASTAUtil;
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
	
	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	
	private ExecutionScheduler scheduler;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
		
//		// start up competitions - competitions should schedule themselves
//		for(Competition comp: assDao.getCompetitionList()){
//			if(comp.isLive()){
//				scheduler.save(new Job("PASTACompetitionRunner", comp.getShortName(), comp.getNextRunDate()));
//			}
//		}
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
				PASTAUtil.extractFolder(location+"/"+form.getFile().getOriginalFilename());
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
					project.setBasedir(unitTestsLocation + "/" + test.getTest().getShortName());
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
						
						PrintStream compileErrors = new PrintStream(
								unitTestsLocation + "/" + test.getTest().getShortName()
								+ "/compile.errors");
						compileErrors.print(e.toString().replaceAll(".*" +
								unitTestsLocation + "/" + test.getTest().getShortName() + "/" , "folder "));
						compileErrors.close();
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
	


	public HashMap<String, AssessmentResult> getLatestResultsForUser(String username){
		return resultDAO.getLatestResults(username);
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
	
	public void runAssessment(Assessment assessment, Collection<PASTAUser> allUsers){
		// scan to see all who made a submission
		for(PASTAUser user: allUsers){
			// add them to the queue
			if(resultDAO.getLatestResults(user.getUsername())!=null){
				AssessmentResult currResult = resultDAO.getLatestResults(user.getUsername()).get(assessment.getShortName());
				if(currResult != null){
					scheduler.save(new Job(user.getUsername(), 
							assessment.getShortName(), 
							currResult.getSubmissionDate()));
				}
			}
		}
	}

}
