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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.template.HandMarking;
import pasta.domain.template.Tuple;

/**
 * Container class for the results of hand marking.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 * 
 */
public class HandMarkingResult implements Comparable <HandMarkingResult>{
	private Map<String, String> result = LazyMap.decorate(new TreeMap<String, String>(), 
			FactoryUtils.instantiateFactory(String.class));
	
	protected final Log logger = LogFactory.getLog(getClass());

	private HandMarking markingTemplate;
	private String handMarkingTemplateShortName;

	public Map<String, String> getResult() {
		return result;
	}

	public void setResult(Map<String, String> result) {
		this.result = result;
	}

	public String getHandMarkingTemplateShortName() {
		return handMarkingTemplateShortName;
	}

	public void setHandMarkingTemplateShortName(String handMarkingTemplateShortName) {
		this.handMarkingTemplateShortName = handMarkingTemplateShortName;
	}

	public HandMarking getMarkingTemplate() {
		return markingTemplate;
	}

	public void setMarkingTemplate(HandMarking markingTemplate) {
		this.markingTemplate = markingTemplate;
	}
	
	public double getPercentage(){
		double percentage = 0;
		for (Tuple t : markingTemplate.getRowHeader()) {
			if(result.containsKey(t.getName()) && markingTemplate.getColumnHeaderAsMap().containsKey(result.get(t.getName()))){
				percentage += (t.getWeight() * markingTemplate
						.getColumnHeaderAsMap().get(result.get(t.getName())));
			}
		}
		
		return Math.max(0, Math.min(percentage, 1.0));
	}
	
	public boolean isFinishedMarking(){
		//logger.info(result.size() + "-" + markingTemplate.getRowHeader().size());
		if(result.size() >= markingTemplate.getRowHeader().size()){
			for(Entry<String, String> entry: result.entrySet()){
				logger.info(entry.getKey());
				logger.info(entry.getValue());
				if(entry.getKey() == null 
						|| !(entry.getValue() instanceof String)
						|| entry.getValue() == null){
					return false;
				}
			}
			return true;
		}
		return false;  
	}

	@Override
	public int compareTo(HandMarkingResult target) {
		return markingTemplate.getName().compareTo(target.getMarkingTemplate().getName());
	}
	
}
