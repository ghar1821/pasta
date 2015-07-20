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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.upload.Submission;
import pasta.domain.user.PASTAUser;
import pasta.repository.AssessmentDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Submission manager.
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 *  
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
@Service("submissionManager")
@Repository
public class SubmissionManager {
	
	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ExecutionScheduler scheduler;
	@Autowired
	private ResultManager resultManager;
	

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(SubmissionManager.class);
	
	/**
	 * Process the submission of an assessment
	 * <p>
	 * <ol>
	 * 	<li>Create new folder to hold the submission
	 *  ($ProjectLocation$/submissions/$username$/assessments/$assessmentId$/$date$/submission)</li>
	 *  <li>Copy submission file from memory to the newly created folder</li>
	 *  <li>Extract the submitted file if it ends with .zip using {@link pasta.util.PASTAUtil#extractFolder(String)}</li>
	 *  <li>If there are any unit tests associated with the assessment, check to see if there are any compilation errors.
	 *  This process is done by copying the submission and then the unit tests to 
	 *  $ProjectLocation$/submissions/$username$/assessments/$assessmentId$/$date$/unitTests/$unitTestId.
	 *  This order of copy was chosen such that the unit test code will override any identically named file that
	 *  the student has submitted (e.g. makefiles), which could compromise the security of the machine or 
	 *  validity of the assessment. The ant tasks compile and clean are executed and any errors during
	 *  this process are written to compile.errors and run.errors.</li>
	 *  <li>Clean up the testing code by deleting everything other that "compile.errors", "run.errors" and "results.xml"</li>
	 *  <li>The cache is updated with the new submission information (testing queued/compile errors)</li>
	 *  <li>If the submission compiled, run the assessment using {@link #runAssessment(String, String, String, AssessmentResult)}</li>
	 * </ol>
	 * 
	 * @param user the user
	 * @param form the submission form
	 */
	public void submit(PASTAUser user, Submission form) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		Date now = new Date();
		String currDate = sdf.format(now);
		String location = ProjectProperties.getInstance().getSubmissionsLocation() + user.getUsername() + "/assessments/"
				+ form.getAssessment() + "/" + currDate + "/submission";
		
		Assessment currAssessment = assDao.getAssessment(form.getAssessment());
		boolean compiled = true;
		
		AssessmentResult result = new AssessmentResult();
		result.setAssessment(currAssessment);
		result.setUser(user);
		result.setSubmittedBy(form.getSubmittingUser());
		result.setGroupResult(user.isGroup());
		
		try {
			result.setSubmissionDate(sdf.parse(currDate));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		resultManager.save(result);

		(new File(location)).mkdirs();
		try {
			form.getFile().transferTo(new File(location+"/"+form.getFile().getOriginalFilename()));
			if(form.getFile().getOriginalFilename().endsWith(".zip")){
				PASTAUtil.extractFolder(location+"/"+form.getFile().getOriginalFilename());
				(new File(location+"/"+form.getFile().getOriginalFilename())).delete();
			}
			
//			String unitTestsLocation = ProjectProperties.getInstance().getSubmissionsLocation() + username + "/assessments/"
//					+ form.getAssessment() + "/" + currDate + "/unitTests";
//			// ensure all unit tests compile
//			for(WeightedUnitTest test: currAssessment.getUnitTests()){
//				try {
//					// create folder
//					(new File(unitTestsLocation + "/" + test.getTest().getId())).mkdirs();
//
//
//					// copy over submission
//					FileUtils.copyDirectory(new File(location),
//							new File(unitTestsLocation + "/" + test.getTest().getId()));
//					
//					// copy over unit test
//					FileUtils.copyDirectory(new File(test.getTest().getFileLocation()
//							+ "/code/"),
//							new File(unitTestsLocation + "/" + test.getTest().getId()));
//					
//					// compile
//					File buildFile = new File(unitTestsLocation + "/" + test.getTest().getId()
//							+ "/build.xml");
//
//					ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
//					Project project = new Project();
//
//					project.setUserProperty("ant.file", buildFile.getAbsolutePath());
//					project.setBasedir(unitTestsLocation + "/" + test.getTest().getId());
//					DefaultLogger consoleLogger = new DefaultLogger();
//					PrintStream runErrors = new PrintStream(
//							unitTestsLocation + "/" + test.getTest().getId()
//							+ "/run.errors");
//					consoleLogger.setOutputPrintStream(runErrors);
//					consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
//					project.addBuildListener(consoleLogger);
//					project.init();
//
//					project.addReference("ant.projectHelper", projectHelper);
//					projectHelper.parse(project, buildFile);
//					
//					try {
//						project.executeTarget("build");
//						project.executeTarget("clean");
//					} catch (BuildException e) {
//						compiled = false;
//						logger.error("Could not compile " + username + " - "
//								+ currAssessment.getName() + " - "
//								+ test.getTest().getName() + e);
//						
//						PrintStream compileErrors = new PrintStream(
//								unitTestsLocation + "/" + test.getTest().getId()
//								+ "/compile.errors");
//						compileErrors.print(e.toString().replaceAll(".*" +
//								unitTestsLocation + "/" + test.getTest().getId() + "/" , "folder "));
//						compileErrors.close();
//					}
//
//					runErrors.flush();
//					runErrors.close();
//					
//					// scrape compiler errors from run.errors
//					try{
//						Scanner in = new Scanner (new File(unitTestsLocation + "/" + test.getTest().getId()
//								+ "/run.errors"));
//						boolean containsError = false;
//						boolean importantData = false;
//						String output = "";
//						while(in.hasNextLine()){
//							String line = in.nextLine();
//							if(line.contains(": error:")){
//								containsError = true;
//							}
//							if(line.contains("[javac] Files to be compiled:")){
//								importantData = true;
//							}
//							if(importantData){
//								output += line.replace("[javac]", "").replaceAll(".*unitTests","") + System.getProperty("line.separator");
//							}
//						}
//						in.close();
//						
//						if(containsError){
//							PrintStream compileErrors = new PrintStream(
//									unitTestsLocation + "/" + test.getTest().getId()
//									+ "/compile.errors");
//							compileErrors.print(output);
//							compileErrors.close();
//						}
//					}
//					catch (Exception e){
//						// do nothing
//					}
//					
//					// delete everything else
//					String[] allFiles = (new File(unitTestsLocation + "/" + test.getTest().getId()))
//							.list();
//					for (String file : allFiles) {
//						File actualFile = new File(unitTestsLocation + "/" + test.getTest().getId()
//								+ "/" + file);
//						if (actualFile.isDirectory()) {
//							FileUtils.deleteDirectory(actualFile);
//						} else {
//							if (!file.equals("result.xml")
//									&& !file.equals("compile.errors")
//									&& !file.equals("run.errors")) {
//								FileUtils.forceDelete(actualFile);
//							}
//						}
//					}
//					
//					if(compiled){
//						try{
//							FileUtils.forceDelete(new File(unitTestsLocation + "/" + test.getTest().getId()
//									+ "/compile.errors"));
//						}
//						catch(FileNotFoundException e){}
//					}
//					
//				} catch (IOException e) {
//					logger.error("Unable to compile unit test "
//							+ currAssessment.getName() + " for " + username
//							+ System.getProperty("line.separator") + e);
//				}
//			}
			
//			resultDAO.updateUnitTestResults(username, currAssessment, now);
			
			// add to scheduler
			if(compiled){
				runAssessment(user, currAssessment.getId(), currDate, result);
			}
		} catch (Exception e) {
			logger.error("Submission error for " + user.getUsername() + " - " + form + "   " + e);
		}
	}
	
	/**
	 * Schedule an assessment attempt for a given user for execution
	 * 
	 * @param user the user
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date of the assessment (format yyyy-MM-dd'T'HH-mm-ss)
	 * @param result the assessment result object to store results in
	 */
	public void runAssessment(PASTAUser user, long assessmentId, String assessmentDate, AssessmentResult result){
		try {
			scheduler.scheduleJob(user, assessmentId, result, PASTAUtil.parseDate(assessmentDate));
		} catch (ParseException e) {
			logger.error("Unable to re-run assessment "
					+ assessmentId + " for " + user.getUsername()
					+ System.getProperty("line.separator") + e);
		}
	}
	
	/**
	 * Schedule the latest attempt of an assessment for a collection of users that have
	 * submitted for execution.
	 * 
	 * @param assessment the assessment for which the latest attempts must be re-run
	 * @param allUsers the collection of users for which the latest attempt will be executed
	 */
	public void runAssessment(Assessment assessment, Collection<PASTAUser> allUsers){
		for(PASTAUser user: allUsers){
			// scan to see all who made a submission
			AssessmentResult currResult = resultManager.getLatestResults(user).get(assessment.getId());
			if(currResult != null){
				// add them to the queue
				scheduler.scheduleJob(user, assessment.getId(), currResult, currResult.getSubmissionDate());
			}
		}
	}

}
