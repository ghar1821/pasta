package pasta.docker;

public class CommandResult {
	private String output;
	private String error;
	public CommandResult(String output, String error) {
		this.output = output;
		this.error = error;
	}
	public String getOutput() {
		return output;
	}
	public String getError() {
		return error;
	}
}
