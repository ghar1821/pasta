package pasta.testing;

import java.io.File;

import pasta.util.ProjectProperties;

public class JUnitTestRunner extends Runner {

	private static String TEMPLATE_FILENAME = ProjectProperties.getInstance().getProjectLocation() + "build_templates" + File.separator + "junit_template.xml";
	
	public JUnitTestRunner() {
		super(new File(TEMPLATE_FILENAME));
		setMaxRunTime(60000);
		addOption("outputFilename", "result");
		addOption("testName", "TestName");
		addOption("submissionDirectory", "src");
		addOption("testDirectory", "test");
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
	 * Note: assumes setTestDirectory(String) has been called previously.
	 * 
	 * <ul>
	 * <li> If testDirectory is "test", and name is "/test/foo/Bar.java", name will be converted to foo.Bar.java
	 * <li> If testDirectory is "test/", and name is "/test/foo/Bar.java", name will be converted to foo.Bar.java
	 * <li> If testDirectory is "test", and name is "ran/foo/Bar.java", name will be converted to ran.foo.Bar.java
	 * </ul>
	 * 
	 * @param name the class name to use
	 */
	public void setMainTestClassname(String name) {
		String testDir = getOption("testDirectory");
		if(name.replaceAll("[\\\\/]", "").startsWith(testDir.replaceAll("[\\\\/]", ""))) {
			for(int i = 0; i < name.length() - testDir.length() + 1; i++) {
				if(name.startsWith(testDir, i)) {
					name = name.substring(i + testDir.length());
					break;
				}
			}
		}
		int firstGoodChar = 0;
		for(; firstGoodChar < name.length(); firstGoodChar++) {
			if(!(name.charAt(firstGoodChar) == '\\' || name.charAt(firstGoodChar) == '/')) {
				break;
			}
		}
		name = name.substring(firstGoodChar).replaceAll("[/\\\\]", ".");
		addOption("testName", name);
	}
	
	public void setSubmissionDirectory(String directory) {
		addOption("submissionDirectory", directory);
	}
	
	public void setTestDirectory(String directory) {
		addOption("testDirectory", directory);
	}
}
