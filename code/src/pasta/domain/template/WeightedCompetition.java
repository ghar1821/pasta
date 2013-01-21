package pasta.domain.template;

public class WeightedCompetition {
	private Competition comp = new Competition();
	private double weight;
	private String compName;
	
	public Competition getTest() {
		return comp;
	}
	public void setTest(Competition comp) {
		this.comp = comp;
		if(!comp.getName().trim().equals("nullgarbagetemptestihopenobodynamestheirtestthis")){
			compName = comp.getName();
		}
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getCompName() {
		return compName;
	}
	public void setCompName(String compName) {
		this.compName = compName;
	}
	
}
