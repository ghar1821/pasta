package pasta.domain.template;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Assessment {
	private ArrayList<WeightedUnitTest> unitTests = new ArrayList<WeightedUnitTest>();
	private ArrayList<WeightedUnitTest> secretUnitTests = new ArrayList<WeightedUnitTest>();
	private String name;
	private double marks;
	private Date dueDate = new Date();
	private String description;
	private int numSubmissionsAllowed;
	private boolean released = false;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	public void addUnitTest(WeightedUnitTest test){
		unitTests.add(test);
	}
	
	public void removeUnitTest(WeightedUnitTest test){
		unitTests.remove(test);
	}
	
	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
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
	
	public String getSimpleDueDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return sdf.format(dueDate);
	}
	
	public void setSimpleDueDate(String date){
		logger.info(date);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			dueDate = sdf.parse(date.trim());
		} catch (ParseException e) {
			logger.error("Could not parse date "+e);
		}
		logger.info(dueDate);
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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isCompletelyTested(){
		for(WeightedUnitTest test: unitTests){
			if(!test.getTest().isTested()){
				return false;
			}
		}
		
		for(WeightedUnitTest test: secretUnitTests){
			if(!test.getTest().isTested()){
				return false;
			}
		}
		return true;
	}
	
	public boolean isClosed(){
		return (new Date()).after(getDueDate());
	}

	public String toString(){
		String output = "";
		output+="<assessment>" + System.getProperty("line.separator");
		output+="\t<name>"+getName()+"</name>" + System.getProperty("line.separator");
		output+="\t<released>"+isReleased()+"</released>" + System.getProperty("line.separator");
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
		output+="\t<dueDate>"+sdf.format(getDueDate())+"</dueDate>" + System.getProperty("line.separator");
		output+="\t<marks>"+getMarks()+"</marks>" + System.getProperty("line.separator");
		output+="\t<submissionsAllowed>"+getNumSubmissionsAllowed()+"</submissionsAllowed>" + System.getProperty("line.separator");
		output+="\t<description>"+getDescription()+"</description>" + System.getProperty("line.separator");
		if(unitTests.size() + secretUnitTests.size() > 0){
			output+="\t<unitTestSuite>" + System.getProperty("line.separator");
			for(WeightedUnitTest unitTest: unitTests){
				output+="\t\t<unitTest name=\""+ unitTest.getTest().getShortName() + "\" weight=\"" + unitTest.getWeight() + "\">" + System.getProperty("line.separator");
			}
			
			for(WeightedUnitTest unitTest: secretUnitTests){
				output+="\t\t<unitTest name=\""+ unitTest.getTest().getShortName() + "\" weight=\"" + unitTest.getWeight() + "\" secret=\"true\" >" + System.getProperty("line.separator");
			}
			output+="\t</unitTestSuite>" + System.getProperty("line.separator");
		}
		// TODO handMarks
		// TODO all competitions
		output+="</assessment>" + System.getProperty("line.separator");
		return output;
	}
}