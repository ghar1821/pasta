package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import pasta.util.ProjectProperties;

public abstract class Runner {

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
		setLibDirectory(ProjectProperties.getInstance().getProjectLocation() + "lib/");
	}
	
	public void addOption(String key, String value) {
		options.put(key, value);
	}
	
	public void addArgsOption(String key, String... values) {
		addArgsOption(key, Arrays.asList(values));
	}
	
	public void addArgsOption(String key, Iterable<String> values) {
		StringBuilder sb = new StringBuilder();
		for(String value : values) {
			sb.append("<arg value=\"" + value + "\"/>");
		}
		options.put(key, sb.toString());
	}
	
	public void addEnvOption(String key, Map<String, Map<String, String>> environments) {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Map<String, String>> environment : environments.entrySet()) {
			String type = environment.getKey();
			for(Map.Entry<String, String> entry : environment.getValue().entrySet()) {
				sb.append("<env key=\"" + entry.getKey() + "\" " + type + "=\"" + entry.getValue() + "\"/>");
			}
		}
		options.put(key, sb.toString());
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
		
		// optional option syntax: only included if option <name> is not empty
		// NOTE: only one per line!
		//		{$?{<name>}<value>}
		Pattern p = Pattern.compile("\\{\\$\\?\\{([^\\}]*)\\}");
		Matcher m = p.matcher(template);
		while(m.find()) {
			int start = m.start();
			int open = 1;
			int end = m.end();
			for(; end < template.length() && open > 0; end++) {
				if(template.charAt(end) == '}') {
					open--;
				} else if(template.charAt(end) == '{') {
					open++;
				}
			}
			end--;
			if(open != 0) {
				break;
			}
			
			String key = m.group(1);
			if(options.get(key) != null && !options.get(key).isEmpty()) {
				template.replace(start, m.end(), "");
				end = end - (m.end() - start);
				template.replace(end, end+1, "");
			} else {
				template.replace(start, end+1, "");
			}
			m.reset();
		}
		
		String templateText = template.toString();
		for(Map.Entry<String, String> option : options.entrySet()) {
			// option syntax: replace with option <name>
			//		{$<name>}
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
			file.delete();
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
	
	public void setLibDirectory(String directory) {
		directory = directory.replaceAll("[\\/]", Matcher.quoteReplacement(File.separator));
		if(!directory.endsWith(File.separator)) {
			directory += File.separator;
		}
		addOption("libDirectory", directory);
	}
	
	public abstract String extractCompileErrors(AntResults results);
	public abstract String extractFilesCompiled(AntResults results);
	
}
