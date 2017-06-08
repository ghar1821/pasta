package pasta.domain.reporting;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import pasta.domain.UserPermissionLevel;

@Entity
@Table(name = "reports")
public class Report {
	
	@Id
	private String id;
	
	@Column(nullable = false)
	private String name;
	
	@ElementCollection(targetClass = UserPermissionLevel.class, fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	@CollectionTable (name = "report_permissions", joinColumns=@JoinColumn(name = "report_id"))
	@Column(name = "permission", nullable = false)
	@Fetch (FetchMode.SELECT)
	private Set<UserPermissionLevel> permissionLevels;
	
	public Report() {
		
	}
	
	public Report(String id, String name, UserPermissionLevel... permissions) {
		this.id = id;
		this.name = name;
		this.permissionLevels = new HashSet<>();
		for(UserPermissionLevel permission : permissions) {
			addPermissionLevel(permission);
		}
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Set<UserPermissionLevel> getPermissionLevels() {
		return permissionLevels;
	}

	public void setPermissionLevels(Collection<UserPermissionLevel> permissionLevels) {
		this.permissionLevels.clear();
		if(permissionLevels != null) {
			this.permissionLevels.addAll(permissionLevels);
		}
	}
	
	public boolean addPermissionLevel(UserPermissionLevel permission) {
		return this.permissionLevels.add(permission);
	}
	public boolean removePermissionLevel(UserPermissionLevel permission) {
		return this.permissionLevels.remove(permission);
	}
}
