/**
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
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
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
import pasta.repository.LoginDAO;
import pasta.repository.PlayerDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UserDAO;

@Component
/**
 * The project properties.
 * 
 * Uses singleton pattern.
 * 
 * @author Alex
 *
 */
public class ProjectProperties {
	protected final Log logger = LogFactory.getLog(getClass());
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");

	private static ProjectProperties properties;
	
	// location of the project
	private static String projectLocation;
	// auth type (dummy, imap, database)
	private static String authType;
	// list of mail servers to auth with (imap auth only)
	private static List<String> serverAddresses;
	// create a new account if not already assigned a class
	private static Boolean createAccountOnSuccessfulLogin;
	// validator
	private static Validator authenticationValidator;
	// proxy
	private static Proxy proxy;
	
	private static LoginDAO loginDAO;
	private static AssessmentDAO assessmentDAO;
	private static ResultDAO resultDAO;
	private static PlayerDAO playerDAO;
	
	private ProjectProperties(String projectLocation, String authType, Boolean createAccountOnSuccessfulLogin){
		initialize(projectLocation, authType, null, createAccountOnSuccessfulLogin);
	}
	
	private ProjectProperties(String projectLocation, String authType, List proxy, Boolean createAccountOnSuccessfulLogin){
		initialize(projectLocation, authType, proxy, createAccountOnSuccessfulLogin);
	}
	
	private void initialize(String projectLocation, String authType,
			List proxy, Boolean createAccountOnSuccessfulLogin) {
		ProjectProperties.projectLocation = projectLocation;
		ProjectProperties.authType=authType; // default to dummy
		serverAddresses = new LinkedList<String>();
		ProjectProperties.createAccountOnSuccessfulLogin = createAccountOnSuccessfulLogin;
		
		if(new File(getProjectLocation()+"/authentication.settings").exists()){
			logger.info("exists");
			decryptAuthContent();
		}
		
		if(ProjectProperties.authType.toLowerCase().trim().equals("imap")){
			authenticationValidator = new ImapAuthValidator();
			logger.info("Using IMAP authentication");
		}
		else if(ProjectProperties.authType.toLowerCase().trim().equals("database")){
			authenticationValidator = new DBAuthValidator();
			((DBAuthValidator)authenticationValidator).setDAO(loginDAO);
			logger.info("Using database authentication");
		}
		else if(ProjectProperties.authType.toLowerCase().trim().equals("ftp")){
			authenticationValidator = new FTPAuthValidator();
			logger.info("Using ftp authentication");
		}
		else if(ProjectProperties.authType.toLowerCase().trim().equals("ldap")){
			authenticationValidator = new LDAPAuthValidator();
			logger.info("Using ldap authentication");
		}
		else{
			authenticationValidator = new DummyAuthValidator();
			logger.info("Using dummy authentication");
		}
		
		if(proxy != null && proxy.size()>=2){
			SocketAddress addr = new InetSocketAddress((String)proxy.get(0),
					Integer.parseInt((String) proxy.get(1)));
			ProjectProperties.proxy = new Proxy(Proxy.Type.HTTP, addr);
			logger.info("Using proxy " + proxy.get(0) + " on port " + proxy.get(1));
		}
		
		assessmentDAO = new AssessmentDAO();
		resultDAO = new ResultDAO(assessmentDAO);
		playerDAO = new PlayerDAO();
	}

	
	private ProjectProperties(){
	}
	
	public static ProjectProperties getInstance(){
		if(properties == null){
			 properties = new ProjectProperties();
		}
		return properties;
	}
	
	
	public String getProjectLocation(){
		return projectLocation;
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
		ProjectProperties.loginDAO = dao;
		if(authType.toLowerCase().trim().equals("database")){
			((DBAuthValidator)authenticationValidator).setDAO(loginDAO);
		}
	}

	public void changeAuthMethod(String type, String[] addresses) {
		
		serverAddresses.clear();
		authType = type;
		for(String address: addresses){
			if(!address.isEmpty()){
				serverAddresses.add(address);
			}
		}
		if(authType.toLowerCase().trim().equals("imap")){
			authenticationValidator = new ImapAuthValidator();
			logger.info("Using IMAP authentication");
		}
		else if(authType.toLowerCase().trim().equals("database")){
			authenticationValidator = new DBAuthValidator();
			((DBAuthValidator)authenticationValidator).setDAO(loginDAO);
			logger.info("Using database authentication");
		}
		else if(authType.toLowerCase().trim().equals("ftp")){
			authenticationValidator = new FTPAuthValidator();
			logger.info("Using ftp authentication");
		}
		else if(authType.toLowerCase().trim().equals("ldap")){
			authenticationValidator = new LDAPAuthValidator();
			logger.info("Using ldap authentication");
		}
		else{
			authenticationValidator = new DummyAuthValidator();
			logger.info("Using dummy authentication");
		}
		
		encryptAuthContent();
	}
	
	private void encryptAuthContent(){

		try {
			PrintWriter out = new PrintWriter(new File(getProjectLocation()+"/authentication.settings"));
			
			out.println(authType);
			for(String address: serverAddresses){
				out.println(address);
			}
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void decryptAuthContent(){

		try {
			Scanner in = new Scanner(new File(getProjectLocation()+"/authentication.settings"));
			
			authType = in.nextLine();
			serverAddresses.clear();
			while(in.hasNextLine()){
				serverAddresses.add(in.nextLine());
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean usingProxy(){
		return proxy != null;
	}
	
	public Proxy getProxy(){
		return proxy;
	}
	
	public AssessmentDAO getAssessmentDAO(){
		return assessmentDAO;
	}
	
	public LoginDAO getLoginDAO(){
			return loginDAO;
	}
	
	public ResultDAO getResultDAO(){
		return resultDAO;
	}
	
	public PlayerDAO getPlayerDAO(){
		return playerDAO;
	}
}