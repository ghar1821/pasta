package pasta.domain.result;

import java.util.ArrayList;
import java.util.Collections;

import pasta.domain.template.UnitTest;

public class UnitTestResult implements Comparable{
	private UnitTest test;
	private boolean secret;
	private String compileErrors;
	private String runtimeErrors;
	private ArrayList<UnitTestCaseResult> testCases;

	public UnitTest getTest() {
		return test;
	}

	public void setTest(UnitTest test) {
		this.test = test;
	}

	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	public String getCompileErrors() {
		return compileErrors;
	}

	public void setCompileErrors(String compileErrors) {
		this.compileErrors = compileErrors;
	}

	public String getRuntimeErrors() {
		return runtimeErrors;
	}

	public void setRuntimeErrors(String runtimeErrors) {
		this.runtimeErrors = runtimeErrors;
	}

	public ArrayList<UnitTestCaseResult> getTestCases() {
		return testCases;
	}

	public void setTestCases(ArrayList<UnitTestCaseResult> testCases) {
		this.testCases = testCases;
		Collections.sort(this.testCases);
	}

	public double getPercentage(){
		/* TODO #41
		 * improve - make it be able to take a list of possible outputs that are
		 * 			going to mean a correct answer
		 */
		if(testCases == null || testCases.isEmpty()){
			return 0;
		}
		double passed = 0;
		for(UnitTestCaseResult result: testCases){
			if(result.getTestResult().equals("pass")){
				++passed;
			}
		}
		return passed/testCases.size(); 	
	}

	@Override
	public int compareTo(Object o) {
		UnitTestResult target = (UnitTestResult)(o);
		return test.getName().compareTo(target.getTest().getName());
	}
	
}
