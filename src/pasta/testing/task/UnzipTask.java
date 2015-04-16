package pasta.testing.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.util.PASTAUtil;

public class UnzipTask implements Task {

	private File zipFile;
	private CommonsMultipartFile formFile;
	private File landingDirectory;
	
	public UnzipTask(File zipFile, File landingDirectory) {
		this.zipFile = zipFile;
		this.landingDirectory = landingDirectory;
	}
	
	public UnzipTask(CommonsMultipartFile formFile, File landingDirectory) {
		this.formFile = formFile;
		this.landingDirectory = landingDirectory;
	}

	@Override
	public boolean go() {
		try {
			File newLocation = null;
			if(zipFile == null) {
				newLocation = new File(landingDirectory, formFile.getOriginalFilename());
				formFile.getInputStream().close();
				formFile.transferTo(newLocation);
			} else {
				newLocation = new File(landingDirectory, zipFile.getName());
				FileUtils.copyFile(zipFile, newLocation);
			}
			PASTAUtil.extractFolder(newLocation.getAbsolutePath());
			FileUtils.forceDelete(newLocation);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " from " + (zipFile == null ? "form " + formFile.getOriginalFilename() : zipFile.getAbsolutePath()) + " to " + landingDirectory;
	}
}
