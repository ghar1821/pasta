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
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringEscapeUtils;

import pasta.docker.Language;
import pasta.domain.template.BlackBoxTestCase;
import pasta.domain.template.UnitTest;
import pasta.util.WhichProgram;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 26 Jun 2015
 */
public abstract class BlackBoxTestRunner extends JUnitTestRunner {

	public BlackBoxTestRunner(File file) {
		super(file);
		addOption("allTestData", "");
		addOption("solutionName", "Solution");
		addOption("runErrorsFile", "run.errors");
		addOption("bbuseroutfile", UnitTest.BB_OUTPUT_FILENAME);
		addOption("bbexpectedfile", UnitTest.BB_EXPECTED_OUTPUT_FILENAME);
		addOption("bbinputfile", UnitTest.BB_INPUT_FILENAME);
		addOption("bbmetafile", UnitTest.BB_META_FILENAME);
		addOption("timeoutPath", WhichProgram.getInstance().path("timeout"));
		addOption("timePath", WhichProgram.getInstance().path("time"));
		addOption("carryDir", "pbbt_carry");
	}

	public void setTestData(List<BlackBoxTestCase> testCases, Language language) {
		StringBuilder indices = new StringBuilder();
		StringBuilder properties = new StringBuilder();
		for(int i = 0; i < testCases.size(); i++) {
			if(i > 0) {
				indices.append(",");
			}
			indices.append(i);
			BlackBoxTestCase testCase = testCases.get(i);
			properties.append("<property name='bbTestName").append(i).append("' value='")
				.append(testCase.getTestName()).append("'/>\n");
			properties.append("<property name='bbTestTimeout").append(i).append("' value='")
				.append(String.valueOf(testCase.getTimeout() + language.getTestCaseExecutionOverhead())).append("'/>\n");
			properties.append("<property name='bbTestTimeoutSeconds").append(i).append("' value='")
				.append(String.valueOf((testCase.getTimeout() + language.getTestCaseExecutionOverhead()) / 1000.0)).append("'/>\n");
			String commandLine = StringEscapeUtils.escapeXml(Optional.ofNullable(testCase.getCommandLine()).orElse(""));
			properties.append("<property name='bbTestCommandLine").append(i).append("' value='")
				.append(commandLine).append("'/>\n");
		}
		addOption("bbTestCaseProperties", properties.toString());
		addOption("bbTestIndices", indices.toString());
	}
	
	public void setSolutionName(String solutionName) {
		addOption("solutionName", solutionName);
	}
	
	public void setRunErrorsFile(String filename) {
		addOption("runErrorsFile", filename);
	}
	
	public void setTimeout(Long timeout) {
		if(timeout == null) {
			throw new NullPointerException("Timeout cannot be null");
		}
		addOption("blackBoxTimeout", timeout.toString());
	}

	@Override
	public String extractCompileErrors(File compileErrorFile, AntResults results) {
		return super.extractCompileErrors(compileErrorFile, results);
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		return super.extractFilesCompiled(results);
	}
}
