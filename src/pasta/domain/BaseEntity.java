package pasta.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.apache.log4j.Logger;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
public abstract class BaseEntity {

	@Transient
	protected transient final Logger logger = Logger.getLogger(getClass());
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Version
	private Long version;
	
	@Column(name = "created_at")
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date created;
	
	@Column(name = "modified_at")
	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	private Date modified;
	
	@Column(name = "modified_by", length = 50)
	@Size(max = 50)
	@ModifiedBy
	private String modifiedBy;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getVersion() {
		return version;
	}
	protected void setVersion(Long version) {
		this.version = version;
	}
	
	public Date getCreated() {
		return created;
	}
	protected void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}
	protected void setModified(Date modified) {
		this.modified = modified;
	}
	
	public String getModifiedBy() {
		return modifiedBy;
	}
	protected void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " #" + getId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseEntity other = (BaseEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
