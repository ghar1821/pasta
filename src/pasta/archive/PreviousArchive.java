package pasta.archive;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;

@Entity
@Table (name = "previous_archives")
@VerboseName("previous archive")
public class PreviousArchive extends BaseEntity {

	private static final long serialVersionUID = -1682361495898830796L;

	@Column(name = "item_class")
	private MappableClass itemClass;
	
	@Column(name = "item_id")
	private Long itemId;
	
	@Column(name = "item_version")
	private Long itemVersion;
	
	@Column(name = "instance_id")
	private Long instanceId;
	
	@Column(name = "mapped_id")
	private Long mappedId;
	
	public PreviousArchive() {}
	
	public PreviousArchive(ArchiveEntry archiveItem) throws InvalidPreviousArchiveException {
		try {
			ArchivableBaseEntity base = archiveItem.getData();
			this.itemClass = new MappableClass(base.getClass());
			this.itemId = ((BaseEntity) base).getId();
			this.itemVersion = ((BaseEntity) base).getVersion();
		} catch (ClassCastException e) {
			throw new InvalidPreviousArchiveException("PreviousArchive object can only be created from an object extending BaseEntity.", e);
		}
		this.instanceId = archiveItem.getInstanceId();
		this.mappedId = null;
	}
	
	public PreviousArchive(ArchiveEntry archiveItem, Long mappedId) throws InvalidPreviousArchiveException {
		this(archiveItem);
		this.mappedId = mappedId;
	}
	
	public Class<?> getItemClass() {
		return itemClass.getBaseClass();
	}
	public void setItemClass(Class<? extends ArchivableBaseEntity> itemClass) {
		this.itemClass = new MappableClass(itemClass);
	}

	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getItemVersion() {
		return itemVersion;
	}
	public void setItemVersion(Long itemVersion) {
		this.itemVersion = itemVersion;
	}

	public Long getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(Long instanceId) {
		this.instanceId = instanceId;
	}
	
	public Long getMappedId() {
		return mappedId;
	}
	public void setMappedId(Long mappedId) {
		this.mappedId = mappedId;
	}
}
