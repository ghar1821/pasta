package pasta.domain.form;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.domain.template.BlackBoxOptions;
import pasta.domain.template.BlackBoxTestCase;
import pasta.domain.template.UnitTest;

/**
 * Form object to update a unit test assessment module.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-02
 *
 */
public class UpdateUnitTestForm {
	
	@Min(0)
	private Long id;
	
	@NotEmpty
	@Length(max=256)
	private String name;
	
	private Long advancedTimeout;
	private Long blackBoxTimeout;
	
	private CommonsMultipartFile file;
	
	private boolean allowAccessoryWrite;
	private CommonsMultipartFile accessoryFile;
	
	private String mainClassName;
	private String submissionCodeRoot;
	
	private List<BlackBoxTestCaseForm> testCases;
	
	private BlackBoxOptions blackBoxOptions;
	
	public UpdateUnitTestForm(UnitTest base) {
		this.id = base.getId();
		this.name = base.getName();
		this.mainClassName = base.getMainClassName();
		this.submissionCodeRoot = base.getSubmissionCodeRoot();
		this.testCases = createTestCaseForms(base.getTestCases());
		this.blackBoxOptions = new BlackBoxOptions(base.getBlackBoxOptions());
		this.allowAccessoryWrite = base.isAllowAccessoryFileWrite();
		this.advancedTimeout = base.getAdvancedTimeout();
		this.blackBoxTimeout = base.getBlackBoxTimeout();
		
		this.file = null;
		this.accessoryFile = null;
	}
	private List<BlackBoxTestCaseForm> createTestCaseForms(List<BlackBoxTestCase> testCases) {
		List<BlackBoxTestCaseForm> forms = new ArrayList<BlackBoxTestCaseForm>();
		for(BlackBoxTestCase testCase : testCases) {
			forms.add(new BlackBoxTestCaseForm(testCase));
		}
		return forms;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getAdvancedTimeout() {
		return advancedTimeout;
	}
	public void setAdvancedTimeout(Long advancedTimeout) {
		this.advancedTimeout = advancedTimeout;
	}
	public Long getBlackBoxTimeout() {
		return blackBoxTimeout;
	}
	public void setBlackBoxTimeout(Long blackBoxTimeout) {
		this.blackBoxTimeout = blackBoxTimeout;
	}
	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
	public CommonsMultipartFile getAccessoryFile() {
		return accessoryFile;
	}
	public void setAccessoryFile(CommonsMultipartFile accessoryFile) {
		this.accessoryFile = accessoryFile;
	}
	
	//JUnitTests
	public String getMainClassName() {
		return mainClassName;
	}
	public void setMainClassName(String mainClassName) {
		this.mainClassName = mainClassName;
	}
	public String getSubmissionCodeRoot() {
		return submissionCodeRoot;
	}
	public void setSubmissionCodeRoot(String submissionCodeRoot) {
		this.submissionCodeRoot = submissionCodeRoot;
	}
	
	//BlackBoxTests
	public List<BlackBoxTestCaseForm> getTestCases() {
		return testCases;
	}
	public void setTestCases(List<BlackBoxTestCaseForm> testCases) {
		if(this.testCases == null || testCases == null) {
			this.testCases = testCases;
		} else {
			this.testCases.clear();
			this.testCases.addAll(testCases);
		}
	}
	public BlackBoxOptions getBlackBoxOptions() {
		return blackBoxOptions;
	}
	public void setBlackBoxOptions(BlackBoxOptions blackBoxOptions) {
		this.blackBoxOptions = blackBoxOptions;
	}
	public boolean isAllowAccessoryWrite() {
		return allowAccessoryWrite;
	}
	public void setAllowAccessoryWrite(boolean allowAccessoryWrite) {
		this.allowAccessoryWrite = allowAccessoryWrite;
	}
	public List<BlackBoxTestCase> getPlainTestCases() {
		List<BlackBoxTestCase> testCases = new ArrayList<BlackBoxTestCase>();
		for(BlackBoxTestCaseForm form : this.testCases) {
			if(!form.isDeleteMe()) {
				testCases.add(form.asPlainTestCase());
			}
		}
		return testCases;
	}
}
