package pasta.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.players.PlayerHistory;
import pasta.domain.players.PlayerResult;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

public class PlayerDAO {

	protected final Log logger = LogFactory.getLog(getClass());

	// username, competition, player
	HashMap<String, HashMap<String, HashMap<String, PlayerHistory>>> players;
	
	public PlayerDAO(){
		// load all players.
		players = new HashMap<String, HashMap<String, HashMap<String, PlayerHistory>>>();
		
		// list of all students
		String[] allStudents = (new File(ProjectProperties.getInstance().getProjectLocation()+"/submissions/")).list();
		
		// list of all compeittions
		String[] allCompetitions = (new File(ProjectProperties.getInstance().getProjectLocation()+"/template/competition")).list();
		
		if(allStudents != null && allCompetitions != null){
			for(String student : allStudents){
				if(new File(ProjectProperties.getInstance().getProjectLocation()+"/submissions/"+student+"/competitions/").exists()){
					if(!players.containsKey(student)){
						players.put(student, new HashMap<String, HashMap<String, PlayerHistory>>());
					}
					for(String competition: allCompetitions){
						players.get(student).put(competition, loadPlayerHistory(student, competition));
					}
				}
			}
		}
	}
	
	public Collection<PlayerHistory> getPlayerHistory(String username,
			String competitionName){
		
		if(!players.containsKey(username)){
			players.put(username, new HashMap<String, HashMap<String, PlayerHistory>>());
		}
		if(!players.get(username).containsKey(competitionName)){
			players.get(username).put(competitionName, new HashMap<String, PlayerHistory>());
		}
		
		return players.get(username).get(competitionName).values();
	}

	public PlayerHistory getPlayerHistory(String username,
			String competitionName, String playerName) {
		
		if(!players.containsKey(username)){
			players.put(username, new HashMap<String, HashMap<String, PlayerHistory>>());
		}
		if(!players.get(username).containsKey(competitionName)){
			players.get(username).put(competitionName, new HashMap<String, PlayerHistory>());
		}
		if(!players.get(username).get(competitionName).containsKey(playerName)){
			players.get(username).get(competitionName).put(playerName, new PlayerHistory(playerName));
		}
		return players.get(username).get(competitionName).get(playerName);
	}
	
	public HashMap<String, PlayerHistory> loadPlayerHistory(String username,
			String competitionName) {
		HashMap<String, PlayerHistory> playerHistory = new HashMap<String, PlayerHistory>();
		
		String[] allPlayers = (new File(ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/"
				+ username
				+ "/competitions/"
				+ competitionName
				+"/")
		).list();
		
		if(allPlayers != null){
			for(String player: allPlayers){
				PlayerHistory currentPlayer = loadPlayerHistory(username, competitionName, player);
				if(currentPlayer != null){
					playerHistory.put(player, currentPlayer);
				}
			}
		}
		
		return playerHistory;
	}

	private PlayerHistory loadPlayerHistory(String username,
			String competitionName, String playerName) {

		PlayerHistory player = null;

		String playerFolder = ProjectProperties.getInstance()
				.getProjectLocation()
				+ "/submissions/"
				+ username
				+ "/competitions/" + competitionName + "/" + playerName + "/";

		if ((new File(playerFolder)).exists()) {
			// there is at least 1 active player
			player = new PlayerHistory(playerName);
			// get active player
			player.activatePlayer(loadPlayerFromDirectory(playerFolder
					+ "active/"));
			// get retired player
			LinkedList<PlayerResult> retiredPlayerList = new LinkedList<PlayerResult>();
			if (new File(playerFolder + "retired/").exists()) {
				String[] retiredPlayers = (new File(playerFolder + "retired/"))
						.list();

				if (retiredPlayers != null) {
					for (String retiredPlayer : retiredPlayers) {
						if ((new File(playerFolder + "retired/" + retiredPlayer))
								.isDirectory()) {
							PlayerResult currentPlayer = loadPlayerFromDirectory(playerFolder
									+ "retired/" + retiredPlayer + "/");
							if (currentPlayer != null) {
								if (currentPlayer.getName() == null
										|| currentPlayer.getName().isEmpty()) {
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

	private PlayerResult loadPlayerFromDirectory(String directory) {
		PlayerResult player = new PlayerResult();

		// read player.info
		{
			try {
				Scanner in = new Scanner(new File(directory + "player.info"));

				player.setName(in.nextLine().replace("name=", ""));
				try {
					player.setFirstUploaded(PASTAUtil.parseDate(in.nextLine()
							.replace("uploadDate=", "")));
				} catch (ParseException e) {
					player.setFirstUploaded(new Date(0));
				}

				in.close();
			} catch (FileNotFoundException e) {
				logger.error("Execution error reading " + directory
						+ "player.info :" + e);
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
			} catch (FileNotFoundException e) {
				logger.error("Execution error reading " + directory
						+ "official.stats :" + e);
			}
		}

		// read unofficial.stats
		if (new File(directory + "unofficial.stats").exists()) {
			try {
				Scanner in = new Scanner(new File(directory
						+ "unofficial.stats"));

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
			} catch (FileNotFoundException e) {
				logger.error("Execution error reading " + directory
						+ "unofficial.stats :" + e);
			}
		}
		return player;
	}
}
