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
