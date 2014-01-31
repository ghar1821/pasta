package pasta.domain.form;

/**
 * Container for for the login informationS
 * 
 * Only has getters and setters.
 * 
 * @author Alex
 *
 */
public class ChangePasswordForm {
	private String oldPassword;
	private String newPassword;
	private String confirmPassword;
	
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getConfirmPassword() {
		return confirmPassword;
	}
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	
}
