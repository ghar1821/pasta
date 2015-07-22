package pasta.domain.form.validate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.UpdateAssessmentForm;
import pasta.domain.template.Assessment;
import pasta.domain.template.BlackBoxTest;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.user.PASTAGroup;
import pasta.service.AssessmentManager;
import pasta.service.GroupManager;
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
	@Autowired
	private GroupManager groupManager;
	
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
		for(WeightedUnitTest wTest : form.getSelectedUnitTests()) {
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
		
		// Group count not less than -1
		if(form.getGroupCount() < -1) {
			errors.rejectValue("groupCount", "Min");
		}
		// Group count not falling below currently used group count
		if(form.getGroupCount() != -1 && form.getGroupCount() < groupManager.getUsedGroupCount(base)) {
			errors.rejectValue("groupCount", "CannotDeleteUsedGroups", new Object[] {form.getGroupCount()}, "");
		}
		
		// Group size in range {-1, [2,Inf)}
		if(form.getGroupSize() < 2 && form.getGroupSize() != -1) {
			errors.rejectValue("groupSize", "Min");
		}
		// Group size not falling below current group sizes
		if(form.getGroupSize() != -1) {
			for(PASTAGroup group : groupManager.getGroups(base)) {
				if(group.getSize() > form.getGroupSize()) {
					errors.rejectValue("groupSize", "CannotShrinkUsedGroups", new Object[] {form.getGroupSize()}, "");
					break;
				}
			}
		}
		
		// TODO: implement penalties (e.g. hand marking, or late) to allow proper 
		// negative weights, then uncomment this section to disallow negative weights
		// (currently you can hack penalties by making negative weight components 
		// balanced by empty positive weight components)
		
		boolean isGroupWork = form.getGroupCount() != 0;
		
		for(int i = 0; i < form.getSelectedUnitTests().size(); i++) {
			WeightedUnitTest module = form.getSelectedUnitTests().get(i);
			if(!isGroupWork && module.isGroupWork()) {
				errors.rejectValue("selectedUnitTests", "NotGroupWork");
			}
//			if(module.getWeight() < 0) {
//				errors.rejectValue("selectedUnitTests", "NotNegative");
//			}
		}
		for(int i = 0; i < form.getSelectedHandMarking().size(); i++) {
			WeightedHandMarking module = form.getSelectedHandMarking().get(i);
			if(!isGroupWork && module.isGroupWork()) {
				errors.rejectValue("selectedHandMarking", "NotGroupWork");
			}
//			if(module.getWeight() < 0) {
//				errors.rejectValue("selectedHandMarking", "NotNegative");
//			}
		}
		for(int i = 0; i < form.getSelectedCompetitions().size(); i++) {
			WeightedCompetition module = form.getSelectedCompetitions().get(i);
			if(!isGroupWork && module.isGroupWork()) {
				errors.rejectValue("selectedCompetitions", "NotGroupWork");
			}
//			if(module.getWeight() < 0) {
//				errors.rejectValue("selectedCompetitions", "NotNegative");
//			}
		}
	}
}
