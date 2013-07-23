package pasta.login;

import java.util.List;
import java.util.Properties;

import javax.mail.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.LoginForm;
import pasta.util.ProjectProperties;

public class FTPAuthValidator implements Validator{

	// what to use to write the log to.
	protected final Log logger = LogFactory.getLog(getClass());

	// not really used as far as i can see.
	@Override
	public boolean supports(Class<?> clazz) {
		return LoginForm.class.isAssignableFrom(clazz);
	}

	/**
	 * Method used for validation
	 */
	@Override
	public void validate(Object target, Errors errors) {
		LoginForm login = (LoginForm) target;
		// reject if the username is empty
		if (login.getUnikey() == null || login.getUnikey().length() == 0) {
			errors.rejectValue("unikey", "NotEmpty.loginForm.unikey");
		}
		// reject if the password is empty
		if (login.getPassword() == null || login.getPassword().length() == 0) {
			errors.rejectValue("password", "NotEmpty.loginForm.password");
		}
		
		// if nothing has gone wrong yet, check using the mail servers. 
		if (!errors.hasErrors()){
			String unikey = login.getUnikey();
			String password = login.getPassword();
			
			List<String> servers = ProjectProperties.getInstance().getServerAddresses();
			boolean authenticated = false;
			for(String server: servers){
				try{
					FTPClient client = new FTPClient();
					client.connect(server.split(":")[0], Integer.parseInt(server.split(":")[1]));
					int replyCode = client.getReplyCode();
					if (!FTPReply.isPositiveCompletion(replyCode)) {
						logger.error("Could not reach " + server + " reply code: " + replyCode);
		            }
					
		            if (client.login(unikey, password)) {
		            	authenticated = true;
						break;
		            } else {
		            	logger.error("Could not authenticate " + unikey + " against " + server);
		            }
					
				}catch (Exception e) {
					logger.error("Could not authenticate " + unikey + " against " + server);
					logger.error(e);
				}
			}
			if(!authenticated){
				errors.rejectValue("password", "Failed.loginForm.password");
			}

		}
	}

}
