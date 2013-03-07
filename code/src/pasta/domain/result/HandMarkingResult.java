package pasta.domain.result;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.template.HandMarking;
import pasta.domain.template.Tuple;

public class HandMarkingResult {
	private Map<String, String> result = LazyMap.decorate(new HashMap<String, String>(), 
			FactoryUtils.instantiateFactory(HashMap.class));
	
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
		logger.info(result.size() + "-" + markingTemplate.getRowHeader().size());
		return (result.size() >= markingTemplate.getRowHeader().size());  
	}
	
}
