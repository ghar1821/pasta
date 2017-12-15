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
	JAVA("Java", "java"), C("C", "c"), CPP("C++", "cpp", "cc"), PYTHON("Python", "py"), MATLAB("MATLAB", "m");
	
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
