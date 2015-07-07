package pasta.domain.upload.validate;

import java.util.HashSet;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import pasta.domain.template.BlackBoxTest;
import pasta.domain.upload.BlackBoxTestCaseForm;
import pasta.domain.upload.UpdateUnitTestForm;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 7 Jul 2015
 *
 */
public class UpdateUnitTestFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UpdateUnitTestForm.class.equals(clazz);
	}

	@Override
	public void validate(Object formObj, Errors errors) {
		UpdateUnitTestForm form = (UpdateUnitTestForm) formObj;
		
		if(BlackBoxTest.class.isAssignableFrom(form.getFormType())) {
			BlackBoxTestCaseFormValidator bbValidator = new BlackBoxTestCaseFormValidator();
			HashSet<String> names = new HashSet<String>();
			for(int i = 0; i < form.getTestCases().size(); i++) {
				BlackBoxTestCaseForm bbForm = form.getTestCases().get(i);
				if(names.contains(bbForm.getTestName())) {
					errors.rejectValue("testCases", "NotUnique", new Object[]{bbForm.getTestName()}, "");
				} else {
					names.add(bbForm.getTestName());
				}
				errors.pushNestedPath("testCases[" + i + "]");
	            ValidationUtils.invokeValidator(bbValidator, bbForm, errors);
	            errors.popNestedPath();
			}
		}
	}

}
