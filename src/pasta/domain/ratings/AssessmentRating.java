package pasta.domain.ratings;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;

@Entity
@Table(name = "assessment_ratings")
public class AssessmentRating implements Serializable {

	private static final long serialVersionUID = -2757383372009772835L;

	@Id
	@GeneratedValue
	private long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private PASTAUser user;
	
	@ManyToOne
	@JoinColumn(name = "assessment_id")
	private Assessment assessment;
	
	private int rating;
	private String comment;
	
	public AssessmentRating() {
	}
	
	public AssessmentRating(Assessment assessment, PASTAUser user) {
		this.user = user;
		this.assessment = assessment;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
		return "AssessmentRating [id=" + id + ", user=" + user.getUsername() + ", assessment=" + assessment
				+ ", rating=" + rating + ", comment=" + comment + "]";
	}
}
