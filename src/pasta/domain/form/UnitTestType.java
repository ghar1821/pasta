package pasta.domain.form;

public enum UnitTestType {
	JUNIT("JUnit test suite"), BLACK_BOX("Black Box test suite");
	
	private String description;
	
	private UnitTestType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
