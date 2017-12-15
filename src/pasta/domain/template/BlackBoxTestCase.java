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

package pasta.domain.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 19 Jun 2015
 */
@Entity
@Table (name = "black_box_test_cases")
@VerboseName("black box test case")
public class BlackBoxTestCase extends BaseEntity {
	private static final long serialVersionUID = -4974380327385340788L;
	public static final String validNameRegex = "[a-zA-Z][a-zA-Z0-9_]*";

	@Column(name = "test_name")
	private String testName;
	
	@Column(name = "command_line_args")
	private String commandLine;
	
	@Column(columnDefinition="TEXT")
	private String input;
	
	@Column(columnDefinition="TEXT")
	private String output;
	
	@Column(columnDefinition="TEXT")
	private String description;
	
	@Column(name="compare")
	private boolean toBeCompared = true;
	
	private int timeout;

	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	public boolean hasValidName() {
		return testName != null && testName.matches(validNameRegex);
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
	
	public boolean exactlyEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlackBoxTestCase other = (BlackBoxTestCase) obj;
		if (commandLine == null) {
			if (other.commandLine != null)
				return false;
		} else if (!commandLine.equals(other.commandLine))
			return false;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (output == null) {
			if (other.output != null)
				return false;
		} else if (!output.equals(other.output))
			return false;
		if (testName == null) {
			if (other.testName != null)
				return false;
		} else if (!testName.equals(other.testName))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (timeout != other.timeout)
			return false;
		if (toBeCompared != other.toBeCompared)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "BlackBoxTestCase [id=" + getId() + ", testName=" + testName + ", commandLine=" + commandLine + ", input="
				+ input + ", output=" + output + ", description=" + description + ", timeout=" + timeout + "]";
	}
}
