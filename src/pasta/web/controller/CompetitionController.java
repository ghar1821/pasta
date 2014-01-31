package pasta.web.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Competition;
import pasta.domain.upload.NewCompetition;
import pasta.service.CompetitionManager;
import pasta.service.SubmissionManager;
import pasta.service.UserManager;
import pasta.util.PASTAUtil;

@Controller
@RequestMapping("competition/")
/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
public class CompetitionController {

	protected final Log logger = LogFactory.getLog(getClass());

	private UserManager userManager;
	private CompetitionManager competitionManager;

	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
	}
	
	@Autowired
	public void setMyService(CompetitionManager myService) {
		this.competitionManager = myService;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newCompetitionModel")
	public NewCompetition returnNewCompetitionModel() {
		return new NewCompetition();
	}
	@ModelAttribute("newArenaModel")
	public Arena returnArenaModel() {
		return new Arena();
	}

	@ModelAttribute("competition")
	public Competition returnCompetitionModel() {
		return new Competition();
	}

	@ModelAttribute("assessmentResult")
	public AssessmentResult returnAssessmentResultModel() {
		return new AssessmentResult();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////

	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		if (username != null) {
			return userManager.getUser(username);
		}
		return null;
	}


	// ///////////////////////////////////////////////////////////////////////////
	// COMPETITIONS //
	// ///////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "")
	public String viewAllCompetitions(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("allCompetitions", competitionManager.getCompetitionList());

		return "assessment/viewAll/competition";
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	public String newCompetition(Model model,
			@ModelAttribute(value = "newCompetitionModel") NewCompetition form) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if(user.isInstructor()){
			competitionManager.addCompetition(form);
		}

		return "redirect:.";
	}

//	@RequestMapping(value = "{competitionName}/", method = RequestMethod.POST)
//	public String updateCompetition(Model model,
//			@PathVariable("competitionName") String competitionName,
//			@ModelAttribute(value = "newCompetitionModel") NewCompetition form) {
//		PASTAUser user = getUser();
//		if (user == null) {
//			return "redirect:/login/";
//		}
//		if (!user.isTutor()) {
//			return "redirect:/home/.";
//		}
//		if(user.isInstructor()){
//			form.setTestName(competitionName);
//			manager.updateCompetition(form);
//		}
//
//		return "redirect:.";
//	}

	// delete a unit test
	@RequestMapping(value = "delete/{competitionName}/")
	public String deleteCompetition(
			@PathVariable("competitionName") String competitionName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {
			competitionManager.removeCompetition(competitionName);
		}
		return "redirect:../../";
	}

	@RequestMapping(value = "{competitionName}")
	public String viewCompetition(
			@PathVariable("competitionName") String competitionName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("competition",
				competitionManager.getCompetition(competitionName));
		model.addAttribute("node", PASTAUtil.generateFileTree(competitionManager
				.getCompetition(competitionName).getFileLocation() + "/code"));

		return "assessment/view/competition";
	}

	@RequestMapping(value = "{competitionName}/", method = RequestMethod.POST)
	public String updateCompetition(@ModelAttribute(value = "newCompetitionModel") NewCompetition form,
			@ModelAttribute(value = "competition") Competition compForm,
			@PathVariable("competitionName") String competitionName, Model model){
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		if(user.isInstructor()){
			if(form.getFile() != null && !form.getFile().isEmpty()){
				// update contents
				form.setTestName(competitionName);
				competitionManager.updateCompetition(form);
			}
			else{
				// update competition
				compForm.setName(competitionName);
				compForm.setArenas(competitionManager.getCompetition(competitionName).getArenas());
				competitionManager.addCompetition(compForm);
			}
		}
		
		return "redirect:.";
	}

	@RequestMapping(value = "view/{competitionName}/")
	public String viewCompetitionPage(Model model,
			@PathVariable("competitionName") String competitionName) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		Competition currComp = competitionManager.getCompetition(competitionName);
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}


		model.addAttribute("unikey", user);
		model.addAttribute("competition", currComp);

		if (currComp.isCalculated()) {
			model.addAttribute("arenaResult",
					competitionManager.getCalculatedCompetitionResult(competitionName));
			model.addAttribute("marks",
					competitionManager.getCompetitionResult(competitionName));
			return "assessment/competition/calculated";
		} else {
			model.addAttribute("arenas", currComp.getArenas());
			return "assessment/competition/arena";
		}
	}
	
	@RequestMapping(value = "view/{competitionName}/", method = RequestMethod.POST)
	public String viewCompetitionPage(Model model,
			@ModelAttribute(value = "newArenaModel") Arena arena,
			@PathVariable("competitionName") String competitionName) {
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}		
		
		if(!currComp.isCalculated()){
			if(currComp != null && (user.isTutor() || currComp.isStudentCreatableArena())){
				if(!arena.isRepeatable() || user.isInstructor() || 
						(currComp.isTutorCreatableRepeatableArena() && user.isTutor() && currComp.isTutorCreatableRepeatableArena())
						|| (currComp.isTutorCreatableRepeatableArena() && currComp.isStudentCreatableRepeatableArena())){
					// accept arena
					competitionManager.addArena(arena, currComp);
				}
			}
		}
		
		return "redirect:/mirror/";
	}
}