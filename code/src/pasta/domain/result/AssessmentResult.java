package pasta.domain.result;

import java.util.ArrayList;
import java.util.Collection;

import pasta.domain.template.Assessment;

public class AssessmentResult {
	private ArrayList<UnitTestResult> unitTests;
	private Assessment assessment;
	private int submissionsMade;
	
	public Collection<UnitTestResult> getUnitTests() {
		return unitTests;
	}

	public void setUnitTests(ArrayList<UnitTestResult> unitTests) {
		this.unitTests = unitTests;
	}

	public Assessment getAssessment() {
		return assessment;
	}

	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	public int getSubmissionsMade() {
		return submissionsMade;
	}

	public void setSubmissionsMade(int submissionsMade) {
		this.submissionsMade = submissionsMade;
	}

	public void addUnitTest(UnitTestResult test){
		unitTests.add(test);
	}
	
	public void removeUnitTest(UnitTestResult test){
		unitTests.remove(test);
	}
	
	public boolean isCompileError() {
		for(UnitTestResult result : unitTests){
			if(result.getCompileErrors()!= null && !result.getCompileErrors().isEmpty()){
				return true;
			}
		}
		return false;
	}

	public double getMarks(){
		double marks = 0;
		double maxWeight = 0;
		for(UnitTestResult result : unitTests){
			marks += result.getPercentage()*assessment.getWeighting(result.getTest());
			maxWeight += assessment.getWeighting(result.getTest());
		}
		if(maxWeight == 0){
			return 0;
		}
		marks = (marks / maxWeight) * assessment.getMarks();
		return marks;
	}

}
