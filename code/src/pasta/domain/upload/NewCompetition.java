package pasta.domain.upload;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class NewCompetition {
	private String testName;
	private String type;
	private CommonsMultipartFile file;
	
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
