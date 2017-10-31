package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.options.Option;

@Transactional
@Repository("optionsDAO")
public class OptionsDAO {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private SessionFactory sessionFactory;
	
	public void saveOrUpdate(Option option) {
		sessionFactory.getCurrentSession().saveOrUpdate(option);
	}
	public void delete(Option option) {
		sessionFactory.getCurrentSession().delete(option);
	}
	
	@SuppressWarnings("unchecked")
	public List<Option> getAllOptions() {
		return sessionFactory.getCurrentSession()
				.createCriteria(Option.class)
				.addOrder(Order.asc("key"))
				.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Option> getAllOptionsStartingWith(String prefix) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(Option.class);
		cr.add(Restrictions.like("key", prefix, MatchMode.START));
		cr.addOrder(Order.asc("key"));
		return cr.list();
	}
	
	public Option getOption(String key) {
		return (Option) sessionFactory.getCurrentSession().get(Option.class, key);
	}
	
	public boolean hasOption(String key) {
		return getOption(key) != null;
	}
}
