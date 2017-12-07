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
	
	public void delete(BaseEntity entity) {
		String desc = entity.toString();
		sessionFactory.getCurrentSession().delete(entity);
		logger.info("Deleted " + desc);
	}
	
	public BaseEntity get(Class<? extends BaseEntity> clazz, Long id) {
		return (BaseEntity) sessionFactory.getCurrentSession().get(clazz, id);
	}
}
