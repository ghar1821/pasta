/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.login;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.LoginForm;
import pasta.util.ProjectProperties;

/**
 * Uses ftp to authenticate a user.
 * <p>
 * It is possible to set multiple ftp servers that are used
 * for authentication. The validator will go over all of the
 * ftp servers and attempt to authenticate. It will only
 * reject the authentication if this process fails across
 * all servers.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-03-12
 *
 */
public class FTPAuthValidator implements Validator{

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
		
		// if nothing has gone wrong yet, check using the ftp servers. 
		if (!errors.hasErrors()){
			String unikey = login.getUnikey();
			String password = login.getPassword();
			
			List<String> servers = ProjectProperties.getInstance().getAuthenticationSettings().getServerAddresses();
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
