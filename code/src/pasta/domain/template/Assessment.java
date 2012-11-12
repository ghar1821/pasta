package pasta.domain.template;

import java.util.ArrayList;

public class Assessment {
	private ArrayList<UnitTest> unitTests;
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
}
