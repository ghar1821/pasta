package pasta.domain.players;

import java.util.LinkedList;

public class PlayerHistory {
	
	String playerName;
	PlayerResult activePlayer = null;
	LinkedList<PlayerResult> retiredPlayers = new LinkedList<PlayerResult>();
	
	public PlayerHistory(String playerName){
		this.playerName = playerName;
	}
	
	public PlayerResult getActivePlayer() {
		return activePlayer;
	}
	public void setActivePlayer(PlayerResult activePlayer) {
		this.activePlayer = activePlayer;
	}
	public LinkedList<PlayerResult> getRetiredPlayers() {
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
}
