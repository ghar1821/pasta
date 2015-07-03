package pasta.testing;

import java.io.File;
import java.util.Scanner;

import pasta.util.ProjectProperties;

public class JUnitTestRunner extends Runner {

	private static String TEMPLATE_FILENAME = ProjectProperties.getInstance().getProjectLocation() + "build_templates" + File.separator + "junit_template.xml";
	
	public JUnitTestRunner() {
		super(new File(TEMPLATE_FILENAME));
		init();
	}
	
	public JUnitTestRunner(File file) {
		super(file);
		init();
	}
	
	private void init() {
		setMaxRunTime(60000);
		addOption("testOutputFile", "result");
		addOption("testName", "TestName");
		addOption("filterStackTraces", "yes");
	}
	
	public void setMaxRunTime(long milliseconds) {
		if(milliseconds >= 0) {
			addOption("maxTimeAllowed", String.valueOf(milliseconds));
		}
	}
	
	public void setFilterStackTraces(boolean filter) {
		addOption("filterStackTraces", filter ? "yes" : "no");
	}
	
	public void setOutputFilename(String filename) {
		if(filename.endsWith(".xml")) {
			filename = filename.substring(0, filename.length() - 4);
		}
		addOption("outputFilename", filename);
	}
	
	/**
	 * @param name the class name to use
	 */
	public void setMainTestClassname(String name) {
		int firstGoodChar = 0;
		for(; firstGoodChar < name.length(); firstGoodChar++) {
			if(!(name.charAt(firstGoodChar) == '\\' || name.charAt(firstGoodChar) == '/')) {
				break;
			}
		}
		name = name.substring(firstGoodChar).replaceAll("[/\\\\]", ".");
		addOption("testName", name);
	}

	@Override
	public String extractCompileErrors(AntResults results) {
		Scanner scn = new Scanner(results.getOutput("build"));
		StringBuilder compErrors = new StringBuilder();
		String line = "";
		if(scn.hasNext()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				if(line.contains("error")) {
					break;
				}
			}
		}
		if(scn.hasNextLine()) {
			compErrors.append(line.replaceFirst("\\s*\\[javac\\]", "")).append('\n');
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				compErrors.append(line.replaceFirst("\\s*\\[javac\\]", "")).append('\n');
			}
			// Chop off the trailing "\n"
			compErrors.replace(compErrors.length()-1, compErrors.length(), "");
		}
		scn.close();
		return compErrors.toString();
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		Scanner scn = new Scanner(results.getOutput("build"));
		StringBuilder files = new StringBuilder();
		String line = "";
		if(scn.hasNext()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				if(line.contains("Files to be compiled")) {
					break;
				}
			}
		}
		if(scn.hasNextLine()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				if(line.contains("error")) {
					break;
				}
				files.append(line.replaceFirst("\\s*\\[javac\\]", "").trim()).append('\n');
			}
			// Chop off the trailing "\n"
			files.replace(files.length()-1, files.length(), "");
		}
		scn.close();
		return files.toString();
	}
}
