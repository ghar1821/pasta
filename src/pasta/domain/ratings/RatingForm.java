package pasta.domain.ratings;

public class RatingForm {

	private String comment;
	private int rating;
	private boolean tooHard;
	
	public RatingForm() {
		this.comment = "";
		this.rating = 1;
		this.tooHard = false;
	}
	
	public RatingForm(AssessmentRating base) {
		this.comment = base.getComment();
		this.rating = base.getRating();
		this.tooHard = base.isTooHard();
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public boolean isTooHard() {
		return tooHard;
	}
	public void setTooHard(boolean tooHard) {
		this.tooHard = tooHard;
	}
}
