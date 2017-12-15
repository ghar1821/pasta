package pasta.login;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Accepts the authentication, does no checking.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-02-25
 *
 */
public class DummyAuthValidator implements Validator{

	@Override
	public boolean supports(Class<?> arg0) {
		return true;
	}

	/**
	 * Validate the form.
	 * <p>
	 * Form is always accepted.
	 */
	@Override
	public void validate(Object arg0, Errors arg1) {
		// accept everything
	}


}
