package pasta.domain.template;

import java.util.ArrayList;
import java.util.Date;

public class Assessment {
	private ArrayList<WeightedUnitTest> unitTests = new ArrayList<WeightedUnitTest>();
	private ArrayList<WeightedUnitTest> secretUnitTests = new ArrayList<WeightedUnitTest>();
	private String name;
	private double marks;
	private Date dueDate;
	private int numSubmissionsAllowed;
	
	public void addUnitTest(WeightedUnitTest test){
		unitTests.add(test);
	}
	
	public void removeUnitTest(WeightedUnitTest test){
		unitTests.remove(test);
	}
	
	public void addSecretUnitTest(WeightedUnitTest test){
		secretUnitTests.add(test);
	}
	
	public void removeSecretUnitTest(WeightedUnitTest test){
		secretUnitTests.remove(test);
	}
	
	public double getMarks(){
		return marks;
	}

	public ArrayList<WeightedUnitTest> getUnitTests() {
		return unitTests;
	}

	public void setUnitTests(ArrayList<WeightedUnitTest> unitTests) {
		this.unitTests = unitTests;
	}
	
	public ArrayList<WeightedUnitTest> getSecretUnitTests() {
		return secretUnitTests;
	}

	public void setSecretUnitTests(ArrayList<WeightedUnitTest> secretUnitTests) {
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