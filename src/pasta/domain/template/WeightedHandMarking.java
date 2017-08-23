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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pasta.archive.Archivable;
import pasta.archive.InvalidRebuildOptionsException;
import pasta.archive.RebuildOptions;

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
public class WeightedHandMarking implements Comparable<WeightedHandMarking>, Archivable<WeightedHandMarking> {

	private static final long serialVersionUID = -3429348535279846933L;
	
	@Id
	@GeneratedValue
	private Long id;
	
	private double weight;
	
	@Column(name= "group_work")
	private boolean groupWork;
	
	@ManyToOne
	@JoinColumn(name = "hand_marking_id")
	private HandMarking handMarking;
	
	@ManyToOne (cascade = CascadeType.ALL)
	private Assessment assessment;
	
	public WeightedHandMarking() {
		setWeight(0);
		setGroupWork(false);
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
		if(this.id == null) {
			return other.id == null ? 0 : -1;
		}
		if(other.id == null) {
			return 1;
		}
		return (this.id < other.id ? -1 : (this.id > other.id ? 1 : 0));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (groupWork ? 1231 : 1237);
		result = prime * result + ((handMarking == null) ? 0 : handMarking.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeightedHandMarking other = (WeightedHandMarking) obj;
		if (groupWork != other.groupWork)
			return false;
		if (handMarking == null) {
			if (other.handMarking != null)
				return false;
		} else if (!handMarking.equals(other.handMarking))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight))
			return false;
		return true;
	}

	@Override
	public WeightedHandMarking rebuild(RebuildOptions options) throws InvalidRebuildOptionsException {
		if(options.getParentAssessment() == null) {
			throw new InvalidRebuildOptionsException("Cannot rebuild a weighted hand marking template without an assessment");
		}
		WeightedHandMarking clone = new WeightedHandMarking();
		clone.setAssessment(options.getParentAssessment());
		clone.setGroupWork(this.isGroupWork());
		clone.setHandMarking(this.getHandMarking() == null ? null : this.getHandMarking().rebuild(options));
		clone.setWeight(this.getWeight());
		return clone;
	}
}
