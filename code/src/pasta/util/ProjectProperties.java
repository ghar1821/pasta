package pasta.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

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

	private static ProjectProperties properties;
	
	// location of the templates (test code)
	private static String templateLocation;
	// location of the arenas (only for the battleship league segment)
	private static String arenasLocation;
	// location of the submissions
	private static String submissionsLocation;
	
	// MySQL data
	private static String url;
	private static String user;
	private static String pass;
	
	private static List<String> tutors;
	
	private static String java6location = null;
	
	/**
	 * The constructor is taken based on the config in
	 * applicationContext.xml
	 * @param tempLoc - template location
	 * @param arenasLoc - arena location
	 * @param subLoc - submission location
	 * @param url - url of the mysql database
	 * @param user - username of the mysql database
	 * @param pass - password of the mysql database
	 */
	private ProjectProperties(String tempLoc, String arenasLoc, String subLoc,
			String url, String user, String pass, List tutors, String java6Location){
		initialize(tempLoc, arenasLoc, subLoc, url, user, pass, tutors, java6Location);
	}
	
	private ProjectProperties(String tempLoc, String arenasLoc, String subLoc,
			String url, String user, String pass, List tutors){
		initialize(tempLoc, arenasLoc, subLoc, url, user, pass, tutors, null);
	}
	
	private void initialize(String tempLoc, String arenasLoc, String subLoc,
			String url, String user, String pass, List tutors, String java6Location){
		templateLocation = tempLoc;
		arenasLocation = arenasLoc;
		submissionsLocation = subLoc;
		
		ProjectProperties.url = url;
		ProjectProperties.user = user;
		ProjectProperties.pass = pass;
		
		ProjectProperties.tutors = tutors;
		ProjectProperties.java6location = java6Location;
	}
	
	private ProjectProperties(){
	}
	
	public static ProjectProperties getInstance(){
		if(properties == null){
			 properties = new ProjectProperties();
		}
		return properties;
	}
	
	public String getTemplateLocation(){
		return templateLocation;
	}
	
	public String getProjectLocation(){
		return templateLocation.replace("/template", "");
	}
	
	public String getArenaLocation(){
		return arenasLocation;
	}
	public String getSubmissionsLocation(){
		return submissionsLocation;
	}
	public String getUrl() {
		return url;
	}
	public String getUser() {
		return user;
	}
	public String getPass() {
		return pass;
	}
	public List<String> getTutors(){
		return tutors;
	}
	
	public String getJava6Location(){
		return java6location;
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
}
