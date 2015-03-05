/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.domain.template;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
public class WeightedHandMarking implements Serializable, Comparable<WeightedHandMarking> {

	private static final long serialVersionUID = -3429348535279846933L;
	
	@Id
	@GeneratedValue
	private long id;
	
	private double weight;
	
	@ManyToOne
	@JoinColumn(name = "hand_marking_id")
	private HandMarking handMarking;
	
	@ManyToOne (cascade = CascadeType.ALL)
	private Assessment assessment;
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
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
	
	@Override
	public int compareTo(WeightedHandMarking other) {
		int diff = this.handMarking.compareTo(other.handMarking);
		if(diff != 0) {
			return diff;
		}
		return (this.weight < other.weight ? -1 : (this.weight > other.weight ? 1 : 0));
	}
}
