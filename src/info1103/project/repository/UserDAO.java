package info1103.project.repository;

import info1103.project.domain.User;
import info1103.project.util.ProjectProperties;

import java.io.File;
import java.util.Arrays;
/**
 * The Data access object for the User class.
 * @author Alex
 *
 */
public class UserDAO {
	// TODO - find a better way of doing this. WASM?
	
	/**
	 * Return the user.
	 * @param unikey - the unkey of the student.
	 * @return the user.
	 */
	public User getUser(String unikey){
		
		if(unikey == null){
			return null;
		}
		User current = new User();
		current.setUnikey(unikey);
		current.setTutor(isTutor(unikey));
		
		// get marks TODO
		
		return current;
	}
	
	/**
	 * check if the student is a tutor
	 * @param unikey - unikey of the student
	 * @return true - if a tutor
	 * @return false - if not a tutor
	 */
	private boolean isTutor(String unikey){
		return ProjectProperties.getInstance().getTutors().contains(unikey);
	}
	
	/**
	 * Get a list of all users.
	 * @return
	 */
	public String[] getUserList(){
		File f = new File(ProjectProperties.getInstance().getSubmissionsLocation());
		String[] userList = f.list();
		Arrays.sort(userList);
		return userList;
	}
}
