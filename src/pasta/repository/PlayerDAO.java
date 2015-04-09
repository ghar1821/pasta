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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

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
@Repository("playerDAO")
@DependsOn("projectProperties")
public class PlayerDAO extends HibernateDaoSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	public PlayerDAO() {
	}

	/**
	 * Get the history (statistics) of all players a user has submitted to a
	 * specific competition from cache
	 * 
	 * @param username name of the user
	 * @param competitionId the id of the competition
	 * @return collection of the player histories (statistics)
	 */
	@SuppressWarnings("unchecked")
	public List<PlayerHistory> getAllPlayerHistories(String username, long competitionId) {
		DetachedCriteria cr = DetachedCriteria.forClass(PlayerHistory.class);
		cr.add(Restrictions.eq("username", username));
		cr.add(Restrictions.eq("competitionId", competitionId));
		return getHibernateTemplate().findByCriteria(cr);
	}

	/**
	 * Get the history (statistics) of a specific player a user has submitted to a
	 * specific competition from cache
	 * 
	 * @param username name of the user
	 * @param competitionId the id of the competition
	 * @param playerName the name of the player being queried
	 * @return the player history (statistics)
	 */
	public PlayerHistory getPlayerHistory(String username, long competitionId, String playerName) {
		DetachedCriteria cr = DetachedCriteria.forClass(PlayerHistory.class);
		cr.add(Restrictions.eq("username", username));
		cr.add(Restrictions.eq("competitionId", competitionId));
		cr.add(Restrictions.eq("playerName", playerName));
		@SuppressWarnings("unchecked")
		List<PlayerHistory> results = getHibernateTemplate().findByCriteria(cr);
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	/**
	 * Get the history (statistics) of all players a user has submitted to a
	 * specific competition from disk.
	 * 
	 * @param username name of the user
	 * @param competitionId the id of the competition
	 * @return map of the player histories (statistics) with the key being the
	 *         player name
	 */
	public Map<String, PlayerHistory> loadPlayerHistories(String username, long competitionId) {
		Map<String, PlayerHistory> playerHistory = new TreeMap<String, PlayerHistory>();
		List<PlayerHistory> results = getAllPlayerHistories(username, competitionId);
		if(results == null || results.isEmpty()) {
			return playerHistory;
		}
		for(PlayerHistory history : results) {
			playerHistory.put(history.getPlayerName(), history);
		}
		return playerHistory;
	}

	// TODO: write equivalent
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

	public void saveOrUpdate(PlayerHistory history) {
		getHibernateTemplate().saveOrUpdate(history);
	}
}
