package pasta.domain.form.validate;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import pasta.domain.form.BlackBoxTestCaseForm;
import pasta.domain.form.UpdateUnitTestForm;
import pasta.service.UnitTestManager;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 7 Jul 2015
 *
 */
@Component
public class UpdateUnitTestFormValidator implements Validator {

	@Autowired
	private UnitTestManager unitTestManager;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return UpdateUnitTestForm.class.equals(clazz);
	}

	@Override
	public void validate(Object formObj, Errors errors) {
		UpdateUnitTestForm form = (UpdateUnitTestForm) formObj;
		
		if(unitTestManager.getUnitTest(form.getId()) == null) {
			errors.reject("NotFound");
		}
		
		BlackBoxTestCaseFormValidator bbValidator = new BlackBoxTestCaseFormValidator();
		HashSet<String> names = new HashSet<String>();
		for(int i = 0; i < form.getTestCases().size(); i++) {
			BlackBoxTestCaseForm bbForm = form.getTestCases().get(i);
			if(!bbForm.isDeleteMe() && names.contains(bbForm.getTestName())) {
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
