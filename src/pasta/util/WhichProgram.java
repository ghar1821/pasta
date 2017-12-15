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
