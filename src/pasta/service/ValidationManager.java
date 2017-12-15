/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.form.Submission;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.validation.PASTASubmissionValidator;
import pasta.service.validation.SubmissionValidationResult;
import pasta.service.validation.ValidationFeedback;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Validation manager.
 * <br/>
 * This class serves to manage the running of 
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 29 Jul 2015
 *
 */
@Service("validationManager")
@Repository
public class ValidationManager {

	@Autowired SubmissionManager submissionManager;
	
	public SubmissionValidationResult validate(PASTAUser user, Assessment assessment, Submission submission) {
		if(!assessment.isCustomValidator()) {
			return null;
		}
		
		String subDate = PASTAUtil.formatDate(submission.getSubmissionDate());
		File sandboxLoc = new File(ProjectProperties.getInstance().getSandboxLocation(), "validate/" + user.getUsername() + "/" + assessment.getId() + "/" + subDate);
		sandboxLoc.mkdirs();
		File savedLoc = submissionManager.saveSubmissionToDisk(user, submission);
		if(savedLoc == null) {
			return null;
		}
		try {
			FileUtils.copyDirectory(savedLoc, sandboxLoc);
		} catch (IOException e) {
			return null;
		}
		
		File validator = assessment.getCustomValidator();
		File parentDir = validator.getParentFile();
		String validatorName = PASTAUtil.extractQualifiedName(validator);
		
		File validatorClass = PASTAUtil.getClassFileForQualifiedClassName(parentDir, validatorName);
		if(validatorClass == null) {
			StringBuilder classpath = new StringBuilder();
			for(URL url : ((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs()) {
				classpath.append(url.getPath()).append(':');
			}
			classpath.append('.');
			
			// Try to compile user validator
			StringWriter errorsString = new StringWriter();
			PrintWriter errors = new PrintWriter(errorsString);
			com.sun.tools.javac.Main.compile(new String[] {"-d", validator.getParentFile().getAbsolutePath(), "-classpath", classpath.toString(), validator.getAbsolutePath()}, errors);
			
			// If compile errors are found, show to a tutor
			String compileError = errorsString.toString().trim().replaceAll(Matcher.quoteReplacement(parentDir.getAbsolutePath()), "");
			if(compileError != null && !compileError.isEmpty()) {
				SubmissionValidationResult result = new SubmissionValidationResult(assessment.getName());
				List<ValidationFeedback> errorsList = new LinkedList<ValidationFeedback>();
				ValidationFeedback feedback = null;
				if(user.isTutor()) {
					feedback = new ValidationFeedback("Validator Compile Error", compileError);
					feedback.setPreFormat(true);
				} else {
					feedback = new ValidationFeedback("Assessment validator setup error", "Please inform an instructor that this error has occured.");
				}
				errorsList.add(feedback);
				result.setErrors(errorsList);
				return result;
			}
		}
		
	    ClassLoader parentLoader = PASTASubmissionValidator.class.getClassLoader();
	    URLClassLoader loader = null;
		try {
			loader = new URLClassLoader(new URL[] {parentDir.toURI().toURL()}, parentLoader);
		} catch (MalformedURLException e) {
			Logger.getLogger(getClass()).error("Could not load custom validator class.", e);
			return null;
		}
	    PASTASubmissionValidator val = null;
		try {
			val = (PASTASubmissionValidator) loader.loadClass(validatorName).newInstance();
			loader.close();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException e) {
			Logger.getLogger(getClass()).error("Could not load custom validator class.", e);
			return null;
		}
		
		SubmissionValidationResult result = new SubmissionValidationResult(assessment.getName());
		result.setErrors(val.getErrors(user.getUsername(), sandboxLoc));
		result.setFeedback(val.getFeedback(user.getUsername(), sandboxLoc));
		
		FileUtils.deleteQuietly(sandboxLoc);
		
		return result;
	}
}
