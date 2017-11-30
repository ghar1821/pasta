package pasta.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import pasta.service.PASTAOptions;

@Service("whichProgram")
public class WhichProgram {

	private Map<String, String> programs;
	
	private static WhichProgram instance;
	
	protected final static Logger logger = Logger.getLogger(WhichProgram.class);
	
	private static final String[] required = {"java", "javac", "time", "timeout", "mysqldump"};
	private static final String[] optional = {"python2", "python3", "g++", "gcc"};
	
	@Autowired
	private WhichProgram(@Qualifier("programProperties") Properties properties) {
		programs = new HashMap<String, String>();
		
		for(Object propKey : properties.keySet()) {
			String key = (String) propKey;
			String value = properties.getProperty(key);
			programs.put(key, value);
			logger.debug("Registering program " + key + " at " + value);
		}
		
		List<String> missing = new LinkedList<>();
		for(String program : required) {
			if(!hasProgram(program)) {
				missing.add(program);
			}
		}
		if(missing.size() != 0) {
			logger.error("Missing required program paths in programs.properties: " + missing.toString());
		}
		
		missing = new LinkedList<>();
		for(String program : optional) {
			if(!hasProgram(program)) {
				missing.add(program);
			}
		}
		if(missing.size() != 0) {
			logger.info("Missing optional program paths in programs.properties: " + missing.toString());
		}
		
		WhichProgram.instance = this;
	}
	
	public boolean hasProgram(String program) {
		return path(program) != null;
	}
	
	public String path(String program) {
		String path = programs.get(program);
		if(path == null) {
			path = PASTAOptions.instance().get("programs." + program);
		}
		return path;
	}
	
	public static WhichProgram getInstance() {
		return instance;
	}
}
