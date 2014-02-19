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
		if (!errors.hasErrors()) {
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

				System.err.println(ldapException);
				System.exit(ldapException.getResultCode().intValue());

			} catch (GeneralSecurityException exception) {

				System.err.println(exception);
				System.exit(1);

			}

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
