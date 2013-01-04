package pasta.domain.template;

public class WeightedHandMarking {
	private HandMarking test = new HandMarking();
	private double weight;
	private String handMarkingName;
	
	public HandMarking getHandMarking() {
		return test;
	}
	public void setHandMarking(HandMarking test) {
		this.test = test;
		if(!test.getName().trim().equals("nullgarbagetemptestihopenobodynamestheirtestthis")){
			handMarkingName = test.getName();
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
