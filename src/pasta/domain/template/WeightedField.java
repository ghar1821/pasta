package pasta.domain.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;

/**
 * Container class for a pairing of String and double, commonly used in
 * the hand marking assessment module template.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-20
 *
 */
@Entity
@Table (name = "weighted_fields")
@VerboseName("weighted field")
public class WeightedField extends BaseEntity implements Comparable<WeightedField> {
	
	private static final long serialVersionUID = -5647759434783526021L;

	@Column (length = 512)
	private String name;
	private double weight;
	
	public WeightedField(){
		name = "";
		weight = 0;
	}
	
	public WeightedField(String name, double weight){
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
	public int compareTo(WeightedField other) {
		int diff = this.name.compareTo(other.name);
		if(diff != 0) {
			return diff;
		}
		diff = (this.weight < other.weight ? -1 : (this.weight > other.weight ? 1 : 0));
		if(diff != 0) {
			return diff;
		}
		if(this.getId() == null) {
			return other.getId() == null ? 0 : -1;
		}
		if(other.getId() == null) {
			return 1;
		}
		return (this.getId() < other.getId() ? -1 : (this.getId() > other.getId() ? 1 : 0));
	}
	
	@Override
	public String toString() {
		return "(" + getId() + ": " + name + ", " + weight + ")";
	}
}
