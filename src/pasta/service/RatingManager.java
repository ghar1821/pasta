package pasta.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.ratings.AssessmentRating;
import pasta.domain.ratings.RatingForm;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.repository.RatingDAO;
import pasta.web.WebUtils;

/**
 * Rating manager.
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 *
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-13
 *
 */
@Service("ratingManager")
@Repository
public class RatingManager {
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private RatingDAO ratingDAO;

	public static final Logger logger = Logger
			.getLogger(RatingManager.class);
	
	public AssessmentRating getRating(Assessment assessment, PASTAUser user) {
		return ratingDAO.getRating(assessment, user);
	}
	
	@Async
	public Future<AssessmentRating> loadRating(Assessment assessment, PASTAUser user) {
		// This method is for test purposes
		try {
			Thread.sleep(5000);
		} catch( Exception e) {
			logger.error("Error sleeping", e);
		}
		
		return new AsyncResult<AssessmentRating>(ratingDAO.getRating(assessment, user));
	}
	
	@Async
	public Future<AssessmentRating> saveRating(Assessment assessment, PASTAUser user, RatingForm form) {
		AssessmentRating rating = ratingDAO.getRating(assessment, user);
		if(rating == null) {
			rating = new AssessmentRating(assessment, user);
		}
		rating.setRating(form.getRating());
		rating.setComment(form.getComment());
		
		ratingDAO.saveOrUpdate(rating);
		
		return new AsyncResult<AssessmentRating>(rating);
	}

	public Map<Long, AssessmentRating> getRatingsForUser(PASTAUser user) {
		Map<Long, AssessmentRating> ratings = new HashMap<Long, AssessmentRating>();
		for(AssessmentRating rating : ratingDAO.getAllRatingsForUser(user)) {
			ratings.put(rating.getAssessment().getId(), rating);
		}
		return ratings;
	}

	public List<AssessmentRating> getRatingsForAssessment(Assessment assessment) {
		return ratingDAO.getAllRatingsForAssessment(assessment);
	}

	public void deleteAllRatingsForAssessment(long assessmentId) {
		for(AssessmentRating rating : ratingDAO.getAllRatingsForAssessment(assessmentId)) {
			ratingDAO.delete(rating);
		}
	}
}
