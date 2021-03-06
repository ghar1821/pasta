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

package pasta.service;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.UserPermissionLevel;
import pasta.domain.form.UpdateUsersForm;
import pasta.domain.template.Assessment;
import pasta.domain.template.AssessmentExtension;
import pasta.domain.user.PASTAUser;
import pasta.repository.LoginDAO;
import pasta.repository.UserDAO;

/**
 * User manager.
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-31
 *
 */
@Service("userManager")
@Repository
public class UserManager {
	
	@Autowired
	private UserDAO userDao;
	@Autowired
	private LoginDAO loginDao;
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(UserManager.class);
	
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getUser(long)
	 * @param userId the id of the user
	 * @return the user object or null if there is no user with the given id
	 */
	public PASTAUser getUser(long userId) {
		return userDao.getUser(userId);
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getUser(String)
	 * @param username the name of the user
	 * @return the user object or null if there is no user with the given name
	 */
	public PASTAUser getUser(String username) {
		return userDao.getUser(username);
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getUserList(boolean)
	 * @return the collection of all users (students, tutors and instructors)
	 */
	public Collection<PASTAUser> getUserList() {
		return userDao.getUserList(false);
	}
	
	public Collection<PASTAUser> getUserListIncludingGroups() {
		return userDao.getUserList(true);
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getStudentList()
	 * @return the collection of only students
	 */
	public Collection<PASTAUser> getStudentList() {
		return userDao.getStudentList();
	}

	/**
	 * Gets the collection of all students that this tutor teaches. If the tutor
	 * teaches "a.c1", all students in the stream "a" and tutorial "c1" are
	 * included. If the tutor teaches just "c1", all students in the tutorial
	 * "c1" are included, regardless of stream.
	 * 
	 * @param user the tutor
	 * @return a list of students taught by this tutor
	 */
	public Collection<PASTAUser> getTutoredStudents(PASTAUser user) {
		Set<PASTAUser> students = new TreeSet<PASTAUser>();
		if(!user.isTutor()) {
			return students;
		}
		String[] classes = user.getTutorClasses();
		for(String tutorial : classes) {
			students.addAll(getUserListByTutorial(tutorial));
		}
		return students;
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getTutorialByStream()
	 * @return a map with the key as the tutorial and value as the collection of students
	 * in that tutorial
	 */
	public Map<String, Set<String>> getTutorialByStream(){
		return userDao.getTutorialByStream();
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getUserListByTutorialAndStream(String, String)
	 * @param className the name of the tutorial class
	 * @return the collection of users that belong to a tutorial
	 */
	public Collection<PASTAUser> getUserListByTutorial(String className) {
		String[] parts = className.split("\\.", 2);
		if(parts.length == 1) {
			return getUserListByTutorialAndStream(null, className);
		} else {
			if(parts[0].isEmpty()) {
				return getUserListByTutorialAndStream(null, parts[1]);
			}
			return getUserListByTutorialAndStream(parts[0], parts[1]);
		}
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getUserListByTutorialAndStream(String, String)
	 * @param stream the name of the stream
	 * @return the collection of users that belong to a stream
	 */
	public Collection<PASTAUser> getUserListByStream(String stream) {
		return getUserListByTutorialAndStream(stream, null);
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getUserListByTutorialAndStream(String, String)
	 * @param stream the name of the stream
	 * @param tutorial the name of the tutorial class
	 * @return the collection of users that belong to a stream and tutorial
	 */
	public Collection<PASTAUser> getUserListByTutorialAndStream(String stream, String tutorial) {
		return userDao.getUserListByTutorialAndStream(stream, tutorial);
	}
	
	/**
	 * Get or create the user with a given name.
	 * <p>
	 * Attempt to retrieve the user from {@link pasta.repository.UserDAO}
	 * if it fails, then create a new user. The new user is placed in the
	 * tutorial "" and the stream "" with permission level of {@link pasta.domain.UserPermissionLevel#STUDENT}
	 * 
	 * @param username the name of the user
	 * @return the user object
	 */
	public PASTAUser getOrCreateUser(PASTAUser checkUser) {
		PASTAUser user = userDao.getUser(checkUser.getUsername());
		if(user == null){
			user = createStudent(checkUser.getUsername(), "", "");
			userDao.add(user);
		} else if(!user.isActive()) {
			userDao.undelete(user);
		}
		return user;
	}
	
	/**
	 * Give a student an extension to a given date.
	 * 
	 * @param user the user of the student getting an extension
	 * @param assessmentId the id of the assessment
	 * @param extension the new due date for the assessment
	 */
	public void giveExtension(PASTAUser user, Assessment assessment, Date extension) {
		userDao.giveExtension(user, assessment, extension);
	}

	public Date getExtension(PASTAUser user, Assessment assessment) {
		return userDao.getExtension(user, assessment);
	}
	
	public Date getDueDate(PASTAUser user, Assessment assessment) {
		Date extDate = getExtension(user, assessment);
		Date normal = assessment.getDueDate();
		if(extDate != null && extDate.after(normal)) {
			return extDate;
		} else {
			return normal;
		}
	}

	/**
	 * Return the Data Access Object used if using {@link pasta.login.DBAuthValidator}
	 * @return the loginDAO object
	 */
	public LoginDAO getLoginDao() {
		return loginDao;
	}

	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#deleteSingleUser(PASTAUser)
	 * @param toDelete
	 */
	public void deleteUser(PASTAUser toDelete) {
		userDao.deleteSingleUser(toDelete);
	}

	/**
	 * Update the password for a given user with a new one.
	 * <p>
	 * Currently uses md5Hex
	 * 
	 * @param username the name of the user
	 * @param newPassword the password in plaintext
	 */
	public void updatePassword(String username, String newPassword) {
		loginDao.updatePassword(username, DigestUtils.md5Hex(newPassword));
	}

	public List<String> getTutorialList() {
		return userDao.getAllStudentTutorials();
	}
	
	public List<String> getStreamList() {
		return userDao.getAllStudentStreams();
	}
	
	public List<String> getStudentUsernameList() {
		return userDao.getAllStudentUsernames();
	}

	/**
	 * Updates the list of either students or tutors/instructors. This may be an update or replace operation.
	 * If the form contains an uploaded file, then parse it, otherwise use the plain text given.
	 * Input should be in the form:
	 * 	<code>username,permission,tutorial{,tutorial}</code> for tutors
	 * 	<code>username,stream,tutorial</code> for students
	 * Each line is turned into a {@link pasta.domain.user.PASTAUser} and then calls either 
	 * {@link UserDAO#replaceUsers(List, boolean)} or {@link UserDAO#updateUsers(List)}
	 * 
	 * @param form the form containing update information
	 */
	public void updateUsers(UpdateUsersForm form) {
		
		// UpdateUsersFormValidator will have extracted CSV file
		// and appended it to the end of updateContents already.
		
		if(form.getUpdateContents().isEmpty()) {
			return;
		}
		
		Scanner content = new Scanner(form.getUpdateContents());
		Set<PASTAUser> users = new TreeSet<PASTAUser>();
		while(content.hasNext()) {
			String line = content.nextLine().replaceAll("\\s+", "");
			if(line.isEmpty()) {
				continue;
			}
			String[] parts = line.split(",", 3);
			
			PASTAUser user = null;
			if(form.isUpdateTutors()) {
				user = createTutor(parts);
			} else {
				user = createStudent(parts);
			}
			if(user != null) {
				users.add(user);
			}
		}
		content.close();
		
		if(form.isReplace()) {
			userDao.replaceUsers(users, form.isUpdateTutors());
		} else {
			userDao.updateUsers(users);
		}
	}

	private PASTAUser createStudent(String... parts) {
		if(parts.length < 1) {
			return null;
		}
		PASTAUser user = new PASTAUser();
		user.setPermissionLevel(UserPermissionLevel.STUDENT);
		user.setUsername(parts[0]);
		if(parts.length > 1) {
			user.setStream(parts[1]);
		}
		if(parts.length > 2) {
			user.setTutorial(parts[2]);
		}
		return user;
	}

	private PASTAUser createTutor(String... parts) {
		if(parts.length < 2) {
			return null;
		}
		PASTAUser user = new PASTAUser();
		user.setUsername(parts[0]);
		if(parts[1].toUpperCase().equals(UserPermissionLevel.TUTOR.name())) {
			user.setPermissionLevel(UserPermissionLevel.TUTOR);
		} else if(parts[1].toUpperCase().equals(UserPermissionLevel.INSTRUCTOR.name())) {
			user.setPermissionLevel(UserPermissionLevel.INSTRUCTOR);
		} else {
			return null;
		}
		if(parts.length > 2) {
			user.setTutorial(parts[2]);
		}
		return user;
	}

	public void deleteAllExtensionsForAssessment(long assessmentId) {
		List<AssessmentExtension> extensions = userDao.getAllExtensionsForAssessment(assessmentId);
		if(!extensions.isEmpty()) {
			logger.info("Deleting " + extensions.size() + " extensions for assessment " + assessmentId);
			for(AssessmentExtension ext : extensions) {
				userDao.delete(ext);
			}
		}
	}
}
