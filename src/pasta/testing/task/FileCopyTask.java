package pasta.testing.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileCopyTask implements Task {

	protected File from;
	protected File to;
	
	public FileCopyTask(File from, File to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public boolean go() {
		try {
			FileUtils.copyFile(from, to);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + ": from " + from.getAbsolutePath() + " to " + to.getAbsolutePath();
	}
}
