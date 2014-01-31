package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.FileTreeNode;
import pasta.domain.PASTAUser;
import pasta.domain.UserPermissionLevel;
import pasta.domain.form.ReleaseForm;
import pasta.domain.result.ArenaResult;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.CompetitionResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewCompetition;
import pasta.domain.upload.NewHandMarking;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.repository.AssessmentDAO;
import pasta.repository.LoginDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UserDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.Job;
import pasta.util.PASTAUtil;
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
	
	private AssessmentDAO assDao = new AssessmentDAO();
	private ResultDAO resultDAO = new ResultDAO(assDao);
	
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
