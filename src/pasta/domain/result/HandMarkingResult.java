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

package pasta.domain.result;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedField;
import pasta.domain.template.WeightedHandMarking;

/**
 * Container class for the results of hand marking.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 * 
 */
@Entity
@Table (name = "hand_marking_results")
public class HandMarkingResult implements Serializable, Comparable<HandMarkingResult> {

	private static final long serialVersionUID = -2181570522930825901L;

	@Id @GeneratedValue
	private long id;
	
	@ElementCollection (fetch=FetchType.EAGER)
    @MapKeyColumn(name="row_id")
    @Column(name="column_id")
    @CollectionTable(name="hand_marking_map_results", joinColumns=@JoinColumn(name="hand_marking_result_id"))
	private Map<Long, Long> result = LazyMap.lazyMap(new TreeMap<Long, Long>(), FactoryUtils.constantFactory(0l));
	
    @Transient
	protected final Log logger = LogFactory.getLog(getClass());

    @ManyToOne
    @JoinColumn (name = "weighted_hand_marking_id")
	private WeightedHandMarking weightedHandMarking;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<Long, Long> getResult() {
		return result;
	}

	public void setResult(Map<Long, Long> result) {
		this.result = result;
	}

	public WeightedHandMarking getWeightedHandMarking() {
		return weightedHandMarking;
	}
	public void setWeightedHandMarking(WeightedHandMarking weightedHandMarking) {
		this.weightedHandMarking = weightedHandMarking;
	}
	
	public HandMarking getHandMarking() {
		return weightedHandMarking.getHandMarking();
	}
	
	public boolean isGroupWork() {
		return weightedHandMarking.isGroupWork();
	}

	public double getPercentage(){
		double percentage = 0;
		for (WeightedField t : getHandMarking().getRowHeader()) {
			if(result.containsKey(t.getId()) && getHandMarking().hasColumn(result.get(t.getId()))){
				percentage += (t.getWeight() * getHandMarking().getColumnWeight(result.get(t.getId())));
			}
		}
		
		return Math.max(0, Math.min(percentage, 1.0));
	}
	
	public boolean isFinishedMarking(){
		if(result.size() >= getHandMarking().getRowHeader().size()){
			for(Entry<Long, Long> entry: result.entrySet()){
				if(entry.getKey() == null 
						|| !(entry.getValue() instanceof Long)
						|| entry.getValue() == null){
					return false;
				}
			}
			return true;
		}
		return false;  
	}

	@Override
	public int compareTo(HandMarkingResult other) {
		return getHandMarking().getName().compareTo(other.getHandMarking().getName());
	}
	
}
