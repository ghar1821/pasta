package pasta.domain.template;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 19 Jun 2015
 */
@Entity
@Table (name = "black_box_test_cases")
public class BlackBoxTestCase implements Serializable {
	private static final long serialVersionUID = -4974380327385340788L;
	public static final String validNameRegex = "[a-zA-Z][a-zA-Z0-9_]*";

	@Id @GeneratedValue
	private long id;
	
	@Column(name = "test_name")
	private String testName;
	
	@Column(name = "command_line_args")
	private String commandLine;
	
	@Column(columnDefinition="TEXT")
	private String input;
	
	@Column(columnDefinition="TEXT")
	private String output;
	
	@Column(name="compare")
	private boolean toBeCompared = true;
	
	private int timeout;

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlackBoxTestCase other = (BlackBoxTestCase) obj;
		if (id == 0 || id != other.id)
			return false;
		return true;
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
		if (id != other.id)
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
		if (timeout != other.timeout)
			return false;
		if (toBeCompared != other.toBeCompared)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "BlackBoxTestCase [id=" + id + ", testName=" + testName + ", commandLine=" + commandLine
				+ ", input=" + input + ", output=" + output + ", timeout=" + timeout + "]";
	}
}
