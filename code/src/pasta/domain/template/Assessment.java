package pasta.domain.template;

import java.util.ArrayList;
import java.util.Date;

public class Assessment {
	private ArrayList<UnitTest> unitTests = new ArrayList<UnitTest>();
	private ArrayList<UnitTest> secretUnitTests = new ArrayList<UnitTest>();
	private String name;
	private double marks;
	private Date dueDate;
	private int numSubmissionsAllowed;
	
	public void addUnitTest(UnitTest test){
		unitTests.add(test);
	}
	
	public void removeUnitTest(UnitTest test){
		unitTests.remove(test);
	}
	
	public void addSecretUnitTest(UnitTest test){
		secretUnitTests.add(test);
	}
	
	public void removeSecretUnitTest(UnitTest test){
		secretUnitTests.remove(test);
	}
	
	public double getMarks(){
		return marks;
	}

	public ArrayList<UnitTest> getUnitTests() {
		return unitTests;
	}

	public void setUnitTests(ArrayList<UnitTest> unitTests) {
		this.unitTests = unitTests;
	}
	
	public ArrayList<UnitTest> getSecretUnitTests() {
		return secretUnitTests;
	}

	public void setSecretUnitTests(ArrayList<UnitTest> secretUnitTests) {
		this.secretUnitTests = secretUnitTests;
	}

	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return name.replace(" ", "");
	}

	public void setName(String name) {
		this.name = name;
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