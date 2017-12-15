/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.domain.user;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.UserPermissionLevel;
import pasta.domain.VerboseName;
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
@VerboseName("assessment group")
public class PASTAGroup extends PASTAUser implements Comparable<PASTAUser> {
	private static final long serialVersionUID = -5991364679026799465L;

	@ManyToMany
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinTable(name="assessment_group_members", 
			joinColumns=@JoinColumn(name = "assessment_group_id"),
			inverseJoinColumns=@JoinColumn(name = "user_id"))
	private Set<PASTAUser> members;
	
	@ManyToOne
	@JoinColumn(name="assessment_id")
	private Assessment assessment;
	
	private int number;
	
	private boolean locked;
	
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

	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
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
	
	public String getName() {
		return assessment.getName() + " - Group " + number; 
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
