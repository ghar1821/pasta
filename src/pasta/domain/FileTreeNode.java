/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
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
		} else if ("competition".equals(owner)) {
			return ProjectProperties.getInstance().getCompetitionsLocation();
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
