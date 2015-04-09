package pasta.testing.task;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class FileOrDirectoryDeleteTask implements Task {

	protected File file;
	
	public FileOrDirectoryDeleteTask(File file) {
		this.file = file;
	}
	
	@Override
	public boolean go() {
		return FileUtils.deleteQuietly(file);
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + ": delete " + file.getAbsolutePath();
	}
}
