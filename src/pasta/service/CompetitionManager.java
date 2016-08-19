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

package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import pasta.domain.form.NewCompetitionForm;
import pasta.domain.form.NewPlayer;
import pasta.domain.form.UpdateCompetitionForm;
import pasta.domain.players.PlayerHistory;
import pasta.domain.players.PlayerResult;
import pasta.domain.result.CompetitionMarks;
import pasta.domain.result.CompetitionResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.user.PASTAUser;
import pasta.repository.PlayerDAO;
import pasta.repository.ResultDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.testing.AntJob;
import pasta.testing.AntResults;
import pasta.testing.PlayerValidationRunner;
import pasta.testing.options.CalculatedCompetitionOptions;
import pasta.testing.task.DirectoryCopyTask;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Competition manager.
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 *
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-31
 *
 */
@Service("competitionManager")
@Repository
public class CompetitionManager {
	
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	private PlayerDAO playerDAO = ProjectProperties.getInstance().getPlayerDAO();
	
	@Autowired
	private ApplicationContext context;
	
	private ExecutionScheduler scheduler;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
	}

	public static final Logger logger = Logger
			.getLogger(CompetitionManager.class);
	

	/**
	 * Add a new competition from {@link pasta.domain.form.NewCompetitionForm}
	 * 
	 * @param form the new competition form
	 */
	public Competition addCompetition(NewCompetitionForm form) {
		Competition newComp = new Competition();
		newComp.setName(form.getName());
		newComp.setCalculated(form.getType().equalsIgnoreCase("calculated"));
		
		if(!newComp.isCalculated()){
			newComp.setOutstandingArenas(new LinkedList<Arena>());
			newComp.setCompletedArenas(new LinkedList<Arena>());
			Arena officialArena = new Arena();
			officialArena.setName("Official Arena");
			officialArena.setFirstStartDate(null);
			officialArena.setFrequency(null);
			newComp.setOfficialArena(officialArena);
		}
		
		return ProjectProperties.getInstance().getCompetitionDAO().saveOrUpdate(newComp);
	}
	
	public Competition updateCompetition(Competition existingComp, UpdateCompetitionForm form) {

		existingComp.setName(form.getName());
		existingComp.setFrequency(form.getFrequency());
		existingComp.setFirstStartDate(form.getFirstStartDate());
		existingComp.setHidden(form.isHidden());
		existingComp.setStudentPermissions(form.getStudentPermissions());
		existingComp.setTutorPermissions(form.getTutorPermissions());
		
		if(existingComp.isCalculated()) {
			CalculatedCompetitionOptions options = new CalculatedCompetitionOptions();
			if(form.isHasRun()) {
				options.setRunOptions(form.getRunOptions());
			}
			if(form.isHasBuild()) {
				options.setBuildOptions(form.getBuildOptions());
			}
			existingComp.setOptions(options);
		} else {
			existingComp.getOfficialArena().setFirstStartDate(existingComp.getFirstStartDate());
			existingComp.getOfficialArena().setFrequency(existingComp.getFrequency());
		}

		return ProjectProperties.getInstance().getCompetitionDAO().saveOrUpdate(existingComp);
	}
	
	public void updateCode(Competition comp, UpdateCompetitionForm form) {
		try {
			// create space on the file system.
			(new File(comp.getFileLocation() + "/code/")).mkdirs();

			// unzip the uploaded code into the code folder. (if exists)
			if (form.getFile() != null && !form.getFile().isEmpty()) {
				// unpack
				form.getFile().getInputStream().close();
				form.getFile().transferTo(
						new File(comp.getFileLocation() + "/code/"
								+ form.getFile().getOriginalFilename()));
				PASTAUtil.extractFolder(comp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename());
				FileUtils.forceDelete(new File(comp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename()));
			}
		} catch (Exception e) {
			(new File(comp.getFileLocation())).delete();
			logger.error("Competition code for " + comp.getName()
					+ " was not loaded successfully.", e);
		}
	}

	/**
	 * Helper method
	 * 
	 * @param id the id of the competition
	 * @see pasta.repository.AssessmentDAO#unlinkCompetition(long)
	 */
	public void removeCompetition(long id) {
		Competition comp = ProjectProperties.getInstance().getCompetitionDAO().getCompetition(id);
		if(comp == null) {
			logger.error("Could not delete competition with ID " + id);
		} else {
			ProjectProperties.getInstance().getAssessmentDAO().unlinkCompetition(id);
			ProjectProperties.getInstance().getCompetitionDAO().delete(comp);
		}
	}

	/**
	 * Helper method
	 * 
	 * @return a collection of {@link pasta.domain.template.Competition}
	 * @see pasta.repository.AssessmentDAO#getCompetitionList()
	 */
	public List<Competition> getCompetitionList() {
		return ProjectProperties.getInstance().getCompetitionDAO().getAllCompetitions();
	}

	/**
	 * Helper method
	 * 
	 * @param id the id of the competition
	 * @return the {@link pasta.domain.template.Competition} or null
	 * @see pasta.repository.AssessmentDAO#getCompetition(id)
	 */
	public Competition getCompetition(long id) {
		return ProjectProperties.getInstance().getCompetitionDAO().getCompetition(id);
	}
	
	/**
	 * Helper method
	 * 
	 * @param competitionId the id of the competition
	 * @return the {@link pasta.domain.result.CompetitionMarks} or null
	 * @see pasta.repository.ResultDAO#getLatestCompetitionMarks(String)
	 */
	public CompetitionMarks getLatestCompetitionMarks(long competitionId) {
		return resultDAO.getLatestCompetitionMarks(competitionId);
	}
	
	/**
	 * Helper method
	 * 
	 * @param competitionId the short name (no whitespace) of the competition
	 * @return the {@link pasta.domain.result.CompetitionResult} or null
	 * @see pasta.repository.ResultDAO#getLatestCalculatedCompetitionResult(long)
	 */
	public CompetitionResult getLatestCalculatedCompetitionResult(long competitionId){
		return resultDAO.getLatestCalculatedCompetitionResult(competitionId);
	}

	/**
	 * Add a new arena
	 * <p>
	 * Also schedules the competition and arena for execution if correct
	 * @param arena the arena
	 * @param currComp the competition
	 */
	public void addArena(Arena arena, Competition currComp) {
		if(currComp.getArena(arena.getId()) == null){
			currComp.addNewArena(arena);
			ProjectProperties.getInstance().getCompetitionDAO().saveOrUpdate(currComp);
			scheduler.scheduleJob(currComp, arena, arena.getNextRunDate());
		}
	}

	/**
	 * Add a player
	 * 
	 * @param playerForm the new player form. See {@link pasta.domain.form.NewPlayer}
	 * @param user the user
	 * @param competitionId the id of the competition
	 * @param result the BindingResult which can be user to give the user feedback and reject the {@link pasta.domain.form.NewPlayer}
	 */
	public void addPlayer(NewPlayer playerForm, PASTAUser user, 
			long competitionId, BindingResult result) {
		// copy player to temp location
		
		Competition comp = ProjectProperties.getInstance().getCompetitionDAO().getCompetition(competitionId);
		if(comp == null) {
			result.rejectValue("competition", "Competition.doesNotExist");
		}
		
		String submitLocation = comp.getFileLocation() + File.separator + "players" + File.separator + user.getUsername();
		File submitDir = new File(submitLocation);
		File tempDir = new File(submitDir, "temp");
		File codeDir = new File(tempDir, "code");
		
		if (playerForm.getFile() != null && !playerForm.getFile().isEmpty()) {
			
			if(tempDir.exists()){
				try {
					FileUtils.deleteDirectory(tempDir);
				} catch (IOException e) { }
			}
			
			codeDir.mkdirs();

			try {
				playerForm.getFile().getInputStream().close();
				
				File playerLocation = new File(codeDir, playerForm.getFile().getOriginalFilename());
				playerForm.getFile().transferTo(playerLocation);
				
				// unpack if necessary
				if(playerLocation.getName().endsWith(".zip")) {
					PASTAUtil.extractFolder(playerLocation.getAbsolutePath());
					FileUtils.forceDelete(playerLocation);
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			result.rejectValue("file", "Player.noFile");
			return;
		}
		
		File testDir = new File(tempDir, "test");
		String validateCodeSubmittedLocation = "validate";
		String validateCodeLocation = "src";
		String playerCodeLocation = "player";
		
		//TODO: make this generic, not just "Validator"
		try {
			PlayerValidationRunner runner = new PlayerValidationRunner(user, "Validator", validateCodeLocation);
			runner.setMaxRunTime(30000);
			runner.setPlayerDirectory(playerCodeLocation);
			
			AntJob antJob = new AntJob(testDir, runner, "build", "validate");
			antJob.addDependency("validate", "build");
			
			// copy over competition
			antJob.addSetupTask(new DirectoryCopyTask(
					new File(new File(comp.getFileLocation(), "code"), validateCodeSubmittedLocation), 
					new File(testDir, validateCodeLocation)));
			// copy over player
			antJob.addSetupTask(new DirectoryCopyTask(
					new File(tempDir, "code"), 
					new File(testDir, playerCodeLocation)));
			
			antJob.run();
		
			AntResults results = antJob.getResults();
			//TODO save full output
	//		logger.warn(results.getFullOutput());
			
			String playerName = null;
			String validationError = null;
			
			if(!results.isSuccess("build")) {
				Scanner scn = new Scanner(results.getOutput("build"));
				StringBuilder compErrors = new StringBuilder();
				String line = "";
				while(scn.hasNextLine()) {
					line = scn.nextLine();
					if(line.contains("error")) {
						compErrors.append(line.replaceFirst("\\s*\\[javac\\]", "")).append('\n');
						break;
					}
				}
				while(scn.hasNextLine()) {
					line = scn.nextLine();
					compErrors.append(line.replaceFirst("\\s*\\[javac\\]", "")).append('\n');
				}
				if(compErrors.length() > 0) {
					compErrors.replace(compErrors.length()-1, compErrors.length(), "");
				}
				scn.close();
				//TODO maybe store and display properly?
				result.rejectValue("file", "Player.errors", compErrors.toString());
			} else {
				if(results.isSuccess("validate")) {
					try {
						Scanner scn = new Scanner(new File(testDir, PlayerValidationRunner.playerNameFilename));
						if(scn.hasNext()) {
							playerName = scn.next();
						}
						scn.close();
					} catch (FileNotFoundException e) {
						logger.error("Could not get player name from validator.");
					}
					try {
						Scanner scn = new Scanner(new File(testDir, PlayerValidationRunner.validationErrorFilename));
						StringBuilder sb = new StringBuilder();
						while(scn.hasNextLine()) {
							sb.append(scn.nextLine()).append(System.lineSeparator());
						}
						scn.close();
						validationError = sb.toString();
					} catch (FileNotFoundException e) {
					}
				}
				
				if(validationError != null) {
					//TODO maybe store and display properly?
					result.rejectValue("file", "Player.errors", validationError);
				}
			}
			
			if(!result.hasErrors()){
				PlayerResult newPlayer = new PlayerResult();
				newPlayer.setFirstUploaded(new Date());
				newPlayer.setName(playerName);
				
				PlayerHistory history = playerDAO.getPlayerHistory(user, competitionId, newPlayer.getName());
				if(history == null){
					history = new PlayerHistory(newPlayer.getName());
					history.setUser(user);
					history.setCompetitionId(competitionId);
				}
				
				// if no errors and the player has the same name as an existing player
				// retire the old player.
				retirePlayer(user, competitionId, newPlayer.getName());
				history.setActivePlayer(newPlayer);
				playerDAO.saveOrUpdate(history);
				
				File playerDir = new File(submitDir, newPlayer.getName());
				playerDir.mkdirs();
				
				try {
					FileUtils.copyDirectory(new File(tempDir, "code"),
							new File(new File(playerDir, "active"), "code"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e1) {
			result.reject("Internal error");
			logger.error("Validator templates not found.", e1);
		}
		
		try {
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retire a player
	 * 
	 * @param username the name of the user
	 * @param competitionId the id of the competition
	 * @param playerName the name of the player
	 */
	public void retirePlayer(PASTAUser user, long competitionId, String playerName){
		Competition comp = ProjectProperties.getInstance().getCompetitionDAO().getCompetition(competitionId);
		PlayerHistory history = playerDAO.getPlayerHistory(user, competitionId, playerName);
		if(history == null) {
			return;
		}
		
		PlayerResult oldPlayer = history.getActivePlayer();
		if(oldPlayer == null) {
			return;
		}
		
		history.retireActivePlayer();
		playerDAO.saveOrUpdate(history);
		
		//TODO move to DAO
		try {
			// archive the old player
			String playerLocation = comp.getFileLocation() + File.separator + "players" + File.separator + user.getUsername() + File.separator + playerName;
			File oldLoc = new File(playerLocation, "active");
			File newLoc = new File(playerLocation, "retired");
			newLoc.mkdirs();
			FileUtils.moveDirectory(oldLoc, new File(newLoc, PASTAUtil.formatDate(oldPlayer.getFirstUploaded())));
		} catch (IOException e) {
			logger.error("Could not retire player " + playerName 
					+ " of user " + user.getUsername() 
					+ " for competition " + comp.getName(), e);
		}
	}

	/**
	 * Get the player history for a user in a competition
	 * 
	 * @see pasta.repository.PlayerDAO#getAllPlayerHistories(String, String)
	 * @param user the user
	 * @param competitionId the id of the competition
	 * @return a list of the collection of player history, empty map if no players or competition/user is invalid
	 */
	public List<PlayerHistory> getPlayers(PASTAUser user, long competitionId) {
		return playerDAO.getAllPlayerHistories(user, competitionId);
	}
	
	/**
	 * Add a player to an arena
	 * 
	 * @param username the name of the user
	 * @param competitionId the id of the competition
	 * @param arenaId the id of the arena
	 * @param playerName the name of the player.
	 */
	public void addPlayerToArena(PASTAUser user, long competitionId,
			long arenaId, String playerName) {
		if(!playerExists(user, competitionId, playerName)) {
			return;
		}
		Competition comp = getCompetition(competitionId);
		Arena arena;
		if(comp == null || (arena = comp.getArena(arenaId)) == null) {
			return;
		}
		arena.addPlayer(user, playerName);
		ProjectProperties.getInstance().getCompetitionDAO().update(arena);
	}
	
	/**
	 * Remove a player form an arena
	 * 
	 * @param username the name of the user
	 * @param competitionId the id of the competition
	 * @param arenaId the id of the arena
	 * @param playerName the name of the player.
	 */
	public void removePlayerFromArena(PASTAUser user, long competitionId,
			long arenaId, String playerName) {
		if(!playerExists(user, competitionId, playerName)) {
			return;
		}
		Competition comp = getCompetition(competitionId);
		Arena arena;
		if(comp == null || (arena = comp.getArena(arenaId)) == null) {
			return;
		}
		arena.removePlayer(user, playerName);
		ProjectProperties.getInstance().getCompetitionDAO().update(arena);
	}

	/**
	 * Get the latest results of a competition execution
	 * 
	 * @param currComp the competition
	 * @param arena 
	 * @see pasta.repository.ResultDAO#getLatestCalculatedCompetitionResult(long)
	 * @return the {@link pasta.domain.result.CompetitionResult} or null.
	 */
	public CompetitionResult getLatestArenaResults(Competition currComp, Arena arena) {
		if(currComp != null){
			return resultDAO.getLatestArenaResult(currComp.getId(), arena.getId());
		}
		return null;
	}
	
	public boolean playerExists(PASTAUser user, long competitionId, String playerName) {
		return playerDAO.getPlayerHistory(user, competitionId, playerName) != null;
	}

	public Map<Long, Integer> getLiveAssessmentCounts(PASTAUser user) {
		Map<Long, Integer> liveCompetitions = new HashMap<Long, Integer>();
		for(Competition comp : getCompetitionList()) {
			liveCompetitions.put(comp.getId(), getLiveAssessmentCount(user, comp));
		}
		return liveCompetitions;
	}
	
	public int getLiveAssessmentCount(PASTAUser user, Competition comp) {
		List<Assessment> assessments = getAssessmentsUsingCompetition(comp);
		int count = 0;
		for(Assessment assessment : assessments) {
			if(assessment.isReleasedTo(user)) {
				count++;
			}
		}
		return count;
	}

	private List<Assessment> getAssessmentsUsingCompetition(Competition comp) {
		return ProjectProperties.getInstance().getCompetitionDAO().getAssessmentsUsingCompetition(comp);
	}
	
}
