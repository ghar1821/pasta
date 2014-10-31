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

package pasta.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.result.AssessmentResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.template.HandMarking;
import pasta.domain.upload.NewHandMarking;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.util.ProjectProperties;


/**
 * Hand marking manager.
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-31
 *
 */
@Service("handMarkingManager")
@Repository
public class HandMarkingManager {
	
	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(HandMarkingManager.class);
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.AssessmentDAO#getHandMarkingList()
	 * @return collection of all of the hand marking templates
	 */
	public Collection<HandMarking> getHandMarkingList() {
		return assDao.getHandMarkingList();
	}

	/**
	 * Helper method
	 * 
	 * @param handMarkingName the short name (no whitespace) of the hand marking template
	 * @see pasta.repository.AssessmentDAO#getHandMarking(String)
	 * @return the hand marking template with that name, null if it does not exist
	 */
	public HandMarking getHandMarking(String handMarkingName) {
		return assDao.getHandMarking(handMarkingName);
	}
	
	/**
	 * Helper method
	 * 
	 * @param marking the hand marking template that will be updated
	 * @see pasta.repository.AssessmentDAO#updateHandMarking(HandMarking)
	 */
	public void updateHandMarking(HandMarking marking){
		assDao.updateHandMarking(marking);
	}

	/**
	 * New hand marking template
	 * 
	 * @param newMarking the new hand marking form
	 * @see pasta.repository.AssessmentDAO#newHandMarking(NewHandMarking)
	 */
	public void newHandMarking(NewHandMarking newMarking){
		assDao.newHandMarking(newMarking);
	}
	
	/**
	 * Save hand marking results.
	 * 
	 * @param username the name of the user
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @param assessmentDate the date of the assessment (format yyyy-MM-dd'T'HH-mm-ss)
	 * @param handMarkingResults the list of hand marking results to save
	 */
	public void saveHandMarkingResults(String username, String assessmentName,
			String assessmentDate, List<HandMarkingResult> handMarkingResults) {
		AssessmentResult result = resultDAO.getAsssessmentResult(username, assDao.getAssessment(assessmentName), assessmentDate);
		// save to memory
		if(result != null){
			result.setHandMarkingResults(handMarkingResults);
			// save to file
			resultDAO.saveHandMarkingToFile(username, assessmentName, assessmentDate, handMarkingResults);
		}
	}

	/**
	 * Remove a hand marking template
	 * 
	 * @param handMarkingName the short name (no whitespace) of the hand marking template
	 */
	public void removeHandMarking(String handMarkingName) {
		assDao.removeHandMarking(handMarkingName);
		// delete file
		try {
			FileUtils.deleteDirectory(new File(ProjectProperties.getInstance().getProjectLocation()
					+ "/template/handMarking/"
					+ handMarkingName));
		} catch (IOException e) {}
	}

	/**
	 * Save the comment
	 * 
	 * @param username the name of the user
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @param assessmentDate the date of assessment submission (format yyyy-MM-dd'T'HH-mm-ss)
	 * @param comments the comments that will be saved
	 */
	public void saveComment(String username, String assessmentName,
			String assessmentDate, String comments) {
		// make that better
		resultDAO.saveHandMarkingComments(username, assessmentName, assessmentDate, comments);
	}
}
