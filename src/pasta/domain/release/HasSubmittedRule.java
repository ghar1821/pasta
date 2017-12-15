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

package pasta.domain.release;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import pasta.domain.result.AssessmentResult;
import pasta.domain.user.PASTAUser;

/**
 * A rule that is met if the user has submitted the given assessment at least
 * once, regardless of outcome.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_has_submitted")
public class HasSubmittedRule extends ReleaseResultsRule implements Serializable {
	private static final long serialVersionUID = 3556811612265185953L;

	@Override
	protected boolean isMet(PASTAUser user, AssessmentResult latestCompareResult) {
		return (latestCompareResult != null);
	}

	@Override
	public String getShortDescription() {
		return "Release after completing another assessment";
	}
	@Override
	public String getDescription() {
		return "This assessment will be released if the user has already "
				+ "made at least one submission to the given assessment.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release if completed assessment ").append(getCompareAssessment() == null ? "null" : getCompareAssessment().getName());
		return sb.toString();
	}
}
