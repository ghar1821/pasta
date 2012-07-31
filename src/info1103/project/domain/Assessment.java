package info1103.project.domain;

import java.util.Date;

/**
 * Holder class for assessment information.
 * 
 * @author Alex
 *
 */
public class Assessment {
	// name of the assessment
	private String name;
	
	// the instructions in raw html
	private String instructions;
	// the due date of the assessment.
	private Date dueDate;
	
	//////////////////////////////////////////////////
	// 			SHOULD ORGANISE THIS BETTER			//
	//////////////////////////////////////////////////
	
	// the result (null, "Processing", "Did not compile", 
	// "Number of tests: XX; passed: XX; failed: XX; errors: XX"
	private String result;
	// the feedback
	private String feedback;
	// the junit table generated
	private String junitTable;
	// the submissionDate (null if not submitted yet)
	private Date submissionDate;
	
	// the percentage
	private String percentage;
	
	//////////////////////////////////////////////////	
	
	// the weight of the assessment
	private Double weight;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInstructions() {
		return instructions;
	}
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public boolean isPastDueDate(){
		return (new Date()).after(dueDate);
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	/**
	 * Method to get the color code 
	 * @return the color code.
	 */
	public String getColorCode(){
		if(result == null || result.equals("No submission")){
			return "";
		}
		else if(result.equals("Compilation successful, queued for testing.")){
			return "background-color:gold;";
		}
		else if(result.equals("Did not compile")){
			return "background-color:red;";
		}
		else{
			int sred = 255;
			int sgreen = 255;
			int sblue = 255;
			
			int ered = 0;
			int egreen = 204;
			int eblue = 0;
			
			return "background-color:rgb(" + 
			(sred + (int)(Double.parseDouble(percentage)*(ered-sred))) + "," +
			(sgreen + (int)(Double.parseDouble(percentage)*(egreen-sgreen))) + "," +
			(sblue + (int)(Double.parseDouble(percentage)*(eblue-sblue))) +
			");";
		}
	}
	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	public Date getSubmissionDate() {
		return submissionDate;
	}
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}
	public String getJunitTable() {
		return junitTable;
	}
	public void setJunitTable(String junitTable) {
		this.junitTable = junitTable;
	}
	public String getPercentage() {
		return percentage;
	}
	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	/**
	 * Get the weighted mark. 
	 * 
	 * If assessment has not been executed correctly, return 0; 
	 * 
	 * weighted mark = percentage * weight;
	 * 
	 * @return weighted mark
	 */
	public double getWeightedMark(){
		try{
			return Double.parseDouble(percentage)*weight;
		}
		catch(NumberFormatException ex){
			return 0;
		}
	}
}
