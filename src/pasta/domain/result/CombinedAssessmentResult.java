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
