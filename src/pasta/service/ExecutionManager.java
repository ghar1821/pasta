package pasta.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.WeightedUnitTest;
import pasta.repository.AssessmentDAO;
import pasta.repository.LoginDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UserDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.Job;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

@Service("executionManager")
@Repository
/**
 * Submission amnager.
 * 
 * Manages interaction between controller and data.
 * 
 * @author Alex
 *
 */
public class ExecutionManager {
	
	private AssessmentDAO assDao = new AssessmentDAO();
	private ResultDAO resultDAO = new ResultDAO(assDao);
	
	private ExecutionScheduler scheduler;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
	}
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(ExecutionManager.class);
	
	private void executeCompetitionJob(Job job){
		
		Competition comp = assDao.getCompetition(job.getAssessmentName());
		if(comp != null){
			
			// if dead, remove from the list and do nothing
			if(!comp.isLive()){
				scheduler.delete(job);
				return;
			}
			
			// create folder
			String competitionLocation = ProjectProperties.getInstance().getProjectLocation()
					+ "/competitions/" + comp.getShortName() + "/competition/" 
					+ PASTAUtil.formatDate(job.getRunDate());
			(new File(competitionLocation)).mkdirs();
			
			// copy across
			try {
				FileUtils.copyDirectory(new File(comp.getFileLocation()+"/code"),
						new File(competitionLocation));
				
				// compile
				File buildFile = new File(competitionLocation + "/build.xml");

				ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
				Project project = new Project();

				project.setUserProperty("ant.file", buildFile.getAbsolutePath());
				project.init();

				project.addReference("ant.projectHelper", projectHelper);
				projectHelper.parse(project, buildFile);
				
				try {
					project.executeTarget("build");
					if(comp.isCalculated()){
						project.executeTarget("compete");
					}
					project.executeTarget("mark");
					project.executeTarget("clean");
				} catch (BuildException e) {
					// TODO
					logger.error("Could run competition " + comp.getName() + " - "
							+ e);
				}

				// delete everything else
				String[] allFiles = (new File(competitionLocation))
						.list();
				for (String file : allFiles) {
					File actualFile = new File(competitionLocation
							+ "/" + file);
					if (actualFile.isDirectory()) {
						FileUtils.deleteDirectory(actualFile);
					} else {
						if (!file.equals("marks.csv")
								&& !file.equals("results.csv")) {
							FileUtils.forceDelete(actualFile);
						}
					}
				}
				// delete it
				scheduler.delete(job);
				
				// update resultDAO
				resultDAO.updateCompetitionResults(job.getAssessmentName());
				
				// check if still live and readd
				if(comp.isLive()){
					scheduler.save(new Job("PASTACompetitionRunner", comp.getShortName(), comp.getNextRunDate()));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void executeArenaJob(Job job){
		// TODO
	}
	
	private void executeNormalJob(Job job){
		// do it
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		String location = ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/" + job.getUsername() + "/assessments/"
				+ job.getAssessmentName() + "/" + sdf.format(job.getRunDate()) + "/submission";
		
		Assessment currAssessment = assDao.getAssessment(job.getAssessmentName());

		try {
			
			String unitTestsLocation = ProjectProperties.getInstance().getProjectLocation()
					+ "/submissions/" + job.getUsername() + "/assessments/"
					+ job.getAssessmentName() + "/" + sdf.format(job.getRunDate()) + "/unitTests";
			// run unit tests
			for(WeightedUnitTest test: currAssessment.getUnitTests()){
				try {
					// create folder
					(new File(unitTestsLocation + "/" + test.getTest().getShortName())).mkdirs();

					// copy over unit test
					FileUtils.copyDirectory(new File(test.getTest().getFileLocation()
							+ "/code/"),
							new File(unitTestsLocation + "/" + test.getTest().getShortName()));
					// copy over submission
					FileUtils.copyDirectory(new File(location),
							new File(unitTestsLocation + "/" + test.getTest().getShortName()));
					
					// compile
					File buildFile = new File(unitTestsLocation + "/" + test.getTest().getShortName()
							+ "/build.xml");

					ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
					Project project = new Project();

					project.setUserProperty("ant.file", buildFile.getAbsolutePath());
					project.setBasedir(unitTestsLocation + "/" + test.getTest().getShortName());
					project.init();

					project.addReference("ant.projectHelper", projectHelper);
					projectHelper.parse(project, buildFile);
					
					try {
						project.executeTarget("build");
						project.executeTarget("test");
						project.executeTarget("clean");
					} catch (BuildException e) {
						logger.error("Could not compile " + job.getUsername() + " - "
								+ currAssessment.getName() + " - "
								+ test.getTest().getName() + e);
						PrintStream compileErrors = new PrintStream(
								unitTestsLocation + "/" + test.getTest().getShortName()
								+ "/compile.errors");
						compileErrors.print(e.toString().replaceAll(".*" +
								unitTestsLocation + "/" + test.getTest().getShortName() + "/" , "folder "));
						compileErrors.close();
					}

					// delete everything else
					String[] allFiles = (new File(unitTestsLocation + "/" + test.getTest().getShortName()))
							.list();
					for (String file : allFiles) {
						File actualFile = new File(unitTestsLocation + "/" + test.getTest().getShortName()
								+ "/" + file);
						if (actualFile.isDirectory()) {
							FileUtils.deleteDirectory(actualFile);
						} else {
							if (!file.equals("result.xml")
									&& !file.equals("compile.errors")
									&& !file.equals("run.errors")) {
								FileUtils.forceDelete(actualFile);
							}
						}
					}
					
					
				} catch (IOException e) {
					logger.error("Unable to compile unit test "
							+ currAssessment.getName() + " for " + job.getUsername()
							+ System.getProperty("line.separator") + e);
				}
			}
			
			// delete it
			scheduler.delete(job);
			
			// update resultDAO
			resultDAO.updateUnitTestResults(job.getUsername(), currAssessment, job.getRunDate());
		} catch (Exception e) {
			logger.error("Execution error for " + job.getUsername() + " - " + job.getAssessmentName() + "   " + e);
		}
	}
	
	@Scheduled(fixedDelay = 5000)
	public void executeRemainingJobs(){
		List<Job> outstandingJobs = scheduler.getOutstandingJobs();
		while(outstandingJobs != null && !outstandingJobs.isEmpty()){
			for(Job job: outstandingJobs){
				if(job.getUsername().equals("PASTACompetitionRunner")){
					if(job.getAssessmentName().contains("#PASTAArena#")){
						executeArenaJob(job);
					}else{
						executeCompetitionJob(job);
					}
				}
				else{
					executeNormalJob(job);
				}
			}
			outstandingJobs = scheduler.getOutstandingJobs();
		}
	}

}
