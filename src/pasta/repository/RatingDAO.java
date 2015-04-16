package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.ratings.AssessmentRating;
import pasta.domain.template.Assessment;

@Repository("ratingDAO")
public class RatingDAO extends HibernateDaoSupport {

	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	public AssessmentRating saveOrUpdate(AssessmentRating rating) {
		getHibernateTemplate().saveOrUpdate(rating);
		logger.info("Saved rating for " + rating.getAssessment().getName() + " by " + rating.getUsername());
		return rating;
	}
	
	public void delete(AssessmentRating rating) {
		getHibernateTemplate().delete(rating);
		logger.info("Deleted rating for " + rating.getAssessment().getName() + " by " + rating.getUsername());
	}
	
	public List<AssessmentRating> getAllRatings() {
		return getHibernateTemplate().loadAll(AssessmentRating.class);
	}

	public AssessmentRating getRating(Assessment assessment, String username) {
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentRating.class);
		cr.add(Restrictions.eq("assessment", assessment));
		cr.add(Restrictions.eq("username", username));
		@SuppressWarnings("unchecked")
		List<AssessmentRating> results = getHibernateTemplate().findByCriteria(cr);
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public List<AssessmentRating> getAllRatingsForAssessment(Assessment assessment) {
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentRating.class);
		cr.add(Restrictions.eq("assessment", assessment));
		return getHibernateTemplate().findByCriteria(cr);
	}
	
	@SuppressWarnings("unchecked")
	public List<AssessmentRating> getAllRatingsForUser(String username) {
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentRating.class);
		cr.add(Restrictions.eq("username", username));
		return getHibernateTemplate().findByCriteria(cr);
	}
	
	public AssessmentRating getRating(long id) {
		return getHibernateTemplate().get(AssessmentRating.class, id);
	}
}
