package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import pasta.util.PASTAUtil;
import pasta.util.WhichProgram;

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
		addOption("javaPath", WhichProgram.getInstance().path("java"));
		addOption("javacPath", WhichProgram.getInstance().path("javac"));
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
	public String extractCompileErrors(File compileErrorFile, AntResults results) {
		Scanner scn = new Scanner(PASTAUtil.scrapeFile(compileErrorFile));
		StringBuilder compErrors = new StringBuilder();
		if(scn.hasNext()) {
			String line = "";
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				String chopped = line.replaceFirst("\\s*\\[javac\\] ", "");
				if(line.length() == chopped.length()) {
					break;
				}
				compErrors.append(chopped).append(System.lineSeparator());
			}
			// Chop off the trailing "\n"
			compErrors.deleteCharAt(compErrors.length()-1);
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
