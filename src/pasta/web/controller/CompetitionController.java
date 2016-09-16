/*
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pasta.domain.UserPermissionLevel;
import pasta.domain.form.NewCompetitionForm;
import pasta.domain.form.NewPlayer;
import pasta.domain.form.UpdateCompetitionForm;
import pasta.domain.form.validate.UpdateCompetitionFormValidator;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Competition;
import pasta.domain.user.PASTAUser;
import pasta.service.CompetitionManager;
import pasta.service.UserManager;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;
import pasta.web.WebUtils;

/**
 * Controller class for Competition functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/competition/...
 * <p>
 * Both students and teaching staff can access this url.
 * Students have highly limited functionality. 
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-08-15
 *
 */
@Controller
@RequestMapping("competition/")
public class CompetitionController {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private CompetitionManager competitionManager;
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private UpdateCompetitionFormValidator updateValidator;

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////
	
	@ModelAttribute("newArenaModel")
	public Arena returnArenaModel() {
		return new Arena();
	}
	
	@ModelAttribute("newPlayerModel")
	public NewPlayer returnPlayerModel() {
		return new NewPlayer();
	}

	@ModelAttribute("competition")
	public Competition returnCompetitionModel(@PathVariable("competitionId") long competitionId) {
		return competitionManager.getCompetition(competitionId);
	}
	
	@ModelAttribute("updateCompetitionForm")
	public UpdateCompetitionForm returnUpdateForm(@PathVariable("competitionId") long competitionId) {
		return new UpdateCompetitionForm(
				competitionManager.getCompetition(competitionId));
	}

	@ModelAttribute("assessmentResult")
	public AssessmentResult returnAssessmentResultModel() {
		return new AssessmentResult();
	}
	
	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// COMPETITIONS //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/competition/delete/{competitionId}/
	 * <p>
	 * Delete a competition from the system.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an Instructor: delete the competition using
	 * {@link pasta.service.CompetitionManager#removeCompetition(long)}
	 * 
	 * redirect to mirror
	 * 
	 * @param competitionId the id of the competition
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../"
	 */
	@RequestMapping(value = "delete/{competitionId}/")
	public String deleteCompetition(
			@PathVariable("competitionId") long competitionId, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		competitionManager.removeCompetition(competitionId);
		return "redirect:../../";
	}

	/**
	 * $PASTAUrl$/competition/{competitionId}/
	 * <p>
	 * View the details of a competition.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>competition</td><td>the competition object</td></tr>
	 * 	<tr><td>node</td><td>the root node for the code being used by the competition</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/view/competition</li></ul>
	 * 
	 * @param competitionId the id of the competition
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/view/competition"
	 */
	@RequestMapping(value = "{competitionId}/")
	public String viewCompetition(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("competitionId") long competitionId, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		//model.addAttribute("competition", competitionManager.getCompetition(competitionId));
		model.addAttribute("node", PASTAUtil.generateFileTree(competitionManager
				.getCompetition(competitionId).getFileLocation(), "/code"));
		model.addAttribute("liveAssessmentCount", competitionManager.getLiveAssessmentCount(user, competitionManager.getCompetition(competitionId)));
		
		Competition comp = competitionManager.getCompetition(competitionId);
		if(comp.hasCode()) {
			model.addAttribute("codeFiles", PASTAUtil.generateFileMap(comp.getCodeLocation()));
		}

		return "assessment/view/competition";
	}

	/**
	 * $PASTAUrl$/competition/{competitionId}/ - POST
	 * <p>
	 * Update a competition
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * If the user is an instructor: Update the competition based on which form was submitted
	 * using either {@link pasta.service.CompetitionManager#updateCompetition(Competition)} or
	 * {@link pasta.service.CompetitionManager#updateCompetition(NewCompetitionForm)}
	 * 
	 * @param form the new competition form (used when changing code)
	 * @param compForm the competition to be updated (used when changing competition information and not code)
	 * @param competitionId the id of the competition
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:."
	 */
	@RequestMapping(value = "{competitionId}/", method = RequestMethod.POST)
	public String updateCompetition(
			@Valid @ModelAttribute(value = "updateCompetitionForm") UpdateCompetitionForm form, BindingResult result,
			@ModelAttribute(value = "competition") Competition comp,
			@PathVariable("competitionId") long competitionId, 
			RedirectAttributes attr, Model model){
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);

		updateValidator.validate(form, result);
		if(result.hasErrors()) {
			attr.addFlashAttribute("updateCompetitionForm", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.updateCompetitionForm", result);
			return "redirect:.";
		}
		
		// update competition
		competitionManager.updateCompetition(comp, form);
		
		if(form.getFile() != null && !form.getFile().isEmpty()){
			// update contents
			competitionManager.updateCode(comp, form);
		}
		
		return "redirect:.";
	}

	/**
	 * $PASTAUrl/competition/view/{competitionId}/
	 * <p>
	 * View the competition page. Differs based on if the competition is 
	 * calculated or arena based.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>competition</td><td>the competition object</td></tr>
	 * 	<tr><td>arenaResult</td><td>the results of the last execution if it's a calculated competition. I know it's called arenaResult, I was lazy and copy-pasting is fun.</td></tr>
	 * 	<tr><td>marks</td><td>The mars awarded to the users based on their performance</td></tr>
	 * </table>
	 * 
	 * JSP: 
	 * <ul>
	 * 	<li>assessment/competition/calculated - if it's a calculated competition</li>
	 * 	<li>assessment/competition/arena -  if it's an arena based competition</li>
	 * </ul>
	 * 
	 * @param model the model being used
	 * @param competitionId the id of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/competition/calculated" or "assessment/competition/arena"
	 */
	@RequestMapping(value = "view/{competitionId}/")
	public String viewCompetitionPage(Model model, @ModelAttribute("user") PASTAUser user, 
			@PathVariable("competitionId") long competitionId) {
		Competition currComp = competitionManager.getCompetition(competitionId);
		if (currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/";
		}

		//model.addAttribute("competition", currComp);

		if (currComp.isCalculated()) {
			// TODO user ids for results
			model.addAttribute("arenaResult",
					competitionManager.getLatestCalculatedCompetitionResult(currComp.getId()));
			model.addAttribute("marks",
					competitionManager.getLatestCompetitionMarks(currComp.getId()));
			return "assessment/competition/calculated";
		} else {
			return "assessment/competition/arena";
		}
	}
	
	/**
	 * $PASTAUrl$/competition/view/{competitionId}/ - POST
	 * <p>
	 * Add an arena to a competition
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * If the arena has a valid name (is only alphanumeric characters) and the 
	 * competition is not calculated and the competition allows the user to
	 * create an arena, then add the arena using 
	 * {@link pasta.service.CompetitionManager#addArena(Arena, Competition)}
	 * 
	 * @param model the model being used
	 * @param arena the arena being added
	 * @param competitionId the id of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "view/{competitionId}/", method = RequestMethod.POST)
	public String addArena(Model model, @ModelAttribute("user") PASTAUser user, 
			@ModelAttribute(value = "newArenaModel") Arena arena,
			@PathVariable("competitionId") long competitionId) {

		Competition currComp = competitionManager.getCompetition(competitionId);
		if (currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/";
		}		
		
		if(!arena.isInvalidName()){
			if(!currComp.isCalculated()){
				if(currComp != null && (user.isTutor() || currComp.isStudentCanCreateArena())){
					if(!arena.isRepeatable() || user.isInstructor() || 
							(currComp.isTutorCanCreateRepeatableArena() && user.isTutor() && currComp.isTutorCanCreateRepeatableArena())
							|| (currComp.isTutorCanCreateRepeatableArena() && currComp.isStudentCanCreateRepeatableArena())
							&& currComp.getArena(arena.getId()) == null){
						// accept arena
						competitionManager.addArena(arena, currComp);
					}
				}
			}
		}
		
		return "redirect:/mirror/";
	}
	
	/**
	 * $PASTAUrl$/competition/view/{competitionId}/{arenaId}/
	 * <p>
	 * View the details on an arena.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * If the competition is calculated: redirect to the competition page.
	 * 
	 * Otherwise: add attributes and serve up jsp
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>players</td><td>A list of players the currently logged in user has uploaded and are not retired</td></tr>
	 * 	<tr><td>arena</td><td>the arena object</td></tr>
	 * 	<tr><td>completed</td><td>a flag for whether the arena has finished executing and will not execute again</td></tr>
	 * 	<tr><td>results</td><td>the results object ({@link pasta.domain.result.CompetitionResult})</td></tr>
	 * 	<tr><td>official</td><td>a flag for whether the arena is the official arena or not</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/competition/arenaDetails</li></ul>
	 * 
	 * @param model the model being used
	 * @param arenaId the id of the arena.
	 * @param competitionId the id of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../" or "assessment/competition/arenaDetails"
	 */
	@RequestMapping(value = "view/{competitionId}/{arenaId}/")
	public String viewArenaPage(Model model, @ModelAttribute("user") PASTAUser user, 
			@PathVariable("arenaId") long arenaId,
			@PathVariable("competitionId") long competitionId) {

		Competition currComp = competitionManager.getCompetition(competitionId);
		if (currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/";
		}		
		
		if(!currComp.isCalculated()){
			model.addAttribute("players", competitionManager.getPlayers(user, competitionId));
			model.addAttribute("arena", currComp.getArena(arenaId));
			model.addAttribute("completed", currComp.isCompleted(arenaId));
			model.addAttribute("results", competitionManager.getLatestArenaResults(currComp, currComp.getArena(arenaId)));
			// check if official
			model.addAttribute("official", currComp.getOfficialArena().getId() == arenaId);
			return "assessment/competition/arenaDetails";	
		}
		
		return "redirect:../";
	}
	
	/**
	 * $PASTAUrl$/competition/view/{competitionId}/{arenaId}/add/{playerName}/
	 * <p>
	 * Add a player to the arena.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * Add the player to the arena using 
	 * {@link pasta.service.CompetitionManager#addPlayerToArena(String, String, String, String)}
	 * and redirects back to $PASTAUrl$/competition/view/{competitionId}/{arenaId}/
	 * 
	 * @param model the model being used
	 * @param arenaId the arena id
	 * @param competitionId the id for the competition
	 * @param playername the player name
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../.."
	 */
	@RequestMapping(value = "view/{competitionId}/{arenaId}/add/{playerName}/")
	public String addPlayerToArena(Model model, @ModelAttribute("user") PASTAUser user, 
			@PathVariable("arenaId") long arenaId,
			@PathVariable("competitionId") long competitionId,
			@PathVariable("playerName") String playername) {
		Competition currComp = competitionManager.getCompetition(competitionId);
		if (currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/";
		}		
		competitionManager.addPlayerToArena(user, competitionId, arenaId, playername);
		
		return "redirect:../..";
	}
	
	/**
	 * $PASTAUrl$/competition/view/{competitionId}/{arenaId}/remove/{playerName}/
	 * <p>
	 * Remove a player from the arena.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * Remove the player from the arena using 
	 * {@link pasta.service.CompetitionManager#removePlayerFromArena(String, String, String, String)}
	 * and redirects back to $PASTAUrl$/competition/view/{competitionId}/{arenaId}/
	 * 
	 * @param model the model being used
	 * @param arenaId the arena id
	 * @param competitionId the id for the competition
	 * @param playername the player name
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../.."
	 */
	@RequestMapping(value = "view/{competitionId}/{arenaId}/remove/{playerName}/")
	public String removePlayerFromArena(Model model, @ModelAttribute("user") PASTAUser user, 
			@PathVariable("arenaId") long arenaId,
			@PathVariable("competitionId") long competitionId,
			@PathVariable("playerName") String playername) {
		Competition currComp = competitionManager.getCompetition(competitionId);
		if (currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/";
		}		
		competitionManager.removePlayerFromArena(user, competitionId, arenaId, playername);
		
		return "redirect:../..";
	}
	
	/**
	 * $PASTAUrl$/competition/{competitionId}/myPlayers/retire/{playerName}/
	 * <p>
	 * Retire a player from the competition
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * Retire the player using {@link pasta.service.CompetitionManager#retirePlayer(String, String, String)}
	 * 
	 * @param model the model being used
	 * @param competitionId the id of the competition
	 * @param playerName the player name
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../.."
	 */
	@RequestMapping(value = "{competitionId}/myPlayers/retire/{playerName}/")
	public String retirePlayer(Model model, @ModelAttribute("user") PASTAUser user, 
			@PathVariable("competitionId") long competitionId,
			@PathVariable("playerName") String playerName) {
		
		Competition currComp = competitionManager.getCompetition(competitionId);
		if (currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/.";
		}	
		competitionManager.retirePlayer(user, competitionId, playerName);
		
		return "redirect:../..";
	}
	
	/**
	 * $PASTAUrl$/competition/{competitionId}/myPlayers/
	 * <p>
	 * Manage my players.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>competition</td><td>the competition object</td></tr>
	 * 	<tr><td>nodeList</td><td>the collection of nodes for the root of the folder structure of the code of the players uploaded, will only be use by tutors atm.</td></tr>
	 * 	<tr><td>players</td><td>the list of all of the players that have been uploaded for this competition by this player</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/competition/players</li></ul>
	 * 
	 * @param model the model being used
	 * @param competitionId the id of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/competition/players"
	 */
	@RequestMapping(value = "{competitionId}/myPlayers/")
	public String manageMyPlayers(Model model, @ModelAttribute("user") PASTAUser user, 
			@PathVariable("competitionId") long competitionId) {
		Competition currComp = competitionManager.getCompetition(competitionId);
		if (currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/";
		}		
		
		//model.addAttribute("competition", currComp);
		model.addAttribute("nodeList", PASTAUtil.generateFileTree(user, competitionManager
				.getCompetition(competitionId), competitionManager.getPlayers(
				user, competitionId)));
		model.addAttribute("players", competitionManager.getPlayers(user, competitionId));
		
		return "assessment/competition/players";
	}
	
	/**
	 * $PASTAUrl$/competition/{competitionId}/myPlayers/" - POST
	 * <p>
	 * Submit a player.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * Validate the player using {@link pasta.service.CompetitionManager#addPlayer(NewPlayer, String, String, BindingResult)},
	 * if the player fails validation, give the feedback to the user.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>competition</td><td>the competition object</td></tr>
	 * 	<tr><td>nodeList</td><td>the collection of nodes for the root of the folder structure of the code of the players uploaded, will only be use by tutors atm.</td></tr>
	 * 	<tr><td>players</td><td>the list of all of the players that have been uploaded for this competition by this player</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/competition/players</li></ul>
	 * 
	 * @param model the model being used
	 * @param competitionId the id of the competition
	 * @param playerForm the new player form containing the code
	 * @param result the binding result used for feedback.
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/" or "assessment/competition/players"
	 */
	@RequestMapping(value = "{competitionId}/myPlayers/", method = RequestMethod.POST)
	public String submitNewPlayer(Model model, @ModelAttribute("user") PASTAUser user, 
			@PathVariable("competitionId") long competitionId,
			@ModelAttribute(value = "newPlayerModel") NewPlayer playerForm,
			BindingResult result) {
		Competition currComp = competitionManager.getCompetition(competitionId);
		if (currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/";
		}		
		
		// validate player
		competitionManager.addPlayer(playerForm, user, currComp.getId(), result);
		
		if(result.hasErrors()){
			//model.addAttribute("competition", currComp);
			model.addAttribute("players", competitionManager.getPlayers(user, competitionId));
			model.addAttribute("nodeList", PASTAUtil.generateFileTree(user,
					competitionManager.getCompetition(competitionId),
					competitionManager.getPlayers(user, competitionId)));
			return "assessment/competition/players";
		}
		
		return "redirect:/mirror/";
	}
	
	/**
	 * $PASTAUrl$/competition/view/{competitionId}/{unikey}/players/
	 * <p>
	 * View another user's list of players.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live or the viewed user doesn't exist: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>viewedUser</td><td>the user object for the currently viewed user</td></tr>
	 * 	<tr><td>nodeList</td><td>the collection of nodes for the root of the folder structure of the code of the players uploaded, will only be use by tutors atm.</td></tr>
	 * 	<tr><td>competition</td><td>the competition object</td></tr>
	 * 	<tr><td>players</td><td>the list of all of the players that have been uploaded for this competition by the viewed player</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/competition/players</il></ul>
	 * 
	 * @param model the model being used
	 * @param unikey the username for the user you are viewing
	 * @param competitionId the id of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/competition/players"
	 */
	@RequestMapping(value = "view/{competitionId}/{unikey}/players/")
	public String viewOthersPlayers(Model model, @ModelAttribute("user") PASTAUser user, 
			@PathVariable("unikey") String unikey,
			@PathVariable("competitionId") long competitionId) {
		PASTAUser viewedUser = userManager.getUser(unikey);
		Competition currComp = competitionManager.getCompetition(competitionId);
		if (viewedUser == null || currComp == null || (!user.isTutor() && !ProjectProperties.getInstance().getCompetitionDAO().isLive(currComp))) {
			return "redirect:/home/";
		}		
		
		model.addAttribute("viewedUser", viewedUser);
		model.addAttribute("nodeList", PASTAUtil.generateFileTree(viewedUser,
				competitionManager.getCompetition(competitionId),
				competitionManager.getPlayers(viewedUser, competitionId)));
		//model.addAttribute("competition", currComp);
		model.addAttribute("players", competitionManager.getPlayers(viewedUser, competitionId));
		
		return "assessment/competition/players";
	}
	
	
}