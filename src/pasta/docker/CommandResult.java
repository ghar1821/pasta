package pasta.docker;

public class CommandResult {
	private String combined;
	private String output;
	private String error;
	public CommandResult(String combined, String output, String error) {
		this.combined = combined;
		this.output = output;
		this.error = error;
	}
	public String getCombined() {
		return combined;
	}
	public String getOutput() {
		return output;
	}
	public String getError() {
		return error;
	}
}
