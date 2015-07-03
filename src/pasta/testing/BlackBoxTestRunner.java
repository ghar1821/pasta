package pasta.testing;

import java.io.File;
import java.util.List;

import pasta.domain.template.BlackBoxTestCase;

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
	}

	public void setTestData(List<BlackBoxTestCase> testCases) {
		StringBuilder sb = new StringBuilder();
		for(BlackBoxTestCase testCase : testCases) {
			sb.append(testCase.getTestName()).append('|');
			sb.append(testCase.getTimeout()).append('|');
			String commandLine = testCase.getCommandLine().replaceAll("\\s+", " ");
			sb.append(commandLine).append(',');
		}
		if(testCases.size() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		addOption("allTestData", sb.toString());
	}
	
	public void setSolutionName(String solutionName) {
		addOption("solutionName", solutionName);
	}
	
	public void setRunErrorsFile(String filename) {
		addOption("runErrorsFile", filename);
	}
}
