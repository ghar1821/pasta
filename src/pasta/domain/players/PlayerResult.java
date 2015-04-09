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
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Contains the player statistics for competitions.
 * <p>
 * The statistics used are:
 * <ul>
 * 	<li>Player name.</li>
 * 	<li>The date the player was first uploaded.</li>
 * 	<li>The rating of the player in official arenas.</li>
 * 	<li>The ranking of the player in the official arenas.</li>
 * 	<li>The number of win/draw/loss in official/unofficial arenas.</li>
 * </ul>
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-05-01
 *
 */
@Entity
@Table (name = "player_results")
public class PlayerResult implements Serializable, Comparable<PlayerResult> {
	private static final long serialVersionUID = 5828653060251210355L;

	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	//TODO: refactor to "uploaded_date"
	private Date firstUploaded;
	
	private double officialRating;
	private int officialRanking;
	
	private int officialWin;
	private int officialDraw;
	private int officialLoss;
	
	private int unofficialWin;
	private int unofficialDraw;
	private int unofficialLoss;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getFirstUploaded() {
		return firstUploaded;
	}
	public void setFirstUploaded(Date firstUploaded) {
		this.firstUploaded = firstUploaded;
	}
	public double getOfficialRating() {
		return officialRating;
	}
	public void setOfficialRating(double officialRating) {
		this.officialRating = officialRating;
	}
	public int getOfficialRanking() {
		return officialRanking;
	}
	public void setOfficialRanking(int officialRanking) {
		this.officialRanking = officialRanking;
	}
	public int getOfficialWin() {
		return officialWin;
	}
	public void setOfficialWin(int officialWin) {
		this.officialWin = officialWin;
	}
	public int getOfficialDraw() {
		return officialDraw;
	}
	public void setOfficialDraw(int officialDraw) {
		this.officialDraw = officialDraw;
	}
	public int getOfficialLoss() {
		return officialLoss;
	}
	public void setOfficialLoss(int officialLoss) {
		this.officialLoss = officialLoss;
	}
	public int getUnofficialWin() {
		return unofficialWin;
	}
	public void setUnofficialWin(int unofficialWin) {
		this.unofficialWin = unofficialWin;
	}
	public int getUnofficialDraw() {
		return unofficialDraw;
	}
	public void setUnofficialDraw(int unofficialDraw) {
		this.unofficialDraw = unofficialDraw;
	}
	public int getUnofficialLoss() {
		return unofficialLoss;
	}
	public void setUnofficialLoss(int unofficialLoss) {
		this.unofficialLoss = unofficialLoss;
	}
	
	@Override
	public int compareTo(PlayerResult o) {
		return this.getOfficialRating() < o.getOfficialRating() ? -1 : this.getOfficialRating() > o
				.getOfficialRating() ? 1 : 0;
	}
	
}
