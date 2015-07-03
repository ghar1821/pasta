package pasta.testing.task;

import java.io.File;

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
		return file.mkdirs();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": mkdirs " + file.getAbsolutePath();
	}
}
