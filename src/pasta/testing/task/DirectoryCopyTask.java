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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class DirectoryCopyTask extends FileCopyTask {

	private boolean contentsOnly;
	
	public DirectoryCopyTask(File from, File to) {
		this(from, to, false);
	}
	
	public DirectoryCopyTask(File from, File to, boolean contentsOnly) {
		super(from, to);
		this.contentsOnly = contentsOnly;
	}

	@Override
	public boolean go() {
		if(!from.isDirectory() || (contentsOnly && !to.isDirectory())) {
			return false;
		}
		
		try {
			if(contentsOnly) {
				Logger.getLogger(getClass()).debug("Copying contents of directory " + from + " to " + to);
				for(File child : from.listFiles()) {
					if(child.isDirectory()) {
						FileUtils.copyDirectoryToDirectory(child, to);
					} else {
						FileUtils.copyFileToDirectory(child, to);
					}
				}
			} else {
				Logger.getLogger(getClass()).debug("Copying directory " + from + " to " + to);
				FileUtils.copyDirectory(from, to);
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
