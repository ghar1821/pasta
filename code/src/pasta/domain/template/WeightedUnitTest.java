package pasta.domain.template;

public class WeightedUnitTest {
	private UnitTest test = new UnitTest();
	private double weight;
	private String unitTestName;
	
	public UnitTest getTest() {
		return test;
	}
	public void setTest(UnitTest test) {
		this.test = test;
		if(!test.getName().trim().equals("nullgarbagetemptestihopenobodynamestheirtestthis")){
			unitTestName = test.getName();
		}
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getUnitTestName() {
		return unitTestName;
	}
	public void setUnitTestName(String unitTestName) {
		this.unitTestName = unitTestName;
	}
	
}
