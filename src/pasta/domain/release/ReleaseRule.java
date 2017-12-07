package pasta.domain.release;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.util.ProjectProperties;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ReleaseRule implements Comparable<ReleaseRule>, Serializable {
	private static final long serialVersionUID = -2751801889498306673L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;
	
	protected abstract boolean isMet(PASTAUser user);
	
	public boolean isReleased(PASTAUser user, Assessment assessment) {
		// If they have ever submitted, it is released.
		if(ProjectProperties.getInstance().getResultDAO()
				.getLastestSubmission(user, assessment) != null) {
			return true;
		}
		return isMet(user);
	}
	
	public abstract String getShortDescription();
	public abstract String getDescription();
	
	@Override
	public int compareTo(ReleaseRule other) {
		return this.getShortDescription().compareTo(other.getShortDescription());
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return getClass().getName();
	}
}
