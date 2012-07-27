package info1103.project.domain;

import java.util.Date;

/**
 * Container class for the execution details.
 * @author Alex
 *
 * Only has getter and setter methods.
 *
 */
public class Execution {
	private String unikey;
	private String assessmentName;
	private Date executionDate;
	
	public Execution(String unikey, String assessmentName, Date executionDate){
		this.unikey = unikey;
		this.assessmentName = assessmentName;
		this.executionDate = executionDate;
	}

	public String getUnikey() {
		return unikey;
	}

	public String getAssessmentName() {
		return assessmentName;
	}

	public Date getExecutionDate() {
		return executionDate;
	}
	
	
}
