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

import pasta.docker.DockerManager;
import pasta.util.PASTAUtil;
import pasta.util.WhichProgram;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 09 Mar 2017
 */
public class MatlabBlackBoxTestRunner extends BlackBoxTestRunner {

	private static String TEMPLATE_FILENAME = "black_box_matlab_template.xml";
	
	public MatlabBlackBoxTestRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
		addOption("mccPath", convertToExecPath(WhichProgram.getInstance().path("mcc")));
		addOption("matlabInstall", convertToExecPath(WhichProgram.getInstance().path("matlab.install")));
	}
	
	private String convertToExecPath(String file) {
		String matlab = WhichProgram.getInstance().path("matlab.install");
		String important = file.substring(matlab.length());
		return DockerManager.PASTA_BIN + "/MATLAB/" + important;
	}
	
	@Override
	public String extractCompileErrors(File compileErrorsFile, AntResults results) {
		String buildError = extractAntBuildError(results);
		if(buildError != null) {
			return buildError;
		}
		
		Scanner scn = new Scanner(PASTAUtil.scrapeFile(compileErrorsFile));
		StringBuilder compErrors = new StringBuilder();
		String line = "";
		if(scn.hasNext()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine().trim();
				if(line.toLowerCase().startsWith("error in")) {
					break;
				}
				compErrors.append(line).append('\n');
			}
			// Chop off the trailing "\n"
			if(compErrors.length() > 0) {
				compErrors.deleteCharAt(compErrors.length()-1);
			}
		}
		scn.close();
		return compErrors.toString();
	}
}
