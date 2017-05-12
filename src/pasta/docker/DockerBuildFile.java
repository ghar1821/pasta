package pasta.docker;

import java.io.File;

public class DockerBuildFile {
	enum Status {
		NOT_YET_BUILT, BUILD_SUCCESS, BUILD_FAILED;
	}
	
	private String id;
	private File file;
	private Status status;
	
	public DockerBuildFile(String id, File file) {
		this.id = id;
		this.file = file;
		this.status = Status.NOT_YET_BUILT;
	}
	
	public String getId() {
		return id;
	}
	public File getFile() {
		return file;
	}
	public Status getStatus() {
		return status;
	}
	
	public void registerSuccess() {
		this.status = Status.BUILD_SUCCESS;
	}
	public void registerFailure() {
		this.status = Status.BUILD_FAILED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
