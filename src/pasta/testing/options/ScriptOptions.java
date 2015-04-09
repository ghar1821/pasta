package pasta.testing.options;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScriptOptions implements Serializable {
	private static final long serialVersionUID = 4085726115768912846L;
	
	public String scriptFilename = null;
	public long timeout = 30000;
	
	public String outputFilename = null;
	public String inputFilename = null;
	public String errorFilename = null;
	
	private List<String> arguments;
	private Map<String, Map<String, String>> environment;
	
	public ScriptOptions() {
		arguments = new LinkedList<String>();
		environment = new HashMap<String, Map<String, String>>();
		environment.put("value", new LinkedHashMap<String, String>());
		environment.put("path", new LinkedHashMap<String, String>());
		environment.put("file", new LinkedHashMap<String, String>());
	}
	
	public void addArgument(String arg) {
		arguments.add(arg);
	}
	
	public void addArguments(Iterable<String> args) {
		for(String arg : args) {
			addArgument(arg);
		}
	}
	
	public void putEnvironmentPath(String key, String path) {
		putEnvironment("path", key, path);
	}
	
	public void putEnvironmentFile(String key, String file) {
		putEnvironment("file", key, file);
	}
	
	public void putEnvironment(String key, String value) {
		putEnvironment("value", key, value);
	}
	
	private void putEnvironment(String type, String key, String value) {
		environment.get(type).put(key, value);
	}
	
	public List<String> getArguments() {
		return arguments;
	}
	
	/**
	 * @return Map of type ("path"|"file"|"value") to key-value pair environments.
	 */
	public Map<String, Map<String, String>> getEnvironment() {
		return environment;
	}

	public String getScriptFilename() {
		return scriptFilename;
	}

	public void setScriptFilename(String scriptFilename) {
		this.scriptFilename = scriptFilename;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getOutputFilename() {
		return outputFilename;
	}

	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}

	public String getInputFilename() {
		return inputFilename;
	}

	public void setInputFilename(String inputFilename) {
		this.inputFilename = inputFilename;
	}

	public String getErrorFilename() {
		return errorFilename;
	}

	public void setErrorFilename(String errorFilename) {
		this.errorFilename = errorFilename;
	}
}
