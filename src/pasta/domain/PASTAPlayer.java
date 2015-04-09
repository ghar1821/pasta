package pasta.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name = "pasta_players")
public class PASTAPlayer implements Serializable, Comparable<PASTAPlayer> {
	private static final long serialVersionUID = 1968049262741984294L;
	
	@Id
	@GeneratedValue
	private long id;
	
	private String username;
	
	@Column (name = "player_name")
	private String playerName;

	public PASTAPlayer() {
		
	}
	
	public PASTAPlayer(String username, String playerName) {
		this.username = username;
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

	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	@Override
	public int compareTo(PASTAPlayer o) {
		int diff = this.username.compareTo(o.username);
		if(diff != 0) {
			return diff;
		}
		return this.playerName.compareTo(o.playerName);
	}
}
