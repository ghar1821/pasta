/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.domain.release;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import pasta.domain.user.PASTAUser;

/**
 * A rule that is met once the date reaches the given release date.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_date")
public class DateRule extends ReleaseRule implements Serializable {
	private static final long serialVersionUID = 2694477219675701797L;

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
		return "Release on a certain date";
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
