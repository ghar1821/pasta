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
