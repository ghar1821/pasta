package pasta.domain.ratings;

public class RatingForm {

	private String comment;
	private int rating;
	
	public RatingForm() {
		this.comment = "";
		this.rating = 1;
	}
	
	public RatingForm(AssessmentRating base) {
		this.comment = base.getComment();
		this.rating = base.getRating();
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
}
