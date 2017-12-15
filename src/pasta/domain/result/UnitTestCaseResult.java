package pasta.domain.result;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;

/**
 * Container class to hold the result of a single unit test.
 * <p>
 * The class contains the result of a single unit test. 
 * This includes:
 * <ul>
 * 	<li>Unit test name</li>
 * 	<li>Unit test result (usually : pass, failure, error)</li>
 * 	<li>Brief and extended unit test message</li>
 * 	<li>Time taken to run the unit test</li>
 * </ul>
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 * 
 */
@Entity
@Table(name = "unit_test_case_results")
@VerboseName("unit test case result")
public class UnitTestCaseResult extends BaseEntity implements Serializable, Comparable<UnitTestCaseResult>{
	
	private static final long serialVersionUID = 6764260613777032069L;
	
	public static final String PASS = "pass";
	public static final String FAILURE = "failure";
	public static final String ERROR = "error";
	
	private final static int MAX_MESSAGE_LENGTH = 8000;
	private final static int MAX_EXT_MESSAGE_LENGTH = 66000;

	@Column(name = "name")
	private String testName;
	
	@Column(name = "result")
	private String testResult;
	
	@Column(name = "message", length = MAX_MESSAGE_LENGTH)
	@Size (max = MAX_MESSAGE_LENGTH)
	private String testMessage;
	
	@Column(name = "extended_message", length = MAX_EXT_MESSAGE_LENGTH)
	@Size (max = MAX_EXT_MESSAGE_LENGTH)
	private String extendedMessage = "";
	
	private String testDescription;
	
	private String type;
	
	private double time;
	
	@ManyToOne
	@JoinColumn(name="unit_test_result_id")
	private UnitTestResult unitTestResult;
	
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName.trim();
	}
	public String getTestResult() {
		return testResult;
	}
	public void setTestResult(String testResult) {
		this.testResult = testResult.trim();
	}
	public String getTestMessage() {
		return testMessage;
	}
	public void setTestMessage(String testMessage) {
		testMessage = testMessage.trim();
		if(testMessage.length() >= (MAX_MESSAGE_LENGTH - 3)) {
			testMessage = testMessage.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
		}
		this.testMessage = testMessage;
	}
	public String getExtendedMessage() {
		return extendedMessage;
	}
	public void setExtendedMessage(String extendedMessage) {
		extendedMessage = extendedMessage.trim();
		if(extendedMessage.length() >= (MAX_EXT_MESSAGE_LENGTH - 3)) {
			extendedMessage = extendedMessage.substring(0, MAX_EXT_MESSAGE_LENGTH - 3) + "...";
		}
		this.extendedMessage = extendedMessage;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type.trim();
	}
	public String getTestDescription() {
		return testDescription;
	}
	public void setTestDescription(String testDescription) {
		this.testDescription = testDescription;
	}
	
	public UnitTestResult getUnitTestResult() {
		return unitTestResult;
	}
	public void setUnitTestResult(UnitTestResult unitTestResult) {
		this.unitTestResult = unitTestResult;
	}
	
	public boolean isPass() {
		return getTestResult().equals(PASS);
	}
	public boolean isFailure() {
		return getTestResult().equals(FAILURE);
	}
	public boolean isError() {
		return getTestResult().equals(ERROR);
	}
	
	@Override
	public int compareTo(UnitTestCaseResult target) {
		return testName.compareTo(target.getTestName());
	}
}