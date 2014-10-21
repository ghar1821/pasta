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


package pasta.domain.template;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.PASTATime;
import pasta.util.PASTAUtil;

public class Arena {
	private String name;
	private String password = null;
	// if null, only run once
	private PASTATime frequency = null;
	private Date firstStartDate;
	private Map<String, Set<String>> players = new TreeMap<String, Set<String>>();
	
	public final static SimpleDateFormat dateParser 
	= new SimpleDateFormat("dd/MM/yyyy HH:mm");
	protected final Log logger = LogFactory.getLog(getClass());
	
	// getters and setters
	public String getName() {
		return name;
	}
	// getters and setters
	public String getShortName() {
		return name.replace(" ", "");
	}
	public void setName(String name) {
		this.name = name;
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
	
	// calculated methods
	public boolean isRepeatable(){
		return (frequency != null && frequency.getTime() > 1000);
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
	
	public Map<String, Set<String>> getPlayers() {
		return players;
	}
	
	public void setPlayers(Map<String, Set<String>> players) {
		this.players = players;
	}
	
	public String toString(){
		/**
		 * <arena>
		 * 	<name>$NAME</name>
		 * 	<password>$PASSWORD</password>
		 * 	<repeats>$FREQUENCY</repeats>
		 * </arena>
		 */
		
		String output = "<arena>" + System.getProperty("line.separator");
		output += "\t<name>" + name + "</name>" + System.getProperty("line.separator");
		output += "\t<firstStartDate>" + PASTAUtil.formatDate(firstStartDate)
					+ "</firstStartDate>" + System.getProperty("line.separator");
		
		if(isPasswordProtected()){
			output += "\t<password>" + password + "</password>" + System.getProperty("line.separator");
		}
		
		if(isRepeatable()){
			output += "\t<repeats>" + frequency + "</repeats>" + System.getProperty("line.separator");
		}
		
		output += "</arena>";
		
		return output;
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
	
	public boolean isInvalidName(){
		return getName().contains("/") 
				|| getName().contains("\\")
				|| getName().contains("?")
				|| getName().contains("%")
				|| getName().contains("*")
				|| getName().contains(":")
				|| getName().contains("|")
				|| getName().contains("\"")
				|| getName().contains("<")
				|| getName().contains(">")
				|| getName().contains(".");
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
	
	public void addPlayer(String user, String playerName){
		logger.info("adding " + user + "-" + playerName);
		if(!players.containsKey(user)){
			logger.info("user not there, adding set");
			players.put(user, new TreeSet<String>());
		}
		// if the official arena
		if(name.replace(" ", "").toLowerCase().equals("officialarena")){
			logger.info("official arena");
			players.get(user).clear();
		}
		players.get(user).add(playerName);
		logger.info(players.get(user).size());
	}
	
	public void removePlayer(String user, String playerName){
		if(players.containsKey(user)){
			players.get(user).remove(playerName);
		}
	}
	
	public int getNumPlayers(){
		int numPlayers = 0;
		if(players != null){
			for(Set<String> playerSet: players.values()){
				if(playerSet != null){
					numPlayers += playerSet.size();
				}
			}
		}
		return numPlayers;
	}
}
