package pasta.testing.task;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class FileOrDirectoryDeleteTask implements Task {

	protected File file;
	
	public FileOrDirectoryDeleteTask(File file) {
		this.file = file;
	}
	
	@Override
	public boolean go() {
		Logger.getLogger(getClass()).debug("Deleting directory: " + file);
		return FileUtils.deleteQuietly(file);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": delete " + file.getAbsolutePath();
	}
}
