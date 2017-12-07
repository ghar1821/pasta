package pasta.archive;

import javax.persistence.MappedSuperclass;

import pasta.domain.BaseEntity;

@MappedSuperclass
public abstract class ArchivableBaseEntity extends BaseEntity {
	private static final long serialVersionUID = -5086965820755892971L;
	
	public void prepareForArchive(ArchiveOptions options) {}
	public void rebuildFromArchive(Archive archive, ArchivableBaseEntity existing) {
		if(existing == null) {
			this.setId(null);
		} else {
			this.setId(existing.getId());
			this.setVersion(existing.getVersion());
		}
	}
}
