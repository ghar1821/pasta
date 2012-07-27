package info1103.project.util;

import info1103.project.domain.AllStudentAssessmentData;

import java.util.List;

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
			String url, String user, String pass, List tutors){
		templateLocation = tempLoc;
		arenasLocation = arenasLoc;
		submissionsLocation = subLoc;
		
		ProjectProperties.url = url;
		ProjectProperties.user = user;
		ProjectProperties.pass = pass;
		
		ProjectProperties.tutors = tutors;
		
		AllStudentAssessmentData.getInstance();
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
}
