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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.user.PASTAUser;
import pasta.util.ProjectProperties;
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
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
@Entity
@Table(name = "assessment_results",
uniqueConstraints = { @UniqueConstraint(columnNames={
		"user_id", "assessment_id", "submission_date"
})})
public class AssessmentResult implements Serializable, Comparable<AssessmentResult>{

	private static final long serialVersionUID = 447867201394779087L;

	@Id
	@GeneratedValue 
	private long id;
	
	@ManyToOne
	@JoinColumn(name="user_id", nullable = false)
	private PASTAUser user;
	
	@ManyToOne
	@JoinColumn(name="submitted_by", nullable = false)
	private PASTAUser submittedBy;
	
	@OneToMany (cascade = CascadeType.ALL)
	@JoinTable(name="assessment_result_unit_test_joins",
		joinColumns=@JoinColumn(name = "assessment_result_id"),
		inverseJoinColumns=@JoinColumn(name = "unit_test_result_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<UnitTestResult> unitTests = new ArrayList<UnitTestResult>();
	
	@OneToMany (cascade = CascadeType.ALL)
	@JoinTable(name="assessment_result_hand_marking_joins",
		joinColumns=@JoinColumn(name = "assessment_result_id"),
		inverseJoinColumns=@JoinColumn(name = "hand_marking_result_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<HandMarkingResult> handMarkingResults = new ArrayList<HandMarkingResult>();
	
	@ManyToOne
	@JoinColumn (name = "assessment_id", nullable = false)
	private Assessment assessment;
	
	@Column (name = "submission_date", nullable = false)
	private Date submissionDate = new Date();
	
	@Column (length=64000)
	@Size (max = 64000)
	private String comments;

	@Column(name="waiting_to_run")
	private boolean waitingToRun = true;
	
	@Column(name="group_result")
	private boolean groupResult;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public PASTAUser getUser() {
		return user;
	}
	public void setUser(PASTAUser user) {
		this.user = user;
	}
	
	public PASTAUser getSubmittedBy() {
		return submittedBy;
	}
	public void setSubmittedBy(PASTAUser submittedBy) {
		this.submittedBy = submittedBy;
	}
	
	public Collection<UnitTestResult> getUnitTests() {
		return unitTests;
	}
	public List<UnitTestResult> getGroupUnitTests() {
		List<UnitTestResult> tests = new LinkedList<UnitTestResult>();
		for(UnitTestResult result : getUnitTests()) {
			if(result.isGroupWork()) {
				tests.add(result);
			}
		}
		return tests;
	}
	public List<UnitTestResult> getNonGroupUnitTests() {
		List<UnitTestResult> tests = new LinkedList<UnitTestResult>();
		for(UnitTestResult result : getUnitTests()) {
			if(!result.isGroupWork()) {
				tests.add(result);
			}
		}
		return tests;
	}
	public List<HandMarkingResult> getGroupHandMarkingResults() {
		List<HandMarkingResult> tests = new LinkedList<HandMarkingResult>();
		for(HandMarkingResult result : getHandMarkingResults()) {
			if(result.isGroupWork()) {
				tests.add(result);
			}
		}
		return tests;
	}
	public List<HandMarkingResult> getNonGroupHandMarkingResults() {
		List<HandMarkingResult> tests = new LinkedList<HandMarkingResult>();
		for(HandMarkingResult result : getHandMarkingResults()) {
			if(!result.isGroupWork()) {
				tests.add(result);
			}
		}
		return tests;
	}
	public void setUnitTests(List<UnitTestResult> unitTests) {
		this.unitTests.clear();
		this.unitTests.addAll(unitTests);
		Collections.sort(this.unitTests);
	}

	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	public int getIndividualSubmissionsMade() {
		if(user == null || assessment == null) {
			return 0;
		}
		return ProjectProperties.getInstance().getResultDAO()
				.getSubmissionCount(user, assessment.getId(), false);
	}
	
	public int getSubmissionsMade() {
		if(user == null || assessment == null) {
			return 0;
		}
		return ProjectProperties.getInstance().getResultDAO()
				.getSubmissionCount(user, assessment.getId(), true);
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
		return sdf.format(getSubmissionDate());
	}
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = new java.sql.Date(submissionDate.getTime());
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
	 * Check if there is any error in any of the unit test results
	 * @return if any of the unit test results have errors
	 */
	public boolean isError() {
		for(UnitTestResult result : unitTests){
			if(result.isError()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if there is a compile error in any of the unit test results
	 * @return if any of the unit test results have compilation errors
	 */
	public boolean isCompileError() {
		for(UnitTestResult result : unitTests){
			if(result.isCompileError()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the set aggregated (unique) compilation errors.
	 * <p>
	 * Iterates over all of the unit test results and compiles all of the 
	 * compilation errors across all of the unit tests that have compilation
	 * errors. 
	 * @return the aggregated (unique) compilation errors for the submission.
	 */
	public String getCompilationError() {
		boolean first = true;
		String compilationError = "";
		for(String compileError : getCompilationErrors()){
			if(first) {
				first = false;
			} else {
				compilationError += System.lineSeparator() + System.lineSeparator();
			}
			compilationError += compileError;
		}
		return compilationError;
	}
	
	public Collection<String> getCompilationErrors() {
		LinkedHashSet<String> uniqueErrors = new LinkedHashSet<String>();
		for(UnitTestResult result : unitTests){
			if(result.getCompileErrors()!= null && !result.getCompileErrors().isEmpty()){
				uniqueErrors.add(result.getCompileErrors());
			}
		}
		return uniqueErrors;
	}
	
	/**
	 * Check if there is a runtime error in any of the unit test results
	 * @return if any of the unit test results have runtime errors
	 */
	public boolean isRuntimeError() {
		for(UnitTestResult result : unitTests){
			if(result.isRuntimeError()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the set aggregated (unique) runtime errors.
	 * <p>
	 * Iterates over all of the unit test results and compiles all of the 
	 * runtime errors across all of the unit tests that have runtime
	 * errors. 
	 * @return the aggregated (unique) runtime errors for the submission.
	 */
	public String getRuntimeError() {
		boolean first = true;
		String runtimeErrors = "";
		for(String runtimeError : getRuntimeErrors()){
			if(first) {
				first = false;
			} else {
				runtimeErrors += System.lineSeparator() + System.lineSeparator();
			}
			runtimeErrors += runtimeError;
		}
		return runtimeErrors;
	}
	
	public Collection<String> getRuntimeErrors() {
		LinkedHashSet<String> uniqueErrors = new LinkedHashSet<String>();
		for(UnitTestResult result : unitTests){
			if(result.getRuntimeErrors()!= null && !result.getRuntimeErrors().isEmpty()){
				uniqueErrors.add(result.getRuntimeErrors());
			}
		}
		return uniqueErrors;
	}
	
	/**
	 * Check if there is a validation error in any of the unit test results
	 * @return if any of the unit test results have validation errors
	 */
	public boolean isValidationError() {
		for(UnitTestResult result : unitTests){
			if(result.isValidationError()){
				return true;
			}
		}
		return false;
	}
	
	public Map<String, Set<String>> getValidationErrors() {
		Map<String, Set<String>> errors = new HashMap<String, Set<String>>();
		for(UnitTestResult result : unitTests){
			for(ResultFeedback error : result.getValidationErrors()) {
				Set<String> categoryErrors = errors.get(error.getCategory());
				if(categoryErrors == null) {
					categoryErrors = new TreeSet<String>();
					errors.put(error.getCategory(), categoryErrors);
				}
				categoryErrors.add(error.getFeedback());
			}
		}
		return errors;
	}

	/**
	 * Get the reason that the test has errors.
	 * 
	 * @return the first error reason encountered when looking through the unit
	 *         test results; null if none are found
	 */
	public String getErrorReason() {
		for(UnitTestResult result : unitTests){
			String reason = result.getErrorReason();
			if(reason != null) {
				return reason;
			}
		}
		return null;
	}
	
	public double getMarks(){
		return getPercentage() * assessment.getMarks();
	}
	
	public double getAutoMarks(){
		return getAutoMarkAsPercentageOfTotal() * assessment.getMarks();
	}
	
	public double getHandMarks(){
		return getHandMarkAsPercentageOfTotal() * assessment.getMarks();
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
				marks += result.getPercentage()*assessment.getWeighting(result.getHandMarking());
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
	
	private double getTotalMaxWeight() {
		return getAssessmentHandMarkingWeight() 
				+ getAssessmentUnitTestsWeight()
				+ getAssessmentCompetitionWeight();
	}
	public double getAutoMarkAsPercentageOfTotal(){
		return getPercentage(getRawAutoMarks(), getTotalMaxWeight());
	}
	public double getAutoMarkPercentage(){
		return getPercentage(getRawAutoMarks(), getAssessmentUnitTestsWeight());
	}
	private double getRawAutoMarks() {
		double marks = 0;
		for(UnitTestResult result : unitTests){
			try{
				marks += result.getPercentage()*assessment.getWeighting(result.getTest());
			}
			catch(Exception e){
				// ignore anything that throws exceptions
			}
		}
		return marks;
	}
	
	public double getHandMarkAsPercentageOfTotal(){
		return getPercentage(getRawHandMarks(), getTotalMaxWeight());
	}
	public double getHandMarkPercentage(){
		return getPercentage(getRawHandMarks(), getAssessmentHandMarkingWeight());
	}
	private double getRawHandMarks() {
		double marks = 0;
		for(HandMarkingResult result : handMarkingResults){
			try{
				marks += result.getPercentage()*assessment.getWeighting(result.getHandMarking());
			}
			catch(Exception e){
				// ignore anything that throws exceptions
			}
		}
		return marks;
	}
	
	private double getPercentage(double mark, double maxMark) {
		if(maxMark == 0) {
			return 0;
		}
		return mark / maxMark;
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
	
	public boolean isWaitingToRun() {
		return waitingToRun;
	}
	public void setWaitingToRun(boolean waitingToRun) {
		this.waitingToRun = waitingToRun;
	}
	
	public boolean isGroupResult() {
		return groupResult;
	}
	public void setGroupResult(boolean groupResult) {
		this.groupResult = groupResult;
	}
	public boolean isFinishedHandMarking(){
		try {
			if(handMarkingResults.size() < (isGroupResult() ? assessment.getGroupHandMarking() : assessment.getIndividualHandMarking()).size()){
				return false;
			}
		} catch(IllegalAccessError e) { 
			if(handMarkingResults.size() < assessment.getHandMarking().size()){
				return false;
			}
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

	@Override
	public int compareTo(AssessmentResult o) {
		int diff = this.getAssessment().compareTo(o.getAssessment());
		if(diff != 0) {
			return diff;
		}
		return o.getSubmissionDate().compareTo(this.submissionDate);
	}
	
	public void addHandMarkingResult(HandMarkingResult result) {
		this.handMarkingResults.add(result);
	}
	
	public void removeHandMarkingResult(HandMarkingResult result) {
		this.handMarkingResults.remove(result);
	}
}
