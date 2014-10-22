/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.login;

import java.security.GeneralSecurityException;

import javax.net.SocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.LoginForm;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.controls.PasswordExpiredControl;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

/**
 * Uses LDAP to authenticate a user.
 * <p>
 * Currently hard coded to use "shared-dc-prd-2.shared.sydney.edu.au" 
 * on port 636.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-02-19
 *
 */
public class LDAPAuthValidator implements Validator {

	// what to use to write the log to.
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The hostname of IP address where the server listens for client
	 * connections.
	 */
	public static final String HOSTNAME = "shared-dc-prd-2.shared.sydney.edu.au";

	/**
	 * The port on which the server listens for client connections.
	 */
	public static final int PORT = 636;

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
		if (!errors.hasErrors()) {
			System.setProperty("https.protocols", "TLSv1");
			
			String unikey = login.getUnikey();
			String password = login.getPassword();

			// Use no key manager, and trust all certificates. This would not be
			// used
			// in non-trivial code.
			SSLUtil sslUtil = new SSLUtil(null, new TrustAllTrustManager());

			SocketFactory socketFactory;
			LDAPConnection ldapConnection = null;
			try {

				// Create the socket factory that will be used to make a secure
				// connection to the server.
				socketFactory = sslUtil.createSSLSocketFactory();
				ldapConnection = new LDAPConnection(socketFactory, HOSTNAME,
						PORT);

			} catch (LDAPException ldapException) {
				logger.error(ldapException);
				errors.rejectValue("password", "Failed.loginForm.connection");

			} catch (GeneralSecurityException exception) {
				logger.error(exception);
				errors.rejectValue("password", "Failed.loginForm.connection");
			}
			

			if(!errors.hasErrors()){
				try {
	
					String dn = "CN=" + unikey
							+ ",OU=People,DC=shared,DC=sydney,DC=edu,DC=au";
					long maxResponseTimeMillis = 1000;
	
					BindRequest bindRequest = new SimpleBindRequest(dn, password);
					bindRequest.setResponseTimeoutMillis(maxResponseTimeMillis);
					BindResult bindResult = ldapConnection.bind(bindRequest);
	
					// Check for the password expiring or password expired
					// response controls
					PasswordExpiredControl pwdExpired = PasswordExpiredControl
							.get(bindResult);
					// expired password
					if (pwdExpired != null) {
						logger.error(unikey+" has an expired password");
						errors.rejectValue("password", "Failed.loginForm.password");
					}
	
					ldapConnection.close();
					if(bindResult.getResultCode() != ResultCode.SUCCESS){
						errors.rejectValue("password", "Failed.loginForm.password");
					}
	
				} catch (LDAPException ldapException) {
					ldapConnection.close();
					errors.rejectValue("password", "Failed.loginForm.password");
				}
			}
		}
	}
}
