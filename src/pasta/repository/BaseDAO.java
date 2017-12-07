package pasta.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.ReplicationMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.archive.ArchivableBaseEntity;
import pasta.archive.MappableClass;
import pasta.archive.PreviousArchive;
import pasta.domain.BaseEntity;

@Transactional
@Repository("baseDAO")
public class BaseDAO {

	protected final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	protected SessionFactory sessionFactory;
	
	public void saveOrUpdate(BaseEntity entity) {
		Long id = entity.getId();
		sessionFactory.getCurrentSession().saveOrUpdate(entity);
		logger.info((id == entity.getId() ? "Updated " : "Created ") + entity.toString());
	}
	
	public Long save(BaseEntity entity) {
		sessionFactory.getCurrentSession().save(entity);
		logger.info("Created " + entity.toString());
		return entity.getId();
	}
	
	public void update(BaseEntity entity) {
		sessionFactory.getCurrentSession().update(entity);
		logger.info("Updated " + entity.toString());
	}
	
	public void merge(BaseEntity entity) {
		sessionFactory.getCurrentSession().merge(entity);
		logger.info("Merged " + entity.toString());
	}
	
	public void evict(BaseEntity entity) {
		sessionFactory.getCurrentSession().evict(entity);
		logger.info("Evicted " + entity.toString());
	}
	
	public void replicate(BaseEntity entity) {
		sessionFactory.getCurrentSession().replicate(entity, ReplicationMode.IGNORE);
		logger.info("Replicated " + entity.toString());
	}
	
	public void delete(BaseEntity entity) {
		if(entity instanceof ArchivableBaseEntity) {
			Criteria cr = sessionFactory.getCurrentSession().createCriteria(PreviousArchive.class);
			cr.add(Restrictions.eq("itemClass", new MappableClass(((ArchivableBaseEntity)entity).getClass())));
			cr.add(Restrictions.eq("mappedId", entity.getId()));
			@SuppressWarnings("unchecked")
			List<PreviousArchive> archiveMappings = cr.list();
			if(archiveMappings != null && !archiveMappings.isEmpty()) {
				logger.info("Deleting " + archiveMappings.size() + " archive record" + (archiveMappings.size() == 1 ? "" : "s"));
				for(PreviousArchive pa : archiveMappings) {
					delete(pa);
				}
			}
		}
		
		String desc = entity.toString();
		sessionFactory.getCurrentSession().delete(entity);
		logger.info("Deleted " + desc);
	}
	
	public BaseEntity get(Class<? extends BaseEntity> clazz, Long id) {
		return (BaseEntity) sessionFactory.getCurrentSession().get(clazz, id);
	}
}
