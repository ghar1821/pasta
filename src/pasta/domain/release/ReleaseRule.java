package pasta.domain.release;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import pasta.domain.PASTAUser;
import pasta.domain.template.Assessment;
import pasta.util.ProjectProperties;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ReleaseRule implements Comparable<ReleaseRule> {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private long id;
	
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

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return getClass().getName();
	}
}
