package pasta.domain.upload.validate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.PASTATime;
import pasta.domain.template.Competition;
import pasta.domain.upload.UpdateCompetitionForm;
import pasta.service.CompetitionManager;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 9 Jul 2015
 *
 */
@Component
public class UpdateCompetitionFormValidator implements Validator {

	@Autowired
	private CompetitionManager compManager;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return UpdateCompetitionForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UpdateCompetitionForm form = (UpdateCompetitionForm) target;
		
		Competition comp = compManager.getCompetition(form.getId());
		if(comp == null) {
			errors.reject("NotFound");
		} else {
			if(!comp.isCalculated()) {
				if(form.getStudentPermissions() == null) {
					errors.rejectValue("studentPermissions", "NotNull");
				}
				if(form.getTutorPermissions() == null) {
					errors.rejectValue("tutorPermissions", "NotNull");
				}
			}
		}
		
		PASTATime freq = form.getFrequency();
		if(freq.getMiliseconds() < 0 || freq.getSeconds() < 0 ||
				freq.getMinutes() < 0 || freq.getHours() < 0 || 
				freq.getDays() < 0 || freq.getYears() < 0) {
			errors.rejectValue("frequency", "NotNegative");
		} 
		else if(form.getFirstStartDate() != null && form.getFrequency().getTime() > 0 && form.getFrequency().tooOften()) {
			errors.rejectValue("frequency", "TooFrequent");
		}
	}

}
