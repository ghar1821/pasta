package pasta.domain.form.validate;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import pasta.domain.form.BlackBoxTestCaseForm;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 7 Jul 2015
 *
 */
public class BlackBoxTestCaseFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return BlackBoxTestCaseForm.class.equals(clazz);
	}

	@Override
	public void validate(Object formObj, Errors errors) {
		BlackBoxTestCaseForm form = (BlackBoxTestCaseForm) formObj;
		
		if(form.isDeleteMe()) {
			return;
		}
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "testName", "NotEmpty");
		
		if(form.getTestName() != null && !form.getTestName().isEmpty()) {
			if(!form.getTestName().toLowerCase().matches("[a-z][a-z0-9_]*[a-z0-9]*")) {
				errors.rejectValue("testName", "NotValid");
			}
		}
		
		if(form.getTimeout() < 0) {
			errors.rejectValue("timeout", "Min");
		}
	}

}
