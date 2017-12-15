package pasta.scheduler;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import pasta.docker.Language;
import pasta.docker.LanguageManager;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Class that holds the details of jobs.
 * <p>
 * This is a the holding object that deals with the 'job' system
 * used in PASTA to ensure only one assessment is executed at a time.
 * 
 * Database schema is automatically created based on this class.
 * 
 * Database schema:
 * <pre>
 * 	Integer ID, 
 *  Text username (not null),
 *  Text assessmentName (not null),
 *  Data runDate (not null)
 * </pre>
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-12-04
 * 
 */
@Entity
@Table(name = "assessment_jobs",
		uniqueConstraints = { @UniqueConstraint(name="unique_job_per_user_per_assessment", columnNames={
				"user_id", "assessment_id", "run_date"
		})})
public class AssessmentJob extends Job implements Serializable{

	private static final long serialVersionUID = 2058301754166837748L;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private PASTAUser user;
	
	@Column(name = "assessment_id", nullable = false)
	private long assessmentId;
	
	@ManyToOne
	@JoinColumn (name = "assessment_result_id")
	private AssessmentResult results;
	
	private boolean running;
	
	@Transient
	private Language language;
	
	public AssessmentJob(){}
	
	public AssessmentJob(PASTAUser user, long assessmentId, Date runDate, AssessmentResult result){
		super(runDate);
		this.user = user;
		this.assessmentId = assessmentId;
		this.results = result;
		this.running = false;
	}

	public PASTAUser getUser() {
		return user;
	}
	public void setUser(PASTAUser user) {
		this.user = user;
	}

	public long getAssessmentId() {
		return assessmentId;
	}
	public void setAssessmentId(long assessmentId) {
		this.assessmentId = assessmentId;
	}

	public AssessmentResult getResults() {
		return results;
	}
	public void setResults(AssessmentResult results) {
		this.results = results;
	}
	
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public Language getLanguage() {
		if(language != null) {
			return language;
		}
		Assessment assessment = results.getAssessment();
		language = LanguageManager.getInstance().guessLanguage(assessment.getSolutionName(), "", getSubmissionRoot());
		return language;
	}
	
	public File getSubmissionRoot() {
		String submissionHome = ProjectProperties.getInstance().getSubmissionsLocation() + user.getUsername() + "/assessments/"
				+ getAssessmentId() + "/" + PASTAUtil.formatDate(getRunDate()) + "/submission";
		return new File(submissionHome);
	}

	@Override
	public String toString() {
		return "Assessment " + assessmentId + 
				" job for " + user.getUsername() + 
				" submitted at " + results.getSubmissionDate() + 
				" by " + results.getSubmittedBy().getUsername() +
				(running ? " (running)" : ""); 
	}
}
