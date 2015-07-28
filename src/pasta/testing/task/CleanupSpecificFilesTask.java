package pasta.testing.task;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class CleanupSpecificFilesTask implements Task {

	protected File reference;
	protected File deleteFrom;
	protected boolean alsoDeleteReference;
	
	public CleanupSpecificFilesTask(File reference, File deleteFrom, boolean alsoDeleteReference) {
		this.reference = reference;
		this.deleteFrom = deleteFrom;
		this.alsoDeleteReference = alsoDeleteReference;
	}

	@Override
	public boolean go() {
		File baseDir = deleteFrom;
		boolean success = deleteFile(baseDir, reference, true);
		if(alsoDeleteReference) {
			Logger.getLogger(getClass()).debug("Deleting " + reference);
			success = FileUtils.deleteQuietly(reference) && success;
		}
		return success;
	}
	
	private boolean deleteFile(File file, File reference, boolean topLevel) {
		boolean success = true;
		if(reference.isDirectory()) {
			for(File child : reference.listFiles()) {
				success = deleteFile(new File(file, child.getName()), child, false) && success;
			}
			if(file.exists() && !topLevel) {
				Logger.getLogger(getClass()).debug("Deleting " + file);
				file.delete(); // will only work if file is empty
			}
		} else {
			if(file.exists()) {
				Logger.getLogger(getClass()).debug("Deleting " + file);
				success = FileUtils.deleteQuietly(file) && success;
			}
		}
		return success;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": delete contents of " + reference.getAbsolutePath() + " from " + deleteFrom.getAbsolutePath();
	}
}
