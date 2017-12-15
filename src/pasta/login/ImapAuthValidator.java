package pasta.login;

import java.util.List;
import java.util.Properties;

import javax.mail.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.LoginForm;
import pasta.util.ProjectProperties;

/**
 * Uses an IMAP mail server to authenticate a user.
 * <p>
 * It is possible to assign multiple mail servers to authenticate against.
 * The validator will try to authenticate against all of them and will
 * only fail if it cannot authenticate against any of them.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-02-25
 *
 */
public class ImapAuthValidator implements Validator{

	// what to use to write the log to.
	protected final Log logger = LogFactory.getLog(getClass());

	// not really used as far as i can see.
	@Override
	public boolean supports(Class<?> clazz) {
		return LoginForm.class.isAssignableFrom(clazz);
	}

	/**
	 * Validate the form.
	 * <p>
	 * Check to see if the username and password are not empty.
	 * If the fields are empty, reject the appropriate field with the appropriate error.
	 * Then check if the username and password authenticate sucessfully
	 * against the system.
	 * 
	 * If authentication is sucessfull, do nothing. If the authentication is 
	 * unsuccessful, reject the password with the appropriate error.
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
			
			List<String> mailServers = ProjectProperties.getInstance().getAuthenticationSettings().getServerAddresses();
			boolean authenticated = false;
			for(String mailServer: mailServers){
				try{
					Properties props = new Properties();
					props.setProperty("mail.imap.socketFactory.fallback", "false");
					props.setProperty("mail.imap.port", mailServer.split(":")[1]);
					props.setProperty("mail.imap.socketFactory.port", mailServer.split(":")[1]);
					props.put("mail.imap.auth.plain.disable","true"); 
		 
					Session session = null;			
					session = Session.getInstance(props);
					session.setDebug(false);			
					javax.mail.Store store = session.getStore(new 
				               javax.mail.URLName("imap://"+mailServer.split(":")[0]));
					store.connect(mailServer.split(":")[0],Integer.parseInt(mailServer.split(":")[1]),unikey,password);
					authenticated = true;
					break;
				}catch (Exception e) {
					logger.error("Could not authenticate " + unikey + " against " + mailServer);
					logger.error(e);
				}
			}
			if(!authenticated){
				errors.rejectValue("password", "Failed.loginForm.password");
			}

		}
	}

}
