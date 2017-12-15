package pasta.archive.legacy;

/**
 * Container class for a weighted hand marking.
 * <p>
 * This class contains a link to a hand marking assessment module to an assessment
 * and the weight of the unit test assessment module in the assessment.
 * 
 * Using this, you can have the same hand marking assessment module as part of multiple
 * assessments with different weights.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-04-01
 *
 */
public class WeightedHandMarking {
	private HandMarking handMarking = new HandMarking();
	private double weight;
	private String handMarkingName;
	
	public HandMarking getHandMarking() {
		return handMarking;
	}
	public void setHandMarking(HandMarking handMarking) {
		this.handMarking = handMarking;
		if(!handMarking.getName().trim().equals("nullgarbagetemptestihopenobodynamestheirtestthis")){
			handMarkingName = handMarking.getName();
		}
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getHandMarkingName() {
		return handMarkingName;
	}
	public void setHandMarkingName(String handMarkingName) {
		this.handMarkingName = handMarkingName;
	}
}