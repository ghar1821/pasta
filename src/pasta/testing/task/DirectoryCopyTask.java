package pasta.testing.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class DirectoryCopyTask extends FileCopyTask {

	public DirectoryCopyTask(File from, File to) {
		super(from, to);
	}

	@Override
	public boolean go() {
		try {
			Logger.getLogger(getClass()).debug("Copying directory " + from + " to " + to);
			FileUtils.copyDirectory(from, to);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
