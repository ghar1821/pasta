/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
