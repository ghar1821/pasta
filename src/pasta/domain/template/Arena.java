package pasta.domain.template;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.PASTATime;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

public class Arena {
	private String name;
	private String password = null;
	// if null, only run once
	private PASTATime frequency = null;
	private Date firstStartDate;
	
	public final static SimpleDateFormat dateParser 
	= new SimpleDateFormat("dd/MM/yyyy HH:mm");
	protected final Log logger = LogFactory.getLog(getClass());

	
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
				+ PASTAUtil.formatDate(firstStartDate) + "\" ";
		if(isPasswordProtected()){
			output += "password=\""+password+"\" ";
		}
		
		if(isRepeatable()){
			output += "repeats=\""+frequency+"\" ";
		}
		return output + "/>";
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
}
