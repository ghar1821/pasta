package pasta.domain.template;

public class UnitTest {
	private String name;
	private boolean tested;
	private boolean secret;
	
	public UnitTest(String name, boolean tested, boolean secret){
		this.name = name;
		this.tested = tested;
		this.secret = secret;
	}

	public String getName() {
		return name;
	}

	public boolean isTested() {
		return tested;
	}
	
	public boolean isSecret() {
		return secret;
	}
}
