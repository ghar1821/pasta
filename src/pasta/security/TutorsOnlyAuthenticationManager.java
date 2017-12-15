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

package pasta.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import pasta.domain.form.LoginForm;
import pasta.domain.user.PASTAUser;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

public class TutorsOnlyAuthenticationManager implements AuthenticationManager {

	@Autowired private UserManager userManager;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		LoginForm form = new LoginForm();
		form.setUnikey(authentication.getName());
		form.setPassword(authentication.getCredentials().toString());
		
		BindingResult authResult = new BeanPropertyBindingResult(form, "form");
		
		ProjectProperties.getInstance().getAuthenticationValidator().validate(form, authResult);
		
		if(authResult.hasErrors()) {
			authentication.setAuthenticated(false);
			throw new BadCredentialsException("Invalid username or password");
		}
		
		PASTAUser user = userManager.getUser(authentication.getName());
		if(user == null) {
			throw new BadCredentialsException("Invalid username or password");
		}
		if(!user.isTutor()) {
			throw new BadCredentialsException("Not authorised");
		}
		
		return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), Arrays.asList(new TutorAuthority()));
	}
	
	static class TutorAuthority implements GrantedAuthority {
		private static final long serialVersionUID = -4057250451818585656L;
		@Override
		public String getAuthority() {
			return "ROLE_TUTOR";
		}
	}
}
