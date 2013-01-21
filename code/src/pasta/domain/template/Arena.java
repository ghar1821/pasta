package pasta.domain.template;

import java.util.Date;

import pasta.domain.PASTATime;
import pasta.util.ProjectProperties;

public class Arena {
	private String name;
	private String password = null;
	// if null, only run once
	private PASTATime frequency = null;
	private Date firstStartDate;
	
	// getters and setters
	public String getName() {
		return name;
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
	
	public String toString(){
		String output = "<arena name=\"" + name + "\" firstStartDate=\""
				+ ProjectProperties.formatDate(firstStartDate) + "\" ";
		if(isPasswordProtected()){
			output += "password=\""+password+"\" ";
		}
		
		if(isRepeatable()){
			output += "repeats=\""+frequency+"\" ";
		}
		return output + "/>";
	}
}
