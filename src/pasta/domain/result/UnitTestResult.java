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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.template.UnitTest;

/**
 * Contains the result of a unit test assessment module.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 */
@Entity
@Table (name = "unit_test_results")
public class UnitTestResult implements Serializable, Comparable<UnitTestResult>{
	
	private static final long serialVersionUID = -4862404513190004578L;

	@Id @GeneratedValue
	@Column (name = "id")
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn (name = "unit_test_id")
	private UnitTest test;
	
	private boolean secret;
	
	@Column(name="group_work")
	private boolean groupWork;
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "test_case_id")
	@OrderBy ("testName")
	@LazyCollection (LazyCollectionOption.FALSE)
	private List<UnitTestCaseResult> testCases;
	
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
	
	@Column (name = "compile_errors", length = 64000)
	@Size (max = 64000)
	private String compileErrors;
	
	@Column (name = "runtime_errors", length = 64000)
	@Size (max = 64000)
	private String runtimeErrors;
	
	@Column (name = "clean_error")
	private boolean cleanError;
	
	@Column (name = "files_compiled", length = 64000)
	@Size (max = 64000)
	private String filesCompiled;
	
	@Column (name = "runtime_output", length = 128000)
	@Size (max = 128000)
	private String fullOutput;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

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

	public boolean isGroupWork() {
		return groupWork;
	}
	public void setGroupWork(boolean groupWork) {
		this.groupWork = groupWork;
	}

	public List<UnitTestCaseResult> getTestCases() {
		return testCases;
	}
	public void setTestCases(List<UnitTestCaseResult> testCases) {
		this.testCases = testCases;
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
		this.compileErrors = compileErrors;
	}

	public boolean isRuntimeError() {
		return runtimeErrors != null && !runtimeErrors.isEmpty();
	}
	public String getRuntimeErrors() {
		return runtimeErrors;
	}
	public void setRuntimeErrors(String runtimeErrors) {
		this.runtimeErrors = runtimeErrors;
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
		this.filesCompiled = filesCompiled;
	}

	public String getFullOutput() {
		return fullOutput;
	}

	public void setFullOutput(String fullOutput) {
		this.fullOutput = fullOutput;
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

	@Override
	public int compareTo(UnitTestResult target) {
		return test.getName().compareTo(target.getTest().getName());
	}
	
	public void combine(UnitTestResult other) {
		if(other == null) {
			return;
		}
		
		this.setBuildError(buildError || other.buildError);
		this.setCleanError(cleanError || other.cleanError);
		this.setCompileErrors(combineStrings(compileErrors, other.compileErrors));
		this.setFilesCompiled(combineStrings(filesCompiled, other.filesCompiled));
		this.setGroupWork(groupWork || other.groupWork);
		this.setInternalError(internalError || other.internalError);
		this.setRuntimeErrors(combineStrings(runtimeErrors, other.runtimeErrors));
		this.setFullOutput(combineStrings(fullOutput, other.fullOutput));
		this.setSecret(secret || other.secret);
		
		if(this.getTestCases() == null && other.getTestCases() != null) {
			this.testCases = new LinkedList<UnitTestCaseResult>();
		}
		if(other.getTestCases() != null) {
			this.getTestCases().addAll(other.getTestCases());
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
