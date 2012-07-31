package info1103.project.domain;

import info1103.project.repository.AssessmentDAO;
import info1103.project.repository.UserDAO;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Caching system for to hold all of the student's assessment data.
 * Used so viewing all of the student's assessment data does not take too long for large classes.
 * 
 * Using the singleton design pattern.
 * 
 * @author Alex
 *
 */
public class AllStudentAssessmentData {
	// itself
	private static AllStudentAssessmentData data = null;
	
	// all of the student Data
	private Map<User, Map<String, Assessment>> studentData;
	// the data access object for users
	private UserDAO userDao = new UserDAO();
	// the data aceess object for the assessments
	private AssessmentDAO assDao = new AssessmentDAO();
	
	/**
	 * Constructor.
	 * 
	 * It will read all of the current student's assessment data.
	 * 
	 * It will take a while to run, but it should only be run at the start of the
	 * server.
	 */
	private AllStudentAssessmentData(){
		String[] allUsers = userDao.getUserList();
		studentData = new TreeMap<User, Map<String, Assessment>>();
		for(String unikey: allUsers){
			User person = userDao.getUser(unikey);
			studentData.put(person, assDao.getAssessments(unikey));
		}
	}
	
	/**
	 * Get the instance.
	 * 
	 * @return the instance of AllStudentAssessmentData
	 */
	public static AllStudentAssessmentData getInstance(){
		if(data == null){
			data = new AllStudentAssessmentData();
		}
		return data;
	}
	
	/**
	 * Update the student data with the given assessments.
	 * @param unikey - unikey of the student
	 * @param assessments - List of assessment names.
	 */
	public void updateStudent(String unikey, List<String> assessments){
		User person = userDao.getUser(unikey);
		for(String assessment: assessments){
			if(studentData.get(person) == null){
				TreeMap<String, Assessment> allAssessments = new TreeMap<String, Assessment>();
				allAssessments.put(assessment, assDao.getAssessment(assessment, unikey));
				studentData.put(person, allAssessments);
			}
			else{
				studentData.get(person).put(assessment, assDao.getAssessment(assessment, unikey));
			}
		}
	}
	
	/**
	 * Update the student data with the given assessment.
	 * @param unikey - unikey of the student
	 * @param assessments - assessment name.
	 */
	public void updateStudent(String unikey, String assessment){
		User person = userDao.getUser(unikey);
		if(studentData.get(person) == null){
			TreeMap<String, Assessment> assessments = new TreeMap<String, Assessment>();
			assessments.put(assessment, assDao.getAssessment(assessment, unikey));
			studentData.put(person, assessments);
		}
		else{
			studentData.get(person).put(assessment, assDao.getAssessment(assessment, unikey));
		}
	}
	
	/**
	 * Update the student data, reloading all assessments.
	 * @param unikey - unikey of the student
	 */
	public void updateStudent(String unikey){
		User person = userDao.getUser(unikey);
		studentData.put(person, assDao.getAssessments(unikey));
	}
	
	/**
	 * Get the cached data.
	 * @return cached data.
	 */
	public Map<User, Map<String, Assessment>> getData(){
		return studentData;
	}
}
