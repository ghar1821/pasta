package pasta.domain.form.validate;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import pasta.domain.form.BlackBoxTestCaseForm;
import pasta.domain.form.UpdateUnitTestForm;
import pasta.domain.template.UnitTest;
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
		
		UnitTest existing = unitTestManager.getUnitTest(form.getId());
		if(existing == null) {
			errors.reject("NotFound");
		}
		
		if(form.getTestCases().size() > 0) {
			if(form.getBlackBoxTimeout() == null) {
				errors.rejectValue("blackBoxTimeout", "NotNull");
			} else if(form.getBlackBoxTimeout() < 0) {
				errors.rejectValue("blackBoxTimeout", "Min");
			}
		}
		if(!form.getFile().isEmpty() || existing.hasCode()) {
			if(form.getAdvancedTimeout() == null) {
				errors.rejectValue("advancedTimeout", "NotNull");
			} else if(form.getAdvancedTimeout() < 0) {
				errors.rejectValue("advancedTimeout", "Min");
			}
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
