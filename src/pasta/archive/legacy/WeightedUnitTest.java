package pasta.archive.legacy;

/**
 * Container class for a weighted unit test.
 * <p>
 * This class contains a link to a unit test assessment module to an assessment
 * and the weight of the unit test assessment module in the assessment.
 * 
 * Using this, you can have the same unit test assessment module as part of multiple
 * assessments with different weights.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-14
 *
 */
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