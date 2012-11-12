package pasta.domain;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Container class for assessment submissions.
 * 
 * Only has getter and setter methods.
 * 
 * @author Alex
 *
 */
public class Submission {
	private String assessmentName;
	private CommonsMultipartFile file;
	private String unikey;
	
	public String getAssessmentName() {
		return assessmentName;
	}
	public void setAssessmentName(String assessmentName) {
		this.assessmentName = assessmentName;
	}
	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
	public String getUnikey() {
		return unikey;
	}
	public void setUnikey(String unikey) {
		this.unikey = unikey;
	}
}
