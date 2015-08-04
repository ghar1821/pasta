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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Container class for a weighted competition.
 * <p>
 * This class contains a link to a competition to an assessment
 * and the weight of the competition in the assessment.
 * 
 * Using this, you can have the same competition as part of multiple
 * assessments with different weights.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-01-22
 *
 */
@Entity
@Table (name = "weighted_competitions")
public class WeightedCompetition implements Serializable, Comparable<WeightedCompetition> {

	private static final long serialVersionUID = -1720735664578767235L;
	
	@Id
	@GeneratedValue
	private long id;
	
	private double weight;
	
	@Column(name= "group_work")
	private boolean groupWork;
	
	@ManyToOne
	@JoinColumn(name = "competition_id")
	private Competition competition;
	
	@ManyToOne (cascade = CascadeType.ALL)
	private Assessment assessment;
	
	public WeightedCompetition() {
		setWeight(0);
		setGroupWork(false);
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	public Competition getCompetition() {
		return competition;
	}
	public void setCompetition(Competition comp) {
		this.competition = comp;
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
	
	@Override
	public int compareTo(WeightedCompetition other) {
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
		if(this.getCompetition() != null) {
			diff = this.getCompetition().compareTo(other.getCompetition());
			if(diff != 0) {
				return diff;
			}
		}
		diff = (this.weight < other.weight ? -1 : (this.weight > other.weight ? 1 : 0));
		if(diff != 0) {
			return diff;
		}
		return (this.id < other.id ? -1 : (this.id > other.id ? 1 : 0));
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((competition == null) ? 0 : competition.hashCode());
		result = prime * result + (groupWork ? 1231 : 1237);
		result = prime * result + (int) (id ^ (id >>> 32));
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
		WeightedCompetition other = (WeightedCompetition) obj;
		if (competition == null) {
			if (other.competition != null)
				return false;
		} else if (!competition.equals(other.competition))
			return false;
		if (groupWork != other.groupWork)
			return false;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight))
			return false;
		return true;
	}
}
