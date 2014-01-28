package pasta.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import pasta.login.DBAuthValidator;
import pasta.login.DummyAuthValidator;
import pasta.login.FTPAuthValidator;
import pasta.login.ImapAuthValidator;
import pasta.repository.LoginDAO;

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
	
	private ProjectProperties(String projectLocation, String authType, Boolean createAccountOnSuccessfulLogin){
		initialize(projectLocation, authType, null, createAccountOnSuccessfulLogin);
	}
	
	private ProjectProperties(String projectLocation, String authType, List proxy, Boolean createAccountOnSuccessfulLogin){
		initialize(projectLocation, authType, proxy, createAccountOnSuccessfulLogin);
	}
	
	private void initialize(String projectLocation, String authType,
			List proxy, Boolean createAccountOnSuccessfulLogin) {
		this.projectLocation = projectLocation;
		this.authType=authType; // default to dummy
		serverAddresses = new LinkedList<String>();
		this.createAccountOnSuccessfulLogin = createAccountOnSuccessfulLogin;
		
		if(new File(getProjectLocation()+"/authentication.settings").exists()){
			logger.info("exists");
			decryptAuthContent();
		}
		
		if(this.authType.toLowerCase().trim().equals("imap")){
			authenticationValidator = new ImapAuthValidator();
			logger.info("Using IMAP authentication");
		}
		else if(this.authType.toLowerCase().trim().equals("database")){
			authenticationValidator = new DBAuthValidator();
			((DBAuthValidator)authenticationValidator).setDAO(loginDAO);
			logger.info("Using database authentication");
		}
		else if(this.authType.toLowerCase().trim().equals("ftp")){
			authenticationValidator = new FTPAuthValidator();
			logger.info("Using ftp authentication");
		}
		else{
			authenticationValidator = new DummyAuthValidator();
			logger.info("Using dummy authentication");
		}
		
		if(proxy != null && proxy.size()>=2){
			SocketAddress addr = new InetSocketAddress((String)proxy.get(0),
					Integer.parseInt((String) proxy.get(1)));
			this.proxy = new Proxy(Proxy.Type.HTTP, addr);
			logger.info("Using proxy " + proxy.get(0) + " on port " + proxy.get(1));
		}
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

	/**
	 * Code used to extract a zip file.
	 * @param zipFile - the file
	 * @throws ZipException
	 * @throws IOException
	 */
	static public void extractFolder(String zipFile) throws ZipException, IOException {
	    System.out.println(zipFile);
	    int BUFFER = 2048;
	    File file = new File(zipFile);

	    ZipFile zip = new ZipFile(file);
	    
	    String newPath = zipFile.substring(0, zipFile.lastIndexOf("/"));

	    new File(newPath).mkdir();
	    Enumeration zipFileEntries = zip.entries();

	    // Process each entry
	    while (zipFileEntries.hasMoreElements()){
	        // grab a zip file entry
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntry = entry.getName();
	        File destFile = new File(newPath, currentEntry);
	        //destFile = new File(newPath, destFile.getName());
	        File destinationParent = destFile.getParentFile();

	        // create the parent directory structure if needed
	        destinationParent.mkdirs();

	        if (!entry.isDirectory()){
	            BufferedInputStream is = new BufferedInputStream(zip
	            .getInputStream(entry));
	            int currentByte;
	            // establish buffer for writing file
	            byte data[] = new byte[BUFFER];

	            // write the current file to disk
	            FileOutputStream fos = new FileOutputStream(destFile);
	            BufferedOutputStream dest = new BufferedOutputStream(fos,
	            BUFFER);

	            // read and write until last byte is encountered
	            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
	                dest.write(data, 0, currentByte);
	            }
	            dest.flush();
	            dest.close();
	            fos.close();
	            is.close();
	        }

	        if (currentEntry.endsWith(".zip")){
	            // found a zip file, try to open
	            extractFolder(destFile.getAbsolutePath());
	        }
	    }
	    zip.close();
	}
	
	public static String formatDate(Date toFormat){
		return sdf.format(toFormat);
	}
	
	public static Date parseDate(String date) throws ParseException{
		return sdf.parse(date);
	}

	public void setDBDao(LoginDAO dao) {
		this.loginDAO = dao;
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
	
}