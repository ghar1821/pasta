package pasta.domain;

import java.util.Date;
/**
 * I could have called it something better, but this sounded amusing
 * @author Alex
 * 
 * Minimum frequency is 1 second (anything below could be abused to 
 * put too much load on the machine by mistake)
 *
 */
public class PASTATime {
	private int years = 0;
	private int days = 0;
	private int hours = 0;
	private int minutes = 0;
	private int seconds = 0;
	private int miliseconds = 0;
	
	public int getYears() {
		return years;
	}

	public void setYears(int years) {
		this.years = years;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getMiliseconds() {
		return miliseconds;
	}

	public void setMiliseconds(int miliseconds) {
		this.miliseconds = miliseconds;
	}
	
	public long getTime(){
		return (((((years*365+days)*24+hours)*60+minutes)*60+seconds)*1000 + miliseconds);
	}

	public Date nextExecution(Date currentDate){
		Date next = new Date();
		Date now = new Date();
		next.setTime(currentDate.getTime() + getTime());
		while(next.before(now)){
			next.setTime(next.getTime() + getTime());
		}
		return next;
	}
	
	public String toString(){
		return years+"y"+days+"d"+hours+"h"+minutes+"m"+seconds+"s"+miliseconds+"ms";
	}
	
	public String getNiceStringRepresentation(){
		String rep = "";
		if(years > 0){
			rep += years+" year";
			if(years > 1){
				rep += "s";
			}
		}
		if(days > 0){
			rep += days+" day";
			if(days > 1){
				rep += "s";
			}
		}
		if(hours > 0){
			rep += hours+" hour";
			if(hours > 1){
				rep += "s";
			}
		}
		if(minutes > 0){
			rep += minutes+" min";
		}
		if(seconds > 0){
			rep += seconds+" sec ";
		}
		if(miliseconds > 0){
			rep += miliseconds+" ms";
		}
		return rep;
	}
	
	public PASTATime(){}
	
	public PASTATime(String stringRepresentation){
		try{
			years = Integer.parseInt(stringRepresentation.split("y")[0]);
			stringRepresentation = stringRepresentation.replace(years+"y", "");
		}catch (Exception e){}
		try{
			days = Integer.parseInt(stringRepresentation.split("d")[0]);
			stringRepresentation = stringRepresentation.replace(days+"d", "");
		}catch (Exception e){}
		try{
			hours = Integer.parseInt(stringRepresentation.split("h")[0]);
			stringRepresentation = stringRepresentation.replace(hours+"h", "");
		}catch (Exception e){}
		try{
			minutes = Integer.parseInt(stringRepresentation.split("m")[0]);
			stringRepresentation = stringRepresentation.replace(minutes+"m", "");
		}catch (Exception e){}
		try{
			seconds = Integer.parseInt(stringRepresentation.split("s")[0]);
			stringRepresentation = stringRepresentation.replace(seconds+"s", "");
		}catch (Exception e){}
		try{
			miliseconds = Integer.parseInt(stringRepresentation.split("ms")[0]);
			stringRepresentation = stringRepresentation.replace(miliseconds+"ms", "");
		}catch (Exception e){}
	}
}
