package pasta.repository;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.BaseEntity;

@Transactional
@Repository("baseDAO")
public class BaseDAO {

	protected final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void saveOrUpdate(BaseEntity entity) {
		Long id = entity.getId();
		sessionFactory.getCurrentSession().saveOrUpdate(entity);
		logger.info((id == entity.getId() ? "Updated" : "Created") +
				" " + entity.getClass().getSimpleName() + " #" + entity.getId());
	}
	
	public Long save(BaseEntity entity) {
		sessionFactory.getCurrentSession().save(entity);
		logger.info("Created " + entity.getClass().getSimpleName() + " #" + entity.getId());
		return entity.getId();
	}
	
	public void update(BaseEntity entity) {
		sessionFactory.getCurrentSession().update(entity);
		logger.info("Updated " + entity.getClass().getSimpleName() + " #" + entity.getId());
	}

	public void delete(BaseEntity entity) {
		Long id = entity.getId();
		sessionFactory.getCurrentSession().delete(entity);
		logger.info("Deleted " + entity.getClass().getSimpleName() + " #" + id);
	}
}
