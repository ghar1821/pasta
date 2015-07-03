package pasta.util;

import java.io.File;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 27 Jun 2015
 */
public enum Language {
	JAVA("java"), C("c"), CPP("cpp", "cc"), PYTHON("py"), MATLAB("m");
	
	private TreeSet<String> extensions;
	private Language(String... extensions) {
		this.extensions = new TreeSet<String>(Arrays.asList(extensions));
	}
	public boolean isLanguage(File file) {
		return file != null && !file.isDirectory() && isLanguage(file.getName());
	}
	public boolean isLanguage(String filename) {
		return filename != null &&
				extensions.contains(filename.substring(filename.lastIndexOf('.') + 1).toLowerCase());
	}
	public static Language getLanguage(File file) {
		if(file == null || file.isDirectory()) {
			return null;
		}
		return getLanguage(file.getName());
	}
	public static Language getLanguage(String filename) {
		for(Language lang : Language.values()) {
			if(lang.isLanguage(filename)) {
				return lang;
			}
		}
		return null;
	}
}
