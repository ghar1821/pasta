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

package pasta.testing.task;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class CleanupSpecificFilesTask implements Task {

	protected File reference;
	protected File deleteFrom;
	protected boolean alsoDeleteReference;
	
	public CleanupSpecificFilesTask(File reference, File deleteFrom, boolean alsoDeleteReference) {
		this.reference = reference;
		this.deleteFrom = deleteFrom;
		this.alsoDeleteReference = alsoDeleteReference;
	}

	@Override
	public boolean go() {
		File baseDir = deleteFrom;
		boolean success = deleteFile(baseDir, reference, true);
		if(alsoDeleteReference) {
			Logger.getLogger(getClass()).debug("Deleting " + reference);
			success = FileUtils.deleteQuietly(reference) && success;
		}
		return success;
	}
	
	private boolean deleteFile(File file, File reference, boolean topLevel) {
		boolean success = true;
		if(reference.isDirectory()) {
			for(File child : reference.listFiles()) {
				success = deleteFile(new File(file, child.getName()), child, false) && success;
			}
			if(file.exists() && !topLevel) {
				Logger.getLogger(getClass()).debug("Deleting " + file);
				file.delete(); // will only work if file is empty
			}
		} else {
			if(file.exists()) {
				Logger.getLogger(getClass()).debug("Deleting " + file);
				success = FileUtils.deleteQuietly(file) && success;
			}
		}
		return success;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": delete contents of " + reference.getAbsolutePath() + " from " + deleteFrom.getAbsolutePath();
	}
}
