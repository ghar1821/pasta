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

package pasta.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import pasta.login.DBAuthValidator;
import pasta.login.DummyAuthValidator;
import pasta.login.FTPAuthValidator;
import pasta.login.ImapAuthValidator;
import pasta.login.LDAPAuthValidator;
import pasta.repository.AssessmentDAO;
import pasta.repository.CompetitionDAO;
import pasta.repository.HandMarkingDAO;
import pasta.repository.LoginDAO;
import pasta.repository.PlayerDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UnitTestDAO;
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
@Component
public class ProjectProperties {
	protected final Log logger = LogFactory.getLog(getClass());

	private static ProjectProperties properties;

	// location of the project
	private String projectLocation;
	// location of the templates
	private String unitTestsLocation = "template" + File.separator + "unitTest" + File.separator;
	// location of the submissions
	private String submissionsLocation = "submissions" + File.separator;
	// location of the submissions
	private String competitionsLocation = "competitions" + File.separator;
	// location of the testing sandbox
	private String sandboxLocation = "sandbox" + File.separator;
	// auth type (dummy, imap, database)
	private String authType;
	// list of mail servers to auth with (imap auth only)
	private List<String> serverAddresses;
	// create a new account if not already assigned a class
	private Boolean createAccountOnSuccessfulLogin;
	// validator
	private Validator authenticationValidator;
	// proxy
	private Proxy proxy;
	
	private LoginDAO loginDAO;
	
	@Autowired
	private AssessmentDAO assessmentDAO;
	@Autowired
	private ResultDAO resultDAO;
	@Autowired
	private ResultManager resultManager;
	@Autowired
	private UnitTestDAO unitTestDAO;
	@Autowired
	private HandMarkingDAO handMarkingDAO;
	@Autowired
	private PlayerDAO playerDAO;
	@Autowired
	private CompetitionDAO compDAO;
	@Autowired
	private UserDAO userDAO;

	private ProjectProperties(Map<String, String> settings) {
		projectLocation = settings.get("location");
		if (projectLocation != null && !projectLocation.isEmpty() && !projectLocation.endsWith(File.separator)) {
			projectLocation += File.separator;
		}

		createAccountOnSuccessfulLogin = Boolean.parseBoolean(settings.get("createAccountOnSuccessfulLogin"));
		if (settings.get("proxydomain") != null && !settings.get("proxydomain").isEmpty()
				&& settings.get("proxyport") != null && !settings.get("proxyport").isEmpty()) {

			SocketAddress addr = new InetSocketAddress(settings.get("proxydomain"), Integer.parseInt(settings
					.get("proxyport")));
			this.proxy = new Proxy(Proxy.Type.HTTP, addr);
			logger.info("Using proxy " + settings.get("proxydomain") + " on port " + settings.get("proxyport"));
		}

		authType = settings.get("authentication").toLowerCase();
		serverAddresses = new LinkedList<String>();

		if (new File(getProjectLocation() + File.separator + "authentication.settings").exists()) {
			logger.info("exists");
			decryptAuthContent();
		}
		
		if(this.authType.toLowerCase().trim().equals("imap")){
			authenticationValidator = new ImapAuthValidator();
			logger.info("Using IMAP authentication");
		}
		else if(this.authType.toLowerCase().trim().equals("database")){
			authenticationValidator = new DBAuthValidator();
			((DBAuthValidator) authenticationValidator).setDAO(loginDAO);
			logger.info("Using database authentication");
		}
		else if(this.authType.toLowerCase().trim().equals("ftp")){
			authenticationValidator = new FTPAuthValidator();
			logger.info("Using ftp authentication");
		}
		else if(this.authType.toLowerCase().trim().equals("ldap")){
			authenticationValidator = new LDAPAuthValidator();
			logger.info("Using ldap authentication");
		} else {
			authenticationValidator = new DummyAuthValidator();
			logger.info("Using dummy authentication");
		}

		unitTestsLocation = checkPath(settings.get("pathUnitTests"), projectLocation + unitTestsLocation);
		submissionsLocation = checkPath(settings.get("pathSubmissions"), projectLocation + submissionsLocation);
		competitionsLocation = checkPath(settings.get("pathCompetitions"), projectLocation + competitionsLocation);
		sandboxLocation = checkPath(settings.get("pathSandbox"), projectLocation + sandboxLocation);

		logger.info("Project location set to: " + projectLocation);
		logger.info("UnitTests location set to: " + unitTestsLocation);
		logger.info("Submissions location set to: " + submissionsLocation);
		logger.info("Competitions Location set to: " + competitionsLocation);
		logger.info("Sandbox Location set to: " + sandboxLocation);
		
		ProjectProperties.properties = this;
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

	public String getProjectLocation() {
		return projectLocation;
	}

	public String getCompetitionsLocation() {
		return competitionsLocation;
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

	public String getAuthType() {
		return authType;
	}

	public List<String> getServerAddresses() {
		return serverAddresses;
	}

	public Boolean getCreateAccountOnSuccessfulLogin() {
		return createAccountOnSuccessfulLogin;
	}

	public Validator getAuthenticationValidator() {
		return authenticationValidator;
	}

	public void setDBDao(LoginDAO dao) {
		this.loginDAO = dao;
		if (authType.toLowerCase().trim().equals("database")) {
			((DBAuthValidator) authenticationValidator).setDAO(loginDAO);
		}
	}

	public void changeAuthMethod(String type, String[] addresses) {

		serverAddresses.clear();
		authType = type;
		for (String address : addresses) {
			if (!address.isEmpty()) {
				serverAddresses.add(address);
			}
		}
		if (authType.toLowerCase().trim().equals("imap")) {
			authenticationValidator = new ImapAuthValidator();
			logger.info("Using IMAP authentication");
		} else if (authType.toLowerCase().trim().equals("database")) {
			authenticationValidator = new DBAuthValidator();
			((DBAuthValidator) authenticationValidator).setDAO(loginDAO);
			logger.info("Using database authentication");
		} else if (authType.toLowerCase().trim().equals("ftp")) {
			authenticationValidator = new FTPAuthValidator();
			logger.info("Using ftp authentication");
		} else if (authType.toLowerCase().trim().equals("ldap")) {
			authenticationValidator = new LDAPAuthValidator();
			logger.info("Using ldap authentication");
		} else {
			authenticationValidator = new DummyAuthValidator();
			logger.info("Using dummy authentication");
		}

		encryptAuthContent();
	}

	/**
	 * Doesn't actually do any ecryption.
	 * <p>
	 * This should be fixed, but I didn't have the time to do it. Doesn't actually
	 * hold anything sensitive, but it should still not be held as plain text.
	 */
	private void encryptAuthContent() {

		try {
			PrintWriter out = new PrintWriter(new File(getProjectLocation() + "/authentication.settings"));

			out.println(authType);
			for (String address : serverAddresses) {
				out.println(address);
			}
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Doesn't actually do any decryption.
	 * <p>
	 * This should be fixed, but I didn't have the time to do it. Doesn't actually
	 * hold anything sensitive, but it should still not be held as plain text.
	 */
	private void decryptAuthContent() {

		try {
			Scanner in = new Scanner(new File(getProjectLocation() + File.separator + "authentication.settings"));

			authType = in.nextLine();
			serverAddresses.clear();
			while (in.hasNextLine()) {
				serverAddresses.add(in.nextLine());
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean usingProxy() {
		return proxy != null;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public AssessmentDAO getAssessmentDAO() {
		return assessmentDAO;
	}
	
	public LoginDAO getLoginDAO(){
		return loginDAO;
	}

	public ResultDAO getResultDAO() {
		return resultDAO;
	}
	public ResultManager getResultManager() {
		return resultManager;
	}

	public PlayerDAO getPlayerDAO() {
		return playerDAO;
	}

	public UnitTestDAO getUnitTestDAO() {
		return unitTestDAO;
	}

	public HandMarkingDAO getHandMarkingDAO() {
		return handMarkingDAO;
	}
	
	public CompetitionDAO getCompetitionDAO() {
		return compDAO;
	}
	
	public UserDAO getUserDAO() {
		return userDAO;
	}
}