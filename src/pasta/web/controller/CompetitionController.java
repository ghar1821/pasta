package pasta.web.controller;


import java.util.HashMap;

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
import pasta.domain.template.Competition;
import pasta.domain.upload.NewCompetition;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.service.SubmissionManager;

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

	public CompetitionController() {
		codeStyle = new HashMap<String, String>();
		codeStyle.put("c", "ccode");
		codeStyle.put("cpp", "cppcode");
		codeStyle.put("h", "cppcode");
		codeStyle.put("cs", "csharpcode");
		codeStyle.put("css", "csscode");
		codeStyle.put("html", "htmlcode");
		codeStyle.put("java", "javacode");
		codeStyle.put("js", "javascriptcode");
		codeStyle.put("pl", "perlcode");
		codeStyle.put("pm", "perlcode");
		codeStyle.put("php", "phpcode");
		codeStyle.put("py", "pythoncode");
		codeStyle.put("rb", "rubycode");
		codeStyle.put("sql", "sqlcode");
		codeStyle.put("xml", "xmlcode");

	}

	protected final Log logger = LogFactory.getLog(getClass());

	private SubmissionManager manager;
	private HashMap<String, String> codeStyle;

	@Autowired
	public void setMyService(SubmissionManager myService) {
		this.manager = myService;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newUnitTestModel")
	public NewUnitTest returnNewUnitTestModel() {
		return new NewUnitTest();
	}

	@ModelAttribute("newCompetitionModel")
	public NewCompetition returnNewCompetitionModel() {
		return new NewCompetition();
	}

	@ModelAttribute("submission")
	public Submission returnSubmissionModel() {
		return new Submission();
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

	/**
	 * Get the currently logged in user.
	 * 
	 * @return
	 */
	public PASTAUser getOrCreateUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		if (username != null) {
			return manager.getOrCreateUser(username);
		}
		return null;
	}

	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		if (username != null) {
			return manager.getUser(username);
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
		model.addAttribute("allCompetitions", manager.getCompetitionList());

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
			manager.addCompetition(form);
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
			manager.removeCompetition(competitionName);
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
				manager.getCompetition(competitionName));
		model.addAttribute("node", manager.generateFileTree(manager
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
				manager.updateCompetition(form);
			}
			else{
				// update competition
				compForm.setName(competitionName);
				compForm.setArenas(manager.getCompetition(competitionName).getArenas());
				manager.addCompetition(compForm);
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
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		Competition currComp = manager.getCompetition(competitionName);
		if (currComp == null) {
			return "redirect:../../../home";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("competition", currComp);

		if (currComp.isCalculated()) {
			model.addAttribute("arenaResult",
					manager.getCalculatedCompetitionResult(competitionName));
			model.addAttribute("marks",
					manager.getCompetitionResult(competitionName));
			return "assessment/competition/calculated";
		} else {
			model.addAttribute("arenas", currComp.getArenas());
			return "assessment/competition/arena";
		}
	}
}