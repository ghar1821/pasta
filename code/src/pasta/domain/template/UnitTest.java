package pasta.domain.template;

public class UnitTest {
	private String name;
	private boolean tested;
	
	public UnitTest(String name, boolean tested){
		this.name = name;
		this.tested = tested;
	}

	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return name.replace(" ", "");
	}

	public boolean isTested() {
		return tested;
	}
}
