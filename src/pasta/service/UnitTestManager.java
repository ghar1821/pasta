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
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.domain.FileTreeNode;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.UnitTest;
import pasta.domain.upload.NewUnitTestForm;
import pasta.domain.upload.TestUnitTestForm;
import pasta.domain.upload.UpdateUnitTestForm;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UnitTestDAO;
import pasta.testing.AntJob;
import pasta.testing.AntResults;
import pasta.testing.JUnitTestRunner;
import pasta.testing.task.DirectoryCopyTask;
import pasta.testing.task.UnzipTask;
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
		logger.info("Testing unit test " + test.getName());
		
		File testLoc = test.getCodeLocation();
		File sandboxLoc = new File(ProjectProperties.getInstance().getSandboxLocation() + "unitTest/" + test.getFileAppropriateName() + "/");
		
		try {
			if (sandboxLoc.exists()) {
				FileUtils.deleteDirectory(sandboxLoc);
			}
		} catch (IOException e) {
			logger.error("Could not delete existing test.", e);
			return;
		}
		
		String mainClass = test.getMainClassName();
		if(mainClass == null || mainClass.isEmpty()) {
			logger.error("No main test class for test " + test.getName());
			return;
		}
		
		sandboxLoc.mkdirs();
		
		JUnitTestRunner runner = new JUnitTestRunner();
		runner.setMainTestClassname(mainClass);
		runner.setFilterStackTraces(false);
		
		//TODO make generic
		runner.setTestDirectory("test");
		runner.setSubmissionDirectory("src");
		
		AntJob antJob = new AntJob(sandboxLoc, runner, "build", "test", "clean");
		antJob.addDependency("test", "build");
		
		antJob.addSetupTask(new DirectoryCopyTask(testLoc, sandboxLoc));
		antJob.addSetupTask(new UnzipTask(testForm.getFile(), sandboxLoc));
		
		antJob.run();
		
		AntResults results = antJob.getResults();
		
		Scanner scn = new Scanner(results.getOutput("build"));
		StringBuilder files = new StringBuilder();
		StringBuilder compErrors = new StringBuilder();
		String line = "";
		if(scn.hasNext()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				if(line.contains("Files to be compiled")) {
					break;
				}
			}
		}
		if(scn.hasNextLine()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				if(line.contains("error")) {
					break;
				}
				files.append(line.replaceFirst("\\s*\\[javac\\]", "").trim()).append('\n');
			}
			// Chop off the trailing "\n"
			files.replace(files.length()-1, files.length(), "");
		}
		if(scn.hasNextLine()) {
			compErrors.append(line.replaceFirst("\\s*\\[javac\\]", "")).append('\n');
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				compErrors.append(line.replaceFirst("\\s*\\[javac\\]", "")).append('\n');
			}
			// Chop off the trailing "\n"
			compErrors.replace(compErrors.length()-1, compErrors.length(), "");
		}
		scn.close();
		
		// Get results from ant output
		UnitTestResult utResults = ProjectProperties.getInstance().getResultDAO()
				.getUnitTestResultFromDisk(sandboxLoc.getAbsolutePath());
		if(utResults == null) {
			utResults = new UnitTestResult();
		}
		utResults.setTest(test);
		
		utResults.setFilesCompiled(files.toString());
		if(!results.isSuccess("build")) {
			utResults.setCompileErrors(compErrors.toString().replaceAll(Matcher.quoteReplacement(testLoc.getAbsolutePath()), ""));
		}
		
		utResults.setRuntimeError(results.hasRun("test") && !results.isSuccess("test"));
		utResults.setCleanError(!results.isSuccess("clean"));
		utResults.setRuntimeOutput(results.getFullOutput());
		
		test.setTestResult(utResults);
		
		ProjectProperties.getInstance().getResultDAO().save(utResults);
		ProjectProperties.getInstance().getUnitTestDAO().update(test);
		
		try {
			FileUtils.deleteDirectory(sandboxLoc);
		} catch (IOException e) {
			logger.error("Error deleting sandbox test at " + sandboxLoc);
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
			test.setMainClassName(null);
			
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
				
				// Automatically set the main class name according to any *Test.java files.
				FileTreeNode node = PASTAUtil.generateFileTree(codeLocation.getAbsolutePath());
				Stack<FileTreeNode> toExpand = new Stack<FileTreeNode>();
				toExpand.push(node);
				int dirStart = node.getLocation().length();
				
				while(!toExpand.isEmpty()) {
					FileTreeNode expandNode = toExpand.pop();
					String location = expandNode.getLocation().substring(dirStart);
					if(location.endsWith(".java")) {
						if(location.endsWith("Test.java")) {
							test.setMainClassName(location.substring(0, location.length()-5));
						}
					}
					if(!expandNode.isLeaf()) {
						for(FileTreeNode child : expandNode.getChildren()) {
							toExpand.push(child);
						}
					}
				}
			}

			// set it as not tested
			test.setTested(false);
			test.setTestResult(null);
			updateUnitTest(test);
		} catch (Exception e) {
			(new File(test.getFileLocation())).delete();
			logger.error("Unit test code for " + test.getName()
					+ " could not be updated successfully!", e);
		}
	}
}
