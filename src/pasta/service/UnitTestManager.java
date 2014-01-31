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

@Service("unitTestManager")
@Repository
/**
 * unitTest manager.
 * 
 * Manages interaction between controller and data.
 * 
 * @author Alex
 *
 */
public class UnitTestManager {
	
	private AssessmentDAO assDao = new AssessmentDAO();
	private ResultDAO resultDAO = new ResultDAO(assDao);
	
	private ExecutionScheduler scheduler;
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(UnitTestManager.class);
	
	
	public UnitTestResult getUnitTestResult(String location) {
		return resultDAO.getUnitTestResult(location);
	}

	// new
	public Collection<UnitTest> getUnitTestList() {
		return assDao.getAllUnitTests().values();
	}

	// new
	public UnitTest getUnitTest(String name) {
		return assDao.getAllUnitTests().get(name);
	}

	public void saveUnitTest(UnitTest thisTest) {
		try {

			// create space on the file system.
			(new File(thisTest.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
			PrintStream out = new PrintStream(thisTest.getFileLocation()
					+ "/unitTestProperties.xml");
			out.print(thisTest);
			out.close();

		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName()
					+ " could not be saved successfully!"
					+ System.getProperty("line.separator") + e);
		}
	}

	// new - unit test is guaranteed to have a unique name
	public void addUnitTest(NewUnitTest newTest) {
		UnitTest thisTest = new UnitTest(newTest.getTestName(), false);

		try {

			// create space on the file system.
			(new File(thisTest.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
			PrintStream out = new PrintStream(thisTest.getFileLocation()
					+ "/unitTestProperties.xml");
			out.print(thisTest);
			out.close();

			// unzip the uploaded code into the code folder. (if exists)
			if (newTest.getFile() != null && !newTest.getFile().isEmpty()) {
				// unpack
				newTest.getFile().transferTo(
						new File(thisTest.getFileLocation() + "/code/"
								+ newTest.getFile().getOriginalFilename()));
				PASTAUtil.extractFolder(thisTest.getFileLocation()
						+ "/code/" + newTest.getFile().getOriginalFilename());
				newTest.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisTest.getFileLocation()
						+ "/code/" + newTest.getFile().getOriginalFilename()));
			}

			FileUtils.deleteDirectory((new File(thisTest.getFileLocation()
					+ "/test/")));

			assDao.addUnitTest(thisTest);
		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName()
					+ " could not be created successfully!"
					+ System.getProperty("line.separator") + e);
		}
	}
	
	// new - unit test is guaranteed to have a unique name
	public void removeUnitTest(String testName) {
		assDao.removeUnitTest(testName);
	}

	// new - test submission
	public void testUnitTest(Submission submission, String testName) {
		PrintStream compileErrors = null;
		PrintStream runErrors = null;
		try {
			UnitTest thisTest = getUnitTest(testName);
			// delete old submission if exists
			FileUtils.deleteDirectory(new File(thisTest.getFileLocation()
					+ "/test/"));

			// create folder
			(new File(thisTest.getFileLocation() + "/test/")).mkdirs();
			// extract submission
			submission.getFile().transferTo(
					new File(thisTest.getFileLocation() + "/test/"
							+ submission.getFile().getOriginalFilename()));
			PASTAUtil.extractFolder(thisTest.getFileLocation()
					+ "/test/" + submission.getFile().getOriginalFilename());
			FileUtils.forceDelete(new File(thisTest.getFileLocation()
					+ "/test/" + submission.getFile().getOriginalFilename()));

			// copy over unit test
			FileUtils.copyDirectory(new File(thisTest.getFileLocation()
					+ "/code/"),
					new File(thisTest.getFileLocation() + "/test/"));

			// compile
			File buildFile = new File(thisTest.getFileLocation()
					+ "/test/build.xml");

			ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
			Project project = new Project();

			project.setUserProperty("ant.file", buildFile.getAbsolutePath());
			project.setBasedir(thisTest.getFileLocation() + "/test");
			DefaultLogger consoleLogger = new DefaultLogger();
			 runErrors = new PrintStream(thisTest.getFileLocation()
					  + "/test/run.errors");
			consoleLogger.setOutputPrintStream(runErrors);
			consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
			project.addBuildListener(consoleLogger);
			project.init();

			project.addReference("ant.projectHelper", projectHelper);
			projectHelper.parse(project, buildFile);
			try {
				project.executeTarget("build");
				project.executeTarget("test");
				project.executeTarget("clean");
			} catch (BuildException e) {
				throw new RuntimeException(String.format(
						"Run %s [%s] failed: %s", buildFile, "everything",
						e.getMessage()), e);
			}

			runErrors.close();
			
			// scrape compiler errors from run.errors
			try{
				Scanner in = new Scanner (new File(thisTest.getFileLocation() + "/test/" + "/run.errors"));
				boolean containsError = false;
				boolean importantData = false;
				String output = "";
				while(in.hasNextLine()){
					String line = in.nextLine();
					if(line.contains(": error:")){
						containsError = true;
					}
					if(line.contains("[javac] Files to be compiled:")){
						importantData = true;
					}
					if(importantData){
						output += line.replace("[javac]", "").replaceAll(".*unitTests","") + System.getProperty("line.separator");
					}
				}
				in.close();
				
				if(containsError){
					compileErrors = new PrintStream(
							thisTest.getFileLocation() + "/test/" + "/compile.errors");
					compileErrors.print(output);
					compileErrors.close();
					
				}
			}
			catch (Exception e){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.error("Something went wrong: " + sw.toString());
			}

//			// delete everything else
//			String[] allFiles = (new File(thisTest.getFileLocation() + "/test/"))
//					.list();
//			for (String file : allFiles) {
//				File actualFile = new File(thisTest.getFileLocation()
//						+ "/test/" + file);
//				if (actualFile.isDirectory()) {
//					FileUtils.deleteDirectory(actualFile);
//				} else {
//					if (!file.equals("result.xml")
//							&& !file.equals("compile.errors")
//							&& !file.equals("run.errors")) {
//						FileUtils.forceDelete(actualFile);
//					}
//				}
//			}

		} catch (IOException e) {
			logger.error("Unable to test unit test "
					+ getUnitTest(testName).getName()
					+ System.getProperty("line.separator") + e);
		} catch (Exception e){
			// catch the rest of the exceptions
		}
		
		
		// ensure everything is closed
		if(runErrors != null){
			runErrors.close();
		}
		if(compileErrors != null){
			compileErrors.close();
		}
	}

	public void updateUpdateUnitTest(NewUnitTest newTest) {
		UnitTest thisTest = getUnitTest(newTest.getTestName());

		if(thisTest != null){
			try {
	
				// force delete old location
				FileUtils.deleteDirectory((new File(thisTest.getFileLocation()
						+ "/code/")));
				
				// create space on the file system.
				(new File(thisTest.getFileLocation() + "/code/")).mkdirs();
	
				// generate unitTestProperties
				PrintStream out = new PrintStream(thisTest.getFileLocation()
						+ "/unitTestProperties.xml");
				out.print(thisTest);
				out.close();
	
				// unzip the uploaded code into the code folder. (if exists)
				if (newTest.getFile() != null && !newTest.getFile().isEmpty()) {
					// unpack
					newTest.getFile().transferTo(
							new File(thisTest.getFileLocation() + "/code/"
									+ newTest.getFile().getOriginalFilename()));
					PASTAUtil.extractFolder(thisTest.getFileLocation()
							+ "/code/" + newTest.getFile().getOriginalFilename());
					newTest.getFile().getInputStream().close();
					FileUtils.forceDelete(new File(thisTest.getFileLocation()
							+ "/code/" + newTest.getFile().getOriginalFilename()));
				}
	
				FileUtils.deleteDirectory((new File(thisTest.getFileLocation()
						+ "/test/")));
	
				// set it as not tested
				thisTest.setTested(false);
				saveUnitTest(thisTest);
			} catch (Exception e) {
				(new File(thisTest.getFileLocation())).delete();
				logger.error("TEST " + thisTest.getName()
						+ " could not be updated successfully!"
						+ System.getProperty("line.separator") + e);
			}
		}
		else{
			addUnitTest(newTest);
		}
	}
	
}
