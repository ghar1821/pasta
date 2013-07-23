package pasta.scheduler;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Jobs")
public class Job implements Serializable{
	private Integer id;
	private String username;
	private String assessmentName;
	private Date runDate;
	
	public Job(){}
	
	public Job(String username, String assessmentName, Date runDate){
		this.username = username;
		this.assessmentName = assessmentName;
		this.runDate = runDate;
	}
	
	@Id
	@GeneratedValue
	@Column(name = "ID", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column(name = "USERNAME", nullable = false)
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Column(name = "ASSESSMENT_NAME", nullable = false)
	public String getAssessmentName() {
		return assessmentName;
	}
	public void setAssessmentName(String assessmentName) {
		this.assessmentName = assessmentName;
	}
	
	@Column(name = "RUN_DATE", nullable = false)
	public Date getRunDate() {
		return runDate;
	}
	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}
}
