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

import java.util.Date;

import pasta.domain.user.PASTAUser;

/**
 * <p>
 * A class to hold a combination of multiple assessment results. All results are
 * assumed to be for the same assessment.
 * 
 * <p>
 * This class would usually be used for combining the results of a user and
 * their group.
 * 
 * <p>
 * You cannot access the following properties of a combined assessment:
 * <ul>
 * <li>ID
 * <li>comments
 * <li>submission date
 * <li>submitted by
 * </ul>
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 19 Jul 2015
 */
public class CombinedAssessmentResult extends AssessmentResult {

	private static final long serialVersionUID = -8539486531742799416L;
	
	public CombinedAssessmentResult(PASTAUser user, AssessmentResult... results) {
		this.setWaitingToRun(false);
		this.setUser(user);
		for(AssessmentResult result : results) {
			addResult(result);
		}
	}
	
	public void addResult(AssessmentResult result) {
		if(result == null) {
			return;
		}
		if(this.getAssessment() == null) {
			this.setAssessment(result.getAssessment());
		}
		this.setWaitingToRun(this.isWaitingToRun() || result.isWaitingToRun());
		for(UnitTestResult utResult : result.getUnitTests()) {
			this.addUnitTest(utResult);
		}
		for(HandMarkingResult hmResult : result.getHandMarkingResults()) {
			this.addHandMarkingResult(hmResult);
		}
	}

	@Override
	public Long getId() {
		throw new IllegalAccessError("Cannot access property 'ID' of a combined assessment result");
	}

	@Override
	public PASTAUser getSubmittedBy() {
		throw new IllegalAccessError("Cannot access property 'submittedBy' of a combined assessment result");
	}

	@Override
	public Date getSubmissionDate() {
		throw new IllegalAccessError("Cannot access property 'submissionDate' of a combined assessment result");
	}

	@Override
	public String getComments() {
		throw new IllegalAccessError("Cannot access property 'comments' of a combined assessment result");
	}
	
	@Override
	public boolean isGroupResult() {
		throw new IllegalAccessError("Cannot access property 'groupResult' of a combined assessment result");
	}
}
