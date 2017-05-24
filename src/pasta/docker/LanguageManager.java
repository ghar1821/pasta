package pasta.docker;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import pasta.util.PASTAUtil;

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
			String runnerClass = properties.getProperty(id + ".runner-class");
			this.languages.put(id, new Language(id, name, extensions, templateFile, dockerFile, runnerClass));
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
	
	public Language getLanguage(String languageId) {
		return languages.get(languageId);
	}

	public Language guessLanguage(String solutionName, String codeBaseDir, File submissionDir) {
		Language language = null;
		if(solutionName.contains(".")) {
			Map<File, String> qualifiedNames = PASTAUtil.mapJavaFilesToQualifiedNames(submissionDir);
			if(!qualifiedNames.isEmpty()) {
				if(qualifiedNames.containsValue(solutionName)) {
					language = getLanguage("java");
				}
			}
		}
		if(language == null) {
			String[] submissionContents = PASTAUtil.listDirectoryContents(submissionDir);
			String shortest = null;
			for(String filename : submissionContents) {
				if(filename.matches(codeBaseDir + ".*" + solutionName + "\\.[^/\\\\]+")) {
					if(getFileLanguage(filename) != null) {
						if(shortest == null || filename.length() < shortest.length()) {
							shortest = filename;
						}
					}
				}
			}
			language = getFileLanguage(shortest);
		}
		return language;
	}
	
	public Language getFileLanguage(File file) {
		if(file == null || file.isDirectory()) {
			return null;
		}
		return getLanguage(file.getName());
	}
	public Language getFileLanguage(String filename) {
		for(Language lang : getLanguages()) {
			if(lang.isLanguage(filename)) {
				return lang;
			}
		}
		return null;
	}
}
