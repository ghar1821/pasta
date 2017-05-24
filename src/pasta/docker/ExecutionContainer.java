package pasta.docker;

import java.io.File;

public class ExecutionContainer {

	private String id;
	private String label;
	private File srcLoc;
	private File outLoc;
	private Language language;
	
	public ExecutionContainer(String label, File srcLoc, File outLoc) {
		this.label = label;
		this.srcLoc = srcLoc;
		this.outLoc = outLoc;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	
	public String getId() {
		return id;
	}
	public String getLabel() {
		return label;
	}
	public File getSrcLoc() {
		return srcLoc;
	}
	public File getOutLoc() {
		return outLoc;
	}
	public Language getLanguage() {
		return language;
	}
	
	public String getImageName() {
		return language.getImageName();
	}
}
