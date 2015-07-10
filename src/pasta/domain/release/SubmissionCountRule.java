package pasta.domain.release;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import pasta.domain.result.AssessmentResult;
import pasta.domain.user.PASTAUser;

/**
 * A rule that is met if the user has submitted the given assessment as many times 
 * as is necessary to follow the count rule.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_submission_count")
public class SubmissionCountRule extends ReleaseAllResultsRule implements Serializable {
	private static final long serialVersionUID = 1082260230827086028L;

	@Column(name = "submission_count")
	private int submissionCount;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "compare_mode")
	private CompareMode compareMode;
	
	@Override
	protected boolean isMet(PASTAUser user, List<AssessmentResult> allCompareResults) {
		int count = 0;
		if(allCompareResults != null) {
			count = allCompareResults.size();
		}
		
		switch(compareMode) {
		case EQUAL:
			return count == submissionCount;
		case GREATER_THAN:
			return count > submissionCount;
		case GREATER_THAN_OR_EQUAL:
			return count >= submissionCount;
		case LESS_THAN:
			return count < submissionCount;
		case LESS_THAN_OR_EQUAL:
			return count <= submissionCount;
		default:
			return false;
		}
	}
	
	public int getSubmissionCount() {
		return submissionCount;
	}
	public void setSubmissionCount(int submissionCount) {
		this.submissionCount = submissionCount;
	}

	public CompareMode getCompareMode() {
		return compareMode;
	}
	public void setCompareMode(CompareMode compareMode) {
		this.compareMode = compareMode;
	}

	@Override
	public String getShortDescription() {
		return "Release by submission count";
	}
	@Override
	public String getDescription() {
		return "This assessment will be released according "
				+ "to the user's submission count for a given assessment.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release if submission count for assessment ")
		.append(getCompareAssessment() == null ? "null" : getCompareAssessment().getName())
		.append(" is ").append(compareMode).append(' ').append(submissionCount);
		return sb.toString();
	}
}
