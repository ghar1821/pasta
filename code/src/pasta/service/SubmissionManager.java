package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

import pasta.domain.Assessment2;
import pasta.domain.PASTAUser;
import pasta.domain.UserPermissionLevel;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.repository.AssessmentDAO;
import pasta.repository.AssessmentDAOold;
import pasta.repository.ResultDAO;
import pasta.repository.UserDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.Job;
import pasta.util.ProjectProperties;

@Service("submissionManager")
@Repository
/**
 * Submission amnager.
 * 
 * Manages interaction between controller and data.
 * 
 * @author Alex
 *
 */
public class SubmissionManager {
	
	private AssessmentDAOold assDao = new AssessmentDAOold();
	private AssessmentDAO assDaoNew = new AssessmentDAO();
	private ResultDAO resultDAO = new ResultDAO();
	
	private ExecutionScheduler scheduler;
	private UserDAO userDao;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
	}
	
	@Autowired
	public void setMyUserDAO(UserDAO myUserDao) {
		this.userDao = myUserDao;
	}

	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(SubmissionManager.class);

	public void submit(String username, Submission form) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		Date now = new Date();
		String currDate = sdf.format(now);
		String location = ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/" + username + "/assessments/"
				+ form.getAssessment() + "/" + currDate + "/submission";
		
		Assessment currAssessment = assDaoNew.getAssessment(form.getAssessment());
		boolean compiled = true;

		(new File(location)).mkdirs();
		try {
			form.getFile().transferTo(new File(location+"/"+form.getFile().getOriginalFilename()));
			if(form.getFile().getOriginalFilename().endsWith(".zip")){
				ProjectProperties.extractFolder(location+"/"+form.getFile().getOriginalFilename());
				(new File(location+"/"+form.getFile().getOriginalFilename())).delete();
			}
			
			String unitTestsLocation = ProjectProperties.getInstance().getProjectLocation()
					+ "/submissions/" + username + "/assessments/"
					+ form.getAssessment() + "/" + currDate + "/unitTests";
			// ensure all unit tests compile
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
					DefaultLogger consoleLogger = new DefaultLogger();
					PrintStream compileErrors = new PrintStream(
							unitTestsLocation + "/" + test.getTest().getShortName()
							+ "/compile.errors");
					PrintStream runErrors = new PrintStream(
							unitTestsLocation + "/" + test.getTest().getShortName()
							+ "/run.errors");
					PrintStream normalErrOut = System.err;
					System.setErr(compileErrors);
					consoleLogger.setOutputPrintStream(runErrors);
					consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
					project.addBuildListener(consoleLogger);
					project.init();

					project.addReference("ant.projectHelper", projectHelper);
					projectHelper.parse(project, buildFile);
					
					try {
						project.executeTarget("build");
						project.executeTarget("clean");
					} catch (BuildException e) {
						compiled = false;
						logger.error("Could not compile " + username + " - "
								+ currAssessment.getName() + " - "
								+ test.getTest().getName() + e);
					}

					runErrors.flush();
					runErrors.close();
					compileErrors.flush();
					compileErrors.close();
					
					System.setErr(normalErrOut);
					
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
					
					if(compiled){
						FileUtils.forceDelete(new File(unitTestsLocation + "/" + test.getTest().getShortName()
								+ "/compile.errors"));
					}
					
				} catch (IOException e) {
					logger.error("Unable to compile unit test "
							+ currAssessment.getName() + " for " + username
							+ System.getProperty("line.separator") + e);
				}
			}
			// add to scheduler
			if(compiled){
				scheduler.save(new Job(username, form.getAssessment(), now));
			}
		} catch (Exception e) {
			logger.error("Submission error for " + username + " - " + form + "   " + e);
		}
	}
	
	@Scheduled(fixedDelay = 30000)
	public void executeRemainingJobs2(){
		List<Job> outstandingJobs = scheduler.getOutstandingJobs();
		while(outstandingJobs != null && !outstandingJobs.isEmpty()){
			for(Job job: outstandingJobs){
				// do it
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
				String location = ProjectProperties.getInstance().getProjectLocation()
						+ "/submissions/" + job.getUsername() + "/assessments/"
						+ job.getAssessmentName() + "/" + sdf.format(job.getRunDate()) + "/submission";
				
				Assessment currAssessment = assDaoNew.getAssessment(job.getAssessmentName());
				boolean compiled = true;

				try {
					
					String unitTestsLocation = ProjectProperties.getInstance().getProjectLocation()
							+ "/submissions/" + job.getUsername() + "/assessments/"
							+ job.getAssessmentName() + "/" + sdf.format(new Date()) + "/unitTests";
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
							project.init();

							project.addReference("ant.projectHelper", projectHelper);
							projectHelper.parse(project, buildFile);
							
							try {
								project.executeTarget("build");
								project.executeTarget("test");
								project.executeTarget("clean");
							} catch (BuildException e) {
								compiled = false;
								logger.error("Could not compile " + job.getUsername() + " - "
										+ currAssessment.getName() + " - "
										+ test.getTest().getName() + e);
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
				} catch (Exception e) {
					logger.error("Execution error for " + job.getUsername() + " - " + job.getAssessmentName() + "   " + e);
				}
			}
			outstandingJobs = scheduler.getOutstandingJobs();
		}
		
		logger.info("Finished executing all jobs");
	}

	public Collection<AssessmentResult> getStudentResults(String username) {
		ArrayList<AssessmentResult> results = new ArrayList<AssessmentResult>();

		for (Assessment assess : getAssessmentListNew()) {
			AssessmentResult assessResult = new AssessmentResult();
			assessResult.setAssessment(assess);

			// get latest submission
			String latest = null;

			// if latest version is overridden to use an older version
			if (new File(ProjectProperties.getInstance().getProjectLocation()
					+ "/submissions/" + username + "/assessments/"
					+ assess.getShortName() + "/used.txt").exists()) {
				// get that version
				try {
					Scanner in = new Scanner(new File(ProjectProperties
							.getInstance().getProjectLocation()
							+ "/submissions/"
							+ username
							+ "/assessments/"
							+ assess.getShortName() + "/used.txt"));
					latest = in.nextLine().trim();
					in.close();
				} catch (Exception e) {
					// use latest version
				}
			}

			String[] allFiles = (new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/submissions/"
					+ username
					+ "/assessments/" + assess.getShortName())).list();

			assessResult.setSubmissionsMade((allFiles == null) ? 0
					: allFiles.length);

			if (latest == null && allFiles != null) {
				// get latest version
				Arrays.sort(allFiles);
				latest = allFiles[allFiles.length - 1];
			}

			ArrayList<UnitTestResult> utresults = new ArrayList<UnitTestResult>();
			for (WeightedUnitTest uTest : assess.getUnitTests()) {
				// TODO
				UnitTestResult result = resultDAO
						.getUnitTestResult(ProjectProperties.getInstance()
								.getProjectLocation()
								+ "/submissions/"
								+ username
								+ "/assessments/"
								+ assess.getShortName()
								+ "/"
								+ latest
								+ "/unitTests/" + uTest.getTest().getShortName());
				if (result == null) {
					result = new UnitTestResult();
				}
				result.setTest(uTest.getTest());
				utresults.add(result);
			}

			assessResult.setUnitTests(utresults);

			// add to collection
			results.add(assessResult);
		}
		return results;
	}

	public UnitTestResult getUnitTestResult(String location) {
		return resultDAO.getUnitTestResult(location);
	}

	// new
	public Collection<UnitTest> getUnitTestList() {
		return assDaoNew.getAllUnitTests().values();
	}

	// new
	public UnitTest getUnitTest(String name) {
		return assDaoNew.getAllUnitTests().get(name);
	}

	// new
	public Collection<Assessment> getAssessmentListNew() {
		return assDaoNew.getAssessmentList();
	}

	// new
	public HandMarking getHandMarking(String handMarkingName) {
		return assDaoNew.getHandMarking(handMarkingName);
	}

	// new
	public Assessment getAssessmentNew(String assessmentName) {
		return assDaoNew.getAssessment(assessmentName);
	}
	
	// new
	public Collection<AssessmentResult> getAssessmentHistoryNew(String username, String assessmentName){
		return resultDAO.getAssessmentHistory(username, getAssessmentNew(assessmentName));
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
				ProjectProperties.extractFolder(thisTest.getFileLocation()
						+ "/code/" + newTest.getFile().getOriginalFilename());
				newTest.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisTest.getFileLocation()
						+ "/code/" + newTest.getFile().getOriginalFilename()));
			}

			FileUtils.deleteDirectory((new File(thisTest.getFileLocation()
					+ "/test/")));

			assDaoNew.addUnitTest(thisTest);
		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName()
					+ " could not be created successfully!"
					+ System.getProperty("line.separator") + e);
		}
	}

	// new - unit test is guaranteed to have a unique name
	public void removeUnitTest(String testName) {
		assDaoNew.removeUnitTest(testName);
	}

	// new - assessment is guaranteed to have a unique name
	public void removeAssessment(String assessment) {
		assDaoNew.removeAssessment(assessment);
	}

	// new - test submission
	public void testUnitTest(Submission submission, String testName) {
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
			ProjectProperties.extractFolder(thisTest.getFileLocation()
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
			DefaultLogger consoleLogger = new DefaultLogger();
			PrintStream compileErrors = new PrintStream(
					thisTest.getFileLocation() + "/test/compile.errors");
			PrintStream runErrors = new PrintStream(thisTest.getFileLocation()
					+ "/test/run.errors");
			consoleLogger.setErrorPrintStream(compileErrors);
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
			compileErrors.flush();
			compileErrors.close();

			// delete everything else
			String[] allFiles = (new File(thisTest.getFileLocation() + "/test/"))
					.list();
			for (String file : allFiles) {
				File actualFile = new File(thisTest.getFileLocation()
						+ "/test/" + file);
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
			logger.error("Unable to test unit test "
					+ getUnitTest(testName).getName()
					+ System.getProperty("line.separator") + e);
		}
	}

	// new TOOD add assessment
	public void addAssessment(Assessment assessmentToAdd) {
		try {

			// reload unit tests
			Collection<UnitTest> tests = getUnitTestList();

			// unit Tests
			for (WeightedUnitTest test : assessmentToAdd.getUnitTests()) {
				if (getUnitTest(test.getUnitTestName().replace(" ", "")) != null) {
					test.setTest(getUnitTest(test.getUnitTestName().replace(
							" ", "")));
				}
			}

			// secret unit tests
			for (WeightedUnitTest test : assessmentToAdd.getSecretUnitTests()) {
				if (getUnitTest(test.getUnitTestName().replace(" ", "")) != null) {
					test.setTest(getUnitTest(test.getUnitTestName().replace(
							" ", "")));
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
			assDaoNew.addAssessment(assessmentToAdd);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PASTAUser getUser(String username) {
		return userDao.getUser(username);
	}
	
	public PASTAUser getOrCreateUser(String username) {
		PASTAUser user = userDao.getUser(username);
		if(user == null){
			user = new PASTAUser();
			user.setUsername(username);
			user.setStream("");
			user.setTutorial("");
			user.setPermissionLevel(UserPermissionLevel.STUDENT);
			
			userDao.add(user);
		}
		return user;
	}

	public List<String> getAssessmentList() {
		return assDao.getAssessmentList();
	}

	public Map<String, Assessment2> getAssessments(String unikey) {
		return assDao.getAssessments(unikey);
	}

	public List<Assessment2> getAssessmentHistory(String unikey,
			String assessmentName) {
		return assDao.getAssessmentHistory(unikey, assessmentName);
	}

	public Assessment2 getAssessment(String unikey, String assessmentName) {
		return assDao.getAssessment(assessmentName, unikey);
	}

}
