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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;

/**
 * Container for the results of an assessment.
 * <p>
 * Contains the collection of:
 * <ul>
 * <li>User</li>
 * <li>Assessment</li>
 * <li>Assessment results</li>
 * </ul>
 * 
 * @author Martin McGrane
 * @version 2.0
 * @since 2016-08-15
 */

@Entity
@Table(name = "assessment_result_summaries")
public class AssessmentResultSummary implements Serializable, Comparable<AssessmentResultSummary> {
	@Embeddable
	public static class AssessmentResultSummaryId implements Serializable, Comparable<AssessmentResultSummaryId> {
		private static final long serialVersionUID = 621374413182503034L;

		@ManyToOne
		@JoinColumn(name = "user_id", nullable = false)
		private PASTAUser user;

		@ManyToOne
		@JoinColumn(name = "assessment_id", nullable = false)
		private Assessment assessment;

		public AssessmentResultSummaryId() {}
		
		public AssessmentResultSummaryId(PASTAUser user, Assessment assessment) {
			this.user = user;
			this.assessment = assessment;
		}

		public PASTAUser getUser() {
			return user;
		}

		public void setUser(PASTAUser user) {
			this.user = user;
		}

		public Assessment getAssessment() {
			return assessment;
		}

		public void setAssessment(Assessment assessment) {
			this.assessment = assessment;
		}

		@Override
		public int compareTo(AssessmentResultSummaryId other) {
			if (other.user.compareTo(user) == 0) {
				return other.assessment.compareTo(assessment);
			}
			return other.user.compareTo(user);
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof AssessmentResultSummaryId)) {
				return false;
			}
			return user.equals(((AssessmentResultSummaryId)other).user)
					&& assessment.equals(((AssessmentResultSummaryId)other).assessment);
		}
		
		@Override
		public int hashCode() {
			return user.hashCode() * assessment.hashCode();
		}
	}

	private static final long serialVersionUID = 447867201394779085L;

	@Id
	@EmbeddedId
	private AssessmentResultSummaryId id;

	@Column(name = "percent")
	private double percent;

	public AssessmentResultSummary() {}

	public AssessmentResultSummary(PASTAUser user, Assessment assessment, double percentage) {
		id = new AssessmentResultSummaryId(user, assessment);
		this.percent = percentage;
	}

	public PASTAUser getUser() {
		return id.user;
	}

	public Assessment getAssessment() {
		return id.assessment;
	}

	public void setPercentage(double percentage) {
		this.percent = percentage;
	}

	public double getPercentage() {
		return percent;
	}

	@Override
	public int compareTo(AssessmentResultSummary other) {
		return id.compareTo(other.id);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AssessmentResultSummary)) {
			return false;
		}
		return id.equals(((AssessmentResultSummary)other).id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
