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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
import pasta.domain.form.NewUnitTestForm;
import pasta.domain.form.TestUnitTestForm;
import pasta.domain.form.UpdateUnitTestForm;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.BlackBoxTestCase;
import pasta.domain.template.UnitTest;
import pasta.repository.AssessmentDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UnitTestDAO;
import pasta.testing.AntJob;
import pasta.testing.AntResults;
import pasta.testing.BlackBoxTestRunner;
import pasta.testing.CBlackBoxTestRunner;
import pasta.testing.CPPBlackBoxTestRunner;
import pasta.testing.JUnitTestRunner;
import pasta.testing.JavaBlackBoxTestRunner;
import pasta.testing.PythonBlackBoxTestRunner;
import pasta.testing.Runner;
import pasta.testing.task.CleanupSpecificFilesTask;
import pasta.testing.task.DirectoryCopyTask;
import pasta.testing.task.MakeDirectoryTask;
import pasta.testing.task.UnzipTask;
import pasta.util.Language;
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
	
	final static String BB_TEST_TEMPLATE = "PASTABlackBoxTest.template";
	final static String BB_TEST_METHOD_TEMPLATE = "PASTABlackBoxTestMethod.template";
	
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
			logger.error("Could not save unit test (" + thisTest.getClass().getSimpleName() + ") " + thisTest.getName(), e);
			return null;
		}
	}
	
	public void removeUnitTest(long id) {
		// Delete unit test from database
		try {
			assDao.unlinkUnitTest(id);
			resultDAO.unlinkUnitTest(id);
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
		
		// This results object will be updated with the results as we go
		UnitTestResult utResults = new UnitTestResult();
		utResults.setTest(test);
		test.setTestResult(utResults);
		
		File sandboxLoc = new File(ProjectProperties.getInstance().getSandboxLocation() + "unitTest/" + test.getFileAppropriateName() + "/");
		// Set up location where test will be run
		try {
			if (sandboxLoc.exists()) {
				logger.debug("Deleting existing sandbox location " + sandboxLoc);
				FileUtils.deleteDirectory(sandboxLoc);
			}
		} catch (IOException e) {
			utResults.addValidationError("Internal error: contact administrator.");
			logger.error("Could not delete existing test.", e);
			return;
		}
		logger.debug("Making directories to " + sandboxLoc);
		sandboxLoc.mkdirs();
		
		//Save submission to disk
		CommonsMultipartFile submission = testForm.getFile();
		File unzipTo = new File(sandboxLoc, "origSubmission/");
		logger.debug("Making directories to " + unzipTo);
		unzipTo.mkdirs();
		logger.debug("Unzipping " + submission.getOriginalFilename() + " to " + unzipTo);
		boolean saved = new UnzipTask(submission, unzipTo).go();
		if(!saved) {
			utResults.addValidationError("Internal error: contact administrator.");
			logger.error("Error saving submission to disk");
			return;
		}
		// Code we are interested in testing
		File importantCode = test.getSubmissionCodeLocation(unzipTo);
		logger.debug("Copying " + importantCode + " to " + sandboxLoc);
		new DirectoryCopyTask(importantCode, sandboxLoc).go();
		
		// Run any black box tests
		if(test.hasBlackBoxTests()) {
			String solutionName = testForm.getSolutionName();
			if(solutionName != null && !solutionName.isEmpty()) {
				runBlackBoxTests(test, solutionName, utResults, sandboxLoc, importantCode);
			}
		}
		
		try {
			logger.debug("Deleting " + unzipTo);
			FileUtils.deleteDirectory(unzipTo);
		} catch (IOException e) {
			utResults.addValidationError("Internal error: contact administrator.");
			logger.error("Error removing unnecessary submission files", e);
			return;
		}
		
		String mainClass = test.getMainClassName();
		if(test.hasCode() && mainClass != null && !mainClass.isEmpty()) {
			runJUnitTests(test, utResults, mainClass, sandboxLoc);
		}
		
		ProjectProperties.getInstance().getResultDAO().save(utResults);
		ProjectProperties.getInstance().getUnitTestDAO().update(test);
		
		logger.debug("Deleting final sandbox location " + sandboxLoc);
		try {
			FileUtils.deleteDirectory(sandboxLoc);
		} catch (IOException e) {
			logger.error("Error deleting sandbox test at " + sandboxLoc);
		}
	}
	
	public void runBlackBoxTests(UnitTest test, String solutionName,
			UnitTestResult utResults,
			File sandboxLoc, File submissionCode) {
		String mainClass = BB_TEST_TEMPLATE.substring(0, BB_TEST_TEMPLATE.lastIndexOf('.'));
		
		String base = test.getSubmissionCodeRoot();
		
		String[] submissionContents = PASTAUtil.listDirectoryContents(submissionCode);
		String shortest = null;
		for(String filename : submissionContents) {
			if(filename.matches(base + ".*" + solutionName + "\\.[^/\\\\]+")) {
				if(Language.getLanguage(filename) != null) {
					if(shortest == null || filename.length() < shortest.length()) {
						shortest = filename;
					}
				}
			}
		}
		Language subLanguage = Language.getLanguage(shortest);
		logger.debug("Submission language is " + subLanguage);
		
		if(subLanguage == null) {
			utResults.addValidationError("Language not recognised.");
			logger.error("Language not recognised.");
			return;
		}
		
		BlackBoxTestRunner runner = null;
		try {
			switch(subLanguage) {
			case JAVA:
				runner = new JavaBlackBoxTestRunner(); break;
			case C:
				runner = new CBlackBoxTestRunner(); break;
			case CPP:
				runner = new CPPBlackBoxTestRunner(); break;
			case PYTHON:
				runner = new PythonBlackBoxTestRunner(); break;
			default:
				utResults.addValidationError("Language not yet implemented.");
				logger.error("Language not implemented.");
				return;
			}
		} catch(FileNotFoundException e) {
			utResults.addValidationError("Internal error - test template not found.");
			logger.error("Test template not found.", e);
			return;
		}
		
		runner.setMainTestClassname(mainClass);
		runner.setFilterStackTraces(false);
		runner.setTestData(test.getTestCases());
		int totalTime = 1000;
		for(BlackBoxTestCase testCase : test.getTestCases()) {
			totalTime += testCase.getTimeout();
		}
		runner.setMaxRunTime(totalTime);
		runner.setSolutionName(solutionName);
		
		String[] targets = new String[] {"build", "run", "test", "clean"};
		File testLoc = test.getGeneratedCodeLocation();
		
		doTest(test, mainClass, runner, targets, testLoc, sandboxLoc, utResults);
	}
	
	public void runJUnitTests(UnitTest test, 
			UnitTestResult utResults,
			String mainClass, File sandboxLoc) {
		JUnitTestRunner runner = null;
		try {
			runner = new JUnitTestRunner();
		} catch (FileNotFoundException e) {
			utResults.addValidationError("Internal error - test template not found.");
			logger.error("Test template not found.", e);
			return;
		}
		runner.setMainTestClassname(mainClass);
		runner.setFilterStackTraces(false);
		String[] targets = new String[] {"build", "test", "clean"};
		File testLoc = test.getCodeLocation();
		doTest(test, mainClass, runner, targets, testLoc, sandboxLoc, utResults);
	}
	
	private void doTest(UnitTest test, String mainClassname, 
			Runner runner, String[] targets, File testCode, File sandboxLoc, 
			UnitTestResult utResults) {
		AntJob antJob = new AntJob(sandboxLoc, runner, targets);
		antJob.addDependency("test", "build");
		antJob.addDependency("run", "build");
		
		logger.debug("Copying " + testCode + " to " + sandboxLoc);
		antJob.addSetupTask(new DirectoryCopyTask(testCode, sandboxLoc));
		
		File binLoc = new File(sandboxLoc, "bin/");
		logger.debug("Making directories to " + binLoc);
		antJob.addSetupTask(new MakeDirectoryTask(binLoc));
		logger.debug("Making directories to " + new File(binLoc, UnitTest.BB_OUTPUT_FILENAME + File.separatorChar));
		antJob.addSetupTask(new MakeDirectoryTask(new File(binLoc, UnitTest.BB_OUTPUT_FILENAME + File.separatorChar)));
		
		antJob.addCleanupTask(new CleanupSpecificFilesTask(testCode, sandboxLoc, false));
		antJob.addCleanupTask(new CleanupSpecificFilesTask(testCode, binLoc, false));
		
		antJob.run();
		
		AntResults results = antJob.getResults();
		
		// Get results from ant output
		UnitTestResult thisResult = ProjectProperties.getInstance().getResultDAO()
				.getUnitTestResultFromDisk(sandboxLoc.getAbsolutePath());
		if(thisResult == null) {
			thisResult = new UnitTestResult();
		}
		
		thisResult.setFilesCompiled(runner.extractFilesCompiled(results));
		if(!results.isSuccess("build")) {
			thisResult.setBuildError(true);
			thisResult.setCompileErrors(runner.extractCompileErrors(results).replaceAll(Matcher.quoteReplacement(sandboxLoc.getAbsolutePath()), ""));
		}
		
		File errorFile = new File(sandboxLoc, "run.errors");
		if(errorFile.exists()) {
			String errorContents = PASTAUtil.scrapeFile(errorFile);
			thisResult.setRuntimeErrors(errorContents.replaceAll(Matcher.quoteReplacement(sandboxLoc.getAbsolutePath()), ""));
		}
		
		thisResult.setCleanError(!results.isSuccess("clean"));
		thisResult.setFullOutput(results.getFullOutput());
		
		utResults.combine(thisResult);
	}
	
	public void updateUnitTest(UnitTest test, UpdateUnitTestForm updateForm) {
		test.setName(updateForm.getName());
		test.setMainClassName(updateForm.getMainClassName());
		test.setSubmissionCodeRoot(updateForm.getSubmissionCodeRoot());
		
		List<BlackBoxTestCase> newCases = updateForm.getPlainTestCases();
		ListIterator<BlackBoxTestCase> newIt = newCases.listIterator();
		List<BlackBoxTestCase> oldCases = new LinkedList<>(test.getTestCases());
		while(newIt.hasNext()) {
			BlackBoxTestCase newTestCase = newIt.next();
			ListIterator<BlackBoxTestCase> oldIt = oldCases.listIterator();
			while(oldIt.hasNext()) {
				BlackBoxTestCase oldTestCase = oldIt.next();
				if(oldTestCase.equals(newTestCase)) {
					if(!oldTestCase.exactlyEquals(newTestCase)) {
						oldTestCase.setTestName(newTestCase.getTestName());
						oldTestCase.setCommandLine(newTestCase.getCommandLine());
						oldTestCase.setInput(newTestCase.getInput());
						oldTestCase.setOutput(newTestCase.getOutput());
						oldTestCase.setTimeout(newTestCase.getTimeout());
					}
					newIt.set(oldTestCase);
					oldIt.remove();
					break;
				}
			}
		}
		test.setTestCases(newCases);
		updateBlackBoxCode(test);
		createInputFiles(test);
		createExpectedOutputFiles(test);
		
		updateUnitTest(test);
	}

	private void updateBlackBoxCode(UnitTest test) {
		logger.info("Creating black box code for " + test.getName());
		
		FileUtils.deleteQuietly(test.getGeneratedCodeLocation());
		if(!test.hasBlackBoxTests()) {
			return;
		}
		
		File bbTemplateFile = null;
		try {
			bbTemplateFile = PASTAUtil.getTemplateResource("build_templates/" + BB_TEST_TEMPLATE);
		} catch (FileNotFoundException e) {
			logger.error("Could not create black box code.", e);
			return;
		}
		
		StringBuilder testContent = new StringBuilder();
		try {
			Scanner testScn = new Scanner(bbTemplateFile);
			while(testScn.hasNextLine()) {
				String line = testScn.nextLine();
				if(line.contains("${tests}")) {
					testContent.append(getTestsContent(test)).append(System.lineSeparator());
				} else {
					testContent.append(line).append(System.lineSeparator());
				}
			}
			testScn.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not read black box template at " + bbTemplateFile);
		}
		
		String filename = bbTemplateFile.getName().replace(".template", ".java");
		File testFile = new File(test.getGeneratedCodeLocation(), filename);
		if(!testFile.exists()) {
			testFile.getParentFile().mkdirs();
		}
		try {
			PrintWriter writer = new PrintWriter(testFile);
			writer.println(testContent.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			logger.error("Unable to write to " + testFile, e);
		}
		
		test.setTested(false);
	}
	
	private String getTestsContent(UnitTest test) {
		StringBuilder methodTemplate = new StringBuilder();
		File bbMethodTemplateFile = null;
		try {
			bbMethodTemplateFile = PASTAUtil.getTemplateResource("build_templates/" + BB_TEST_METHOD_TEMPLATE);
			Scanner methodScn = new Scanner(bbMethodTemplateFile);
			while(methodScn.hasNextLine()) {
				methodTemplate.append(methodScn.nextLine()).append(System.lineSeparator());
			}
			methodScn.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not read black box method template at " + bbMethodTemplateFile);
		} 
		
		StringBuilder allTests = new StringBuilder();
		for(BlackBoxTestCase testCase : test.getTestCases()) {
			if(testCase.hasValidName() && testCase.isToBeCompared()) {
				String content = new String(methodTemplate.toString());
				content = content.replaceAll("\\$\\{timeout\\}", String.valueOf(testCase.getTimeout()));
				content = content.replaceAll("\\$\\{testName\\}", testCase.getTestName());
				allTests.append(content);
			}
		}
		
		return allTests.toString();
	}
	
	private void createExpectedOutputFiles(UnitTest test) {
		File parentDir = new File(test.getGeneratedCodeLocation(), UnitTest.BB_EXPECTED_OUTPUT_FILENAME + File.separatorChar);
		parentDir.mkdirs();
		for(BlackBoxTestCase testCase : test.getTestCases()) {
			if(testCase.hasValidName() && testCase.isToBeCompared()) {
				File testOut = new File(parentDir, testCase.getTestName());
				try {
					PrintWriter writer = new PrintWriter(testOut);
					String output = testCase.getOutput();
					output = output.replaceAll("\r", "");
					writer.print(output);
					writer.close();
				} catch (FileNotFoundException e) {
					logger.error("Cannot write to " + testOut, e);
					break;
				}
			}
		}
	}
	
	private void createInputFiles(UnitTest test) {
		File parentDir = new File(test.getGeneratedCodeLocation(), UnitTest.BB_INPUT_FILENAME + File.separatorChar);
		parentDir.mkdirs();
		for(BlackBoxTestCase testCase : test.getTestCases()) {
			if(testCase.hasValidName()) {
				File testOut = new File(parentDir, testCase.getTestName());
				try {
					PrintWriter writer = new PrintWriter(testOut);
					String input = testCase.getInput();
					input = input.replaceAll("\r", "");
					writer.print(input);
					writer.close();
				} catch (FileNotFoundException e) {
					logger.error("Cannot write to " + testOut, e);
					break;
				}
			}
		}
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
				if(newLocation.getName().endsWith(".zip")) {
					PASTAUtil.extractFolder(newLocation.getAbsolutePath());
					try{
						FileUtils.forceDelete(newLocation);
					} catch (Exception e) {
						logger.error("Could not delete the zip for "
								+ test.getName());
					}
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
