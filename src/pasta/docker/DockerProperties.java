package pasta.docker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import pasta.util.PASTAUtil;

public class DockerProperties {

	private Map<String, DockerBuildFile> buildFiles;
	
	private static DockerProperties instance;
	
	protected final static Logger logger = Logger.getLogger(DockerProperties.class);
	
	private DockerProperties(Properties properties) {
		buildFiles = new HashMap<String, DockerBuildFile>();
		
		// Copy docker build files to content
		try {
			PASTAUtil.getTemplateResource("docker/");
		} catch (FileNotFoundException e) {}
		
		String prefix = "build.";
		for(Object propKey : properties.keySet()) {
			String key = (String) propKey;
			String value = properties.getProperty(key);
			if(key.startsWith(prefix)) {
				String buildKey = key.substring(prefix.length());
				try {
					File file = PASTAUtil.getTemplateResource("docker/" + value);
					buildFiles.put(buildKey, new DockerBuildFile(buildKey, file));
					logger.debug("Registering docker buildfile " + buildKey + " at " + file);
				} catch (FileNotFoundException e) {
					logger.error("Error registering docker buildfile " + buildKey + "; not found: " + value);
				}
			}
		}
		
		DockerProperties.instance = this;
		DockerManager.instance();
	}
	
	public static DockerProperties getInstance() {
		return instance;
	}
	
	public Collection<DockerBuildFile> getBuildFiles() {
		return buildFiles.values();
	}
}
