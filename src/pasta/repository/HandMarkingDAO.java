package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.template.HandMarkData;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedField;

@Repository("handMarkingDAO")
public class HandMarkingDAO extends HibernateDaoSupport {

	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	public void saveOrUpdate(HandMarking template) {
		long id = template.getId();
		getHibernateTemplate().saveOrUpdate(template);
		logger.info((id == template.getId() ? "Updated" : "Created") +
				" hand marking template " + template.getName());
	}
	
	public void delete(HandMarking template) {
		getHibernateTemplate().delete(template);
		logger.info("Deleted hand marking template " + template.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<HandMarking> getAllHandMarkings() {
		return getHibernateTemplate().find("FROM HandMarking");
	}
	
	@Deprecated
	public HandMarking getHandMarking(String name) {
		@SuppressWarnings("unchecked")
		List<HandMarking> results = getHibernateTemplate().find("FROM HandMarking WHERE name = ?", name);
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	public HandMarking getHandMarking(long id) {
		return getHibernateTemplate().get(HandMarking.class, id);
	}
	
	public WeightedField getWeightedField(long id) {
		return getHibernateTemplate().get(WeightedField.class, id);
	}
	
	/**
	 * Create and store a new blank row/column
	 * 
	 * @return the newly created row/column
	 */
	public WeightedField createNewWeightedField() {
		long newId = (long) getHibernateTemplate().save(new WeightedField());
		logger.info("Created new weighted field with ID " + newId);
		return getWeightedField(newId);
	}
	
	@Deprecated
	public HandMarkData getData(long handMarkingId, long columnId, long rowId) {
		@SuppressWarnings("unchecked")
		List<HandMarkData> results = getHibernateTemplate()
				.find("FROM HandMarkData WHERE hand_marking_id = ? AND column_id = ? AND row_id = ?",
				handMarkingId, columnId, rowId);
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
}
