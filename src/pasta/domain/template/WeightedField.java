/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
