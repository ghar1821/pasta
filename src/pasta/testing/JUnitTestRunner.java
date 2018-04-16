/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
		setMaxRunTime(10000L);
		addOption("testOutputFile", "result");
		addOption("testName", "TestName");
		addOption("filterStackTraces", "yes");
		addOption("javaPath", WhichProgram.getInstance().path("java"));
		addOption("javacPath", WhichProgram.getInstance().path("javac"));
	}
	
	public void setMaxRunTime(Long milliseconds) {
		if(milliseconds != null && milliseconds >= 0) {
			addOption("advancedTimeout", String.valueOf(milliseconds));
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
		String buildError = extractAntBuildError(results);
		StringBuilder compErrors = new StringBuilder();
		if(buildError != null && !buildError.isEmpty()) {
			compErrors.append(buildError).append(System.lineSeparator());
		}
		
		Scanner scn = new Scanner(PASTAUtil.scrapeFile(compileErrorFile));
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
		}
		
		// Chop off the trailing "\n"
		if(compErrors.length() > 0) {
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
	
	protected String extractAntBuildError(AntResults results) {
		String buildOutput = results.getOutput("build");
		if(buildOutput.contains("BUILD FAILED")) {
			String failureText = buildOutput.split("BUILD FAILED")[1].trim();
			String firstLine = failureText.split("[\r\n]+")[0];
			return firstLine.replaceFirst(".*/build.xml:[0-9]+:", "").trim();
		}
		return null;
	}
}
