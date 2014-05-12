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

@Service("handMarkingManager")
@Repository
/**
 * Submission amnager.
 * 
 * Manages interaction between controller and data.
 * 
 * @author Alex
 *
 */
public class HandMarkingManager {
	
	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(HandMarkingManager.class);
	
	// new
	public Collection<HandMarking> getHandMarkingList() {
		return assDao.getHandMarkingList();
	}

	// new
	public HandMarking getHandMarking(String handMarkingName) {
		return assDao.getHandMarking(handMarkingName);
	}
	
	// new
	public Collection<HandMarking> getAllHandMarking() {
		return assDao.getHandMarkingList();
	}

	public void updateHandMarking(HandMarking marking){
		assDao.updateHandMarking(marking);
	}

	public void newHandMarking(NewHandMarking newMarking){
		assDao.newHandMarking(newMarking);
	}
	
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

	public void removeHandMarking(String handMarkingName) {
		assDao.removeHandMarking(handMarkingName);
		// delete file
		try {
			FileUtils.deleteDirectory(new File(ProjectProperties.getInstance().getProjectLocation()
					+ "/template/handMarking/"
					+ handMarkingName));
		} catch (IOException e) {}
	}

	public void saveComment(String username, String assessmentName,
			String assessmentDate, String comments) {
		// make that better
		resultDAO.saveHandMarkingComments(username, assessmentName, assessmentDate, comments);
	}

	public void updateComment(String username, String assessmentName,
			String newComment) {
		// TODO Auto-generated method stub
		
	}
}
