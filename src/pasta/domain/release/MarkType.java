package pasta.domain.release;


public enum MarkType {
	OVERALL("Overall"), AUTO_MARK("Auto mark"), MANUAL_MARK("Manual mark");
	
	private String text;
	private MarkType(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
}
