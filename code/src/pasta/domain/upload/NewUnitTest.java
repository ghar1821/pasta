package pasta.domain.upload;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class NewUnitTest {
	private String testName;
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
}
