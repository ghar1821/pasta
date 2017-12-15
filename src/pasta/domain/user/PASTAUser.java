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

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import pasta.domain.BaseEntity;
import pasta.domain.UserPermissionLevel;
import pasta.domain.VerboseName;
import pasta.domain.ratings.AssessmentRating;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.AssessmentResultSummary;
import pasta.domain.template.AssessmentExtension;
import pasta.scheduler.AssessmentJob;

/**
 * @author Alex Radu
 * @version 2.0
 * @since 2012-10-12
 */
@Entity
@Table(name = "users",uniqueConstraints={@UniqueConstraint(columnNames={"username"})})
@Inheritance(strategy=InheritanceType.JOINED)
@VerboseName("user")
public class PASTAUser extends BaseEntity implements Serializable, Comparable<PASTAUser>{

	private static final long serialVersionUID = -9070027568016757820L;
	
	@Column(name = "username", nullable = false)
	private String username = "";
	
	@Column(name = "tutorial", nullable = false)
	private String tutorial = "";
	
	@Column(name = "stream", nullable = false)
	private String stream = "";
	
	@Column(name = "permission_level", nullable = false)
	@Enumerated(EnumType.STRING)
	private UserPermissionLevel permissionLevel;
	
	@Column(name="active")
	private boolean active = true;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		if(username == null){
			username = "";
		}
		this.username = username.trim();
	}
	
	public String getTutorial() {
		return tutorial;
	}
	public void setTutorial(String tutorial) {
		if(tutorial == null){
			tutorial = "";
		}
		this.tutorial = tutorial.trim();
	}
	
	public String getStream() {
		return stream;
	}
	public void setStream(String stream) {
		if(stream == null){
			stream = "";
		}
		this.stream = stream.trim();
	}
	
	public UserPermissionLevel getPermissionLevel() {
		return permissionLevel;
	}
	public void setPermissionLevel(UserPermissionLevel permissionLevel) {
		this.permissionLevel = permissionLevel;
	}
	
	public String getFullTutorial() {
		String tutorial = getTutorial() == null ? "" : getTutorial();
		if(tutorial.isEmpty()){
			return tutorial;
		}
		String full = "";
		String stream = getStream() == null ? "" : getStream();
		if(!stream.isEmpty()) {
			full += this.getStream();
		}
		if(!full.isEmpty()) {
			full += ".";
		}
		full += tutorial;
		return full;
	}
	
	public boolean isTutor(){
		return (permissionLevel == UserPermissionLevel.TUTOR) 
				|| permissionLevel == UserPermissionLevel.INSTRUCTOR; 
	}
	
	public boolean isInstructor(){
		return permissionLevel == UserPermissionLevel.INSTRUCTOR; 
	}
	
	public boolean isGroup() {
		return permissionLevel == UserPermissionLevel.GROUP;
	}
	
	public String[] getTutorClasses(){
		if ((permissionLevel == UserPermissionLevel.TUTOR)  
				|| permissionLevel == UserPermissionLevel.INSTRUCTOR){
			return tutorial.split(",");
		}
		return new String[0];
	}
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean equals(PASTAUser other){
		if(other == null) {
			return false;
		}
		return getUsername().equals(other.getUsername());
	}
	
	@Override
	public int compareTo(PASTAUser other) {
		return getUsername().compareTo(other.getUsername());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}
	
	@Override
	public String toString() {
		return username;
	}
	
	/*===========================
	 * CONVENIENCE RELATIONSHIPS
	 * 
	 * Making unidirectional many-to-one relationships into bidirectional 
	 * one-to-many relationships for ease of deletion by Hibernate
	 *===========================
	 */
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentRating> ratings;
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentExtension> extensions;
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentResult> results;
	@OneToMany(mappedBy = "id.user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentResultSummary> summaries;
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentJob> jobs;
}
