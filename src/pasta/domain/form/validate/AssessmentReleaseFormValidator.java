package pasta.domain.form.validate;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import pasta.domain.form.AssessmentReleaseForm;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 22 Jul 2015
 *
 */
@Component
public class AssessmentReleaseFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return AssessmentReleaseForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		AssessmentReleaseForm form = (AssessmentReleaseForm) target;
		
		// Setting to null
		if(form.getRuleName() == null) {
			return;
		}
		
		String simpleName = form.getRuleName().substring(form.getRuleName().lastIndexOf('.') + 1);
		
		switch(simpleName) {
		case "ClassRule":
			if(form.getClasses() == null) {
				errors.rejectValue("classes", "NotNull");
			}
			break;
		case "StreamRule":
			if(form.getStreams() == null) {
				errors.rejectValue("streams", "NotNull");
			}
			break;
		case "UsernameRule":
			if(form.getUsernames() == null) {
				errors.rejectValue("usernames", "NotNull");
			}
			break;
		case "DateRule":
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "strDate", "NotEmpty");
			break;
		case "MarkCompareRule":
			if(form.getMarkType() == null) {
				errors.rejectValue("markType", "NotNull");
			}
			if(form.isAsPercentage()) {
				if(form.getCompareMark() < 0 || form.getCompareMark() > 100) {
					errors.rejectValue("compareMark", "NotPercentage");
				}
			}
		case "SubmissionCountRule":
			if(form.getCompareMode() == null) {
				errors.rejectValue("compareMode", "NotNull");
			}
		case "HasSubmittedRule":
			if(form.getCompareAssessment() == null) {
				errors.rejectValue("compareAssessment", "NotNull");
			}
			break;
		case "ReleaseAndRule":
		case "ReleaseOrRule":
			if(form.getRules() == null || form.getRules().isEmpty()) {
				errors.rejectValue("rules", "NotEmpty");
			} else {
				for(int i = 0; i < form.getRules().size(); i++) {
					errors.pushNestedPath("rules[" + i + "]");
					ValidationUtils.invokeValidator(this, form.getRules().get(i), errors);
					errors.popNestedPath();
				}
			}
			break;
		default:
			errors.reject("InvalidRuleType");
		}
	}
}
