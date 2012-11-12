package pasta.domain.template;

public class UnitTest {
	private String name;
	private double weighting;
	private boolean tested;
	private boolean secret;
	
	public UnitTest(String name, double weighting, boolean tested){
		this.name = name;
		this.weighting = weighting;
		this.tested = tested;
	}

	public String getName() {
		return name;
	}

	public double getWeighting() {
		return weighting;
	}

	public boolean isTested() {
		return tested;
	}
	
	public boolean isSecret() {
		return secret;
	}
}
