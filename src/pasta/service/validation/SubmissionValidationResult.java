package pasta.service.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 29 Jul 2015
 *
 */
public class SubmissionValidationResult {
	private Collection<ValidationFeedback> feedback;
	private Collection<ValidationFeedback> errors;
	private String assessmentName;
	
	public SubmissionValidationResult(String assessmentName) {
		this.feedback = null;
		this.errors = null;
		this.assessmentName = assessmentName;
	}
	
	public String getAssessmentName() {
		return assessmentName;
	}

	public Map<String, Set<ValidationFeedback>> getFeedbackMap() {
		Map<String, Set<ValidationFeedback>> allFeedback = new HashMap<>();
		for(ValidationFeedback feedback : this.feedback) {
			Set<ValidationFeedback> categoryFeedback = allFeedback.get(feedback.getCategory());
			if(categoryFeedback == null) {
				categoryFeedback = new TreeSet<ValidationFeedback>();
				allFeedback.put(feedback.getCategory(), categoryFeedback);
			}
			categoryFeedback.add(feedback);
		}
		return allFeedback;
	}
	public Map<String, Set<ValidationFeedback>> getErrorsMap() {
		Map<String, Set<ValidationFeedback>> allErrors = new HashMap<>();
		for(ValidationFeedback error : this.errors) {
			Set<ValidationFeedback> categoryErrors = allErrors.get(error.getCategory());
			if(categoryErrors == null) {
				categoryErrors = new TreeSet<ValidationFeedback>();
				allErrors.put(error.getCategory(), categoryErrors);
			}
			categoryErrors.add(error);
		}
		return allErrors;
	}
	
	public Collection<ValidationFeedback> getFeedback() {
		return feedback;
	}
	public Collection<ValidationFeedback> getErrors() {
		return errors;
	}
	public void setFeedback(Collection<ValidationFeedback> feedback) {
		this.feedback = feedback;
	}
	public void setErrors(Collection<ValidationFeedback> errors) {
		this.errors = errors;
	}
	
	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}
	public boolean hasFeedback() {
		return feedback != null && !feedback.isEmpty();
	}
}
