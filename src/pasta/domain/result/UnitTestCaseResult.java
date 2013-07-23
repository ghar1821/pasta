package pasta.domain.result;


public class UnitTestCaseResult implements Comparable{
	private String testName;
	private String testResult;
	private String testMessage;
	private String extendedMessage = "";
	private String type;
	private double time;
	
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
		this.testMessage = testMessage.trim();
	}
	public String getExtendedMessage() {
		return extendedMessage;
	}
	public void setExtendedMessage(String extendedMessage) {
		this.extendedMessage = extendedMessage.trim();
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
	
	@Override
	public int compareTo(Object o) {
		UnitTestCaseResult target = (UnitTestCaseResult)(o);
		return testName.compareTo(target.getTestName());
	}
}