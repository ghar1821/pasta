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

	/**
	 * Get the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in or user isn't registered.
	 */
	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return getUser(username);
	}
	
	/**
	 * Get the user given a username
	 * 
	 * @param username the username of the user
	 * @return the user, null if the username is null or user isn't registered.
	 */
	public PASTAUser getUser(String username) {
		if (username != null) {
			return userManager.getUser(username);
		}
		return null;
	}


	// ///////////////////////////////////////////////////////////////////////////
	// COMPETITIONS //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/competition/
	 * <p>
	 * The landing page for competitions.
	 * Lists all competitions and shows how the user can interact with them.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>The user object</td></tr>
	 * 	<tr><td>allCompetitions</td><td>The collection of all competitions</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/viewAll/competition</li></ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "assessment/viewAll/competition"
	 */
	@RequestMapping(value = "")
	public String viewAllCompetitions(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		
		model.addAttribute("unikey", user);
		model.addAttribute("allCompetitions", competitionManager.getCompetitionList());

		return "assessment/viewAll/competition";
	}

	/**
	 * $PASTAUrl$/competition/ - POST
	 * <p>
	 * Add a new competition
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an Instructor: add the competition using 
	 * {@link pasta.service.CompetitionManager#addCompetition(NewCompetition)
	 * 
	 * redirect to mirror
	 * 
	 * @param model the model being used
	 * @param form the new competition form
	 * @return "redirect:/login" or "redirect:/home/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String newCompetition(Model model,
			@ModelAttribute(value = "newCompetitionModel") NewCompetition form) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if(user.isInstructor()){
			competitionManager.addCompetition(form);
		}

		return "redirect:/mirror/";
	}

	/**
	 * $PASTAUrl$/competition/delete/{competitionName}/
	 * <p>
	 * Delete a competition from the system.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an Instructor: delete the competition using
	 * {@link pasta.service.CompetitionManager#removeCompetition(String)}
	 * 
	 * redirect to mirror
	 * 
	 * @param competitionName the short name (no whitespace) of the competition
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../"
	 */
	@RequestMapping(value = "delete/{competitionName}/")
	public String deleteCompetition(
			@PathVariable("competitionName") String competitionName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (user.isInstructor()) {
			competitionManager.removeCompetition(competitionName);
		}
		return "redirect:../../";
	}

	/**
	 * $PASTAUrl$/competition/{competitionName}/
	 * <p>
	 * View the details of a competition.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>the user object for the currently logged in user</td></tr>
	 * 	<tr><td>competition</td><td>the competition object</td></tr>
	 * 	<tr><td>node</td><td>the root node for the code being used by the competition</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/view/competition</li></ul>
	 * 
	 * @param competitionName the short name (no whitespace) of the competition
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/view/competition"
	 */
	@RequestMapping(value = "{competitionName}/")
	public String viewCompetition(
			@PathVariable("competitionName") String competitionName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("competition",
				competitionManager.getCompetition(competitionName));
		model.addAttribute("node", PASTAUtil.generateFileTree(competitionManager
				.getCompetition(competitionName).getFileLocation() + "/code"));

		return "assessment/view/competition";
	}

	/**
	 * $PASTAUrl$/competition/{competitionName}/ - POST
	 * <p>
	 * Update a competition
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * If the user is an instructor: Update the competition based on which form was submitted
	 * using either {@link pasta.service.CompetitionManager#updateCompetition(Competition)} or
	 * {@link pasta.service.CompetitionManager#updateCompetition(NewCompetition)}
	 * 
	 * @param form the new competition form (used when changing code)
	 * @param compForm the competition to be updated (used when changing competition information and not code)
	 * @param competitionName the short name (no whitespace) of the competition
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:."
	 */
	@RequestMapping(value = "{competitionName}/", method = RequestMethod.POST)
	public String updateCompetition(@ModelAttribute(value = "newCompetitionModel") NewCompetition form,
			@ModelAttribute(value = "competition") Competition compForm,
			@PathVariable("competitionName") String competitionName, Model model){
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
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

	/**
	 * $PASTAUrl/competition/view/{competitionName}/
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
	 * 	<tr><td>unikey</td><td>the user object of the currently logged in user</td></tr>
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
	 * @param competitionName the short name (no whitespace) of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/competition/calculated" or "assessment/competition/arena"
	 */
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
	
	/**
	 * $PASTAUrl$/competition/view/{competitionName}/ - POST
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
	 * @param competitionName the short name (no whitespace) of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/"
	 */
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
			return "redirect:/home/";
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
	
	/**
	 * $PASTAUrl$/competition/view/{competitionName}/{arenaName}/
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
	 * 	<tr><td>unikey</td><td>The user object for the currently logged in user</td></tr>
	 * 	<tr><td>players</td><td>A list of players the currently logged in user has uploaded and are not retired</td></tr>
	 * 	<tr><td>arena</td><td>the arena object</td></tr>
	 * 	<tr><td>completed</td><td>a flag for whether the arena has finished executing and will not execute again</td></tr>
	 * 	<tr><td>results</td><td>the results object ({@link pasta.domain.result.ArenaResult})</td></tr>
	 * 	<tr><td>official</td><td>a flag for whether the arena is the official arena or not</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/competition/arenaDetails</li></ul>
	 * 
	 * @param model the model being used
	 * @param arenaName the name of the arena.
	 * @param competitionName the short name (no whitespace) of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../" or "assessment/competition/arenaDetails"
	 */
	@RequestMapping(value = "view/{competitionName}/{arenaName}/")
	public String viewArenaPage(Model model,
			@PathVariable("arenaName") String arenaName,
			@PathVariable("competitionName") String competitionName) {
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/";
		}		
		
		if(!currComp.isCalculated()){
			// check if official
			model.addAttribute("unikey", user);
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
		
		return "redirect:../";
	}
	
	/**
	 * $PASTAUrl$/competition/view/{competitionName}/{arenaName}/add/{playerName}/
	 * <p>
	 * Add a player to the arena.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * Add the player to the arena using 
	 * {@link pasta.service.CompetitionManager#addPlayerToArena(String, String, String, String)}
	 * and redirects back to $PASTAUrl$/competition/view/{competitionName}/{arenaName}/
	 * 
	 * @param model the model being used
	 * @param arenaName the arena name
	 * @param competitionName the short name (no whitespace) for the competition
	 * @param playername the player name
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../.."
	 */
	@RequestMapping(value = "view/{competitionName}/{arenaName}/add/{playerName}/")
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
			return "redirect:/home/";
		}		
		competitionManager.addPlayerToArena(user.getUsername(), competitionName, arenaName, playername);
		
		return "redirect:../..";
	}
	
	/**
	 * $PASTAUrl$/competition/view/{competitionName}/{arenaName}/remove/{playerName}/
	 * <p>
	 * Remove a player from the arena.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * Remove the player from the arena using 
	 * {@link pasta.service.CompetitionManager#removePlayerFromArena(String, String, String, String)}
	 * and redirects back to $PASTAUrl$/competition/view/{competitionName}/{arenaName}/
	 * 
	 * @param model the model being used
	 * @param arenaName the arena name
	 * @param competitionName the short name (no whitespace) for the competition
	 * @param playername the player name
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../.."
	 */
	@RequestMapping(value = "view/{competitionName}/{arenaName}/remove/{playerName}/")
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
			return "redirect:/home/";
		}		
		competitionManager.removePlayerFromArena(user.getUsername(), competitionName, arenaName, playername);
		
		return "redirect:../..";
	}
	
	/**
	 * $PASTAUrl$/competition/{competitionName}/myPlayers/retire/{playerName}/
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
	 * @param competitionName the short name (no whitespace) of the competition
	 * @param playerName the player name
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../.."
	 */
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
	
	/**
	 * $PASTAUrl$/competition/{competitionName}/myPlayers/
	 * <p>
	 * Manage my players.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>the user object of the currently logged in player</td></tr>
	 * 	<tr><td>competition</td><td>the competition object</td></tr>
	 * 	<tr><td>nodeList</td><td>the collection of nodes for the root of the folder structure of the code of the players uploaded, will only be use by tutors atm.</td></tr>
	 * 	<tr><td>players</td><td>the list of all of the players that have been uploaded for this competition by this player</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/competition/players</li></ul>
	 * 
	 * @param model the model being used
	 * @param competitionName the short name (no whitespace) of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/competition/players"
	 */
	@RequestMapping(value = "{competitionName}/myPlayers/")
	public String manageMyPlayers(Model model,
			@PathVariable("competitionName") String competitionName) {
		PASTAUser user = getUser();
		Competition currComp = competitionManager.getCompetition(competitionName);

		if (user == null) {
			return "redirect:/login/";
		}
		if (currComp == null || (!user.isTutor() && !currComp.isLive())) {
			return "redirect:/home/";
		}		
		
		model.addAttribute("unikey", user);
		model.addAttribute("competition", currComp);
		model.addAttribute("nodeList", PASTAUtil.generateFileTree(user.getUsername(), 
				competitionName, competitionManager.getPlayers(user.getUsername(), competitionName)));
		model.addAttribute("players", competitionManager.getLatestPlayers(user.getUsername(), competitionName));
		
		return "assessment/competition/players";
	}
	
	/**
	 * $PASTAUrl$/competition/{competitionName}/myPlayers/" - POST
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
	 * 	<tr><td>unikey</td><td>the user object of the currently logged in player</td></tr>
	 * 	<tr><td>competition</td><td>the competition object</td></tr>
	 * 	<tr><td>nodeList</td><td>the collection of nodes for the root of the folder structure of the code of the players uploaded, will only be use by tutors atm.</td></tr>
	 * 	<tr><td>players</td><td>the list of all of the players that have been uploaded for this competition by this player</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/competition/players</li></ul>
	 * 
	 * @param model the model being used
	 * @param competitionName the short name (no whitespace) of the competition
	 * @param playerForm the new player form containing the code
	 * @param result the binding result used for feedback.
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:/mirror/" or "assessment/competition/players"
	 */
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
			return "redirect:/home/";
		}		
		
		// validate player
		competitionManager.addPlayer(playerForm, user.getUsername(), currComp.getShortName(), result);
		
		if(result.hasErrors()){
			model.addAttribute("unikey", user);
			model.addAttribute("competition", currComp);
			model.addAttribute("players", competitionManager.getPlayers(user.getUsername(), competitionName));
			model.addAttribute("nodeList", PASTAUtil.generateFileTree(user.getUsername(), 
					competitionName, competitionManager.getPlayers(user.getUsername(), competitionName)));
			return "assessment/competition/players";
		}
		
		return "redirect:/mirror/";
	}
	
	/**
	 * $PASTAUrl$/competition/view/{competitionName}/{unikey}/players/
	 * <p>
	 * View another user's list of players.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor and the competition isn't live or the viewed user doesn't exist: redirect to home.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>the user object for the currently logged in user</td></tr>
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
	 * @param competitionName the short name (no whitespace) of the competition
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/competition/players"
	 */
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
			return "redirect:/home/";
		}		
		
		model.addAttribute("unikey", user);
		model.addAttribute("viewedUser", viewedUser);
		model.addAttribute("nodeList", PASTAUtil.generateFileTree(viewedUser.getUsername(), 
				competitionName, competitionManager.getPlayers(viewedUser.getUsername(), competitionName)));
		model.addAttribute("competition", currComp);
		model.addAttribute("players", competitionManager.getLatestPlayers(viewedUser.getUsername(), competitionName));
		
		return "assessment/competition/players";
	}
	
	
}