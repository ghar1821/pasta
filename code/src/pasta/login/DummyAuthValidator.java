package pasta.login;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DummyAuthValidator implements Validator{

	@Override
	public boolean supports(Class<?> arg0) {
		return true;
	}

	@Override
	public void validate(Object arg0, Errors arg1) {
		// acept everything
	}


}
