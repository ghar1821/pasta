package pasta.domain.template;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
	private HashMap<String, Set<String>> players = new HashMap<String, Set<String>>();
	
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
	
	public HashMap<String, Set<String>> getPlayers() {
		return players;
	}
	
	public void setPlayers(HashMap<String, Set<String>> players) {
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
			logger.info("user not there, adding hashset");
			players.put(user, new HashSet<String>());
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
