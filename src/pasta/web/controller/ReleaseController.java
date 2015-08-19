package pasta.web.controller;

import java.beans.PropertyEditorSupport;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pasta.domain.UserPermissionLevel;
import pasta.domain.form.AssessmentReleaseForm;
import pasta.domain.form.validate.AssessmentReleaseFormValidator;
import pasta.domain.release.ReleaseRule;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.ReleaseManager;
import pasta.service.UserManager;
import pasta.web.WebUtils;

@Controller
@SessionAttributes({"allUsernames", "allTutorials", "allStreams", "allAssessments", "allRules"})
@RequestMapping("assessments/release/")
public class ReleaseController {
	@Autowired private ReleaseManager releaseManager;
	@Autowired private UserManager userManager;
	@Autowired private AssessmentManager assessmentManager;
	@Autowired private AssessmentReleaseFormValidator updateValidator;
	
	@ModelAttribute("newRule")
	public AssessmentReleaseForm getNewRule(@RequestParam(value="ruleName",required=false) String ruleName) {
		if(ruleName == null || ruleName.isEmpty()) {
			return null;
		} else {
			return new AssessmentReleaseForm(ruleName);
		}
	}
	
	@ModelAttribute("assessment")
	public Assessment loadAssessment(@PathVariable("assessmentId") long assessmentId) {
		return assessmentManager.getAssessment(assessmentId);
	}
	
	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}
	
	
	@RequestMapping(value = "{assessmentId}/")
	public String loadRule(ModelMap model, @ModelAttribute("assessment") Assessment assessment) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		TreeSet<String> allUsernames = new TreeSet<>(userManager.getStudentUsernameList());
		TreeSet<String> allTutorials = new TreeSet<>(userManager.getTutorialList());
		TreeSet<String> allStreams = new TreeSet<>(userManager.getStreamList());
		TreeSet<Assessment> allAssessments = new TreeSet<>(assessmentManager.getAssessmentList());
		model.addAttribute("allUsernames", allUsernames);
		model.addAttribute("allTutorials", allTutorials);
		model.addAttribute("allStreams", allStreams);
		model.addAttribute("allAssessments", allAssessments);

		Set<ReleaseRule> allRules = ReleaseManager.getOneOfEach();
		model.addAttribute("allRules", allRules);
		
		if(!model.containsAttribute("releaseRuleForm")) {
			if(assessment.isReleased()) {
				model.addAttribute("releaseRuleForm", new AssessmentReleaseForm(assessment.getReleaseRule()));
			} else {
				model.addAttribute("releaseRuleForm", new AssessmentReleaseForm());
			}
		}
		
		return "assessment/release/release";
	}
	
	@RequestMapping(value = "{assessmentId}/", method=RequestMethod.POST)
	public String saveRule(@Valid @ModelAttribute("releaseRuleForm") AssessmentReleaseForm form, BindingResult result,
			@ModelAttribute("assessment") Assessment assessment,
			ModelMap model, SessionStatus status, RedirectAttributes attr) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);

		updateValidator.validate(form, result);
		if(result.hasErrors()) { 
			attr.addFlashAttribute("releaseRuleForm", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.releaseRuleForm", result);
			return "redirect:.";
		}
		
		releaseManager.updateRelease(assessment, form);
		status.setComplete();
		model.clear();
		return "redirect:../../" + assessment.getId() + "/";
	}
	
	@RequestMapping(value = "{assessmentId}/convertToJoin/", method=RequestMethod.POST)
	public String changeToJoinRule(@ModelAttribute("releaseRuleForm") AssessmentReleaseForm form,
			@RequestParam(value="newRuleType") String newRuleType,
			@RequestParam(value="basePath") String basePath,
			@RequestParam(value="changeConjunction", required=false) Boolean changeConjunction,
			RedirectAttributes attr) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		// The newly added form (selected as the "change to" rule)
		AssessmentReleaseForm newForm = new AssessmentReleaseForm("pasta.domain.release.Release" + newRuleType + "Rule");
		
		// The rule being changed
		AssessmentReleaseForm base;
		
		boolean isRoot = basePath.equals("releaseRuleForm");
		if(isRoot) {
			base = form;
		} else {
			// The parent of the rule being changed
			AssessmentReleaseForm parent;
			
			String parentPath = basePath.substring(0, basePath.lastIndexOf('.'));
			basePath = basePath.substring("releaseRuleForm.".length());
			
			BeanWrapper wrapper = new BeanWrapperImpl(form);
			
			base = (AssessmentReleaseForm) wrapper.getPropertyValue(basePath);
			if(parentPath.equals("releaseRuleForm")) {
				parent = form;
			} else {
				parent = (AssessmentReleaseForm) wrapper.getPropertyValue(parentPath);
			}
			
			Iterator<AssessmentReleaseForm> subRuleIt = parent.getRules().iterator();
			while(subRuleIt.hasNext()) {
				if(subRuleIt.next() == base) {
					subRuleIt.remove();
					break;
				}
			}
			parent.getRules().add(newForm);
		}
		
		if(changeConjunction != null && changeConjunction) {
			Iterator<AssessmentReleaseForm> subRuleIt = base.getRules().iterator();
			while(subRuleIt.hasNext()) {
				newForm.getRules().add(subRuleIt.next());
				subRuleIt.remove();
			}
		} else {
			newForm.getRules().add(base);
		}
		
		attr.addFlashAttribute("releaseRuleForm", isRoot ? newForm : form);
		return "redirect:../";
	}
	
	@RequestMapping(value = "{assessmentId}/load/")
	public String loadPartialRule(Model model, @RequestParam("pathPrefix") String pathPrefix, @ModelAttribute("newRule") AssessmentReleaseForm newForm, @ModelAttribute("releaseRuleForm") AssessmentReleaseForm releaseRuleForm) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		model.addAttribute("pathPrefix", pathPrefix);
		if(pathPrefix.equals("releaseRuleForm")) {
			model.addAttribute("releaseRuleForm", newForm);
		} else {
			model.addAttribute("releaseRuleForm", releaseRuleForm);
		}
		return "assessment/release/partialRelease";
	}
	
	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
	    // when passing an assessment as an id, convert that id into an actual Assessment object
		binder.registerCustomEditor(Assessment.class, new PropertyEditorSupport() {
		    @Override
		    public void setAsText(String text) {
		    	try {
		    		long id = Long.parseLong(text);
		    		Assessment assessment = assessmentManager.getAssessment(id);
		    		setValue(assessment);
		    	} catch(NumberFormatException e) {
		    		setValue(null);
		    	}
		    }
	    });
	}
}
