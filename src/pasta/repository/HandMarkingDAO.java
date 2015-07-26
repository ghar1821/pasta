package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedField;
import pasta.domain.template.WeightedHandMarking;

@Transactional
@Repository("handMarkingDAO")
public class HandMarkingDAO {

	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void saveOrUpdate(HandMarking template) {
		long id = template.getId();
		sessionFactory.getCurrentSession().saveOrUpdate(template);
		logger.info((id == template.getId() ? "Updated" : "Created") +
				" hand marking template " + template.getName());
	}
	
	public void delete(HandMarking template) {
		sessionFactory.getCurrentSession().delete(template);
		logger.info("Deleted hand marking template " + template.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<HandMarking> getAllHandMarkings() {
		return sessionFactory.getCurrentSession().createCriteria(HandMarking.class).list();
	}

	public HandMarking getHandMarking(long id) {
		return (HandMarking) sessionFactory.getCurrentSession().get(HandMarking.class, id);
	}
	
	public WeightedField getWeightedField(long id) {
		return (WeightedField) sessionFactory.getCurrentSession().get(WeightedField.class, id);
	}
	
	public WeightedHandMarking getWeightedHandMarking(long id) {
		return (WeightedHandMarking) sessionFactory.getCurrentSession().get(WeightedHandMarking.class, id);
	}
	
	/**
	 * Create and store a new blank row/column
	 * 
	 * @return the newly created row/column
	 */
	public WeightedField createNewWeightedField() {
		long newId = (long) sessionFactory.getCurrentSession().save(new WeightedField());
		logger.info("Created new weighted field with ID " + newId);
		return getWeightedField(newId);
	}
}
