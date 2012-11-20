package pasta.domain.template;

import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;

public class HandMarking {

	private String name;
	private String description;
	private double marks;
	private Date dueDate;
	private int numSubmissionsAllowed;
	private String[] columns;
	private String[] rows;
	private ArrayList<ArrayList<Tuple>> data;
	
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

	public void setColumns(String[] c) {
		this.columns = c;
	}
	public String[] getColumns() {
		return columns;
	}
	public void setRows(String[] r) {
		this.rows = r;
	}
	public String[] getRows() {
		return rows;
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
	public void setData(ArrayList<ArrayList<Tuple>> data) {
		this.data = data;
	}

	public ArrayList<ArrayList<Tuple>> getData() {
		return data;
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
