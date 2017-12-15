package pasta.scheduler;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Job implements Serializable {

	private static final long serialVersionUID = 2058301754166837748L;
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(name = "run_date", nullable = false)
	private Date runDate;
	
	public Job(){}
	
	public Job(Date runDate) {
		this.runDate = runDate;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Date getRunDate() {
		return runDate;
	}
	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}
}
