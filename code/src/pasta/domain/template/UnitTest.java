package pasta.domain.template;

import pasta.util.ProjectProperties;

public class UnitTest {
	private String name;
	private boolean tested;
	
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
	
	public String toString(){
		String output = "<unitTestProperties>" + System.getProperty("line.separator");
		output += "\t<name>"+name+"</name>" + System.getProperty("line.separator");
		output += "\t<tested>"+tested+"</tested>" + System.getProperty("line.separator");
		output += "</unitTestProperties>";
		return output;
	}
}
