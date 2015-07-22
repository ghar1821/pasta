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
import pasta.domain.template.BlackBoxTest;
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
import pasta.testing.task.DirectoryCopyTask;
import pasta.testing.task.FileOrDirectoryDeleteTask;
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
	
	private final String BB_TEST_TEMPLATE = "PASTABlackBoxTest.template";
	private final String BB_TEST_METHOD_TEMPLATE = "PASTABlackBoxTestMethod.template";
	
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
		
		UnitTest thisTest;
		switch(newTest.getType()) {
		case BLACK_BOX:
			thisTest = new BlackBoxTest(newTest.getName(), false);
			break;
		default:
			thisTest = new UnitTest(newTest.getName(), false);
		}

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
		
		String mainClass = test.getMainClassName();
		if(mainClass == null || mainClass.isEmpty()) {
			finishTesting(test, "Internal error: contact administrator.");
			logger.error("No main test class for test " + test.getName());
			return;
		}
		
		File testLoc = test.getCodeLocation();
		File sandboxLoc = new File(ProjectProperties.getInstance().getSandboxLocation() + "unitTest/" + test.getFileAppropriateName() + "/");
		
		try {
			if (sandboxLoc.exists()) {
				FileUtils.deleteDirectory(sandboxLoc);
			}
		} catch (IOException e) {
			finishTesting(test, "Internal error: contact administrator.");
			logger.error("Could not delete existing test.", e);
			return;
		}
		
		sandboxLoc.mkdirs();
		
		// Save submission to disk
		CommonsMultipartFile submission = testForm.getFile();
		File unzipTo = new File(sandboxLoc, "unzipped/");
		unzipTo.mkdirs();
		File submissionFile = new File(unzipTo, submission.getOriginalFilename());
		try {
			submission.getInputStream().close();
			submission.transferTo(submissionFile);
		} catch (IOException e) {
			finishTesting(test, "Internal error: contact administrator.");
			logger.error("Error moving submission initially", e);
			return;
		}
		
		Runner runner = null;
		String[] targets = null;
		
		if(test instanceof BlackBoxTest) {
			String solutionName = testForm.getSolutionName();
			String base = test.getSubmissionCodeRoot();
			
			Language subLanguage = null;
			try {
				String[] submissionContents = PASTAUtil.listZipContents(submissionFile);
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
				subLanguage = Language.getLanguage(shortest);
			} catch (IOException e) {
				// Maybe submission wasn't a zip
				subLanguage = Language.getLanguage(submissionFile);
			}
			
			if(subLanguage == null) {
				finishTesting(test, "Language not recognised");
				logger.error("Language not recognised.");
				return;
			}
			
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
					finishTesting(test, "Language not yet implemented");
					logger.error("Language not implemented.");
					return;
				}
			} catch(FileNotFoundException e) {
				finishTesting(test, "Internal error - test template not found.");
				logger.error("Test template not found.", e);
				return;
			}
			
			((BlackBoxTestRunner) runner).setMainTestClassname(mainClass);
			((BlackBoxTestRunner) runner).setFilterStackTraces(false);
			((BlackBoxTestRunner) runner).setTestData(((BlackBoxTest) test).getTestCases());
			int totalTime = 1000;
			for(BlackBoxTestCase testCase : ((BlackBoxTest) test).getTestCases()) {
				totalTime += testCase.getTimeout();
			}
			((BlackBoxTestRunner) runner).setMaxRunTime(totalTime);
			((BlackBoxTestRunner) runner).setSolutionName(solutionName);
			targets = new String[] {"build", "run", "test", "clean"};
		} else {
			try {
				runner = new JUnitTestRunner();
			} catch (FileNotFoundException e) {
				finishTesting(test, "Internal error - test template not found.");
				logger.error("Test template not found.", e);
				return;
			}
			((JUnitTestRunner) runner).setMainTestClassname(mainClass);
			((JUnitTestRunner) runner).setFilterStackTraces(false);
			targets = new String[] {"build", "test", "clean"};
		}
		
		AntJob antJob = new AntJob(sandboxLoc, runner, targets);
		antJob.addDependency("test", "build");
		antJob.addDependency("run", "build");
		
		
		antJob.addSetupTask(new UnzipTask(submissionFile, unzipTo));
		File importantCode = test.getSubmissionCodeLocation(unzipTo);
		antJob.addSetupTask(new DirectoryCopyTask(importantCode, sandboxLoc));
		antJob.addSetupTask(new FileOrDirectoryDeleteTask(unzipTo));
		antJob.addSetupTask(new DirectoryCopyTask(testLoc, sandboxLoc));
		
		File binLoc = new File(sandboxLoc, "bin/");
		antJob.addSetupTask(new MakeDirectoryTask(binLoc));
		antJob.addSetupTask(new MakeDirectoryTask(new File(binLoc, "userout/")));
		
		antJob.run();
		
		AntResults results = antJob.getResults();
		
		// Get results from ant output
		UnitTestResult utResults = ProjectProperties.getInstance().getResultDAO()
				.getUnitTestResultFromDisk(sandboxLoc.getAbsolutePath());
		if(utResults == null) {
			utResults = new UnitTestResult();
		}
		utResults.setTest(test);
		
		utResults.setFilesCompiled(runner.extractFilesCompiled(results));
		if(!results.isSuccess("build")) {
			utResults.setBuildError(true);
			utResults.setCompileErrors(runner.extractCompileErrors(results).replaceAll(Matcher.quoteReplacement(sandboxLoc.getAbsolutePath()), ""));
		}
		
		utResults.setRuntimeError(results.hasRun("test") && !results.isSuccess("test"));
		utResults.setCleanError(!results.isSuccess("clean"));
		utResults.setRuntimeOutput(results.getFullOutput());
		
		test.setTestResult(utResults);
		
		ProjectProperties.getInstance().getResultDAO().save(utResults);
		ProjectProperties.getInstance().getUnitTestDAO().update(test);
		
//		try {
//			FileUtils.deleteDirectory(sandboxLoc);
//		} catch (IOException e) {
//			logger.error("Error deleting sandbox test at " + sandboxLoc);
//		}
	}
	private void finishTesting(UnitTest test, String errorMessage) {
		UnitTestResult results = new UnitTestResult();
		results.addValidationError(errorMessage);
		test.setTestResult(results);
		ProjectProperties.getInstance().getResultDAO().save(results);
		ProjectProperties.getInstance().getUnitTestDAO().update(test);
	}

	public void updateUnitTest(UnitTest test, UpdateUnitTestForm updateForm) {
		test.setName(updateForm.getName());

		if(test instanceof BlackBoxTest) {
			BlackBoxTest bbTest = (BlackBoxTest) test;
			List<BlackBoxTestCase> newCases = updateForm.getPlainTestCases();
			ListIterator<BlackBoxTestCase> newIt = newCases.listIterator();
			List<BlackBoxTestCase> oldCases = new LinkedList<>(bbTest.getTestCases());
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
			bbTest.setTestCases(newCases);
			updateBlackBoxCode(bbTest);
			createInputFiles(bbTest);
			createExpectedOutputFiles(bbTest);
		} else {
			test.setMainClassName(updateForm.getMainClassName());
			test.setSubmissionCodeRoot(updateForm.getSubmissionCodeRoot());
		}
		updateUnitTest(test);
	}

	private void updateBlackBoxCode(BlackBoxTest test) {
		logger.info("Creating black box code for " + test.getName());
		
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
		File testFile = new File(test.getCodeLocation(), filename);
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
		
		test.setMainClassName(bbTemplateFile.getName().substring(0, bbTemplateFile.getName().lastIndexOf('.')));
		test.setTested(false);
	}
	
	private String getTestsContent(BlackBoxTest test) {
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
			if(testCase.hasValidName()) {
				String content = new String(methodTemplate.toString());
				content = content.replaceAll("\\$\\{timeout\\}", String.valueOf(testCase.getTimeout()));
				content = content.replaceAll("\\$\\{testName\\}", testCase.getTestName());
				allTests.append(content);
			}
		}
		
		return allTests.toString();
	}
	
	private void createExpectedOutputFiles(BlackBoxTest test) {
		File parentDir = new File(test.getCodeLocation(), "bbexpected/");
		parentDir.mkdirs();
		for(BlackBoxTestCase testCase : test.getTestCases()) {
			if(testCase.hasValidName()) {
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
	
	private void createInputFiles(BlackBoxTest test) {
		File parentDir = new File(test.getCodeLocation(), "bbinput/");
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
