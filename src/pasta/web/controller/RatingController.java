/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.web.controller;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.domain.ratings.AssessmentRating;
import pasta.domain.ratings.RatingForm;
import pasta.domain.template.Assessment;
import pasta.service.AssessmentManager;
import pasta.service.RatingManager;
import pasta.service.UserManager;

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


	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////
//	@ModelAttribute("ratingForm")
//	public RatingForm loadRatingForm() {
//		Assessment ass = assessmentManager.getAssessment(3);
//		String username = "josh";
//		AssessmentRating rating = ratingManager.getRating(ass, username);
//		if(rating == null) {
//			rating = new AssessmentRating(ass, username);
//		}
//		return new RatingForm(rating);
//	}
	

	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Get the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in or user isn't registered.
	 */
	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return getUser(username);
	}
	
	/**
	 * Get the user given a username
	 * 
	 * @param username the username of the user
	 * @return the user, null if the username is null or user isn't registered.
	 */
	public PASTAUser getUser(String username) {
		if (username != null) {
			return userManager.getUser(username);
		}
		return null;
	}


	// ///////////////////////////////////////////////////////////////////////////
	// RATINGS //
	// ///////////////////////////////////////////////////////////////////////////
	
	@RequestMapping("/ratingStatus")  
	@ResponseBody  
	public String reportStatus(HttpSession session) {  
		@SuppressWarnings("unchecked")
		Future<AssessmentRating> rating = (Future<AssessmentRating>) session.getAttribute("rating");
		logger.warn("Checking status | Rating: " + rating);
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
	
	
	@RequestMapping(value = "/saveRating/{username}/{assessmentId}/", method = RequestMethod.POST)
	@ResponseBody
	public void saveRating(
			@PathVariable("username") String username, 
			@PathVariable("assessmentId") long assessmentId,
			@ModelAttribute("ratingForm") RatingForm form,
			HttpSession session) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		Future<AssessmentRating> rating = ratingManager.saveRating(assessment, username, form);
		session.setAttribute("rating", rating);
	}
	
}