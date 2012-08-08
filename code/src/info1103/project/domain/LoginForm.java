package info1103.project.domain;

/**
 * Container for for the login informationS
 * 
 * Only has getters and setters.
 * 
 * @author Alex
 *
 */
public class LoginForm {
	private String unikey;
	private String password;
	
	public String getUnikey() {
		return unikey;
	}
	public void setUnikey(String unikey) {
		this.unikey = unikey.toLowerCase();
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
