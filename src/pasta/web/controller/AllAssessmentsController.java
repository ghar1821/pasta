package pasta.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pasta.domain.UserPermissionLevel;
import pasta.domain.form.NewAssessmentForm;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.UserManager;
import pasta.web.WebUtils;

/**
 * Controller class for Assessment functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/assessments/...
 * <p>
 * Only teaching staff can access this url.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-08-15
 *
 */
@Controller
@RequestMapping("assessments/")
public class AllAssessmentsController {

	public AllAssessmentsController() {
	}

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentManager assessmentManager;


	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////


	@ModelAttribute("newAssessmentForm")
	public NewAssessmentForm loadNewAssessmentForm() {
		return new NewAssessmentForm();
	}

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// ASSESSMENTS //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/assessments/
	 * <p>
	 * View the list of all assessments.
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * <p>
	 * Attributes:
	 * <table>
	 * 	<tr><td>tutorialByStream</td><td>All tutorials by stream, used for releases from this page</td></tr>
	 * 	<tr><td>allAssessments</td><td>All assessments</td></tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul><li>assessment/viewAll/assessment</li></ul>
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/viewAll/assessment" 
	 */
	@RequestMapping(value = "")
	public String viewAllAssessment(Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		model.addAttribute("tutorialByStream", userManager.getTutorialByStream());
		model.addAttribute("allAssessments", assessmentManager.getAllAssessmentsByCategory(true));
		return "assessment/viewAll/assessment";
	}

	/**
	 * $PASTAUrl$/assessments/ - POST
	 * <p>
	 * Add a new assessment
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor: 
	 * <ol>
	 * 	<li>Check if the assessment has a name given, if it does reject the form with the reason "Assessment.new.noname".</li>
	 * 	<li>If the assessment has a name, add one using {@link pasta.service.AssessmentManager#addAssessment(Assessment)}</li>
	 * </ol>
	 * redirect to the non post version of this page.
	 *
	 * 
	 * @param form the new assessment form
	 * @param result the result used for giving feedback
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:."
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String newAssessmentAssessment(
			@Valid @ModelAttribute(value = "newAssessmentForm") NewAssessmentForm form,
			BindingResult result, RedirectAttributes attr, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		if(result.hasErrors()) {
			attr.addFlashAttribute("newAssessmentForm", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.newAssessmentForm", result);
			return "redirect:.";
		}
		
		Assessment newAssessment = assessmentManager.addAssessment(form);
		return "redirect:./" + newAssessment.getId() + "/";
	}
}
