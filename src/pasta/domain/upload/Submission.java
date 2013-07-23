package pasta.domain.upload;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class Submission {
	private CommonsMultipartFile file;
	private String assessment;
	private String submittingUsername;
	private String submittingForUsername;
	
	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
	public String getAssessment() {
		return assessment;
	}
	public void setAssessment(String assessment) {
		this.assessment = assessment;
	}
	public String getSubmittingUsername() {
		return submittingUsername;
	}
	public void setSubmittingUsername(String submittingUsername) {
		this.submittingUsername = submittingUsername;
	}
	public String getSubmittingForUsername() {
		return submittingForUsername;
	}
	public void setSubmittingForUsername(String submittingForUsername) {
		this.submittingForUsername = submittingForUsername;
	}
}
