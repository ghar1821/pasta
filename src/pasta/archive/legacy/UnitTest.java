package pasta.archive.legacy;

import pasta.util.ProjectProperties;
/**
 * Container class for a unit test.
 * 
 * Contains the name of the unit test and whether it has been tested.
 * 
 * String representation:
 * <pre>{@code <unitTestProperties>
	<name>name</name>
	<tested>true|false</tested>
</unitTestProperties>}</pre>
 * 
 * <p>
 * File location on disk: $projectLocation$/template/unitTest/$unitTestName$
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
public class UnitTest {
	private String name;
	private boolean tested;
	
	/**
	 * Default constructor
	 * <p>
	 * name="nullgarbagetemptestihopenobodynamestheirtestthis"
	 * 
	 * tested=false
	 * 
	 */
	public UnitTest(){
		this.name = "nullgarbagetemptestihopenobodynamestheirtestthis";
		this.tested = false;
	}
	
	public UnitTest(String name, boolean tested){
		this.name = name;
		this.tested = tested;
	}

	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return name.replace(" ", "");
	}

	public String getFileLocation(){
		return ProjectProperties.getInstance().getProjectLocation()+"/template/unitTest/"+getShortName();
	}
	
	public boolean isTested() {
		return tested;
	}
	
	public void setTested(boolean tested) {
		this.tested = tested;
	}
	
	@Override
	public String toString(){
		String output = "<unitTestProperties>" + System.getProperty("line.separator");
		output += "\t<name>"+name+"</name>" + System.getProperty("line.separator");
		output += "\t<tested>"+tested+"</tested>" + System.getProperty("line.separator");
		output += "</unitTestProperties>";
		return output;
	}
}