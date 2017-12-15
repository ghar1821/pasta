/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.domain.form.validate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.Submission;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.GroupManager;
import pasta.service.ResultManager;
import pasta.service.SubmissionManager;
import pasta.service.UserManager;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 8 Jul 2015
 *
 */
@Component
public class SubmissionValidator implements Validator {

	@Autowired private ResultManager resultManager;
	@Autowired private AssessmentManager assessmentManager;
	@Autowired private GroupManager groupManager;
	@Autowired private SubmissionManager submissionManager;
	@Autowired private UserManager userManager;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Submission.class.equals(clazz);
	}

	public void validate(PASTAUser forUser, Object target, Errors errors) {
		Submission form = (Submission) target;
		
		validate(target, errors);
		if(!errors.hasErrors()) {
			try {
				submissionManager.saveSubmissionToDisk(forUser, form);
			} catch (InvalidMediaTypeException e) {
				errors.rejectValue("file", "NotRealZip");
			}
		}
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		Submission form = (Submission) target;
		
		PASTAUser user = form.getSubmittingUser();
		if(user == null) {
			errors.reject("NoUser");
		}
		
		if (form.getFile() == null || form.getFile().isEmpty()) {
			errors.rejectValue("file", "NoFile");
		}
		
		if(user.isTutor()) {
			return;
		}
		
		Assessment assessment = assessmentManager.getAssessment(form.getAssessment());
		PASTAGroup group = groupManager.getGroup(user, assessment);
		if(form.isGroupSubmission() && group == null) {
			errors.reject("NoGroup");
		}
		if(form.isGroupSubmission() && (!assessment.isGroupWork() || assessment.isOnlyIndividualWork())) {
			errors.reject("NotGroupWork");
		}
		if(!form.isGroupSubmission() && assessment.isOnlyGroupWork()) {
			errors.reject("MustBeGroup");
		}
		
		if (assessment.isClosedFor(user, userManager.getExtension(user, assessment))) {
			errors.reject("AfterClosingDate");
		}
		
		AssessmentResult latestResult = resultManager.getLatestResultIncludingGroup(user, form.getAssessment());
		int maxSubmissions = assessment.getNumSubmissionsAllowed();
		if (latestResult != null && maxSubmissions != 0
				&& latestResult.getSubmissionsMadeThatCount() >= maxSubmissions) {
			errors.reject("MaxAttempts");
		}
	}
}
