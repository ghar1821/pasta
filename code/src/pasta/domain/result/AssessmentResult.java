package pasta.domain.result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;

import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedUnitTest;

public class AssessmentResult {
	private ArrayList<UnitTestResult> unitTests;
	private List<HandMarkingResult> handMarkingResults = LazyList.decorate(new ArrayList<HandMarkingResult>(),
			FactoryUtils.instantiateFactory(HandMarkingResult.class));
	private Assessment assessment;
	private int submissionsMade;
	private Date submissionDate;
	private String comments;
	
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

	public Date getSubmissionDate() {
		return submissionDate;
	}
	
	public String getFormattedSubmissionDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		return sdf.format(submissionDate);
	}

	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}
	
	public List<HandMarkingResult> getHandMarkingResults() {
		return handMarkingResults;
	}

	public void setHandMarkingResults(List<HandMarkingResult> handMarkingResults) {
		this.handMarkingResults.clear();
		this.handMarkingResults.addAll(handMarkingResults);
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
	
	public String getCompilationError() {
		String compilationError = "";
		for(UnitTestResult result : unitTests){
			if(result.getCompileErrors()!= null && !result.getCompileErrors().isEmpty()){
				compilationError += result.getCompileErrors() 
						+ System.getProperty("line.separator")
						+ System.getProperty("line.separator")
						+ System.getProperty("line.separator");
			}
		}
		return compilationError;
	}

	public double getMarks(){
		double marks = 0;
		double maxWeight = 0;
		// unit tests
		// regular
		for(UnitTestResult result : unitTests){
			marks += result.getPercentage()*assessment.getWeighting(result.getTest());
			maxWeight += assessment.getWeighting(result.getTest());
		}
		
		// hand marking
		for(HandMarkingResult result : handMarkingResults){
			marks += result.getPercentage()*assessment.getWeighting(result.getMarkingTemplate());
			maxWeight += assessment.getWeighting(result.getMarkingTemplate());
		}
		
		if(maxWeight == 0){
			return 0;
		}
		marks = (marks / maxWeight) * assessment.getMarks();
		return marks;
	}
	
	public double getPercentage(){
		double marks = 0;
		double maxWeight = 0;
		// unit tests
		// regular
		for(UnitTestResult result : unitTests){
			marks += result.getPercentage()*assessment.getWeighting(result.getTest());
			maxWeight += assessment.getWeighting(result.getTest());
		}
		
		// hand marking
		for(HandMarkingResult result : handMarkingResults){
			marks += result.getPercentage()*assessment.getWeighting(result.getMarkingTemplate());
			maxWeight += assessment.getWeighting(result.getMarkingTemplate());
		}
		
		if(maxWeight == 0){
			return 0;
		}
		return (marks / maxWeight);
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
