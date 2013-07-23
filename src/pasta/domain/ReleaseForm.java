package pasta.domain;

/**
 * Container for for the login informationS
 * 
 * Only has getters and setters.
 * 
 * @author Alex
 *
 */
public class ReleaseForm {
	private String assessmentName;
	private String list;
	private String specialRelease;
	
	public String getSpecialRelease() {
		return specialRelease;
	}
	public void setSpecialRelease(String specialRelease) {
		this.specialRelease = specialRelease;
	}
	public String getList() {
		return list;
	}
	public void setList(String list) {
		this.list = list;
	}
	public String getAssessmentName() {
		return assessmentName;
	}
	public void setAssessmentName(String assessmentName) {
		this.assessmentName = assessmentName;
	}
	
	
}
