package pasta.domain.template;

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
