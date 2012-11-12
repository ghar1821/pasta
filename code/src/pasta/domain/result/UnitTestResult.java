package pasta.domain.result;

import java.util.ArrayList;

import pasta.domain.template.UnitTest;

public class UnitTestResult {
	private String name;
	private double weighting;
	private boolean tested;
	private UnitTest test;
	private boolean secret;
	private ArrayList<UnitTestCaseResult> testCases;
	
	public UnitTestResult(UnitTest test, String name, double weighting, boolean tested, boolean secret){
		this.name = name;
		this.weighting = weighting;
		this.tested = tested;
		this.secret = secret;
		this.test = test;
	}

	public String getName() {
		return name;
	}

	public double getWeighting() {
		return weighting;
	}

	public boolean isTested() {
		return tested;
	}
	
	public boolean isSecret() {
		return secret;
	}

	public UnitTest getTest() {
		return test;
	}
	
	public ArrayList<UnitTestCaseResult> getTestCases() {
		return testCases;
	}

	public double getPercentage(){
		/* TODO #41
		 * improve - make it be able to take a list of possible outputs that are
		 * 			going to mean a correct answer
		 */
		return 0; 	
	}
	
	
}
