package pasta.web.controller;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pasta.domain.ratings.AssessmentRating;
import pasta.domain.ratings.RatingForm;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.RatingManager;
import pasta.service.UserManager;
import pasta.web.WebUtils;

/**
 * Controller class for Rating functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/rating/...
 * 
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-13
 *
 */
@Controller
@RequestMapping("rating/")
public class RatingController {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private UserManager userManager;
	@Autowired
	private RatingManager ratingManager;
	@Autowired
	private AssessmentManager assessmentManager;

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// RATINGS //
	// ///////////////////////////////////////////////////////////////////////////
	
	//This is an example of how to get the results of an async task
	@RequestMapping("/ratingStatus")  
	@ResponseBody  
	public String reportStatus(HttpSession session) {  
		@SuppressWarnings("unchecked")
		Future<AssessmentRating> rating = (Future<AssessmentRating>) session.getAttribute("rating");
		if(rating == null || !rating.isDone()) {
			return "WORKING";
		}
		try {
			return rating.get().toString();
		} catch (InterruptedException e) {
			logger.error("Error checking for rating", e);
		} catch (ExecutionException e) {
			logger.error("Error checking for rating", e);
		}
		return "ERROR";
	}  
	
	@RequestMapping(value = "/saveRating/{ratingUsername}/{assessmentId}/", method = RequestMethod.POST)
	@ResponseBody
	public void saveRating(
			@PathVariable("ratingUsername") String username, 
			@PathVariable("assessmentId") long assessmentId,
			@ModelAttribute("ratingForm") RatingForm form,
			HttpSession session) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		Future<AssessmentRating> rating = ratingManager.saveRating(assessment, userManager.getUser(username), form);
		session.setAttribute("rating", rating);
	}
	
}