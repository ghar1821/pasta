package pasta.domain.result;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.LazyMap;

import pasta.domain.template.Assessment;

public class HandMarkingResult {
	private Map<String, String> result = LazyMap.decorate(new HashMap<String, String>(), 
			FactoryUtils.instantiateFactory(HashMap.class));
	
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

}
