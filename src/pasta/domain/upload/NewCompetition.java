package pasta.domain.upload;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.domain.PASTATime;
import pasta.domain.template.Competition;

public class NewCompetition {
	private String name;
	private String type;
	private PASTATime frequency = new PASTATime();
	private Date firstStartDate = new Date();
	private CommonsMultipartFile file;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
	public void setFirstRunDateStr(String firstStartDateStr){
		try {
			firstStartDate = Competition.dateParser.parse(firstStartDateStr);
		} catch (ParseException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.error("Could not parse " + firstStartDateStr
					+ "\r\n" + sw.toString());
		}
	}
	
	public String getFirstStartDateStr(){
		firstStartDate.setTime(firstStartDate.getTime()+31536000000l);
		return Competition.dateParser.format(firstStartDate);
	}
}
