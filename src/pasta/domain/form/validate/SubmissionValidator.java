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
