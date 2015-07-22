package pasta.domain.form.validate;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.TestUnitTestForm;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 8 Jul 2015
 *
 */
@Component
public class TestUnitTestFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return TestUnitTestForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		TestUnitTestForm form = (TestUnitTestForm) target;
		
		if(form.getFile() == null || form.getFile().isEmpty()) {
			errors.rejectValue("file", "NotEmpty");
		}
	}

}
