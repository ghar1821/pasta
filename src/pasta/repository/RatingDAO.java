/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
