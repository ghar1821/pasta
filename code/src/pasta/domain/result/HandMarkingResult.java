package pasta.domain.result;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.LazyMap;

public class HandMarkingResult {
	private Map<String, String> result = LazyMap.decorate(new HashMap<String, String>(), 
			FactoryUtils.instantiateFactory(HashMap.class));

	public Map<String, String> getResult() {
		return result;
	}

	public void setResult(Map<String, String> result) {
		this.result = result;
	}
}
