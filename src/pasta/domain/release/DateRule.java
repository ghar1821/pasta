package pasta.domain.release;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import pasta.domain.PASTAUser;

/**
 * A rule that is met once the date reaches the given release date.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_date")
public class DateRule extends ReleaseRule {
	private final static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	
	@Column(name = "release_date")
	private Date releaseDate;
	
	public DateRule() {
		releaseDate = new Date();
	}
	public DateRule(Date date) {
		releaseDate = date;
	}
	
	@Override
	protected boolean isMet(PASTAUser user) {
		if(releaseDate == null) {
			return false;
		}
		return !(new Date()).before(releaseDate);
	}
	
	public Date getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	public String getStrDate() {
		if(releaseDate == null) {
			return "";
		}
		return sdf.format(releaseDate);
	}
	public void setStrDate(String date) {
		if(date == null || date.isEmpty()) {
			releaseDate = null;
		}
		try {
			releaseDate = sdf.parse(date);
		} catch (ParseException e) {
		}
	}
	
	@Override
	public String getShortDescription() {
		return "Release on a certain date.";
	}
	@Override
	public String getDescription() {
		return "The assessment will be released if the current date is on or after the specified date.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release on ").append(getStrDate());
		return sb.toString();
	}
}
