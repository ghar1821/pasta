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
