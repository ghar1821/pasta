package pasta.domain.upload;

import pasta.domain.template.BlackBoxTestCase;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 23 Jun 2015
 */
public class BlackBoxTestCaseForm {
	private long id;
	private String testName;
	private String commandLine;
	private String input;
	private String output;
	private int timeout;
	private boolean deleteMe;
	
	public BlackBoxTestCaseForm() {}
	
	public BlackBoxTestCaseForm(BlackBoxTestCase testCase) {
		this.id = testCase.getId();
		this.testName = new String(testCase.getTestName());
		this.commandLine = new String(testCase.getCommandLine());
		this.input = new String(testCase.getInput());
		this.output = new String(testCase.getOutput());
		this.timeout = testCase.getTimeout();
		this.deleteMe = false;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	
	public String getCommandLine() {
		return commandLine;
	}
	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}
	
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	public String getOutput() {
		return output;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public boolean isDeleteMe() {
		return deleteMe;
	}
	public void setDeleteMe(boolean deleteMe) {
		this.deleteMe = deleteMe;
	}

	public BlackBoxTestCase asPlainTestCase() {
		BlackBoxTestCase plain = new BlackBoxTestCase();
		plain.setId(id);
		plain.setTestName(testName);
		plain.setCommandLine(commandLine);
		plain.setInput(input);
		plain.setOutput(output);
		plain.setTimeout(timeout);
		return plain;
	}
}
