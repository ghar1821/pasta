package pasta.testing.options;

import java.io.Serializable;

public class CalculatedCompetitionOptions implements Serializable {

	private static final long serialVersionUID = 3609336305981667120L;
	
	private ScriptOptions runOptions;
	private ScriptOptions buildOptions;
	
	public ScriptOptions getRunOptions() {
		return runOptions;
	}
	public void setRunOptions(ScriptOptions runOptions) {
		this.runOptions = runOptions;
	}
	public ScriptOptions getBuildOptions() {
		return buildOptions;
	}
	public void setBuildOptions(ScriptOptions buildOptions) {
		this.buildOptions = buildOptions;
	}
	
	public boolean hasRunOptions() {
		return runOptions != null;
	}
	public boolean hasBuildOptions() {
		return buildOptions != null;
	}
}
