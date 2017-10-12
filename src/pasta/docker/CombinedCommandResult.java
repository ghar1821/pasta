package pasta.docker;

public class CombinedCommandResult extends CommandResult {
	private String combined;
	public CombinedCommandResult(String combined, String output, String error) {
		super(output, error);
		this.combined = combined;
	}
	public String getCombined() {
		return combined;
	}
}
