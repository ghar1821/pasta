package pasta.domain.release;


public enum MarkType {
	OVERALL("overall mark"), AUTO_MARK("auto mark"), MANUAL_MARK("manual mark");
	
	private String text;
	private MarkType(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
}
