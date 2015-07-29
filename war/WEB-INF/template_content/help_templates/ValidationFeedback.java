package pasta.service.validation;

/**
 * An item of feedback to be delivered to a user.
 */
public final class ValidationFeedback implements Comparable<ValidationFeedback> {
	// Creates an empty feedback item
	public ValidationFeedback() {}
	
	// Creates a feedback item with an empty category
	public ValidationFeedback(String feedback) {}
	
	// Creates a feedback item with a category
	public ValidationFeedback(String category, String feedback) {}
	
	// All feedback in the same category is grouped when displayed to the user
	public String getCategory() {}
	public void setCategory(String category) {}

	// The text to display to the user
	public String getFeedback() {}
	public void setFeedback(String feedback) {}
	
	// Mark as preFormat for feedback text to be displayed in a 
	// monospace font, maintaining whitespace
	public boolean isPreFormat() {}
	public void setPreFormat(boolean preFormat) {}
}
