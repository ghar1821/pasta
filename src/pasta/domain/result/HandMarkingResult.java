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

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;
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
@VerboseName("hand-marking result")
public class HandMarkingResult extends BaseEntity implements Serializable, Comparable<HandMarkingResult> {

	private static final long serialVersionUID = -2181570522930825901L;

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
    
    @ManyToOne
	@JoinColumn (name = "assessment_result_id")
	private AssessmentResult assessmentResult;
	
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
		return weightedHandMarking == null ? null : weightedHandMarking.getHandMarking();
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
	
	public AssessmentResult getAssessmentResult() {
		return assessmentResult;
	}
	public void setAssessmentResult(AssessmentResult assessmentResult) {
		this.assessmentResult = assessmentResult;
	}

	@Override
	public int compareTo(HandMarkingResult other) {
		if(this.getHandMarking() == null) {
			return other.getHandMarking() == null ? 0 : 1;
		}
		if(other.getHandMarking() == null) {
			return -1;
		}
		return getHandMarking().getName().compareTo(other.getHandMarking().getName());
	}
	
}
