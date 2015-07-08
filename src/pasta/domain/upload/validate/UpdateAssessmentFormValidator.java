package pasta.domain.upload.validate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.template.Assessment;
import pasta.domain.template.BlackBoxTest;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.UpdateAssessmentForm;
import pasta.service.AssessmentManager;
import pasta.service.UnitTestManager;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 8 Jul 2015
 *
 */
@Component
public class UpdateAssessmentFormValidator implements Validator {

	@Autowired
	private AssessmentManager assessmentManager;
	@Autowired
	private UnitTestManager unitTestManager;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return UpdateAssessmentForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UpdateAssessmentForm form = (UpdateAssessmentForm) target;
		
		// Ensure assessment is found
		Assessment base = assessmentManager.getAssessment(form.getId());
		if(base == null) {
			errors.reject("NotFound");
		}
		
		// Ensure solution name is present only if it needs to be
		boolean hasBlackBox = false;
		for(WeightedUnitTest wTest : form.getAllUnitTests()) {
			hasBlackBox = hasBlackBox || (unitTestManager.getUnitTest(wTest.getTest().getId()) instanceof BlackBoxTest);
		}
		boolean emptySolutionName = (form.getSolutionName() == null || form.getSolutionName().isEmpty());
		if(hasBlackBox) {
			if(emptySolutionName) {
				errors.rejectValue("solutionName", "BlackBox.NotEmpty");
			}
		} else {
			if(!emptySolutionName) {
				errors.rejectValue("solutionName", "BlackBox.Empty");
			}
			if(form.getLanguages() != null && !form.getLanguages().isEmpty()) {
				errors.rejectValue("languages", "BlackBox.Empty");
			}
		}
		
		// TODO: implement penalties (e.g. hand marking, or late) to allow proper 
		// negative weights, then uncomment this section to disallow negative weights
		// (currently you can hack penalties by making negative weight components 
		// balanced by empty positive weight components)
		
//		for(int i = 0; i < form.getNewUnitTests().size(); i++) {
//			if(form.getNewUnitTests().get(i).getWeight() < 0) {
//				errors.pushNestedPath("newUnitTests[" + i + "]");
//				errors.rejectValue("weight", "NotNegative");
//				errors.popNestedPath();
//			}
//		}
//		for(int i = 0; i < form.getNewSecretUnitTests().size(); i++) {
//			if(form.getNewSecretUnitTests().get(i).getWeight() < 0) {
//				errors.pushNestedPath("newSecretUnitTests[" + i + "]");
//				errors.rejectValue("weight", "NotNegative");
//				errors.popNestedPath();
//			}
//		}
//		for(int i = 0; i < form.getNewHandMarking().size(); i++) {
//			if(form.getNewHandMarking().get(i).getWeight() < 0) {
//				errors.pushNestedPath("newHandMarking[" + i + "]");
//				errors.rejectValue("weight", "NotNegative");
//				errors.popNestedPath();
//			}
//		}
//		for(int i = 0; i < form.getNewCompetitions().size(); i++) {
//			if(form.getNewCompetitions().get(i).getWeight() < 0) {
//				errors.pushNestedPath("newCompetitions[" + i + "]");
//				errors.rejectValue("weight", "NotNegative");
//				errors.popNestedPath();
//			}
//		}
	}
}
