package pasta.testing.task;

import java.io.File;

import org.apache.log4j.Logger;

public class MakeDirectoryTask implements Task {

	protected File file;
	
	public MakeDirectoryTask(File file) {
		this.file = file;
	}
	
	@Override
	public boolean go() {
		if(file.exists() && file.isDirectory()) {
			return true;
		}
		Logger.getLogger(getClass()).debug("Making directories to " + file);
		return file.mkdirs();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": mkdirs " + file.getAbsolutePath();
	}
}
