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
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.util.ProjectProperties;

/**
 * A rule that takes all of user's submissions for a given assessment into
 * consideration
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ReleaseAllResultsRule extends ReleaseRule implements Serializable {	
	private static final long serialVersionUID = 8770896473121146463L;

	@ManyToOne
	@JoinColumn(name = "compare_assessment_id")
	private Assessment compareAssessment;
	
	@Override
	protected boolean isMet(PASTAUser user) {
		List<AssessmentResult> results = ProjectProperties.getInstance().getResultDAO()
				.getAllResults(user, compareAssessment.getId(), true, false);
		return isMet(user, results);
	}
	
	protected abstract boolean isMet(PASTAUser user, List<AssessmentResult> allCompareResults);

	public Assessment getCompareAssessment() {
		return compareAssessment;
	}
	public void setCompareAssessment(Assessment compareAssessment) {
		this.compareAssessment = compareAssessment;
	}
}
