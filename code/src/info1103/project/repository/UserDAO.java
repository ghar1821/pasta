package info1103.project.repository;

import info1103.project.domain.User;
import info1103.project.scheduler.ExecutionScheduler;
import info1103.project.util.ProjectProperties;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		current.setTutorialClass(getTutorialClass(unikey));
		
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
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".");
		    }
		};
		String[] userList = f.list(filter);
		Arrays.sort(userList);
		return userList;
	}
	
	public String getTutorialClass(String unikey){
		// connect
		Connection con = ExecutionScheduler.getInstance().getDatabaseConnection();
		String tutorialClass = null;
		if(con != null){
			PreparedStatement st = null;
			ResultSet result = null;
			try {
				// create prepared statement
				st = con.prepareStatement("SELECT unikey, tutorialClass FROM students WHERE unikey = ?;\n");
				st.setString(1, unikey);
				
				// execute query
				result = st.executeQuery();
				
				// get data
				if(result.next()){
					tutorialClass = result.getString("tutorialClass");
				}
				
				// cleanup
				result.close();
				st.close();
				st = null;
				result = null;
				
				return tutorialClass;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(con != null){
					try {
						con.rollback();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
//						e1.printStackTrace();
					}
				}
			} 
		}
		return null;
	}
}
