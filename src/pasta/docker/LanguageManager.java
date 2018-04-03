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

package pasta.docker;

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import pasta.util.PASTAUtil;

@Service("languageManager")
@DependsOn("pastaOptions")
public class LanguageManager {
	protected static Logger logger = Logger.getLogger(LanguageManager.class);
	private static LanguageManager instance;
	
	private Map<String, Language> languages;
	
	private Properties properties;
	
	@Autowired
	private LanguageManager(@Qualifier("languageProperties") Properties properties) {
		this.languages = new HashMap<>();
		String languages = properties.getProperty("languages", "");
		for(String language : languages.split(",")) {
			String id = language.trim();
			String templateFile = properties.getProperty(id + ".build-template");
			String dockerFile = properties.getProperty(id + ".docker-build");
			String runnerClass = properties.getProperty(id + ".runner-class");

			HashMap<String, String> buildArgs = new HashMap<>();
			Enumeration<?> propNames = properties.propertyNames();
			while(propNames.hasMoreElements()) {
				String propKey = propNames.nextElement().toString();
				if(propKey.startsWith(id + ".build-arg.")) {
					String buildArgKey = propKey.substring((id + ".build-arg.").length());
					String buildArgValue = properties.getProperty(propKey);
					buildArgs.put(buildArgKey, buildArgValue);
				}
			}

			Language newLang = new Language(id, templateFile, dockerFile, buildArgs, runnerClass);
			this.languages.put(id, newLang);
			logger.info("Registered language " + newLang.getName());
		}
		this.properties = properties;
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
	
	public String getProperty(String key) {
		return properties.getProperty(key);
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
