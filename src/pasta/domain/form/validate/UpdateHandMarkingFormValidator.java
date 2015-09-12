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
