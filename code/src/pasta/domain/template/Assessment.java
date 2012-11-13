package pasta.domain.template;

import java.util.ArrayList;

public class Assessment {
	private ArrayList<UnitTest> unitTests = new ArrayList<UnitTest>();
	private String name;
	private double marks;
	
	public void addUnitTest(UnitTest test){
		unitTests.add(test);
	}
	
	public void removeUnitTest(UnitTest test){
		unitTests.remove(test);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMarks(double marks) {
		this.marks = marks;
	}

	
}
