package pasta.domain.upload.validate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.upload.UpdateHandMarkingForm;
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
	}

}
