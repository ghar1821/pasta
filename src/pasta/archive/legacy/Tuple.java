package pasta.archive.legacy;

/**
 * Container class for a pairing of String and double, commonly used in
 * the hand marking assessment module template.
 * <p>
 * Somewhat poorly named, should be called a PASTAPair or similar.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-20
 *
 */
public class Tuple {
	private String name;
	private double weight;
	
	public Tuple(){}
	
	public Tuple(String name, double weight){
		this.name = name;
		this.weight = weight;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		return "{" + name + ", " + weight + "}";
	}
}