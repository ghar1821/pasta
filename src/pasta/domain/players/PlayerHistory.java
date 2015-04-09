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

package pasta.domain.players;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
import pasta.domain.template.Competition;
 * Class to contain the player history of a competition.
 * <p>
 * The data that is held is the player name, current active player, list of retired players.
 * 
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-05-01
 *
 */
@Entity
@Table (name = "player_histories")
public class PlayerHistory implements Serializable, Comparable<PlayerHistory> {
	private static final long serialVersionUID = 745002463058416366L;

	@Id
	@GeneratedValue
	private long id;
	
	private String username;
	
	@Column (name = "competition_id")
	private long competitionId;
	
	@Column (name = "player_name")
	private String playerName;
	
	@ManyToOne (cascade = CascadeType.ALL)
	@JoinColumn (name = "active_player_result_id")
	private PlayerResult activePlayer = null;
	
	@ManyToMany (cascade = CascadeType.ALL)
	@JoinTable(name="player_history_retired_players",
			joinColumns=@JoinColumn(name = "player_history_id"),
			inverseJoinColumns=@JoinColumn(name = "player_result_id"))
    @OrderColumn(name = "retired_index")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<PlayerResult> retiredPlayers = new LinkedList<PlayerResult>();
	
	public PlayerHistory() { }
	
	public PlayerHistory(String playerName){
		this.playerName = playerName;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public long getCompetitionId() {
		return competitionId;
	}
	public void setCompetitionId(long competitionId) {
		this.competitionId = competitionId;
	}

	public PlayerResult getActivePlayer() {
		return activePlayer;
	}
	public void setActivePlayer(PlayerResult activePlayer) {
		this.activePlayer = activePlayer;
	}
	public List<PlayerResult> getRetiredPlayers() {
		return retiredPlayers;
	}
	public void setRetiredPlayers(LinkedList<PlayerResult> retiredPlayers) {
		this.retiredPlayers = retiredPlayers;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public void retireActivePlayer(){
		if(activePlayer != null){
			retiredPlayers.add(activePlayer);
			activePlayer = null;
		}
	}
	
	public void activatePlayer(PlayerResult player){
		if(activePlayer != null){
			retireActivePlayer();
		}
		activePlayer = player;
	}

	@Override
	public int compareTo(PlayerHistory o) {
		return this.getPlayerName().compareTo(o.getPlayerName());
	}
}
