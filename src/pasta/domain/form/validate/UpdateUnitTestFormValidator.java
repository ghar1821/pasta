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
