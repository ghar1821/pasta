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

package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import pasta.docker.DockerManager;

public abstract class Runner {

	private File templateFile;
	private Map<String, String> options;
	private List<String> writableFiles;
	
	public Runner(File templateFile) {
		this.templateFile = templateFile;
		if(!templateFile.exists()) {
			Logger.getLogger(getClass())
					.error("Template file \"" + templateFile + "\" not found for "
							+ this.getClass().getSimpleName());
			templateFile = null;
		}
		options = new HashMap<String, String>();
		writableFiles = new LinkedList<String>();
		setLibDirectory(DockerManager.PASTA_LIB);
		setOutDirectory(DockerManager.PASTA_OUT);
		setBinDirectory(DockerManager.PASTA_BIN);
		setSrcDirectory(DockerManager.PASTA_SRC);
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
	
	public void addWritableFilePaths(String... paths) {
		writableFiles.addAll(Arrays.asList(paths));
	}
	
	public String getOption(String key) {
		return options.get(key);
	}

	public String getBuildFileContents() {
		compileWritableFilesList();
		
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
			if(option.getValue() == null) {
				Logger.getLogger(getClass()).warn("Skipping replacement of \"${" + option.getKey() + "}\"; value is null");
			} else {
				templateText = templateText.replaceAll("(?i)\\$\\{" + option.getKey() + "\\}", Matcher.quoteReplacement(option.getValue()));
			}
		}
		
		return templateText;
	}
	
	private void compileWritableFilesList() {
		StringBuilder str = new StringBuilder();
		for(String path : writableFiles) {
			str.append("<include name=\"" + path + "\"/>");
		}
		options.put("writableFiles", str.toString());
		if(str.length() > 0) {
			options.put("hasWritable", "yes");
		}
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
	
	private String fixPath(String directory) {
		directory = directory.replaceAll("[\\/]", Matcher.quoteReplacement(File.separator));
		return directory;
	}
	public void setSrcDirectory(String directory) {
		addOption("srcDirectory", fixPath(directory));
	}
	public void setOutDirectory(String directory) {
		addOption("outDirectory", fixPath(directory));
	}
	public void setBinDirectory(String directory) {
		addOption("binDirectory", fixPath(directory));
	}
	public void setLibDirectory(String directory) {
		addOption("libDirectory", fixPath(directory));
	}
	
	public abstract String extractCompileErrors(File compileErrorFile, AntResults results);
	public abstract String extractFilesCompiled(AntResults results);
	
}
