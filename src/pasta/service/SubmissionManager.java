/**
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.Submission;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
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
						try{
							FileUtils.forceDelete(new File(unitTestsLocation + "/" + test.getTest().getShortName()
									+ "/compile.errors"));
						}
						catch(FileNotFoundException e){}
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
				runAssessment(username, currAssessment.getShortName(), currDate);
			}
		} catch (Exception e) {
			logger.error("Submission error for " + username + " - " + form + "   " + e);
		}
	}
	


	public Map<String, AssessmentResult> getLatestResultsForUser(String username){
		return resultDAO.getLatestResults(username);
	}
	
	public void runAssessment(String username, String assessmentName, String assessmentDate){
		try {
			scheduler.save(new Job(username, assessmentName, PASTAUtil.parseDate(assessmentDate)));
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
