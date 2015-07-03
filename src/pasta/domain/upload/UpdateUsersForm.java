package pasta.domain.upload;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 30 Jun 2015
 */
public class UpdateUsersForm {
	private boolean updateTutors;
	private boolean replace;
	private String updateContents;
	private CommonsMultipartFile updateFile;
	
	public UpdateUsersForm() {
		updateTutors = false;
		replace = false;
		updateContents = "";
		updateFile = null;
	}

	public boolean isUpdateTutors() {
		return updateTutors;
	}

	public void setUpdateTutors(boolean updateTutors) {
		this.updateTutors = updateTutors;
	}
	
	public boolean isReplace() {
		return replace;
	}

	public void setReplace(boolean replace) {
		this.replace = replace;
	}

	public String getUpdateContents() {
		return updateContents;
	}

	public void setUpdateContents(String updateContents) {
		this.updateContents = updateContents;
	}

	public CommonsMultipartFile getUpdateFile() {
		return updateFile;
	}

	public void setUpdateFile(CommonsMultipartFile updateFile) {
		this.updateFile = updateFile;
	}
}
