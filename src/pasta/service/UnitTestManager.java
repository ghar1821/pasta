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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.result.UnitTestResult;
import pasta.domain.template.UnitTest;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UnitTestDAO;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Unit Test manager.
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
@Service("unitTestManager")
@Repository
public class UnitTestManager {
	
	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	
	@Autowired
	private UnitTestDAO unitTestDAO;
		
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(UnitTestManager.class);
	
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.ResultDAO#getUnitTestResult(String)
	 * @param location get the non cached unit test results from a location
	 * @return the unit test result
	 */
	public UnitTestResult getUnitTestResult(String location) {
		return resultDAO.getUnitTestResult(location);
	}

	/**
	 * Get the collection of all unit test templates
	 * 
	 * @see pasta.repository.AssessmentDAO#getAllUnitTests()
	 * @return the collection of all unit test templates
	 */
	public Collection<UnitTest> getUnitTestList() {
		return unitTestDAO.getAllUnitTests();
		//return assDao.getAllUnitTests().values();
	}

	/**
	 * Get a unit test template
	 * 
	 * @param name the short name (no whitespace) of a unit test.
	 * @return the unit test template or null if not available
	 */
	@Deprecated
	public UnitTest getUnitTest(String name) {
		return assDao.getAllUnitTests().get(name);
	}
	
	public UnitTest getUnitTest(long id) {
		return unitTestDAO.getUnitTest(id);
	}

	/**
	 * Save the unit test template to disk
	 * 
	 * @param thisTest the unit test to save
	 */
	public void updateUnitTest(UnitTest thisTest) {
		try {

			// create space on the file system.
			(new File(thisTest.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
      saveUnitTestXML(thisTest);

		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName()
					+ " could not be saved successfully!"
					+ System.getProperty("line.separator") + e);
		}
		
		// Save unit test to database
		try {
			unitTestDAO.update(thisTest);
		} catch (Exception e) {
			logger.error("Could not update unit test " + thisTest.getName(), e);
		}
	}

	/**
	 * Add a new unit test
	 * <p>
	 * Assume the unit test name is unique. This must be enforced in another
	 * class for now (thought this should probably be moved here at a later date).
	 * 
	 * The unit test properties xml will be created in the correct folder and any
	 * zip file that was uploaded will be unpacked using {@link pasta.util.PASTAUtil#extractFolder(String)}.
	 *  
	 * @param newTest the new unit test form used to create a new unit test template
	 */
	public void addUnitTest(NewUnitTest newTest) {
		UnitTest thisTest = new UnitTest(newTest.getTestName(), false);

		// Save unit test to database
		try {
			unitTestDAO.save(thisTest);
		} catch (Exception e) {
			logger.error("Could not save unit test " + thisTest.getName(), e);
		}
		
		try {

			// create space on the file system.
			(new File(thisTest.getFileLocation() + "/code/")).mkdirs();

			// generate unitTestProperties
      saveUnitTestXML(thisTest);

			// unzip the uploaded code into the code folder. (if exists)
			if (newTest.getFile() != null && !newTest.getFile().isEmpty()) {
				// unpack
				newTest.getFile().getInputStream().close();
        if (!newTest.getFile().getOriginalFilename().endsWith(".java")) {
          newTest.getFile().transferTo(
						new File(thisTest.getFileLocation() + "/code/"
								+ newTest.getFile().getOriginalFilename()));
  				PASTAUtil.extractFolder(thisTest.getFileLocation()
  						+ "/code/" + newTest.getFile().getOriginalFilename());
  				try{
  					FileUtils.forceDelete(new File(thisTest.getFileLocation()
  							+ "/code/" + newTest.getFile().getOriginalFilename()));
  				} catch (Exception e) {
  					logger.error("Could not delete the zip for "
  							+ thisTest.getName());
  				}
				} else {
		      (new File(thisTest.getFileLocation() + "/code/test/")).mkdirs();
		      String newTestPath = thisTest.getFileLocation() + "/code/test/"
              + newTest.getFile().getOriginalFilename();
          newTest.getFile().transferTo(
              new File(newTestPath));
          generateBuildXML(newTest.getFile().getOriginalFilename(),
              thisTest.getFileLocation() + "/code/build.xml");
				}
			}

			FileUtils.deleteDirectory((new File(thisTest.getFileLocation()
					+ "/test/")));
		} catch (Exception e) {
			(new File(thisTest.getFileLocation())).delete();
			logger.error("TEST " + thisTest.getName()
					+ " could not be created successfully!"
					+ System.getProperty("line.separator") + e);
		}
	}
	
	private void generateBuildXML(String newTestFileName, String newTestFileXML) throws IOException {
	  int startPos = newTestFileName.lastIndexOf('/');
	  startPos = 0 > startPos ? 0 : startPos;
    String testName = newTestFileName.substring(startPos,
        newTestFileName.lastIndexOf(".java"));
    String newXML = getDefaultBuildXML().replaceAll("\\$\\$TESTNAME\\$\\$", testName);
    Files.write(Paths.get(newTestFileXML), newXML.getBytes());
  }
	
	private String getDefaultBuildXML() throws IOException {
    File templateBuildXML = new File(ProjectProperties.getInstance()
        .getUnitTestsLocation() + "build.xml");
    Charset charset = StandardCharsets.UTF_8;
    return new String(Files.readAllBytes(templateBuildXML.toPath()), charset);
	}

  /**
	 * Helper method
	 * 
	 * @see pasta.repository.AssessmentDAO#removeUnitTest(String)
	 * @param testName the short name (no whitespace) of the unit test template to be removed
	 */
	@Deprecated
	public void removeUnitTest(String testName) {
		assDao.removeUnitTest(testName);
		
		// Delete unit test from database
		try {
			unitTestDAO.delete(unitTestDAO.getUnitTest(testName));
		} catch (Exception e) {
			logger.error("Could not delete unit test " + testName, e);
		}
	}
	
	public void removeUnitTest(long id) {
		// Delete unit test from database
		try {
			unitTestDAO.delete(unitTestDAO.getUnitTest(id));
		} catch (Exception e) {
			logger.error("Could not delete unit test " + id, e);
		}
	}

	/**
	 * Method to test the unit tests uploaded
	 * <p>
	 * This method is used to test whether the unit tests are working
	 * as anticipated on the system before assigning it as a part of an assessment
	 * and releasing it to students.
	 * 
	 * There is no queue system implemented for this now. As soon as the method
	 * is called, the testing proceeds. Use at your own risk.
	 * 
	 * <ol>
	 * 	<li>Create a new folder $ProjectLocation$/template/unitTest/$unitTestName$/test, 
	 * if folder already exists, delete it and make a new one</li>
	 * 	<li>Copy and extract submission code to that folder using {@link pasta.util.PASTAUtil#extractFolder(String)}</li>
	 * 	<li>Copy the unit test code from $ProjectLocation$/template/unitTest/$unitTestName$/code</li>
	 * 	<li>run the ant targets "build", "test", "clean"</li>
	 * 	<li>any errors will be written to "compile.errors" and "run.errors" and the results are written to
	 * results.xml</li>
	 * </ol>
	 * 
	 * @param submission the submission form (containing the zip)
	 * @param testName the short name (no whitespace) of the test
	 */
	@Deprecated
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
	
	/**
	 * Method to test the unit tests uploaded
	 * <p>
	 * This method is used to test whether the unit tests are working
	 * as anticipated on the system before assigning it as a part of an assessment
	 * and releasing it to students.
	 * 
	 * There is no queue system implemented for this now. As soon as the method
	 * is called, the testing proceeds. Use at your own risk.
	 * 
	 * <ol>
	 * 	<li>Create a new folder $ProjectLocation$/template/unitTest/$unitTestId$/test, 
	 * if folder already exists, delete it and make a new one</li>
	 * 	<li>Copy and extract submission code to that folder using {@link pasta.util.PASTAUtil#extractFolder(String)}</li>
	 * 	<li>Copy the unit test code from $ProjectLocation$/template/unitTest/$unitTestId$/code</li>
	 * 	<li>run the ant targets "build", "test", "clean"</li>
	 * 	<li>any errors will be written to "compile.errors" and "run.errors" and the results are written to
	 * results.xml</li>
	 * </ol>
	 * 
	 * @param submission the submission form (containing the zip)
	 * @param testId the id of the test
	 */
	public void testUnitTest(Submission submission, long testId) {
		PrintStream compileErrors = null;
		PrintStream runErrors = null;
		try {
			UnitTest thisTest = getUnitTest(testId);
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
			
		} catch (IOException e) {
			logger.error("Unable to test unit test "
					+ getUnitTest(testId).getName()
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

	/**
	 * Update the unit test with new code.
	 * <p>
	 * @param newTest the form containing the new unit test form.
	 */
	public void updateUnitTestCode(NewUnitTest newTest) {
		UnitTest thisTest;
		
		if(newTest.getTestId() == null || 
				(thisTest = getUnitTest(newTest.getTestId())) == null) {
			addUnitTest(newTest);
		} else {
			try {
	
				// force delete old location
				FileUtils.deleteDirectory((new File(thisTest.getFileLocation()
						+ "/code/")));
				
				// create space on the file system.
				(new File(thisTest.getFileLocation() + "/code/")).mkdirs();
	
				// generate unitTestProperties
				saveUnitTestXML(thisTest);
				
				// unzip the uploaded code into the code folder. (if exists)
				if (newTest.getFile() != null && !newTest.getFile().isEmpty()) {
					// unpack
					newTest.getFile().getInputStream().close();
					newTest.getFile().transferTo(
							new File(thisTest.getFileLocation() + "/code/"
									+ newTest.getFile().getOriginalFilename()));
					PASTAUtil.extractFolder(thisTest.getFileLocation()
							+ "/code/" + newTest.getFile().getOriginalFilename());
					try{
						FileUtils.forceDelete(new File(thisTest.getFileLocation()
								+ "/code/" + newTest.getFile().getOriginalFilename()));
					} catch (Exception e) {
						logger.error("Could not delete the zip for "
								+ thisTest.getName());
					}
				}
	
				// set it as not tested
				thisTest.setTested(false);
				updateUnitTest(thisTest);
			} catch (Exception e) {
				(new File(thisTest.getFileLocation())).delete();
				logger.error("TEST " + thisTest.getName()
						+ " could not be updated successfully!"
						+ System.getProperty("line.separator") + e);
			}
		}
	}
	
	protected void saveUnitTestXML(UnitTest test) throws FileNotFoundException {
    PrintStream out = new PrintStream(test.getFileLocation()
        + "/unitTestProperties.xml");
    out.print(test);
    out.close();
	}
	
}
