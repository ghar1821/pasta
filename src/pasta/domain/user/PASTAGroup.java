package pasta.domain.user;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.UserPermissionLevel;
import pasta.domain.template.Assessment;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 10 Jul 2015
 *
 */
@Entity
@Table(	name = "assessment_groups", 
		uniqueConstraints = {
				@UniqueConstraint(columnNames={"assessment_id", "number"})
		})
public class PASTAGroup extends PASTAUser implements Comparable<PASTAUser> {
	private static final long serialVersionUID = -5991364679026799465L;

	@OneToMany
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinTable(name="assessment_group_members", 
			joinColumns=@JoinColumn(name = "assessment_group_id"),
			inverseJoinColumns=@JoinColumn(name = "user_id"))
	private Set<PASTAUser> members;
	
	@ManyToOne
	@JoinColumn(name="assessment_id")
	private Assessment assessment;
	
	private int number;
	
	public PASTAGroup() {
		super();
		super.setPermissionLevel(UserPermissionLevel.GROUP);
		members = new TreeSet<PASTAUser>();
		refreshUsername();
	}
	
	private void refreshUsername() {
		super.setUsername("grp_" + (assessment == null ? 0 : assessment.getId()) + "_" + number);
	}

	public Set<PASTAUser> getMembers() {
		return members;
	}
	public void setMembers(Set<PASTAUser> members) {
		this.members = members;
	}
	
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
		refreshUsername();
	}

	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
		refreshUsername();
	}

	public boolean addMember(PASTAUser member) {
		return members.add(member);
	}
	
	public boolean removeMember(PASTAUser member) {
		// cannot delegate to members.remove(member)
		// (and I don't know why...)
		Iterator<PASTAUser> it = members.iterator();
		while(it.hasNext()) {
			if(it.next().equals(member)) {
				it.remove();
				return true;
			}
		}
		return false;
	}
	
	public void removeAllMembers() {
		members.clear();
	}

	public boolean isMember(PASTAUser member) {
		// cannot delegate to members.contains(member)
		// (and I don't know why...)
		for(PASTAUser user : members) {
			if(user.equals(member)) {
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty() {
		return members.isEmpty();
	}
	
	public boolean isFull() {
		if(assessment == null) {
			return true;
		}
		return !assessment.isUnlimitedGroupSize() && 
				getSize() >= assessment.getGroupSize();
	}

	public int getSize() {
		return members.size();
	}

	@Override
	public int compareTo(PASTAUser o) {
		if(o instanceof PASTAGroup) {
			PASTAGroup oGroup = (PASTAGroup) o;
			int diff = this.getAssessment().compareTo(oGroup.getAssessment());
			if(diff != 0) {
				return diff;
			}
			diff = this.number - oGroup.number;
			if(diff != 0) {
				return diff;
			}
		}
		return super.compareTo(o);
	}

	/**
	 * This method will do nothing. All groups have a permission level of UserPermissionLevel.GROUP.
	 */
	@Override
	public final void setPermissionLevel(UserPermissionLevel toBeIgnored) { }
	
	/**
	 * This method will do nothing. All groups have a username of grp_{assessmentID}_{number}.
	 */
	@Override
	public final void setUsername(String toBeIgnored) { }
}
