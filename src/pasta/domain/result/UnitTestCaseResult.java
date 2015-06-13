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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Container class to hold the result of a single unit test.
 * <p>
 * The class contains the result of a single unit test. 
 * This includes:
 * <ul>
 * 	<li>Unit test name</li>
 * 	<li>Unit test result (usually : pass, failure, error)</li>
 * 	<li>Brief and extended unit test message</li>
 * 	<li>Time taken to run the unit test</li>
 * </ul>
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 * 
 */
@Entity
@Table(name = "unit_test_case_results")
public class UnitTestCaseResult implements Serializable, Comparable<UnitTestCaseResult>{
	
	private static final long serialVersionUID = 6764260613777032069L;
	
	public static final String PASS = "pass";
	public static final String FAILURE = "failure";
	public static final String ERROR = "error";

	@Id @GeneratedValue
	private long id;
	
	@Column(name = "name")
	private String testName;
	
	@Column(name = "result")
	private String testResult;
	
	@Column(name = "message")
	private String testMessage;
	
	@Column(name = "extended_message", length = 64000)
	@Size (max = 64000)
	private String extendedMessage = "";
	
	private String type;
	
	private double time;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName.trim();
	}
	public String getTestResult() {
		return testResult;
	}
	public void setTestResult(String testResult) {
		this.testResult = testResult.trim();
	}
	public String getTestMessage() {
		return testMessage;
	}
	public void setTestMessage(String testMessage) {
		this.testMessage = testMessage.trim();
	}
	public String getExtendedMessage() {
		return extendedMessage;
	}
	public void setExtendedMessage(String extendedMessage) {
		this.extendedMessage = extendedMessage.trim();
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type.trim();
	}
	
	public boolean isPass() {
		return getTestResult().equals(PASS);
	}
	public boolean isFailure() {
		return getTestResult().equals(FAILURE);
	}
	public boolean isError() {
		return getTestResult().equals(ERROR);
	}
	
	@Override
	public int compareTo(UnitTestCaseResult target) {
		return testName.compareTo(target.getTestName());
	}
}