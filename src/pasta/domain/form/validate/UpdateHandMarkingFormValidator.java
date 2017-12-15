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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import pasta.domain.form.UpdateHandMarkingForm;
import pasta.domain.template.HandMarkData;
import pasta.domain.template.WeightedField;
import pasta.service.HandMarkingManager;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 9 Jul 2015
 *
 */
@Component
public class UpdateHandMarkingFormValidator implements Validator {

	@Autowired
	private HandMarkingManager handMarkManager;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return UpdateHandMarkingForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UpdateHandMarkingForm form = (UpdateHandMarkingForm) target;
		
		if(handMarkManager.getHandMarking(form.getId()) == null) {
			errors.reject("NotFound");
		}
		
		if(form.getId() < 0) {
			errors.rejectValue("id", "Min");
		}
		
		ValidationUtils.rejectIfEmpty(errors, "name", "NotEmpty");
		if(form.getName().length() > 256) {
			errors.rejectValue("name", "Length", new Object[] {0, 256}, "");
		}
		
		
		List<WeightedField> rows = form.getNewRowHeader();
		for(int i = 0; i < rows.size(); i++) {
			WeightedField row = rows.get(i);
			if(row == null || row.getName() == null) {
				continue;
			}
			errors.pushNestedPath("newRowHeader[" + i + "]");
			ValidationUtils.rejectIfEmpty(errors, "name", "NotEmpty");
			if(row.getName().length() > 512) {
				errors.rejectValue("name", "Length", new Object[] {0, 512}, "");
			}
			errors.popNestedPath();
		}
		
		List<WeightedField> cols = form.getNewColumnHeader();
		for(int i = 0; i < cols.size(); i++) {
			WeightedField col = cols.get(i);
			if(col == null || col.getName() == null) {
				continue;
			}
			errors.pushNestedPath("newColumnHeader[" + i + "]");
			ValidationUtils.rejectIfEmpty(errors, "name", "NotEmpty");
			if(col.getName().length() > 512) {
				errors.rejectValue("name", "Length", new Object[] {0, 512}, "");
			}
			errors.popNestedPath();
		}

		List<HandMarkData> newData = form.getNewData();
		for(int i = 0; i < newData.size(); i++) {
			HandMarkData datum = newData.get(i);
			if(datum == null || datum.getData() == null) {
				continue;
			}
			errors.pushNestedPath("newData[" + i + "]");
			if(datum.getData().length() > 4096) {
				errors.rejectValue("data", "Length", new Object[] {0, 4096}, "");
			}
			errors.popNestedPath();
		}
	}

}
