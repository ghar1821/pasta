package pasta.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
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
		
		String subDate = PASTAUtil.formatDate(new Date());
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
		String validatorName = validator.getName().substring(0, validator.getName().lastIndexOf('.'));
		
		StringBuilder classpath = new StringBuilder();
		for(URL url : ((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs()) {
			classpath.append(url.getPath()).append(':');
		}
		classpath.append('.');
		
		File validatorClass = new File(validator.getParentFile(), validatorName + ".class");
		if(!validatorClass.exists()) {
			// Try to compile user validator
			StringWriter errorsString = new StringWriter();
			PrintWriter errors = new PrintWriter(errorsString);
			com.sun.tools.javac.Main.compile(new String[] {"-classpath", classpath.toString(), validator.getAbsolutePath()}, errors);
			
			// If compile errors are found, show to a tutor
			String compileError = errorsString.toString().trim().replaceAll(Matcher.quoteReplacement(validator.getParentFile().getAbsolutePath()), "");
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
			loader = new URLClassLoader(new URL[] {validator.getParentFile().toURI().toURL()}, parentLoader);
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
