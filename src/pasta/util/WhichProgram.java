package pasta.util;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import pasta.service.PASTAOptions;

public class WhichProgram {

	private static WhichProgram instance;
	
	protected final static Logger logger = Logger.getLogger(WhichProgram.class);
	
	private static final String[] required = {"java", "javac", "time", "timeout", "mysqldump"};
	private static final String[] optional = {"matlab", "python2", "python3", "g++", "gcc"};
	
	private WhichProgram() {
		List<String> missing = new LinkedList<>();
		for(String program : required) {
			if(!hasProgram(program)) {
				missing.add(program);
			}
		}
		if(missing.size() != 0) {
			logger.error("Missing required program paths in options: " + missing.toString());
		}
		
		missing = new LinkedList<>();
		for(String program : optional) {
			if(!hasProgram(program)) {
				missing.add(program);
			}
		}
		if(missing.size() != 0) {
			logger.info("Missing optional program paths in options: " + missing.toString());
		}
		
		WhichProgram.instance = this;
	}
	
	public boolean hasProgram(String program) {
		return PASTAOptions.instance().hasKey("programs." + program);
	}
	
	public String path(String program) {
		return PASTAOptions.instance().get("programs." + program);
	}
	
	public static WhichProgram getInstance() {
		return instance;
	}
}
