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

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 26 Jun 2015
 */
public class CBlackBoxTestRunner extends BlackBoxTestRunner {

	private static String TEMPLATE_FILENAME = "black_box_c_template.xml";
	
	public CBlackBoxTestRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
		addOption("gccPath", WhichProgram.getInstance().path("gcc"));
	}
	
	public void setGCCArguments(String argsLine) {
		addOption("gccCommandLineArgs", argsLine);
	}

	@Override
	public String extractCompileErrors(File compileErrorFile, AntResults results) {
		Scanner scn = new Scanner(PASTAUtil.scrapeFile(compileErrorFile));
		StringBuilder compErrors = new StringBuilder();
		boolean startedJavacErrors = false;
		if(scn.hasNext()) {
			String line = "";
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				String chopped = line.replaceFirst("\\s*\\[javac\\] ", "");
				if(line.length() == chopped.length()) {
					if(startedJavacErrors) break;
				} else {
					startedJavacErrors = true;
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
		// TODO Extract c-specific files compiled and append super files to those
		return super.extractFilesCompiled(results);
	}
}
