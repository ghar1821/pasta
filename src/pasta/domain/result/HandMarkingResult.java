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

public class HandMarkingResult implements Comparable{
	private Map<String, String> result = LazyMap.decorate(new TreeMap<String, String>(), 
			FactoryUtils.instantiateFactory(TreeMap.class));
	
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
		
		return Math.min(percentage, 1.0);
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
	public int compareTo(Object o) {
		HandMarkingResult target = (HandMarkingResult)(o);
		return markingTemplate.getName().compareTo(target.getMarkingTemplate().getName());
	}
	
}
