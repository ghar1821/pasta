/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.domain.form;

import pasta.domain.template.BlackBoxTestCase;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 23 Jun 2015
 */
public class BlackBoxTestCaseForm {
	private Long id;
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
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
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
