package pasta.docker;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import pasta.util.PASTAUtil;

public class Language {
	protected static Logger logger = Logger.getLogger(Language.class);
	
	private String id;
	private String name;
	private String[] extensions;
	private File templateFile;
	private File dockerFile;
	
	private DockerBuildFile dockerBuildFile;
	
	public Language(String id, String name, String extensions, String templateFile, String dockerFile) {
		this.id = id;
		this.name = name;
		this.extensions = extensions.split(",");
		try {
			this.templateFile = PASTAUtil.getTemplateResource("build_templates/" + templateFile);
		} catch (FileNotFoundException e) {
			logger.error("Cannot find template file for " + this.id + ": " + templateFile);
		}
		try {
			this.dockerFile = PASTAUtil.getTemplateResource("docker/" + dockerFile);
		} catch (FileNotFoundException e) {
			logger.error("Cannot find docker file for " + this.id + ": " + dockerFile);
		}
		this.dockerBuildFile = new DockerBuildFile(id + "-exec", getDockerFile());
	}

	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String[] getExtensions() {
		return extensions;
	}
	public File getTemplateFile() {
		return templateFile;
	}
	public File getDockerFile() {
		return dockerFile;
	}
	public DockerBuildFile getDockerBuildFile() {
		return dockerBuildFile;
	}
	
}
