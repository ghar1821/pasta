package pasta.login;

import java.util.Properties;

import javax.mail.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;

import pasta.domain.LoginForm;

public class EmailAuthValidator {

	// what to use to write the log to.
	protected final Log logger = LogFactory.getLog(getClass());

	// not really used as far as i can see.
	public boolean supports(Class<?> clazz) {
		return LoginForm.class.isAssignableFrom(clazz);
	}

	/**
	 * Method used for validation
	 */
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
			// Initialise mail session and check username and password.
			try{
				Properties props = new Properties();
				props.setProperty("mail.imap.socketFactory.fallback", "false");
				props.setProperty("mail.imap.port", "143");
				props.setProperty("mail.imap.socketFactory.port", "143");
				props.put("mail.imap.auth.plain.disable","true"); 
	 
				Session session = null;			
				session = Session.getInstance(props);
				session.setDebug(false);			
				javax.mail.Store store = session.getStore(new 
			               javax.mail.URLName("imap://imap.it.usyd.edu.au"));
				store.connect("imap.it.usyd.edu.au",143,unikey,password);
			}catch (Exception e) {
				System.out.println("Didn't auth against normal server, trying ug");
				try{
					Properties props = new Properties();
					props.setProperty("mail.imap.socketFactory.fallback", "false");
					props.setProperty("mail.imap.port", "143");
					props.setProperty("mail.imap.socketFactory.port", "143");
					props.put("mail.imap.auth.plain.disable","true"); 
		 
					Session session = null;			
					session = Session.getInstance(props);
					session.setDebug(false);			
					javax.mail.Store store = session.getStore(new 
				               javax.mail.URLName("imap://imap.ug.cs.usyd.edu.au"));
					store.connect("imap.ug.cs.usyd.edu.au",143,unikey,password);
					
				}
				catch (Exception ex){
					errors.rejectValue("password", "Failed.loginForm.password");
				}
			}
		}
	}

}
