/**
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */


package pasta.web.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import pasta.domain.upload.NewPlayer;
import pasta.service.CompetitionManager;
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
	
	@ModelAttribute("newPlayerModel")
	public NewPlayer returnPlayerModel() {
		return new NewPlayer();
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
		return getUser(username);
	}
	
	public PASTAUser getUser(String username) {
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
//		if (!user.isTutor()) {
//			return "redirect:/home/.";
//		}

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

		return "redirect:/mirror/";
	}

	// delete a competition
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
		if (user.isInstructor()) {
			competitionManager.removeCompetition(competitionName);
		}
		return "redirect:../../";
	}

	@RequestMapping(value = "{competitionName}/")
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
				form.setName(competitionName);
				competitionManager.updateCompetition(form);
			}
			else{
				// update competition
				compForm.setName(competitionName);
				competitionManager.updateCompetition(compForm);
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
			return "assessment/competition/arena";
		}
	}
	
	@RequestMapping(value = "view/{competitionName}/", method = RequestMethod.POST)
	public String addArena(Model model,
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
		
		if(!arena.isInvalidName()){
			if(!currComp.isCalculated()){
				if(currComp != null && (user.isTutor() || currComp.isStudentCreatableArena())){
					if(!arena.isRepeatable() || user.isInstructor() || 
							(currComp.isTutorCreatableRepeatableArena() && user.isTutor() && currComp.isTutorCreatableRepeatableArena())
							|| (currComp.isTutorCreatableRepeatableArena() && currComp.isStudentCreatableRepeatableArena())
							&& currComp.getArena(arena.getName()) == null){
						// accept arena
						competitionManager.addArena(arena, currComp);
					}
				}
			}
		}
		
		return "redirect:/mirror/";
	}
	
	@RequestMapping(value = "view/{competitionName}/{arenaName}")
	public String viewArenaPage(Model model,
			@PathVariable("arenaName") String arenaName,
			@PathVariable("competitionName") String competitionName) {
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}		
		
		model.addAttribute("unikey", user);

		if(!currComp.isCalculated()){
			// check if official
			model.addAttribute("players", competitionManager.getPlayers(user.getUsername(), competitionName));
			model.addAttribute("arena", currComp.getArena(arenaName));
			model.addAttribute("completed", currComp.isCompleted(arenaName));
			model.addAttribute("results", competitionManager.getArenaResults(currComp, currComp.getArena(arenaName)));
			if(arenaName.replace(" ", "").toLowerCase().equals("officialarena")){
				model.addAttribute("official", true);
			}
			else{
				model.addAttribute("official", false);
			}
			return "assessment/competition/arenaDetails";	
		}
		
		return "redirect:/mirror/";
	}
	
	@RequestMapping(value = "view/{competitionName}/{arenaName}/add/{playerName}")
	public String addPlayerToArena(Model model,
			@PathVariable("arenaName") String arenaName,
			@PathVariable("competitionName") String competitionName,
			@PathVariable("playerName") String playername) {
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}		
		competitionManager.addPlayerToArena(user.getUsername(), competitionName, arenaName, playername);
		
		return "redirect:../..";
	}
	
	@RequestMapping(value = "view/{competitionName}/{arenaName}/remove/{playerName}")
	public String removePlayerFromArena(Model model,
			@PathVariable("arenaName") String arenaName,
			@PathVariable("competitionName") String competitionName,
			@PathVariable("playerName") String playername) {
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}		
		competitionManager.removePlayerFromArena(user.getUsername(), competitionName, arenaName, playername);
		
		return "redirect:../..";
	}
	
	@RequestMapping(value = "{competitionName}/myPlayers/retire/{playerName}/")
	public String retirePlayer(Model model,
			@PathVariable("competitionName") String competitionName,
			@PathVariable("playerName") String playerName) {
		
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}	
		competitionManager.retirePlayer(user.getUsername(), competitionName, playerName);
		
		return "redirect:../..";
	}
	
	@RequestMapping(value = "{competitionName}/myPlayers/")
	public String manageMyPlayers(Model model,
			@PathVariable("competitionName") String competitionName) {
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}		
		
		model.addAttribute("unikey", user);
		model.addAttribute("competition", currComp);
		model.addAttribute("nodeList", PASTAUtil.generateFileTree(user.getUsername(), 
				competitionName, competitionManager.getPlayers(user.getUsername(), competitionName)));
		model.addAttribute("players", competitionManager.getLatestPlayers(user.getUsername(), competitionName));
		
		return "assessment/competition/players";
	}
	
	@RequestMapping(value = "view/{competitionName}/{unikey}/players/")
	public String viewOthersPlayers(Model model,
			@PathVariable("unikey") String unikey,
			@PathVariable("competitionName") String competitionName) {
		PASTAUser user = getUser();
		PASTAUser viewedUser = getUser(unikey);
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (viewedUser == null || currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}		
		
		model.addAttribute("unikey", user);
		model.addAttribute("viewedUser", viewedUser);
		model.addAttribute("nodeList", PASTAUtil.generateFileTree(viewedUser.getUsername(), 
				competitionName, competitionManager.getPlayers(viewedUser.getUsername(), competitionName)));
		model.addAttribute("competition", currComp);
		model.addAttribute("players", competitionManager.getLatestPlayers(viewedUser.getUsername(), competitionName));
		
		return "assessment/competition/players";
	}
	
	@RequestMapping(value = "{competitionName}/myPlayers/", method = RequestMethod.POST)
	public String submitNewPlayer(Model model,
			@PathVariable("competitionName") String competitionName,
			@ModelAttribute(value = "newPlayerModel") NewPlayer playerForm,
			BindingResult result) {
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/.";
		}		
		
		// validate player
		competitionManager.addPlayer(playerForm, user.getUsername(), currComp.getShortName(), result);
		
		if(result.hasErrors()){
			model.addAttribute("unikey", user);
			model.addAttribute("competition", currComp);
			model.addAttribute("players", competitionManager.getPlayers(user.getUsername(), competitionName));
			return "assessment/competition/players";
		}
		
		return "redirect:/mirror/";
	}
}