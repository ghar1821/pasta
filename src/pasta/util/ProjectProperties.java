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

package pasta.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import pasta.domain.UserPermissionLevel;
import pasta.domain.security.AuthenticationSettings;
import pasta.domain.user.PASTAUser;
import pasta.login.DBAuthValidator;
import pasta.login.DummyAuthValidator;
import pasta.login.FTPAuthValidator;
import pasta.login.ImapAuthValidator;
import pasta.login.LDAPAuthValidator;
import pasta.repository.LoginDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UserDAO;
import pasta.service.ResultManager;

/**
 * The project properties.
 * <p>
 * Uses singleton pattern. Pretty self explanatory. It just holds all of the
 * configuration that is used by the system.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 */
@Component("projectProperties")
public class ProjectProperties {
	protected final Log logger = LogFactory.getLog(getClass());

	private static ProjectProperties properties;

	// name of the project
	private String name;
	// location of the project (inside the docker container)
	private String projectLocation;
	// location of the templates
	private String unitTestsLocation = "template" + File.separator + "unitTest" + File.separator;
	// location of the submissions
	private String submissionsLocation = "submissions" + File.separator;
	// location of the testing sandbox
	private String sandboxLocation = "sandbox" + File.separator;
	// location of validator uploads
	private String validatorLocation = "validation" + File.separator;
	
	// Location on the host where the project is kept
	private String hostLocation;
	
	private AuthenticationSettings authSettings;
	
	// create a new account if not already assigned a class
	private Boolean createAccountOnSuccessfulLogin;
	// validator
	private Validator authenticationValidator;
	// proxy
	private Proxy proxy;
	
	private String initialInstructor;
	
	private Long instanceID;
	
	@Autowired
	private LoginDAO loginDAO;
	@Autowired
	private ResultDAO resultDAO;
	@Autowired
	private ResultManager resultManager;
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private ServletContext servletContext;

	private String settingsAuthType;
	
	@Autowired
	private ProjectProperties(@Qualifier("projectSettings") Properties settings) {
		name = settings.getProperty("name");
		
		projectLocation = settings.getProperty("location");
		if (projectLocation != null && !projectLocation.isEmpty() && !projectLocation.endsWith(File.separator)) {
			projectLocation += File.separator;
		}

		createAccountOnSuccessfulLogin = Boolean.parseBoolean(settings.getProperty("createAccountOnSuccessfulLogin"));
		if (settings.getProperty("proxydomain") != null && !settings.getProperty("proxydomain").isEmpty()
				&& settings.getProperty("proxyport") != null && !settings.getProperty("proxyport").isEmpty()) {

			SocketAddress addr = new InetSocketAddress(settings.getProperty("proxydomain"), Integer.parseInt(settings
					.getProperty("proxyport")));
			this.proxy = new Proxy(Proxy.Type.HTTP, addr);
			logger.info("Using proxy " + settings.getProperty("proxydomain") + " on port " + settings.getProperty("proxyport"));
		}

		// Store for use in post construct method
		settingsAuthType = settings.getProperty("authentication");
		
		unitTestsLocation = checkPath(settings.getProperty("pathUnitTests"), projectLocation + unitTestsLocation);
		submissionsLocation = checkPath(settings.getProperty("pathSubmissions"), projectLocation + submissionsLocation);
		sandboxLocation = checkPath(settings.getProperty("pathSandbox"), projectLocation + sandboxLocation);
		validatorLocation = checkPath(settings.getProperty("pathValidation"), projectLocation + validatorLocation);

		logger.info("Project location set to: " + projectLocation);
		logger.info("UnitTests location set to: " + unitTestsLocation);
		logger.info("Submissions location set to: " + submissionsLocation);
		logger.info("Sandbox Location set to: " + sandboxLocation);
		logger.info("Validators Location set to: " + validatorLocation);
		
		this.initialInstructor = settings.getProperty("initialInstructor");
		
		this.hostLocation = settings.getProperty("hostLocation");
		logger.info("Host content location set to \"" + hostLocation + "\". Note that this cannot be verified, so an incorrect value may cause unexpected errors.");
		
		try {
			instanceID = Long.parseLong(settings.getProperty("instanceID"));
		} catch(NumberFormatException | NullPointerException e) {}
		
		if(instanceID == null) {
			File instanceFile = new File(projectLocation, "instance.id");
			
			if(instanceFile.exists()) {
				try {
					String content = PASTAUtil.scrapeFile(instanceFile).trim();
					instanceID = Long.parseLong(content);
				} catch(NumberFormatException e) {logger.info("Not parsable.");}
			}
			
			if(instanceID == null) {
				// Generate new positive long
				instanceID = new Long(new Random().nextLong() & ((-1L) >>> 1));
				try {
					FileOutputStream os = new FileOutputStream(instanceFile);
					os.write(String.valueOf(instanceID).getBytes());
					os.close();
				} catch (IOException e) {
					logger.error("Could not write instance ID to file", e);
				}
			}
		}
		
		ProjectProperties.properties = this;
	}
	
	@PostConstruct
	private void afterInit() {
		// Copy the lib folder to content
		try {
			PASTAUtil.getTemplateResource("lib/");
		} catch (FileNotFoundException e) {
			// ignore: no lib folder to copy
		}
		
		authSettings = loginDAO.getAuthSettings();
		if(authSettings == null) {
			String authType = settingsAuthType;
			if(authType == null) {
				authType = "dummy";
			}
			authType = authType.trim().toLowerCase();
			authSettings = loginDAO.createAuthSettings(authType, new LinkedList<String>());
		}
		
		String authType = authSettings.getType();
		if(authType.equals("imap")){
			authenticationValidator = new ImapAuthValidator();
			logger.info("Using IMAP authentication");
		}
		else if(authType.equals("database")){
			authenticationValidator = new DBAuthValidator();
			((DBAuthValidator) authenticationValidator).setDAO(loginDAO);
			logger.info("Using database authentication");
		}
		else if(authType.equals("ftp")){
			authenticationValidator = new FTPAuthValidator();
			logger.info("Using ftp authentication");
		}
		else if(authType.equals("ldap")){
			authenticationValidator = new LDAPAuthValidator();
			logger.info("Using ldap authentication");
		} else {
			authenticationValidator = new DummyAuthValidator();
			logger.info("Using dummy authentication");
		}
		
		if(getInitialInstructor() != null && !getInitialInstructor().isEmpty()) {
			PASTAUser initial = userDAO.getUser(getInitialInstructor());
			if(initial == null) {
				initial = new PASTAUser();
				initial.setActive(true);
				initial.setUsername(getInitialInstructor());
				initial.setPermissionLevel(UserPermissionLevel.INSTRUCTOR);
				logger.info("Creating new user as initial instructor: " + initial.getUsername());
				userDAO.add(initial);
			} else {
				initial.setActive(true);
				initial.setPermissionLevel(UserPermissionLevel.INSTRUCTOR);
				logger.info("Updating existing user as initial instructor: " + initial.getUsername());
				userDAO.update(initial);
			}
		}
	}
	
	/**
	 * Test a new path for validity. Create the path if it does not already exist.
	 * 
	 * Generated path will have a trailing '/'.
	 * 
	 * @param path The new location for this directory.
	 * @param defaultPath The default location for this directory.
	 * @return The validated path.
	 */
	private String checkPath(String path, String defaultPath) {
		String newPath = null;
		if (path == null || path.isEmpty()) {
			newPath = defaultPath;
		} else {
			newPath = path;
		}
		File location = new File(newPath);
		if (!location.exists()) {
			try {
				Files.createDirectories(location.toPath());
			} catch (IOException e) {
				logger.error("Directory \"" + newPath + "\"could not be created.");
			}
		}

		if (newPath != null && !newPath.isEmpty() && !newPath.endsWith(File.separator)) {
			newPath += File.separator;
		}
		return newPath;
	}

	private ProjectProperties() {
	}
	
	public static ProjectProperties getInstance(){
		return properties;
	}

	public String getName() {
		return name;
	}
	
	public String getProjectLocation() {
		return projectLocation;
	}

	public String getSubmissionsLocation() {
		return submissionsLocation;
	}

	public String getUnitTestsLocation() {
		return unitTestsLocation;
	}
	
	public String getSandboxLocation() {
		return sandboxLocation;
	}
	
	public String getValidatorLocation() {
		return validatorLocation;
	}
	
	public String getAssessmentValidatorLocation() {
		return getValidatorLocation() + "assessments/";
	}

	public AuthenticationSettings getAuthenticationSettings() {
		return authSettings;
	}

	public Boolean getCreateAccountOnSuccessfulLogin() {
		return createAccountOnSuccessfulLogin;
	}

	public Validator getAuthenticationValidator() {
		return authenticationValidator;
	}

	public void setDBDao(LoginDAO dao) {
		this.loginDAO = dao;
		if (authSettings.getType().equals("database")) {
			((DBAuthValidator) authenticationValidator).setDAO(loginDAO);
		}
	}

	public void changeAuthMethod(String type, String[] addresses) {
		authSettings.getServerAddresses().clear();
		authSettings.setType(type);
		for (String address : addresses) {
			if (!address.isEmpty()) {
				authSettings.getServerAddresses().add(address);
			}
		}
		String authType = authSettings.getType();
		if (authType.equals("imap")) {
			authenticationValidator = new ImapAuthValidator();
			logger.info("Using IMAP authentication");
		} else if (authType.equals("database")) {
			authenticationValidator = new DBAuthValidator();
			((DBAuthValidator) authenticationValidator).setDAO(loginDAO);
			logger.info("Using database authentication");
		} else if (authType.equals("ftp")) {
			authenticationValidator = new FTPAuthValidator();
			logger.info("Using ftp authentication");
		} else if (authType.equals("ldap")) {
			authenticationValidator = new LDAPAuthValidator();
			logger.info("Using ldap authentication");
		} else {
			authenticationValidator = new DummyAuthValidator();
			logger.info("Using dummy authentication");
		}

		loginDAO.updateAuthSettings(authSettings);
	}

	public boolean usingProxy() {
		return proxy != null;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public ResultDAO getResultDAO() {
		return resultDAO;
	}
	public ResultManager getResultManager() {
		return resultManager;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public String getInitialInstructor() {
		return initialInstructor;
	}

	public String getHostLocation() {
		return hostLocation;
	}
	
	public long getInstanceId() {
		return instanceID;
	}
}