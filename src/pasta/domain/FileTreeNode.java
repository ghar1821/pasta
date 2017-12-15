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

package pasta.domain;

import java.io.File;
import java.util.List;

import pasta.util.ProjectProperties;

/**
 * @author Alex Radu
 * @version 2.0
 * @since 2013-01-15
 */
public class FileTreeNode {
	private boolean leaf;
	private String name;
	private List<FileTreeNode> children;
	private String owner;
	private String path;
	private File file;

	/**
	 * Constructor to set up the node
	 * 
	 * @param location the location of the node in the file system
	 * @param children the list of children for the node
	 */
	public FileTreeNode(File file, List<FileTreeNode> children, String owner) {
		this.name = file.getName();
		this.children = children;
		if (children == null || children.size() == 0) {
			leaf = true;
		}
		this.owner = owner;
		this.path = getPath(file);
		this.file = file;
	}
	
	private static String getFileBase(String owner) {
		if ("unitTest".equals(owner)) {
			return ProjectProperties.getInstance().getUnitTestsLocation();
		} else if ("assessment".equals(owner)) {
			return ProjectProperties.getInstance().getAssessmentValidatorLocation();
		} else {
			return ProjectProperties.getInstance().getSubmissionsLocation();
		}
	}
	private String getPath(File file) {
		String base = getFileBase(owner);
		return file.getAbsolutePath().substring(base.length());
	}

	public boolean isLeaf() {
		return leaf;
	}
	
	public void setLeaf(boolean leaf){
		this.leaf = leaf;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPath() {
		return path;
	}

	public List<FileTreeNode> getChildren() {
		return children;
	}
	
	public String getExtension() {
		return name.substring(name.lastIndexOf('.') + 1);
	}

	public String getOwner() {
		return owner;
	}
	
	public File getFile() {
		return file;
	}
}
