package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

public class Runner {

	private File templateFile;
	private Map<String, String> options;
	
	public Runner(File templateFile) {
		this.templateFile = templateFile;
		if(!templateFile.exists()) {
			Logger.getLogger(getClass())
					.error("Template file \"" + templateFile + "\" not found for "
							+ this.getClass().getSimpleName());
			templateFile = null;
		}
		options = new HashMap<String, String>();
	}
	
	public void addOption(String key, String value) {
		options.put(key, value);
	}
	
	public String getOption(String key) {
		return options.get(key);
	}

	public String getBuildFileContents() {
		StringBuilder template = new StringBuilder();
		try {
			Scanner scn = new Scanner(templateFile);
			while(scn.hasNext()) {
				template.append(scn.nextLine()).append(System.lineSeparator());
			}
			scn.close();
		} catch (FileNotFoundException e) {
			return "";
		}
		
		String templateText = template.toString();
		for(Map.Entry<String, String> option : options.entrySet()) {
			templateText = templateText.replaceAll("(?i)\\$\\{" + option.getKey() + "\\}", Matcher.quoteReplacement(option.getValue()));
		}
		
		return templateText;
	}
	
	public File createBuildFile(String filename) {
		File file = new File(filename);
		return createBuildFile(file);
	}
	
	public File createBuildFile(File file) {
		if(file.exists()) {
			Logger.getLogger(getClass()).warn("Overriding build file " + file.getAbsolutePath());
		}
		try {
			PrintWriter out = new PrintWriter(file);
			out.println(getBuildFileContents());
			out.close();
		} catch (FileNotFoundException e) {
			Logger.getLogger(getClass()).error("Could not write build file " + file.getAbsolutePath(), e);
		}
		return file;
	}
	
}
