package pasta.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.release.ReleaseRule;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Transactional
@Repository("releaseDAO")
public class ReleaseDAO {
protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public ReleaseRule getReleaseRule(long id) {
		return (ReleaseRule) sessionFactory.getCurrentSession().get(ReleaseRule.class, id);
	}
	
	public void delete(ReleaseRule rule) {
		sessionFactory.getCurrentSession().delete(rule);
	}
	
	public ReleaseRule getReleaseRuleForAssessment(long assessmentId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(ReleaseRule.class);
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		return null;
	}
}
