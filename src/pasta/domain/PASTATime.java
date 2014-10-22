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


package pasta.domain;

import java.util.Date;
/**
 * 
 * I could have called it something better, but this sounded amusing
 *
 * Minimum frequency is 1 second (anything below could be abused to 
 * put too much load on the machine by mistake)
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-01-21
 *
 */
public class PASTATime {
	private int years = 0;
	private int days = 0;
	private int hours = 0;
	private int minutes = 0;
	private int seconds = 0;
	private int miliseconds = 0;
	
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

	/**
	 * Calculate the next execution.
	 * 
	 * @param currentDate the current assessment run date
	 * @return the next run date of execution
	 */
	public Date nextExecution(Date currentDate){
		if(getTime() == 0){
			return currentDate;
		}
		Date next = new Date();
		Date now = new Date();
		next.setTime(currentDate.getTime() + getTime());
		if(getTime() > 0){
			while(next.before(now)){
				next.setTime(next.getTime() + getTime());
			}
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
	
}
