package pasta.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.release.ReleaseAllResultsRule;
import pasta.domain.release.ReleaseResultsRule;
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

	public boolean isAssessmentLinked(long assessmentId) {
		Session session = sessionFactory.getCurrentSession();
		
		Criteria cr = session.createCriteria(ReleaseResultsRule.class)
			.setProjection(Projections.rowCount())
			.createAlias("compareAssessment", "a")
			.add(Restrictions.eq("a.id", assessmentId));
		boolean linked = DataAccessUtils.intResult(cr.list()) > 0;
		if(!linked) {
			cr = session.createCriteria(ReleaseAllResultsRule.class)
				.setProjection(Projections.rowCount())
				.createAlias("compareAssessment", "a")
				.add(Restrictions.eq("a.id", assessmentId));
			linked = DataAccessUtils.intResult(cr.list()) > 0;
		}
		return linked;
	}
}
