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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import pasta.domain.template.UnitTest;

/**
 * Contains the result of a unit test assessment module.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 * 
 */
@Entity
@Table (name = "unit_test_results")
public class UnitTestResult implements Serializable, Comparable<UnitTestResult>{
	
	private static final long serialVersionUID = -4862404513190004578L;

	@Id @GeneratedValue
	@Column (name = "id")
	private long id;
	
	@ManyToOne
	@JoinColumn (name = "unit_test_id")
	private UnitTest test;
	
	private boolean secret;
	
	@Column (name = "compile_errors")
	private String compileErrors;
	
	@Column (name = "runtime_errors")
	private String runtimeErrors;
	
	@OneToMany (cascade = CascadeType.ALL)
	@JoinColumn (name = "test_case_id")
	@OrderBy ("testName")
	private List<UnitTestCaseResult> testCases;

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

	public List<UnitTestCaseResult> getTestCases() {
		return testCases;
	}

	public void setTestCases(ArrayList<UnitTestCaseResult> testCases) {
		this.testCases = testCases;
		Collections.sort(this.testCases);
	}

	public double getPercentage(){
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
	public int compareTo(UnitTestResult target) {
		return test.getName().compareTo(target.getTest().getName());
	}
	
}
