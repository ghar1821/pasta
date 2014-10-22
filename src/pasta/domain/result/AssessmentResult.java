/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.domain.result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;

import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
/**
 * Container for the results of an assessment.
 * <p>
 * Contains the collection of:
 * <ul>
 * 	<li>Unit test results</li>
 * 	<li>Hand marking results</li>
 * 	<li>Link to the assessment</li>
 * 	<li>Number of submissions made</li>
 * 	<li>Time stamp of the submission</li>
 * 	<li>Due date of the assessment</li>
 * 	<li>Feedback comments</li>
 * </ul>
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
public class AssessmentResult {
	private ArrayList<UnitTestResult> unitTests;
	private List<HandMarkingResult> handMarkingResults = LazyList.decorate(new ArrayList<HandMarkingResult>(),
			FactoryUtils.instantiateFactory(HandMarkingResult.class));
	private Assessment assessment;
	private int submissionsMade;
	private Date submissionDate;
	private Date dueDate;
	private String comments;
	
	public Collection<UnitTestResult> getUnitTests() {
		return unitTests;
	}

	public void setUnitTests(ArrayList<UnitTestResult> unitTests) {
		this.unitTests = unitTests;
		Collections.sort(this.unitTests);
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
	/**
	 * Formatted submission date for easy lexographical comparison
	 * <p>
	 * Format: yyyy-MM-dd'T'HH-mm-ss e.g. 2014-03-21T09-00-00
	 * @return formatted submission date
	 */
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
	
	/**
	 * Check if there is a compile error in any of the unit test results
	 * @return if any of the unit test results have compilation errors
	 */
	public boolean isCompileError() {
		for(UnitTestResult result : unitTests){
			if(result.getCompileErrors()!= null && !result.getCompileErrors().isEmpty()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the aggregated compilation errors.
	 * <p>
	 * Iterates over all of the unit test results and compiles all of the 
	 * compilation errors across all of the unit tests that have compilation
	 * errors. 
	 * @return the aggregated compilation errors for the submission.
	 */
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
		return getPercentage() * assessment.getMarks();
	}
	
	public double getAutoMarks(){
		return getAutoPercentage() * assessment.getMarks();
	}
	
	public double getPercentage(){
		double marks = 0;
		double maxWeight = getAssessmentHandMarkingWeight() 
				+ getAssessmentUnitTestsWeight()
				+ getAssessmentCompetitionWeight();
		// unit tests
		// regular
		for(UnitTestResult result : unitTests){
			try{
				marks += result.getPercentage()*assessment.getWeighting(result.getTest());
			}
			catch(Exception e){
				// ignore anything that throws exceptions
			}
		}
		
		// hand marking
		for(HandMarkingResult result : handMarkingResults){
			try{
				marks += result.getPercentage()*assessment.getWeighting(result.getMarkingTemplate());
			}
			catch(Exception e){
				// ignore anything that throws exceptions (probably a partially marked submission)
			}
		}
				
		if(maxWeight == 0){
			return 0;
		}
		return (marks / maxWeight);
	}
	
	public double getAutoPercentage(){
		double marks = 0;
		double maxWeight = getAssessmentHandMarkingWeight() 
				+ getAssessmentUnitTestsWeight()
				+ getAssessmentCompetitionWeight();
		// unit tests
		// regular
		for(UnitTestResult result : unitTests){
			try{
				marks += result.getPercentage()*assessment.getWeighting(result.getTest());
			}
			catch(Exception e){
				// ignore anything that throws exceptions
			}
		}
		
		if(maxWeight == 0){
			return 0;
		}
		return (marks / maxWeight);
	}
	
	private double getAssessmentHandMarkingWeight(){
		double weight = 0;
		if(assessment.getHandMarking() != null){
			for(WeightedHandMarking marking: assessment.getHandMarking()){
				weight += marking.getWeight();
			}
		}
		return weight;
	}
	
	private double getAssessmentUnitTestsWeight(){
		double weight = 0;
		if(assessment.getAllUnitTests() != null){
			for(WeightedUnitTest marking: assessment.getAllUnitTests()){
				weight += marking.getWeight();
			}
		}
		return weight;
	}
	
	private double getAssessmentCompetitionWeight(){
		double weight = 0;
		if(assessment.getCompetitions() != null){
			for(WeightedCompetition marking: assessment.getCompetitions()){
				weight += marking.getWeight();
			}
		}
		return weight;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public boolean isFinishedHandMarking(){
		if(handMarkingResults.size() < assessment.getHandMarking().size()){
			return false;
		}
		if(handMarkingResults != null){
			for(HandMarkingResult res : handMarkingResults){
				if(res == null || !res.isFinishedMarking()){
					return false;
				}
			}
		}
		return true;
	}

}
