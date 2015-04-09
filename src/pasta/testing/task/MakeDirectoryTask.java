package pasta.testing.task;

import java.io.File;

public class MakeDirectoryTask implements Task {

	protected File file;
	
	public MakeDirectoryTask(File file) {
		this.file = file;
	}
	
	@Override
	public boolean go() {
		return file.mkdirs();
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + ": mkdirs " + file.getAbsolutePath();
	}
}
