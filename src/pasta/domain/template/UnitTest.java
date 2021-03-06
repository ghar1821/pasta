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

package pasta.domain.template;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Container class for a unit test. Contains the name of the unit test and
 * whether it has been tested. String representation:
 * 
 * <pre>
 * {@code <unitTestProperties>
 * 	<name>name</name>
 * 	<tested>true|false</tested>
 * </unitTestProperties>}
 * </pre>
 * <p>
 * File location on disk: $projectLocation$/template/unitTest/$unitTestId$
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 */

@Entity
@Table (name = "unit_tests")
@VerboseName("unit test module")
public class UnitTest extends BaseEntity implements Comparable<UnitTest> {
	
	private static final long serialVersionUID = -7413957282304051135L;
	
	public static String BB_INPUT_FILENAME = "bbinput";
	public static String BB_EXPECTED_OUTPUT_FILENAME = "bbexpected";
	public static String BB_OUTPUT_FILENAME = "userout";
	public static String BB_META_FILENAME = "usermeta";

	private String name;
	
	private boolean tested;
	
	@Column (name = "black_box_timeout")
	private Long blackBoxTimeout;
	
	@Column (name = "advanced_timeout")
	private Long advancedTimeout;
	
	@Column (name = "main_class_name")
	private String mainClassName;
	
	@Column (name = "submission_code_root")
	private String submissionCodeRoot;
	
	@Column (name = "allow_accessory_write")
	private boolean allowAccessoryFileWrite;
	
	@OneToOne (cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "testerTest", optional = true)
	private UnitTestResult testResult;
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name="unit_test_id")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<BlackBoxTestCase> testCases;
	
	@OneToOne (cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "test")
	private BlackBoxOptions blackBoxOptions;

	/**
	 * Default constructor
	 * <p>
	 * name="Default" tested=false
	 */
	public UnitTest() {
		init("Default", false);
	}

	public UnitTest(String name, boolean tested) {
		init(name, tested);
	}
	
	public void init(String name, boolean tested) {
		this.name = name;
		this.blackBoxTimeout = null;
		this.advancedTimeout = null;
		this.tested = tested;
		this.submissionCodeRoot = "";
		this.testCases = new ArrayList<BlackBoxTestCase>();
		setBlackBoxOptions(new BlackBoxOptions());
		this.allowAccessoryFileWrite = false;
	}

	public String getName() {
		return name;
	}

	public String getFileAppropriateName() {
		return name.replaceAll("[^\\w]+", "");
	}

	public String getFileLocation() {
		return ProjectProperties.getInstance().getUnitTestsLocation() + getId();
	}
	
	public String getRelativeFileLocation() {
		String location = getFileLocation();
		String base = ProjectProperties.getInstance().getProjectLocation();
		if (location.startsWith(base)) {
			return location.substring(base.length());
		}
		return location;
	}

	public File getCodeLocation() {
		return new File(getFileLocation(), "code");
	}
	
	public File getAccessoryLocation() {
		return new File(getFileLocation(), "accessory");
	}
	
	public File getGeneratedCodeLocation() {
		return new File(getFileLocation(), "generated");
	}
	
	public boolean hasCode() {
		return getCodeLocation().exists();
	}
	public boolean isHasCode() {
		return hasCode();
	}
	public boolean hasAccessoryFiles() {
		return getAccessoryLocation().exists();
	}
	public boolean isHasAccessoryFiles() {
		return hasAccessoryFiles();
	}

	public boolean isTested() {
		return tested;
	}

	public void setTested(boolean tested) {
		this.tested = tested;
	}
		
	/**
	 * Get a list of the most recent test case names. These will be retrieved
	 * according to the last run test on the unit test. If the unit test has not
	 * yet been tested, there will be no test result yet.
	 * 
	 * @return a list of unit test names
	 */
	public List<String> getAllTestNames() {
		List<String> names = new LinkedList<String>();
		if(testResult != null && testResult.getTestCases() != null) {
			for(UnitTestCaseResult result : testResult.getTestCases()) {
				names.add(result.getTestName());
			}
		}
		return names;
	}

	@Override
	public int compareTo(UnitTest other) {
		if(other == null) {
			return 1;
		}
		return this.name.compareTo(other.name);
	}

	public String getMainClassName() {
		return mainClassName;
	}

	public void setMainClassName(String mainClassName) {
		this.mainClassName = mainClassName;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public UnitTestResult getTestResult() {
		return testResult;
	}

	public void setTestResult(UnitTestResult testResult) {
		if(this.testResult != null) {
			this.testResult.setTesterTest(null);
		}
		if(testResult != null) {
			testResult.setTesterTest(this);
		}
		this.testResult = testResult;
	}
	
	public String getSubmissionCodeRoot() {
		return submissionCodeRoot;
	}
	public void setSubmissionCodeRoot(String submissionCodeRoot) {
		while(submissionCodeRoot.matches("[/\\\\]+.*")) {
			submissionCodeRoot = submissionCodeRoot.substring(1);
		}
		this.submissionCodeRoot = submissionCodeRoot;
	}
	
	public File getSubmissionCodeLocation(File submissionRoot) {
		return new File(submissionRoot, submissionCodeRoot);
	}
	
	public List<BlackBoxTestCase> getTestCases() {
		return testCases;
	}
	public void setTestCases(List<BlackBoxTestCase> testCases) {
		if(this.testCases == null || testCases == null) {
			this.testCases = testCases;
		} else {
			this.testCases.clear();
			this.testCases.addAll(testCases);
		}
	}

	public BlackBoxOptions getBlackBoxOptions() {
		if(blackBoxOptions == null) {
			setBlackBoxOptions(new BlackBoxOptions());
		}
		return blackBoxOptions;
	}

	public void setBlackBoxOptions(BlackBoxOptions blackBoxOptions) {
		if(this.blackBoxOptions != null) {
			this.blackBoxOptions.setTest(null);
		}
		blackBoxOptions.setTest(this);
		this.blackBoxOptions = blackBoxOptions;
	}

	public boolean isAllowAccessoryFileWrite() {
		return allowAccessoryFileWrite;
	}

	public void setAllowAccessoryFileWrite(boolean allowAccessoryFileWrite) {
		this.allowAccessoryFileWrite = allowAccessoryFileWrite;
	}
	
	public Long getBlackBoxTimeout() {
		if(getTestCases().isEmpty()) {
			return null;
		}
		return blackBoxTimeout;
	}
	public void setBlackBoxTimeout(Long blackBoxTimeout) {
		this.blackBoxTimeout = blackBoxTimeout;
	}

	public Long getAdvancedTimeout() {
		if(!hasCode() || getMainClassName() == null) {
			return null;
		}
		return advancedTimeout;
	}
	public void setAdvancedTimeout(Long advancedTimeout) {
		this.advancedTimeout = advancedTimeout;
	}

	public boolean hasBlackBoxTests() {
		return !getTestCases().isEmpty();
	}
	public boolean hasBlackBoxTestsWithOutputCheck() {
		for(BlackBoxTestCase testCase : testCases) {
			if(testCase.isToBeCompared()) {
				return true;
			}
		}
		return false;
	}
	public boolean isHasBlackBoxTests() {
		return hasBlackBoxTests();
	}

	public Map<String, String> getTestDescriptions() {
		Map<String, String> testDescriptions = PASTAUtil.extractTestDescriptions(getCodeLocation());
		for(BlackBoxTestCase testCase : getTestCases()) {
			String desc = testCase.getDescription();
			if(desc != null) {
				testDescriptions.put(testCase.getTestName(), desc);
			}
		}
		return testDescriptions;
	}
	
	public File getMainSourceFile() {
		String mainClass = getMainClassName();
		if(!hasCode() || mainClass == null || mainClass.isEmpty()) {
			return null;
		}
		File base = getCodeLocation();
		Map<File, String> fullNames = PASTAUtil.mapJavaFilesToQualifiedNames(base);
		for(File file : fullNames.keySet()) {
			if(fullNames.get(file).equals(mainClass)) {
				return file;
			}
		}
		return null;
	}
	
	/*===========================
	 * CONVENIENCE RELATIONSHIPS
	 * 
	 * Making unidirectional many-to-one relationships into bidirectional 
	 * one-to-many relationships for ease of deletion by Hibernate
	 *===========================
	 */
	@OneToMany(mappedBy = "test", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<WeightedUnitTest> weightedUnitTests;
}
