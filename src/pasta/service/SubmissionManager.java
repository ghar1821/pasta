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
import java.text.ParseException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.form.Submission;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
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
		Assessment currAssessment = assDao.getAssessment(form.getAssessment());
		
		AssessmentResult result = new AssessmentResult();
		result.setAssessment(currAssessment);
		result.setUser(user);
		result.setSubmittedBy(form.getSubmittingUser());
		result.setGroupResult(user.isGroup());
		result.setSubmissionDate(form.getSubmissionDate());
		resultManager.save(result);

		if(saveSubmissionToDisk(user, form) != null) {
			runAssessment(user, currAssessment.getId(), PASTAUtil.formatDate(form.getSubmissionDate()), result);
		}
	}
	
	/**
	 * <p>Save the submission to the appropriate submission location. If it is a
	 * zip file, extract it.
	 * 
	 * <p>This method will only move and unzip once. After the first call, the
	 * method will simply return the location of the submission.
	 * 
	 * @param user
	 *            the submitting user
	 * @param form
	 *            the submission form
	 * @return the base directory of the submission
	 * @throws InvalidMediaTypeException
	 *             if the file has a .zip extension but isn't a zip file
	 */
	public File saveSubmissionToDisk(PASTAUser user, Submission form) throws InvalidMediaTypeException {
		String currDate = PASTAUtil.formatDate(form.getSubmissionDate());
		String location = ProjectProperties.getInstance().getSubmissionsLocation() + user.getUsername() + "/assessments/"
				+ form.getAssessment() + "/" + currDate + "/submission";
		File unzipTo = new File(location);
		if(unzipTo.exists()) {
			return unzipTo;
		}
		unzipTo.mkdirs();
		String filename = form.getFile().getOriginalFilename();
		try {
			File newLocation = new File(unzipTo, filename);
			form.getFile().transferTo(newLocation);
			if(filename.endsWith(".zip")) {
				if(PASTAUtil.isZipFile(newLocation)) {
					PASTAUtil.extractFolder(newLocation.getAbsolutePath());
					newLocation.delete();
				} else {
					throw new InvalidMediaTypeException("ZIP", "Not really a zip file.");
				}
			}
		} catch (IllegalStateException | IOException e) {
			logger.error("Cannot save submission to disk.", e);
			return null;
		}
		return unzipTo;
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
