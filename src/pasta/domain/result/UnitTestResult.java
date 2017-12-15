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

package pasta.domain.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;
import pasta.util.PASTAUtil;

/**
 * Contains the result of a unit test assessment module.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 */
@Entity
@Table (name = "unit_test_results")
@VerboseName("unit test result")
public class UnitTestResult extends BaseEntity implements Serializable, Comparable<UnitTestResult>{
	
	private static final long serialVersionUID = -4862404513190004578L;
	
	private static final int COMPILE_ERROR_MAX_LENGTH = 66000;
	private static final int RUNTIME_ERROR_MAX_LENGTH = 66000;
	private static final int FILES_COMPILED_MAX_LENGTH = 66000;
	private static final int RUNTIME_OUTPUT_MAX_LENGTH = 128000;

	@OneToOne
	@JoinColumn (name="tester_unit_test_id", nullable = true)
	private UnitTest testerTest; // When the test is for testing a unit test
	
	@ManyToOne
	@JoinColumn (name="weighted_unit_test_id", nullable = true)
	private WeightedUnitTest weightedUnitTest; // When the test is part of an assessment
	
	@ManyToOne
	@JoinColumn (name = "assessment_result_id", nullable = true)
	private AssessmentResult assessmentResult;
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "unitTestResult")
	@OrderBy ("testName")
	@LazyCollection (LazyCollectionOption.FALSE)
	private List<UnitTestCaseResult> testCases = new ArrayList<>();
	
	@Column (name = "internal_error")
	private boolean internalError;
	
	@Column (name = "build_error")
	private boolean buildError;
	
	@ElementCollection
	@CollectionTable(name = "unit_test_results_validation_errors", 
		joinColumns = @JoinColumn(name = "unit_test_result_id"))
	@Column(name = "error")
	@LazyCollection (LazyCollectionOption.FALSE)
	private Set<ResultFeedback> validationErrors = new TreeSet<ResultFeedback>();
	
	@Column (name = "compile_errors", length = COMPILE_ERROR_MAX_LENGTH)
	@Size (max = COMPILE_ERROR_MAX_LENGTH)
	private String compileErrors;
	
	@Column (name = "runtime_errors", length = RUNTIME_ERROR_MAX_LENGTH)
	@Size (max = RUNTIME_ERROR_MAX_LENGTH)
	private String runtimeErrors;
	
	@Column (name = "clean_error")
	private boolean cleanError;
	
	@Column (name = "files_compiled", length = FILES_COMPILED_MAX_LENGTH)
	@Size (max = FILES_COMPILED_MAX_LENGTH)
	private String filesCompiled;
	
	@Column (name = "runtime_output", length = RUNTIME_OUTPUT_MAX_LENGTH)
	@Size (max = RUNTIME_OUTPUT_MAX_LENGTH)
	private String fullOutput;

	public UnitTest getTest() {
		return getTesterTest() == null ? (getWeightedUnitTest() == null ? null : getWeightedUnitTest().getTest()) : getTesterTest();
	}
	
	public UnitTest getTesterTest() {
		return testerTest;
	}
	public void setTesterTest(UnitTest test) {
		this.testerTest = test;
	}
	
	public WeightedUnitTest getWeightedUnitTest() {
		return weightedUnitTest;
	}
	public void setWeightedUnitTest(WeightedUnitTest weightedUnitTest) {
		this.weightedUnitTest = weightedUnitTest;
	}
	
	public boolean isSecret() {
		WeightedUnitTest wut = getWeightedUnitTest();
		return wut == null ? false : wut.isSecret();
	}
	public boolean isGroupWork() {
		WeightedUnitTest wut = getWeightedUnitTest();
		return wut == null ? false : wut.isGroupWork();
	}

	public List<UnitTestCaseResult> getTestCases() {
		return testCases;
	}
	public void addTestCaseResult(UnitTestCaseResult utcr) {
		utcr.setUnitTestResult(this);
		this.testCases.add(utcr);
	}
	public void addAllTestCaseResults(Collection<UnitTestCaseResult> results) {
		for(UnitTestCaseResult result : results) {
			addTestCaseResult(result);
		}
	}
	public void setTestCases(List<UnitTestCaseResult> testCases) {
		removeAllTestCases();
		addAllTestCaseResults(testCases);
	}
	public void removeAllTestCases() {
		for(UnitTestCaseResult result : this.testCases) {
			result.setUnitTestResult(null);
		}
		this.testCases.clear();
	}

	public boolean isInternalError() {
		return internalError;
	}
	public void setInternalError(boolean internalError) {
		this.internalError = internalError;
	}

	public boolean isBuildError() {
		return buildError;
	}
	public void setBuildError(boolean buildError) {
		this.buildError = buildError;
	}

	public Set<ResultFeedback> getValidationErrors() {
		return validationErrors;
	}
	public Map<String, Set<String>> getValidationErrorsMap() {
		Map<String, Set<String>> errors = new HashMap<String, Set<String>>();
		for(ResultFeedback error : getValidationErrors()) {
			Set<String> categoryErrors = errors.get(error.getCategory());
			if(categoryErrors == null) {
				categoryErrors = new TreeSet<String>();
				errors.put(error.getCategory(), categoryErrors);
			}
			categoryErrors.add(error.getFeedback());
		}
		return errors;
	}
	public boolean addValidationError(ResultFeedback error) {
		return validationErrors.add(error);
	}
	public boolean addValidationError(String category, String feedback) {
		return addValidationError(new ResultFeedback(category, feedback));
	}
	public boolean addValidationError(String feedback) {
		return addValidationError(new ResultFeedback("", feedback));
	}
	public void clearValidationErrors() {
		this.validationErrors.clear();
	}
	public boolean addAllValidationErrors(Collection<ResultFeedback> feedback) {
		return validationErrors.addAll(feedback);
	}
	public void setValidationErrors(Set<ResultFeedback> validationErrors) {
		clearValidationErrors();
		addAllValidationErrors(validationErrors);
	}

	public String getCompileErrors() {
		return compileErrors;
	}

	public void setCompileErrors(String compileErrors) {
		this.compileErrors = PASTAUtil.truncate(compileErrors, COMPILE_ERROR_MAX_LENGTH);
	}

	public boolean isRuntimeError() {
		return runtimeErrors != null && !runtimeErrors.isEmpty();
	}
	public String getRuntimeErrors() {
		return runtimeErrors;
	}
	public void setRuntimeErrors(String runtimeErrors) {
		this.runtimeErrors = PASTAUtil.truncate(runtimeErrors, RUNTIME_ERROR_MAX_LENGTH);
	}

	public boolean isCleanError() {
		return cleanError;
	}

	public void setCleanError(boolean cleanError) {
		this.cleanError = cleanError;
	}

	public String getFilesCompiled() {
		return filesCompiled;
	}

	public void setFilesCompiled(String filesCompiled) {
		this.filesCompiled = PASTAUtil.truncate(filesCompiled, FILES_COMPILED_MAX_LENGTH);
	}

	public String getFullOutput() {
		return fullOutput;
	}

	public void setFullOutput(String fullOutput) {
		this.fullOutput = PASTAUtil.truncate(fullOutput, RUNTIME_OUTPUT_MAX_LENGTH);
	}

	public double getPercentage(){
		if(testCases == null || testCases.isEmpty()){
			return 0;
		}
		double passed = 0;
		for(UnitTestCaseResult result: testCases){
			if(result.isPass()){
				++passed;
			}
		}
		return passed/testCases.size(); 	
	}
	
	public boolean isCompileError() {
		return compileErrors != null && !compileErrors.isEmpty();
	}
	
	public boolean isTestCrashed() {
		if(testCases == null || testCases.size() != 1) {
			return false;
		}
		return testCases.get(0).getTestName().equals("BeforeFirstTest") &&
				testCases.get(0).isError();
	}
	
	public boolean isValidationError() {
		return !validationErrors.isEmpty();
	}
	
	public boolean isError() {
		return internalError || buildError || 
				isRuntimeError() || cleanError || 
				isCompileError() || isTestCrashed() ||
				isValidationError();
	}
	
	public String getErrorReason() {
		if(isValidationError()) {
			return "Submission not valid";
		}
		if(isCompileError()) {
			return "Error compiling submission";
		}
		if(buildError) {
			return "Error building submission";
		}
		if(isRuntimeError()) {
			return "Error running submission";
		}
		if(internalError || cleanError || isTestCrashed()) {
			return "Internal error - contact administrator";
		}
		return null;
	}
	
	public AssessmentResult getAssessmentResult() {
		return assessmentResult;
	}
	public void setAssessmentResult(AssessmentResult assessmentResult) {
		this.assessmentResult = assessmentResult;
	}
	
	@Override
	public int compareTo(UnitTestResult target) {
		return getTest().getName().compareTo(target.getTest().getName());
	}
	
	public void combine(UnitTestResult other) {
		if(other == null) {
			return;
		}
		
		this.setBuildError(buildError || other.buildError);
		this.setCleanError(cleanError || other.cleanError);
		this.setCompileErrors(combineStrings(compileErrors, other.compileErrors));
		this.setFilesCompiled(combineStrings(filesCompiled, other.filesCompiled));
		this.setInternalError(internalError || other.internalError);
		this.setRuntimeErrors(combineStrings(runtimeErrors, other.runtimeErrors));
		this.setFullOutput(combineStrings(fullOutput, other.fullOutput));
		
		if(this.getTestCases() == null && other.getTestCases() != null) {
			this.testCases = new LinkedList<UnitTestCaseResult>();
		}
		if(other.getTestCases() != null) {
			this.addAllTestCaseResults(other.getTestCases());
		}
		this.getValidationErrors().addAll(other.getValidationErrors());
	}
	private String combineStrings(String s1, String s2) {
		if(s1 == null && s2 == null) {
			return null;
		}
		if(s1 == null && s2 != null) {
			return s2;
		}
		if(s1 != null && s2 == null) {
			return s1;
		}
		if(s1.trim().equals(s2.trim())) {
			return s1.trim();
		}
		return (s1 + (!s1.isEmpty() && !s2.isEmpty() ? System.lineSeparator() + "===============" : "") + System.lineSeparator() + s2).trim();
	}
}
