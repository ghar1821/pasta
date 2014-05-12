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
public class PASTAUser implements Serializable{
	private String username;
	private String tutorial;
	private String stream;
	private UserPermissionLevel permissionLevel;
	private Map<String, Date> extensions = new TreeMap<String, Date>();
	
	@Id
	@Column(name = "USERNAME", nullable = false)
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Column(name = "TUTORIAL", nullable = false)
	public String getTutorial() {
		return tutorial;
	}
	public void setTutorial(String tutorial) {
		this.tutorial = tutorial;
	}
	
	@Column(name = "STREAM", nullable = false)
	public String getStream() {
		return stream;
	}
	public void setStream(String stream) {
		this.stream = stream;
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
	
}
