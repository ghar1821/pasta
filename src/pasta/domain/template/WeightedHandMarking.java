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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;
import pasta.domain.result.HandMarkingResult;

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

@Entity
@Table (name = "weighted_hand_markings")
@VerboseName("weighted hand-marking module")
public class WeightedHandMarking extends BaseEntity implements Comparable<WeightedHandMarking> {

	private static final long serialVersionUID = -3429348535279846933L;
	
	private double weight;
	
	@Column(name= "group_work")
	private boolean groupWork;
	
	@ManyToOne
	@JoinColumn(name = "hand_marking_id")
	private HandMarking handMarking;
	
	@ManyToOne
	@JoinColumn(name = "assessment_id")
	private Assessment assessment;
	
	public WeightedHandMarking() {
		setWeight(0);
		setGroupWork(false);
	}
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public boolean isGroupWork() {
		return groupWork;
	}
	public void setGroupWork(boolean groupWork) {
		this.groupWork = groupWork;
	}
	
	public HandMarking getHandMarking() {
		return handMarking;
	}
	public void setHandMarking(HandMarking handMarking) {
		this.handMarking = handMarking;
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	public double getMaxMark() {
		double total = assessment.getRawTotalWeight();
		if(total == 0) {
			return 0;
		}
		return (getWeight() / total) * assessment.getMarks();
	}
	
	@Override
	public int compareTo(WeightedHandMarking other) {
		if(other == null) {
			return 1;
		}
		if(other.groupWork && !this.groupWork) {
			return 1;
		}
		if(this.groupWork && !other.groupWork) {
			return -1;
		}
		int diff;
		if(this.getHandMarking() != null) {
			diff = this.getHandMarking().compareTo(other.getHandMarking());
			if(diff != 0) {
				return diff;
			}
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

	/*===========================
	 * CONVENIENCE RELATIONSHIPS
	 * 
	 * Making unidirectional many-to-one relationships into bidirectional 
	 * one-to-many relationships for ease of deletion by Hibernate
	 *===========================
	 */
	@OneToMany(mappedBy = "weightedHandMarking", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<HandMarkingResult> handMarkingResults;
}
