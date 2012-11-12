package pasta.domain.result;


public class UnitTestCaseResult {
	private String testName;
	private String testResult;
	private String testMessage;
	private String extendedMessage = "";
	
	public UnitTestCaseResult(String name, String result, String message){
		testName = name;
		testResult = result;
		testMessage = message;
	}
	
	public UnitTestCaseResult(String name, String result, String message, String extendedMessage){
		testName = name;
		testResult = result;
		testMessage = message;
		this.extendedMessage = extendedMessage;
	}

	public String getTestName() {
		return testName;
	}

	public String getTestResult() {
		return testResult;
	}

	public String getTestMessage() {
		return testMessage;
	}

	public String getExtendedMessage() {
		return extendedMessage;
	}
}