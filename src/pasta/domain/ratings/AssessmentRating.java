package pasta.domain.ratings;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pasta.domain.template.Assessment;

@Entity
@Table(name = "assessment_ratings")
public class AssessmentRating implements Serializable {

	private static final long serialVersionUID = -2757383372009772835L;

	@Id
	@GeneratedValue
	private long id;
	
	private String username;
	
	@ManyToOne
	@JoinColumn(name = "assessment_id")
	private Assessment assessment;
	
	@Column(name = "too_hard")
	private boolean tooHard;
	private int rating;
	private String comment;
	
	public AssessmentRating() {
	}
	
	public AssessmentRating(Assessment assessment, String username) {
		this.username = username;
		this.assessment = assessment;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	public boolean isTooHard() {
		return tooHard;
	}
	public void setTooHard(boolean tooHard) {
		this.tooHard = tooHard;
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
		return "AssessmentRating [id=" + id + ", username=" + username + ", assessment=" + assessment
				+ ", tooHard=" + tooHard + ", rating=" + rating + ", comment=" + comment + "]";
	}
}
