package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import pasta.util.PASTAUtil;

public class JUnitTestRunner extends Runner {

	private static String TEMPLATE_FILENAME = "junit_template.xml";
	
	public JUnitTestRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
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
