package pasta.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.archive.ArchivableBaseEntity;
import pasta.archive.ArchiveEntry;
import pasta.archive.InvalidPreviousArchiveException;
import pasta.archive.MappableClass;
import pasta.archive.PreviousArchive;

@Transactional
@Repository("archiveDao")
public class ArchiveDAO extends BaseDAO {

	public ArchivableBaseEntity getPreviouslyMappedEntity(Class<? extends ArchivableBaseEntity> clazz, Long itemId, Long itemVersion, Long instanceId) {
		PreviousArchive pa = getPreviousArchive(clazz, itemId, itemVersion, instanceId);
		return getPreviouslyMappedEntity(pa);
	}
	
	public ArchivableBaseEntity getPreviouslyMappedEntity(PreviousArchive previousArchive) {
		if(previousArchive == null || previousArchive.getMappedId() == null) {
			return null;
		}
		Object mapped = sessionFactory.getCurrentSession().get(previousArchive.getItemClass(), previousArchive.getMappedId());
		try {
			return (ArchivableBaseEntity) mapped;
		} catch (ClassCastException e) {
			logger.warn("Trying to get mapped instance of non-archivable type: " + mapped.getClass().getName());
			return null;
		}
	}
	
	public PreviousArchive getPreviousArchive(Class<? extends ArchivableBaseEntity> clazz, Long itemId, Long itemVersion, Long instanceId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PreviousArchive.class);
		cr.add(Restrictions.eq("itemClass", new MappableClass(clazz)));
		cr.add(Restrictions.eq("itemId", itemId));
		cr.add(Restrictions.eq("itemVersion", itemVersion));
		cr.add(Restrictions.eq("instanceId", instanceId));
		PreviousArchive pa = (PreviousArchive) cr.uniqueResult();
		return pa;
	}
	
	public void savePreviousArchive(ArchiveEntry archiveItem, Long mappedId) {
		ArchivableBaseEntity data = archiveItem.getData();
		PreviousArchive pa = getPreviousArchive((Class<? extends ArchivableBaseEntity>) data.getClass(), data.getId(), data.getVersion(), archiveItem.getInstanceId());
		if(pa == null) {
			try {
				pa = new PreviousArchive(archiveItem, mappedId);
				save(pa);
			} catch (InvalidPreviousArchiveException e) {
				logger.error("Error saving new PreviousArchive", e);
			}
		} else {
			pa.setMappedId(mappedId);
			update(pa);
		}
	}
}
