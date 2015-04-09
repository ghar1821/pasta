package pasta.testing.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class DirectoryCopyTask extends FileCopyTask {

	public DirectoryCopyTask(File from, File to) {
		super(from, to);
	}

	@Override
	public boolean go() {
		try {
			FileUtils.copyDirectory(from, to);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
