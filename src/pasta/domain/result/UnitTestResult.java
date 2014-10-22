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

import java.util.ArrayList;
import java.util.Collections;

import pasta.domain.template.UnitTest;

/**
 * Contains the result of a unit test assessment module.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 * 
 */
public class UnitTestResult implements Comparable{
	private UnitTest test;
	private boolean secret;
	private String compileErrors;
	private String runtimeErrors;
	private ArrayList<UnitTestCaseResult> testCases;

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

	public ArrayList<UnitTestCaseResult> getTestCases() {
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
	public int compareTo(Object o) {
		UnitTestResult target = (UnitTestResult)(o);
		return test.getName().compareTo(target.getTest().getName());
	}
	
}
