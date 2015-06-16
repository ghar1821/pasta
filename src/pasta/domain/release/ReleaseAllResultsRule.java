package pasta.domain.release;

import java.io.Serializable;
import java.util.List;

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

	@OneToOne
	@JoinColumn(name = "compare_assessment_id")
	private Assessment compareAssessment;
	
	@Override
	protected boolean isMet(PASTAUser user) {
		List<AssessmentResult> results = ProjectProperties.getInstance().getResultDAO()
				.getAllResultsForUserAssessment(user, compareAssessment.getId());
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
