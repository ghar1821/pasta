package pasta.domain.form;

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
	private String description;
	private int timeout;
	private boolean toBeCompared;
	private boolean deleteMe;
	
	public BlackBoxTestCaseForm() {}
	
	public BlackBoxTestCaseForm(BlackBoxTestCase testCase) {
		this.id = testCase.getId();
		this.testName = testCase.getTestName();
		this.commandLine = testCase.getCommandLine();
		this.input = testCase.getInput();
		this.output = testCase.getOutput();
		this.description = testCase.getDescription();
		this.timeout = testCase.getTimeout();
		this.toBeCompared = testCase.isToBeCompared();
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
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public boolean isToBeCompared() {
		return toBeCompared;
	}
	public void setToBeCompared(boolean toBeCompared) {
		this.toBeCompared = toBeCompared;
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
		plain.setDescription(description);
		plain.setTimeout(timeout);
		plain.setToBeCompared(toBeCompared);
		return plain;
	}
}
