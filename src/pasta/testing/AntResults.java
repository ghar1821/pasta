package pasta.testing;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AntResults {

	private Map<String, StringBuilder> outputs;
	private Map<String, Boolean> success;
	
	public AntResults() {
		outputs = new LinkedHashMap<String, StringBuilder>();
		success = new HashMap<String, Boolean>();
	}
	
	public AntResults(String... targets) {
		this();
		addTargets(targets);
	}
	
	public void addTargets(String... targets) {
		for(String target : targets) {
			outputs.put(target, new StringBuilder());
		}
	}
	
	public boolean append(String target, String contents) {
		StringBuilder sb = outputs.get(target);
		if(sb == null) {
			return false;
		}
		sb.append(contents).append(System.lineSeparator());
		return true;
	}
	
	public String getOutput(String target) {
		if(!outputs.containsKey(target)) {
			return "";
		}
		return outputs.get(target).toString();
	}
	
	public boolean isSuccess(String target) {
		return hasRun(target) && success.get(target);
	}
	
	public boolean hasRun(String target) {
		return success.get(target) != null;
	}
	
	public void setSuccess(String target, boolean success) {
		this.success.put(target, success);
	}
	
	public String getFullOutput() {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, StringBuilder> entry : outputs.entrySet()) {
			sb.append(entry.getValue().toString()).append(System.lineSeparator());
		}
		return sb.toString();
	}
}
