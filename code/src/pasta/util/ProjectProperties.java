package pasta.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import pasta.login.DBAuthValidator;
import pasta.login.DummyAuthValidator;
import pasta.login.ImapAuthValidator;

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
	private static List<String> emailAddresses;
	// create a new account if not already assigned a class
	private static Boolean createAccountOnSuccessfulLogin;
	// validator
	private static Validator authenticationValidator;
	
	private ProjectProperties(String projectLocation, String authType, Boolean createAccountOnSuccessfulLogin){
		initialize(projectLocation, authType, null, createAccountOnSuccessfulLogin);
	}
	
	private ProjectProperties(String projectLocation, String authType, List emailAddresses, Boolean createAccountOnSuccessfulLogin){
		initialize(projectLocation, authType, emailAddresses, createAccountOnSuccessfulLogin);
	}
	
	private void initialize(String projectLocation, String authType,
			List emailAddresses, Boolean createAccountOnSuccessfulLogin) {
		this.projectLocation = projectLocation;
		this.authType=authType; // default to dummy
		this.emailAddresses = emailAddresses;
		this.createAccountOnSuccessfulLogin = createAccountOnSuccessfulLogin;
		
		if(authType.toLowerCase().trim().equals("imap")){
			authenticationValidator = new ImapAuthValidator();
			logger.info("Using IMAP authentication");
		}
		else if(authType.toLowerCase().trim().equals("database")){
			authenticationValidator = new DBAuthValidator();
			logger.info("Using database authentication");
		}
		else{
			authenticationValidator = new DummyAuthValidator();
			logger.info("Using dummy authentication");
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

	public List<String> getEmailAddresses() {
		return emailAddresses;
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
}
