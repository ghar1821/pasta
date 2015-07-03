package pasta.testing;

import java.io.File;

import pasta.util.ProjectProperties;

public class ArenaCompetitionRunner extends Runner {

	private static String TEMPLATE_FILENAME = ProjectProperties.getInstance().getProjectLocation() + "build_templates" + File.separator + "arena_comp_template.xml";
	
	public ArenaCompetitionRunner() {
		super(new File(TEMPLATE_FILENAME));
		setMaxRunTime(1800000);
		setRepeats(false);
		setCompetitionCodeLocation("src");
		setOutputFilename("out.txt");
		setErrorFilename("error.txt");
		setMainClassname("tournament.Tournament");
		
		addArgsOption("arenaArgs", "config.txt", "players", "${basedir}");
	}
	
	public void setMaxRunTime(long milliseconds) {
		if(milliseconds >= 0) {
			addOption("compTimeout", String.valueOf(milliseconds));
		}
	}
	
	/**
	 * @param filename relative to the base directory of the test
	 */
	public void setOutputFilename(String filename) {
		while(filename.startsWith("\\") || filename.startsWith("/")) {
			filename = filename.substring(1);
		}
		addOption("outputFilename", filename);
	}
	
	/**
	 * @param filename relative to the base directory of the test
	 */
	public void setErrorFilename(String filename) {
		while(filename.startsWith("\\") || filename.startsWith("/")) {
			filename = filename.substring(1);
		}
		addOption("errorFilename", filename);
	}
	
	public void setRepeats(boolean repeats) {
		addOption("repeats", String.valueOf(repeats));
	}

	public void setMainClassname(String name) {
		name = name.replaceAll("[/\\\\]", ".");
		addOption("mainClassname", name);
	}
	
	/**
	 * @param filename relative to the base directory of the test
	 */
	public void setCompetitionCodeLocation(String directory) {
		while(directory.startsWith("\\") || directory.startsWith("/")) {
			directory = directory.substring(1);
		}
		addOption("compCodeDirectory", directory);
	}

	@Override
	public String extractCompileErrors(AntResults results) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		// TODO Auto-generated method stub
		return "";
	}
}
