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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.domain.result.UnitTestResult;
import pasta.domain.template.UnitTest;
import pasta.domain.upload.NewUnitTestForm;
import pasta.domain.upload.TestUnitTestForm;
import pasta.domain.upload.UpdateUnitTestForm;
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
	 * @see pasta.repository.ResultDAO#getUnitTestResultFromDisk(String)
	 * @param location get the non cached unit test results from a location
	 * @return the unit test result
	 */
	public UnitTestResult getUnitTestResult(String location) {
		return resultDAO.getUnitTestResultFromDisk(location);
	}

	/**
	 * Get the collection of all unit test templates
	 * 
	 * @see pasta.repository.AssessmentDAO#getAllUnitTests()
	 * @return the collection of all unit test templates
	 */
	public Collection<UnitTest> getUnitTestList() {
		return unitTestDAO.getAllUnitTests();
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
		// Save unit test to database
		try {
			unitTestDAO.update(thisTest);
		} catch (Exception e) {
			logger.error("Could not update unit test " + thisTest.getName(), e);
		}
	}

	/**
	 * Add a new unit test
	 * 
	 * @param newTest the new unit test form used to create a new unit test template
	 */
	public UnitTest addUnitTest(NewUnitTestForm newTest) {
		UnitTest thisTest = new UnitTest(newTest.getName(), false);

		// Save unit test to database
		try {
			unitTestDAO.save(thisTest);
			return thisTest;
		} catch (Exception e) {
			logger.error("Could not save unit test " + thisTest.getName(), e);
			return null;
		}
	}
	
	public void removeUnitTest(long id) {
		// Delete unit test from database
		try {
			assDao.unlinkUnitTest(id);
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
	public void testUnitTest(UnitTest test, TestUnitTestForm testForm) {
		//TODO redo with AntJob
		PrintStream compileErrors = null;
		PrintStream runErrors = null;
		try {
			// delete old submission if exists
			FileUtils.deleteDirectory(new File(test.getFileLocation() + "/test/"));
			
			// create folder
			(new File(test.getFileLocation() + "/test/")).mkdirs();
			// extract submission
			testForm.getFile().transferTo(
					new File(test.getFileLocation() + "/test/"
							+ testForm.getFile().getOriginalFilename()));
			PASTAUtil.extractFolder(test.getFileLocation()
					+ "/test/" + testForm.getFile().getOriginalFilename());
			FileUtils.forceDelete(new File(test.getFileLocation()
					+ "/test/" + testForm.getFile().getOriginalFilename()));
			
			// copy over unit test
			FileUtils.copyDirectory(new File(test.getFileLocation()
					+ "/code/"),
					new File(test.getFileLocation() + "/test/"));
			
			// compile
			File buildFile = new File(test.getFileLocation()
					+ "/test/build.xml");
			
			ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
			Project project = new Project();
			
			project.setUserProperty("ant.file", buildFile.getAbsolutePath());
			project.setBasedir(test.getFileLocation() + "/test");
			DefaultLogger consoleLogger = new DefaultLogger();
			runErrors = new PrintStream(test.getFileLocation()
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
				Scanner in = new Scanner (new File(test.getFileLocation() + "/test/" + "/run.errors"));
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
							test.getFileLocation() + "/test/" + "/compile.errors");
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
			logger.error("Unable to test unit test " + test.getName(), e);
		} catch (Exception e){
			logger.error("Unknown error while testing unit test: ", e);
		}
		
		// ensure everything is closed
		if(runErrors != null){
			runErrors.close();
		}
		if(compileErrors != null){
			compileErrors.close();
		}
	}

	public void updateUnitTest(UnitTest test, UpdateUnitTestForm updateForm) {
		test.setName(updateForm.getName());
		test.setMainClassName(updateForm.getMainClassName());
		updateUnitTest(test);
	}

	public void updateUnitTestCode(UnitTest test, UpdateUnitTestForm updateForm) {
		try {
			File codeLocation = test.getCodeLocation();
			
			// force delete old location
			FileUtils.deleteDirectory(codeLocation);
			
			// create space on the file system.
			codeLocation.mkdirs();

			CommonsMultipartFile submission = updateForm.getFile();
			// unzip the uploaded code into the code folder. (if exists)
			if (submission != null && !submission.isEmpty()) {
				// unpack
				submission.getInputStream().close();
				File newLocation = new File(codeLocation, updateForm.getFile().getOriginalFilename());
				submission.transferTo(newLocation);
				PASTAUtil.extractFolder(newLocation.getAbsolutePath());
				try{
					FileUtils.forceDelete(newLocation);
				} catch (Exception e) {
					logger.error("Could not delete the zip for "
							+ test.getName());
				}
			}

			// set it as not tested
			test.setTested(false);
			updateUnitTest(test);
		} catch (Exception e) {
			(new File(test.getFileLocation())).delete();
			logger.error("Unit test code for " + test.getName()
					+ " could not be updated successfully!", e);
		}
	}
}
