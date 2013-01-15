package pasta.domain;

import java.util.List;

public class FileTreeNode {
	private boolean leaf;
	private String name;
	private List<FileTreeNode> children;
	private String location;

	public FileTreeNode(String location, List<FileTreeNode> children) {
		int lastIndex = location.lastIndexOf("/");
		if(lastIndex == -1){
			lastIndex = location.lastIndexOf("\\");
		}
		this.location = location;
		this.name = location.substring(lastIndex+1);
		this.children = children;
		if (children == null || children.size() == 0) {
			leaf = true;
		}
	}

	public boolean isLeaf() {
		return leaf;
	}

	public String getName() {
		return name;
	}
	
	public String getLocation() {
		return location;
	}

	public List<FileTreeNode> getChildren() {
		return children;
	}
	
}
