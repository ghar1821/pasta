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
		
		if(!form.isToBeCompared() && (form.getOutput() != null && !form.getOutput().isEmpty())) {
			errors.rejectValue("output", "NotComparedEmpty");
		}
	}

}
