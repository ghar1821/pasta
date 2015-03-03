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

package pasta.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.players.PlayerHistory;
import pasta.domain.players.PlayerResult;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Data Access Object for Players.
 * <p>
 * This class is responsible for all of the interaction between the data layer
 * (disk in this case) and the system for players.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-05-01
 */
public class PlayerDAO {

	protected final Log logger = LogFactory.getLog(getClass());

	// username, competition, player
	Map<String, Map<String, Map<String, PlayerHistory>>> players;

	/**
	 * Loads all player histories (statistics) into cache
	 */
	public PlayerDAO() {
		// load all players.
		players = new TreeMap<String, Map<String, Map<String, PlayerHistory>>>();

		// list of all students
		String[] allStudents = (new File(ProjectProperties.getInstance().getSubmissionsLocation())).list();

		// list of all compeittions
		String[] allCompetitions = (new File(ProjectProperties.getInstance().getCompetitionsLocation())).list();

		if (allStudents != null && allCompetitions != null) {
			for (String student : allStudents) {
				if (new File(ProjectProperties.getInstance().getSubmissionsLocation() + student + "/competitions/")
						.exists()) {
					if (!players.containsKey(student)) {
						players.put(student, new TreeMap<String, Map<String, PlayerHistory>>());
					}
					for (String competition : allCompetitions) {
						players.get(student).put(competition, loadPlayerHistory(student, competition));
					}
				}
			}
		}
	}

	/**
	 * Get the history (statistics) of all players a user has submitted to a
	 * specific competition from cache
	 * 
	 * @param username name of the user
	 * @param competitionName the short name (no whitespace) of the competition
	 * @return collection of the player histories (statistics)
	 */
	public Collection<PlayerHistory> getPlayerHistory(String username, String competitionName) {

		if (!players.containsKey(username)) {
			players.put(username, new TreeMap<String, Map<String, PlayerHistory>>());
		}
		if (!players.get(username).containsKey(competitionName)) {
			players.get(username).put(competitionName, new TreeMap<String, PlayerHistory>());
		}

		return players.get(username).get(competitionName).values();
	}

	/**
	 * Get the history (statistics) of a specific player a user has submitted to a
	 * specific competition from cache
	 * 
	 * @param username name of the user
	 * @param competitionName the short name (no whitespace) of the competition
	 * @param playerName the name of the player being queried
	 * @return the player history (statistics)
	 */
	public PlayerHistory getPlayerHistory(String username, String competitionName, String playerName) {

		if (!players.containsKey(username)) {
			players.put(username, new TreeMap<String, Map<String, PlayerHistory>>());
		}
		if (!players.get(username).containsKey(competitionName)) {
			players.get(username).put(competitionName, new TreeMap<String, PlayerHistory>());
		}
		if (!players.get(username).get(competitionName).containsKey(playerName)) {
			players.get(username).get(competitionName).put(playerName, new PlayerHistory(playerName));
		}
		return players.get(username).get(competitionName).get(playerName);
	}

	/**
	 * Get the history (statistics) of all players a user has submitted to a
	 * specific competition from disk.
	 * <p>
	 * Performs multiple calls to
	 * {@link #loadPlayerHistory(String, String, String)}.
	 * 
	 * @param username name of the user
	 * @param competitionName the short name (no whitespace) of the competition
	 * @return map of the player histories (statistics) with the key being the
	 *         player name
	 */
	public Map<String, PlayerHistory> loadPlayerHistory(String username, String competitionName) {
		Map<String, PlayerHistory> playerHistory = new TreeMap<String, PlayerHistory>();

		String[] allPlayers = (new File(ProjectProperties.getInstance().getSubmissionsLocation() + username
				+ "/competitions/" + competitionName + "/")).list();

		if (allPlayers != null) {
			for (String player : allPlayers) {
				PlayerHistory currentPlayer = loadPlayerHistory(username, competitionName, player);
				if (currentPlayer != null) {
					playerHistory.put(player, currentPlayer);
				}
			}
		}

		return playerHistory;
	}

	/**
	 * Get the history (statistics) of a specific player a user has submitted to a
	 * specific competition from disk.
	 * <p>
	 * Calls {@link #loadPlayerFromDirectory(String)} to aggregate the history of
	 * all players with the same name
	 * 
	 * @param username name of the user
	 * @param competitionName the short name (no whitespace) of the competition
	 * @param playerName the name of the player being queried
	 * @return the player histories (statistics)
	 */
	private PlayerHistory loadPlayerHistory(String username, String competitionName, String playerName) {

		PlayerHistory player = null;

		String playerFolder = ProjectProperties.getInstance().getSubmissionsLocation() + username
				+ "/competitions/" + competitionName + "/" + playerName + "/";

		if ((new File(playerFolder)).exists()) {
			// there is at least 1 active player
			player = new PlayerHistory(playerName);
			// get active player
			player.activatePlayer(loadPlayerFromDirectory(playerFolder + "active/"));
			// get retired player
			LinkedList<PlayerResult> retiredPlayerList = new LinkedList<PlayerResult>();
			if (new File(playerFolder + "retired/").exists()) {
				String[] retiredPlayers = (new File(playerFolder + "retired/")).list();

				if (retiredPlayers != null) {
					for (String retiredPlayer : retiredPlayers) {
						if ((new File(playerFolder + "retired/" + retiredPlayer)).isDirectory()) {
							PlayerResult currentPlayer = loadPlayerFromDirectory(playerFolder + "retired/" + retiredPlayer
									+ "/");
							if (currentPlayer != null) {
								if (currentPlayer.getName() == null || currentPlayer.getName().isEmpty()) {
									currentPlayer.setName(playerName);
								}

								retiredPlayerList.add(currentPlayer);
							}
						}
					}
				}
			}
			player.setRetiredPlayers(retiredPlayerList);

		}
		return player;
	}

	/**
	 * Load the results of a single player from a directory.
	 * 
	 * @param directory location of the player
	 * @return the player result (statistic).
	 */
	private PlayerResult loadPlayerFromDirectory(String directory) {
		PlayerResult player = new PlayerResult();

		// read player.info
		{
			try {
				Scanner in = new Scanner(new File(directory + "player.info"));

				player.setName(in.nextLine().replace("name=", ""));
				try {
					player.setFirstUploaded(PASTAUtil.parseDate(in.nextLine().replace("uploadDate=", "")));
				} catch (ParseException e) {
					player.setFirstUploaded(new Date(0));
				}

				in.close();
			} catch (FileNotFoundException e) {
				logger.error("Execution error reading " + directory + "player.info :" + e);
				return null;
			}
		}

		// read official.stats
		if (new File(directory + "official.stats").exists()) {
			double latestRating = 0;
			int latestRanking = 0;

			int win = 0;
			int draw = 0;
			int loss = 0;

			try {
				Scanner in = new Scanner(new File(directory + "official.stats"));

				while (in.hasNextLine()) {
					// ratings, rankings, win, draw, loss
					String[] line = in.nextLine().split(",");

					if (line.length >= 5) {
						latestRating = Double.parseDouble(line[0]);
						latestRanking = Integer.parseInt(line[1]);

						win += Integer.parseInt(line[2]);
						draw += Integer.parseInt(line[3]);
						loss += Integer.parseInt(line[4]);
					}
				}

				in.close();

				player.setOfficialRating(latestRating);
				player.setOfficialRanking(latestRanking);

				player.setOfficialWin(win);
				player.setOfficialDraw(draw);
				player.setOfficialLoss(loss);
			} catch (Exception e) {
				logger.error("Execution error reading " + directory + "official.stats :" + e);
			}
		}

		// read unofficial.stats
		if (new File(directory + "unofficial.stats").exists()) {
			try {
				Scanner in = new Scanner(new File(directory + "unofficial.stats"));

				int win = 0;
				int draw = 0;
				int loss = 0;

				while (in.hasNextLine()) {
					// ratings, rankings, win, draw, loss
					String[] line = in.nextLine().split(",");

					if (line.length >= 5) {

						win += Integer.parseInt(line[2]);
						draw += Integer.parseInt(line[3]);
						loss += Integer.parseInt(line[4]);
					}
				}

				in.close();

				player.setUnofficialWin(win);
				player.setUnofficialDraw(draw);
				player.setUnofficialLoss(loss);
			} catch (Exception e) {
				logger.error("Execution error reading " + directory + "unofficial.stats :" + e);
			}
		}
		return player;
	}
}
