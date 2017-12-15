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

package pasta.domain.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Form object for a new assessment.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-07
 *
 */
public class NewAssessmentForm {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	@NotEmpty
	@Length(max=256)
	private String name;
	
	@Min(0)
	private double marks;
	
	@NotNull
	private Date dueDate;
	
	@Min(0)
	private int maxSubmissions;
	
	public NewAssessmentForm() {
		this.name = "";
		this.marks = 0.0;
		this.dueDate = new Date();
		this.maxSubmissions = 0;
	}
	
	public String getStrDate() {
		if(dueDate == null) {
			return "";
		}
		return sdf.format(dueDate);
	}
	
	public void setStrDate(String date) {
		if(date == null || date.isEmpty()) {
			dueDate = null;
		}
		try {
			dueDate = sdf.parse(date);
		} catch (ParseException e) {
			dueDate = null;
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getMarks() {
		return marks;
	}
	public void setMarks(double marks) {
		this.marks = marks;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public int getMaxSubmissions() {
		return maxSubmissions;
	}
	public void setMaxSubmissions(int maxSubmissions) {
		this.maxSubmissions = maxSubmissions;
	}
}
