package pasta.docker;

import java.io.File;

public class DockerBuildFile {
	enum Status {
		NOT_YET_BUILT, BUILD_SUCCESS, BUILD_FAILED;
	}
	
	private String id;
	private String tag;
	private File file;
	private Status status;
	
	public DockerBuildFile(String tag, File file) {
		this.tag = tag;
		this.file = file;
		this.status = Status.NOT_YET_BUILT;
		this.id = null;
	}
	
	public String getTag() {
		return tag;
	}
	public File getFile() {
		return file;
	}
	public Status getStatus() {
		return status;
	}
	public String getId() {
		return id;
	}
	
	public void registerSuccess(String id) {
		this.status = Status.BUILD_SUCCESS;
		this.id = id;
	}
	public void registerFailure() {
		this.status = Status.BUILD_FAILED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DockerBuildFile other = (DockerBuildFile) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}
}
