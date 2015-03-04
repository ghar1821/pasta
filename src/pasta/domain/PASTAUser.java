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

package pasta.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "Users")
/**
 * @author Alex Radu
 * @version 2.0
 * @since 2012-10-12
 */
public class PASTAUser implements Serializable, Comparable<PASTAUser>{

	private static final long serialVersionUID = -9070027568016757820L;
	
	private String username = "";
	private String tutorial = "";
	private String stream = "";
	private UserPermissionLevel permissionLevel;
	private Map<String, Date> extensions = new TreeMap<String, Date>();
	
	@Id
	@Column(name = "USERNAME", nullable = false)
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		if(username == null){
			username = "";
		}
		this.username = username.trim();
	}
	
	@Column(name = "TUTORIAL", nullable = false)
	public String getTutorial() {
		return tutorial;
	}
	
	public void setTutorial(String tutorial) {
		if(tutorial == null){
			tutorial = "";
		}
		this.tutorial = tutorial.trim();
	}
	
	@Column(name = "STREAM", nullable = false)
	public String getStream() {
		return stream;
	}
	
	public void setStream(String stream) {
		if(stream == null){
			stream = "";
		}
		this.stream = stream.trim();
	}
	
	@Column(name = "PERMISSION_LEVEL", nullable = false)
	@Enumerated(EnumType.STRING)
	public UserPermissionLevel getPermissionLevel() {
		return permissionLevel;
	}
	
	public void setPermissionLevel(UserPermissionLevel permissionLevel) {
		this.permissionLevel = permissionLevel;
	}
	
	@Transient
	public boolean isTutor(){
		return (permissionLevel == UserPermissionLevel.TUTOR) 
				|| permissionLevel == UserPermissionLevel.INSTRUCTOR; 
	}
	
	@Transient
	public boolean isInstructor(){
		return permissionLevel == UserPermissionLevel.INSTRUCTOR; 
	}
	
	@Transient
	public String[] getTutorClasses(){
		if ((permissionLevel == UserPermissionLevel.TUTOR)  
				|| permissionLevel == UserPermissionLevel.INSTRUCTOR){
			return tutorial.split(",");
		}
		return new String[0];
	}
	
	@Transient
	public Map<String, Date> getExtensions(){
		return extensions;
	}
	
	@Transient
	public void setExtension(Map<String, Date> extensions){
		this.extensions = extensions;
	}
	
	@Transient
	public void giveExtension(String assessmentName, Date newDueDate){
		extensions.put(assessmentName, newDueDate);
	}
	
	@Transient
	public boolean equals(PASTAUser user){
		return (username.equals(user.getUsername()));
	}
	@Override
	public int compareTo(PASTAUser o) {
		return getUsername().trim().compareTo(o.getUsername().trim());
	}
	
}
