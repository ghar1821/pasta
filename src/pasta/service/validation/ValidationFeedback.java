package pasta.service.validation;

import pasta.domain.result.ResultFeedback;

public final class ValidationFeedback implements Comparable<ValidationFeedback> {
	private ResultFeedback base;
	private boolean preFormat = false;

	public ValidationFeedback() {
		base = new ResultFeedback();
	}
	public ValidationFeedback(String feedback) {
		base = new ResultFeedback(feedback);
	}
	public ValidationFeedback(String category, String feedback) {
		base = new ResultFeedback(category, feedback);
	}
	
	public boolean isPreFormat() {
		return preFormat;
	}
	public void setPreFormat(boolean preFormat) {
		this.preFormat = preFormat;
	}

	@Override
	public String toString() {
		return base.toString();
	}

	public String getCategory() {
		return base.getCategory();
	}

	public void setCategory(String category) {
		base.setCategory(category);
	}

	public String getFeedback() {
		return base.getFeedback();
	}

	public void setFeedback(String feedback) {
		base.setFeedback(feedback);
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return base.equals(obj);
	}
	
	@Override
	public int compareTo(ValidationFeedback o) {
		return base.compareTo(o.base);
	}
}
