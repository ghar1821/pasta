package pasta.domain.result;

import java.util.ArrayList;

import pasta.domain.template.Assessment;

public class AssessmentResult {
	private ArrayList<UnitTestResult> unitTests;
	private String name;
	private Assessment assessment;
	
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
			marks += result.getPercentage()*result.getWeighting();
			maxWeight += result.getWeighting();
		}
		marks = (marks / maxWeight) * assessment.getMarks();
		return marks;
	}
}
