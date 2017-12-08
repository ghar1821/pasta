package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.ratings.AssessmentRating;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;

@Transactional
@Repository("ratingDAO")
public class RatingDAO extends BaseDAO {

	protected final Log logger = LogFactory.getLog(getClass());

	@SuppressWarnings("unchecked")
	public List<AssessmentRating> getAllRatings() {
		return sessionFactory.getCurrentSession().createCriteria(AssessmentRating.class).list();
	}

	public AssessmentRating getRating(Assessment assessment, PASTAUser user) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentRating.class);
		cr.add(Restrictions.eq("assessment", assessment));
		cr.add(Restrictions.eq("user", user));
		@SuppressWarnings("unchecked")
		List<AssessmentRating> results = cr.list();
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public List<AssessmentRating> getAllRatingsForAssessment(Assessment assessment) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentRating.class);
		cr.add(Restrictions.eq("assessment", assessment));
		return cr.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<AssessmentRating> getAllRatingsForAssessment(long assessmentId) {
		Criteria cr = sessionFactory.getCurrentSession()
				.createCriteria(AssessmentRating.class)
				.createCriteria("assessment")
					.add(Restrictions.eq("id", assessmentId));
		return cr.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<AssessmentRating> getAllRatingsForUser(PASTAUser user) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentRating.class);
		cr.add(Restrictions.eq("user", user));
		return cr.list();
	}
	
	public AssessmentRating getRating(long id) {
		return (AssessmentRating) sessionFactory.getCurrentSession().get(AssessmentRating.class, id);
	}
}
