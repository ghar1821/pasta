package pasta.domain.release;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.util.ProjectProperties;

/**
 * A rule that takes a user's latest result for a given assessment into
 * consideration
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ReleaseResultsRule extends ReleaseRule {	
	@OneToOne
	@JoinColumn(name = "compare_assessment_id")
	private Assessment compareAssessment;
	
	@Override
	protected boolean isMet(PASTAUser user) {
		AssessmentResult result = ProjectProperties.getInstance().getResultDAO()
				.getLatestResultsForUserAssessment(user.getUsername(), compareAssessment.getId());
		return isMet(user, result);
	}
	
	protected abstract boolean isMet(PASTAUser user, AssessmentResult latestCompareResult);

	public Assessment getCompareAssessment() {
		return compareAssessment;
	}
	public void setCompareAssessment(Assessment compareAssessment) {
		this.compareAssessment = compareAssessment;
	}
}
