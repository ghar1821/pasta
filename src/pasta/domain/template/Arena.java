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

package pasta.domain.template;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.PASTAPlayer;
import pasta.domain.PASTATime;
import pasta.domain.user.PASTAUser;
/**
 * Container class for the Arena information.
 * <p>
 * String representation:
 * <pre>{@code <arena>
	<name>name</name>
	<firstStartDate>yyyy-MM-dd'T'HH-mm-ss</firstStartDate>
	<password>plaintext</password>
	<repeats>PASTATime</repeats>
</arena>}</pre>
 * 
 * Password protected arenas are not fully implemented
 * 
 * <p>
 * File location on disk: $projectLocation$/template/competition/$competitionName$/arenas/$arenaName$
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-01-21
 * 
 */
@Entity
@Table (name = "arenas")
public class Arena {
	
	public final static SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	private String password = null;
	
	// if null, only run once
	@Column (name = "frequency")
	private PASTATime frequency = null;
	
	@Column (name = "first_start_date")
	private Date firstStartDate;
	
	@OneToMany (cascade = CascadeType.ALL)
	@JoinColumn (name = "arena_id")
	@LazyCollection (LazyCollectionOption.FALSE)
	private Set<PASTAPlayer> players = new HashSet<PASTAPlayer>();
	
	@OneToOne (cascade = CascadeType.ALL)
	private Competition competition;
	
	@Transient
	protected final Log logger = LogFactory.getLog(getClass());
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	// getters and setters
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFileAppropriateName() {
		return name.replaceAll("[\\w+]", "");
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public PASTATime getFrequency() {
		return frequency;
	}
	public void setFrequency(PASTATime frequency) {
		this.frequency = frequency;
	}
	
	public Date getFirstStartDate() {
		return firstStartDate;
	}
	public void setFirstStartDate(Date firstStartDate) {
		this.firstStartDate = firstStartDate;
	}
	
	public Competition getCompetition() {
		return competition;
	}
	public void setCompetition(Competition competition) {
		this.competition = competition;
	}
	
	public Set<PASTAPlayer> getPlayers() {
		return players;
	}
	public void setPlayers(Set<PASTAPlayer> players) {
		this.players = players;
	}
	
	// calculated methods
	public boolean isRepeatable(){
		return (frequency != null && !frequency.tooOften());
	}
	
	public boolean isPasswordProtected(){
		return password != null;
	}
	
	public boolean correctPassword(String password){
		if(this.password != null && password != null){
			return this.password.equals(password);
		}
		return false;
	}
	
	// calculated methods
	public String getFirstStartDateStr(){
		if(firstStartDate == null){
			return dateParser.format(new Date());
		}
		return dateParser.format(firstStartDate);
	}
	
	public void setFirstStartDateStr(String firstStartDateStr){
		try {
			firstStartDate = dateParser.parse(firstStartDateStr);
		} catch (ParseException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.error("Could not parse " + firstStartDateStr
					+ "\r\n" + sw.toString());
		}
	}
	
	/**
	 * Check if the arena has a valid name.
	 * <p>
	 * A valid arena name only contains whitespace and word characters
	 * @return whether the name of the arena is valid.
	 */
	public boolean isInvalidName(){
		return !getName().matches("[\\s\\w]+");
	}
	
	public Date getNextRunDate(){
		
		if(firstStartDate == null){
			return null;
		}
		if(frequency == null){
			return firstStartDate;
		}
		return frequency.nextExecution(firstStartDate);
	}
	
	/**
	 * Add a player to the arena.
	 * <p>
	 * If the arena is an official arena, only 1 player from a
	 * username is allowed. The old player is overridden with the current one.
	 * If the arena is not official (used for marking),
	 * the student is allowed to add as many players as they wish.
	 * 
	 * @param user the user id of the user adding the player
	 * @param playerName the name of the player the user is adding to the arena
	 */
	public void addPlayer(PASTAUser user, String playerName){
		if(this.isOfficialArena()) {
			for(Iterator<PASTAPlayer> it = players.iterator(); it.hasNext();) {
				PASTAPlayer player = it.next();
				if(player.getUser().equals(user)) {
					it.remove();
				}
			}
		}
		
		players.add(new PASTAPlayer(user, playerName));
	}
	
	public void removePlayer(PASTAUser user, String playerName){
		players.remove(new PASTAPlayer(user, playerName));
	}
	
	public boolean isOfficialArena() {
		if(competition == null) {
			return this.getFileAppropriateName().toLowerCase().equals("officialarena");
		}
		return competition.getOfficialArena() != null 
				&& competition.getOfficialArena() == this;
	}
	
	public boolean hasPlayers(PASTAUser user) {
		for(PASTAPlayer player : players) {
			if(player.getUser().equals(user)) {
				return true;
			}
		}
		return false;
	}
	
	public int getNumPlayers(){
		return players.size();
	}
	
}
