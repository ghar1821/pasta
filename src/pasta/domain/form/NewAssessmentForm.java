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
