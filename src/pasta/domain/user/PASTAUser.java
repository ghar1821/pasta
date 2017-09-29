/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import pasta.domain.UserPermissionLevel;
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
public class PASTAUser implements Serializable, Comparable<PASTAUser>{

	private static final long serialVersionUID = -9070027568016757820L;
	
	@Id
	@GeneratedValue
	private Long id;
	
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
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

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
