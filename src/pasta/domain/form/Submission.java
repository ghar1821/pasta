package pasta.domain.form;

import java.util.Date;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.domain.user.PASTAUser;

/**
 * Form object for a submission.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-16
 *
 */
public class Submission {
	private CommonsMultipartFile file;
	private Long assessment;
	private PASTAUser submittingUser;
	private boolean groupSubmission;
	private Date submissionDate;

	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
	public Long getAssessment() {
		return assessment;
	}
	public void setAssessment(Long assessment) {
		this.assessment = assessment;
	}
	
	public PASTAUser getSubmittingUser() {
		return submittingUser;
	}
	public void setSubmittingUser(PASTAUser submittingUser) {
		this.submittingUser = submittingUser;
	}
	
	public boolean isGroupSubmission() {
		return groupSubmission;
	}
	public void setGroupSubmission(boolean groupSubmission) {
		this.groupSubmission = groupSubmission;
	}
	
	public Date getSubmissionDate() {
		return submissionDate;
	}
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}
}
