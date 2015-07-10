package pasta.web.controller;

import java.beans.PropertyEditorSupport;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.release.ReleaseRule;
import pasta.domain.release.form.AssessmentReleaseForm;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.ReleaseManager;
import pasta.service.UserManager;

@Controller
@SessionAttributes({"allUsernames", "allTutorials", "allStreams", "allAssessments", "allRules"})
@RequestMapping("assessments/release/")
public class ReleaseController {
	@Autowired private ReleaseManager releaseManager;
	@Autowired private UserManager userManager;
	@Autowired private AssessmentManager assessmentManager;
	
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
	
	
	/**
	 * Get the currently logged in user. If it doesn't exist, don't create it.
	 * 
	 * @return the currently logged in user, null if not logged in or doesn't already exist.
	 */
	public PASTAUser getUser() {
		PASTAUser user = (PASTAUser) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return user;
	}
	
	
	@RequestMapping(value = "{assessmentId}/")
	public String loadRule(ModelMap model, @ModelAttribute("assessment") Assessment assessment) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		model.addAttribute("unikey", user);
		
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
		
		if(assessment.isReleased()) {
			model.addAttribute("releaseRuleForm", new AssessmentReleaseForm(assessment.getReleaseRule()));
		} else {
			model.addAttribute("releaseRuleForm", new AssessmentReleaseForm());
		}
		
		return "assessment/release/release";
	}
	
	@RequestMapping(value = "{assessmentId}/", method=RequestMethod.POST)
	public String saveRule(@ModelAttribute("releaseRuleForm") AssessmentReleaseForm form, 
			@ModelAttribute("assessment") Assessment assessment,
			ModelMap model, SessionStatus status, HttpServletRequest request) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		releaseManager.updateRelease(assessment, form);
		status.setComplete();
		model.clear();
		return "redirect:../../" + assessment.getId() + "/";
	}
	
	@RequestMapping(value = "{assessmentId}/load/")
	public String loadPartialRule(Model model, @RequestParam("pathPrefix") String pathPrefix, @ModelAttribute("newRule") AssessmentReleaseForm newForm, @ModelAttribute("releaseRuleForm") AssessmentReleaseForm releaseRuleForm) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		
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
