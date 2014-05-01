package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.PASTAUser;
import pasta.domain.form.ReleaseForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.Job;
import pasta.util.ProjectProperties;

@Service("assessmentManager")
@Repository
/**
 * Assessment manager.
 * 
 * Manages interaction between controller and data.
 * 
 * @author Alex
 *
 */
public class AssessmentManager {
	
	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	
	private ExecutionScheduler scheduler;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
	}
	
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(AssessmentManager.class);
	
	// new
	public Collection<Assessment> getAssessmentList() {
		return assDao.getAssessmentList();
	}
	
	// new
	public Assessment getAssessment(String assessmentName) {
		return assDao.getAssessment(assessmentName);
	}
	
	// new
	public Collection<AssessmentResult> getAssessmentHistory(String username, String assessmentName){
		return resultDAO.getAssessmentHistory(username, getAssessment(assessmentName));
	}

	public void releaseAssessment(String AssessmentName, ReleaseForm released)
	{
		assDao.releaseAssessment(AssessmentName,released);
		
	}

	public void removeAssessment(String assessment) {
		assDao.removeAssessment(assessment);
	}
	
	// new add assessment
	public void addAssessment(Assessment assessmentToAdd) {
		try {

			// unit Tests
			for (WeightedUnitTest test : assessmentToAdd.getUnitTests()) {
				if (assDao.getAllUnitTests().get(test.getUnitTestName().replace(" ", "")) != null) {
					test.setTest(assDao.getAllUnitTests().get(test.getUnitTestName().replace(
							" ", "")));
				}
			}

			// secret unit tests
			for (WeightedUnitTest test : assessmentToAdd.getSecretUnitTests()) {
				if (assDao.getAllUnitTests().get(test.getUnitTestName().replace(" ", "")) != null) {
					test.setTest(assDao.getAllUnitTests().get(test.getUnitTestName().replace(
							" ", "")));
				}
			}
			
			// hand marking
			for (WeightedHandMarking handMarking : assessmentToAdd.getHandMarking()) {
				if (assDao.getHandMarking(handMarking.getHandMarkingName().replace(" ", "")) != null) {
					handMarking.setHandMarking(assDao.getHandMarking(handMarking.getHandMarkingName().replace(
							" ", "")));
				}
			}
			
			// competitions
			for (WeightedCompetition compeition : assessmentToAdd.getCompetitions()) {
				if (assDao.getCompetition(compeition.getCompName().replace(" ", "")) != null) {
					compeition.setTest(assDao.getCompetition(compeition.getCompName().replace(
							" ", "")));
					
					// if the competition is not already live, add comp/arenas to the scheduler
					if(!assDao.getCompetition(compeition.getCompName().replace(" ", "")).isLive()){
						if(assDao.getCompetition(compeition.getCompName().replace(" ", "")).isCalculated()){
							// add competition 
							scheduler.save(new Job("PASTACompetitionRunner", 
									assDao.getCompetition(compeition.getCompName().replace(" ", "")).getShortName(), 
									assDao.getCompetition(compeition.getCompName().replace(" ", "")).getNextRunDate()));
						}
						else{
							// add arenas
							// official
							if(assDao.getCompetition(compeition.getCompName().replace(" ", "")).getOfficialArena() != null){
								Arena arena = assDao.getCompetition(compeition.getCompName().replace(" ", "")).getOfficialArena();
								scheduler.save(new Job("PASTACompetitionRunner", 
										assDao.getCompetition(compeition.getCompName().replace(" ", "")).getShortName()+"#PASTAArena#"+arena.getName(), 
										arena.getNextRunDate()));
							}
							// outstanding
							if(assDao.getCompetition(compeition.getCompName().replace(" ", "")).getOutstandingArenas() != null){
								for(Arena arena : assDao.getCompetition(compeition.getCompName().replace(" ", "")).getOutstandingArenas()){
									scheduler.save(new Job("PASTACompetitionRunner", 
											assDao.getCompetition(compeition.getCompName().replace(" ", "")).getShortName()+"#PASTAArena#"+arena.getName(), 
											arena.getNextRunDate()));
								}
							}
						}
					}
					
					assDao.getCompetition(compeition.getCompName().replace(" ", "")).addAssessment(assessmentToAdd);
				}
			}

			// add it to the directory structure
			File location = new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/template/assessment/"
					+ assessmentToAdd.getName().replace(" ", ""));
			location.mkdirs();

			PrintStream out = new PrintStream(location.getAbsolutePath()
					+ "/assessmentProperties.xml");
			out.print(assessmentToAdd);
			out.close();

			PrintStream descriptionOut = new PrintStream(
					location.getAbsolutePath() + "/description.html");
			descriptionOut.print(assessmentToAdd.getDescription());
			descriptionOut.close();

			// add it to the list.
			assDao.addAssessment(assessmentToAdd);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HashMap<String, AssessmentResult> getLatestResultsForUser(String username){
		return resultDAO.getLatestResults(username);
	}
	
	public HashMap<String, HashMap<String, AssessmentResult>> getLatestResults(Collection<PASTAUser> allUsers){
		HashMap<String, HashMap<String, AssessmentResult>> results = new HashMap<String, HashMap<String, AssessmentResult>>();
		
		for(PASTAUser user: allUsers){
			HashMap<String, AssessmentResult> currResultMap = resultDAO.getLatestResults(user.getUsername());
			results.put(user.getUsername(), currResultMap);
		}
		
		return results;
	}
	
	public AssessmentResult getAssessmentResult(String username, String assessmentName,
			String assessmentDate) {
		return resultDAO.getAsssessmentResult(username, assDao.getAssessment(assessmentName), assessmentDate);
	}
	
	public Map<String, List<Assessment>> getAllAssessmentsByCategory() {
		return assDao.getAllAssessmentsByCategory();
	}

	
}
