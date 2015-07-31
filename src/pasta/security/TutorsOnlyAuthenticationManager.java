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
