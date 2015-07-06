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
	JAVA("Java", "java"), C("C", "c"), CPP("C++", "cpp", "cc"), PYTHON("Python", "py");
	
	private String name;
	private TreeSet<String> extensions;
	private Language(String name, String... extensions) {
		this.name = name;
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
	public String getDescription() {
		return toString();
	}
	public TreeSet<String> getExtensions() {
		return extensions;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" [");
		for(String ext : extensions) {
			 sb.append("\".").append(ext).append("\", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		if(extensions.size() > 0) {
			sb.append("]");
		}
		return sb.toString();
	}
}
