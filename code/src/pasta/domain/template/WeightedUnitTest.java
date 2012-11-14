package pasta.domain.template;

public class WeightedUnitTest {
	private UnitTest test;
	private double weight;
	
	public WeightedUnitTest(UnitTest test, double weight){
		this.test = test;
		this.weight = weight;
	}

	public UnitTest getTest() {
		return test;
	}

	public double getWeight() {
		return weight;
	}
}
