package pasta.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.release.ReleaseRule;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Repository("releaseDAO")
public class ReleaseDAO extends HibernateDaoSupport {
protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	public ReleaseRule getReleaseRule(long id) {
		return getHibernateTemplate().get(ReleaseRule.class, id);
	}
	
	public void delete(ReleaseRule rule) {
		getHibernateTemplate().delete(rule);
	}
	
	public ReleaseRule getReleaseRuleForAssessment(long assessmentId) {
		DetachedCriteria cr = DetachedCriteria.forClass(ReleaseRule.class);
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		return null;
	}
}
