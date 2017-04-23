package pasta.domain.release;

public enum CompareMode {
	LESS_THAN("less than"), LESS_THAN_OR_EQUAL("less than or equal to"), EQUAL("equal to"), GREATER_THAN_OR_EQUAL("greater than or equal to"), GREATER_THAN("greater than");
	
	private String text;
	private CompareMode(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
}
