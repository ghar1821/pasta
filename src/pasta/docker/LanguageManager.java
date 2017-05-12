package pasta.docker;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class LanguageManager {
	protected static Logger logger = Logger.getLogger(LanguageManager.class);
	private static LanguageManager instance;
	
	private Map<String, Language> languages;
	
	private LanguageManager(Properties properties) {
		this.languages = new HashMap<>();
		String languages = properties.getProperty("languages", "");
		for(String language : languages.split(",")) {
			String id = language.trim();
			String name = properties.getProperty(id + ".name", "Unnamed");
			String extensions = properties.getProperty(id + ".extensions");
			String templateFile = properties.getProperty(id + ".build-template");
			String dockerFile = properties.getProperty(id + ".docker-build");
			this.languages.put(id, new Language(id, name, extensions, templateFile, dockerFile));
			logger.info("Registered language " + name);
		}
		
		instance = this;
		DockerManager.instance();
	}

	public static LanguageManager getInstance() {
		return instance;
	}
	
	public Collection<Language> getLanguages() {
		return languages.values();
	}
	
	public List<DockerBuildFile> getDockerBuildFiles() {
		List<DockerBuildFile> results = new LinkedList<>();
		for(Language lang : languages.values()) {
			results.add(lang.getDockerBuildFile());
		}
		return results;
	}
}
