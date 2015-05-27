package pasta.domain.release;

public enum CompareMode {
	LESS_THAN("Less than"), LESS_THAN_OR_EQUAL("Less than or equal to"), EQUAL("Equal to"), GREATER_THAN_OR_EQUAL("Greater than or equal to"), GREATER_THAN("Greater than");
	
	private String text;
	private CompareMode(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
}
