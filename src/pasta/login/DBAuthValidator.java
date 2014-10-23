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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.LoginForm;
import pasta.repository.LoginDAO;

/**
 * Uses the database to authenticate a user.
 * <p>
 * The password is hashed using md5Hex
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-02-25
 *
 */
public class DBAuthValidator implements Validator{
	
	LoginDAO database;
	
	public void setDAO(LoginDAO dao){
		database = dao;
	}
	
	// what to use to write the log to.
	protected final Log logger = LogFactory.getLog(getClass());

	// not really used as far as i can see.
	public boolean supports(Class<?> clazz) {
		return LoginForm.class.isAssignableFrom(clazz);
	}
	
	/**
	 * Authenticate the username with the password 
	 * <p>
	 * If the user does not have a password registered on the system
	 * the defualt password is their username.
	 * 
	 * @param username the username in plaintext
	 * @param password the password in plaintext (it gets hashed)
	 * @return whether the user has authenticated against the system.
	 */
	public boolean authenticate(String username, String password){
		if(database.hasPassword(username)){
			String hashedPassword = DigestUtils.md5Hex(password);
			return database.authenticate(username, hashedPassword);
		}
		else{
			return password.equals(username);
		}
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
		
		// if nothing has gone wrong yet, check using the database servers. 
		if (!errors.hasErrors()){
			String username = login.getUnikey();
			String password = login.getPassword();
			
			if(!authenticate(username, password)){
				errors.rejectValue("password", "Failed.loginForm.password");
			}
		}
	}


}
