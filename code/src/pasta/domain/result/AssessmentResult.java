package pasta.domain.result;

import java.util.ArrayList;
import java.util.Collection;

import pasta.domain.template.Assessment;

public class AssessmentResult {
	private ArrayList<UnitTestResult> unitTests;
	private Assessment assessment;
	
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

	public void addUnitTest(UnitTestResult test){
		unitTests.add(test);
	}
	
	public void removeUnitTest(UnitTestResult test){
		unitTests.remove(test);
	}
	
	public double getMarks(){
		double marks = 0;
		double maxWeight = 0;
		for(UnitTestResult result : unitTests){
//			marks += result.getPercentage()*result.getWeighting();
//			maxWeight += result.getWeighting();
		}
		if(maxWeight == 0){
			return 0;
		}
		marks = (marks / maxWeight) * assessment.getMarks();
		return marks;
	}
}
