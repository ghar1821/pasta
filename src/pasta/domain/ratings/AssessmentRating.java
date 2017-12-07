package pasta.domain.ratings;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;

@Entity
@Table(name = "assessment_ratings")
@VerboseName("assessment rating")
public class AssessmentRating extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -2757383372009772835L;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private PASTAUser user;
	
	@ManyToOne
	@JoinColumn(name = "assessment_id")
	private Assessment assessment;
	
	private int rating;
	
	@Column(columnDefinition="TEXT")
	private String comment;
	
	public AssessmentRating() {
	}
	
	public AssessmentRating(Assessment assessment, PASTAUser user) {
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
	
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "assessment rating for " + getAssessment().getName() + " by " + getUser().getUsername();
	}
}
