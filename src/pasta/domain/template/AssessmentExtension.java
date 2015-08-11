package pasta.domain.template;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pasta.domain.user.PASTAUser;

@Entity
@Table(name="assessment_extensions")
public class AssessmentExtension  implements Serializable, Comparable<AssessmentExtension> {
	private static final long serialVersionUID = -1323359810061509591L;

	public AssessmentExtension() {}
	
	public AssessmentExtension(PASTAUser user, Assessment assessment, Date newDueDate) {
		this.user = user;
		this.assessment = assessment;
		this.newDueDate = newDueDate;
	}
	@Id
	@GeneratedValue
	private long id;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="user", nullable=false)
	private PASTAUser user;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="assessment", nullable=false)
	private Assessment assessment;
	
	@Column(name="new_due_date")
	private Date newDueDate;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public PASTAUser getUser() {
		return user;
	}
	public void setUser(PASTAUser user) {
		this.user = user;
	}

	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}

	public Date getNewDueDate() {
		return newDueDate;
	}
	public void setNewDueDate(Date newDueDate) {
		this.newDueDate = newDueDate;
	}

	@Override
	public int compareTo(AssessmentExtension o) {
		int diff = this.assessment.compareTo(o.assessment);
		if(diff != 0) {
			return diff;
		}
		diff = this.user.compareTo(o.user);
		if(diff != 0) {
			return diff;
		}
		return this.newDueDate.compareTo(o.newDueDate);
	}
}
