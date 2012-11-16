package pasta.domain.template;

import java.util.ArrayList;
import java.util.Date;

public class HandMarking {

	private String name;
	private String description;
	private double marks;
	private Date dueDate;
	private int numSubmissionsAllowed;
	
	public double getMarks(){
		return marks;
	}

	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	
	public String getShortName() {
		return name.replace(" ", "");
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
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

	public int getNumSubmissionsAllowed() {
		return numSubmissionsAllowed;
	}

	public void setNumSubmissionsAllowed(int numSubmissionsAllowed) {
		this.numSubmissionsAllowed = numSubmissionsAllowed;
	}
}
