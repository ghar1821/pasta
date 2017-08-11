package pasta.testing;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringEscapeUtils;

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
	}

	public void setTestData(List<BlackBoxTestCase> testCases) {
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
				.append(String.valueOf(testCase.getTimeout())).append("'/>\n");
			properties.append("<property name='bbTestTimeoutSeconds").append(i).append("' value='")
				.append(String.valueOf(testCase.getTimeout() / 1000.0)).append("'/>\n");
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

	@Override
	public String extractCompileErrors(File compileErrorFile, AntResults results) {
		return super.extractCompileErrors(compileErrorFile, results);
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		return super.extractFilesCompiled(results);
	}
}
