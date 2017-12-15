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
