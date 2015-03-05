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

package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.PASTAUser;
import pasta.domain.UserPermissionLevel;
import pasta.repository.LoginDAO;
import pasta.repository.UserDAO;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

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
	 * @see pasta.repository.UserDAO#getUserList()
	 * @return the collection of all users (students, tutors and instructors)
	 */
	public Collection<PASTAUser> getUserList() {
		return userDao.getUserList();
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
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getTutorialByStream()
	 * @return a map with the key as the tutorial and value as the collection of students
	 * in that tutorial
	 */
	public Map<String, Collection<String>> getTutorialByStream(){
		return userDao.getTutorialByStream();
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getUserListByTutorial(String)
	 * @param className the name of the tutorial class
	 * @return the collection of users that belong to a tutorial
	 */
	public Collection<PASTAUser> getUserListByTutorial(String className) {
		return userDao.getUserListByTutorial(className);
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#getUserListByStream(String)
	 * @param stream the name of the stream
	 * @return the collection of users that belong to a stream
	 */
	public Collection<PASTAUser> getUserListByStream(String stream) {
		return userDao.getUserListByStream(stream);
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
	public PASTAUser getOrCreateUser(String username) {
		PASTAUser user = userDao.getUser(username);
		if(user == null){
			user = new PASTAUser();
			user.setUsername(username);
			user.setStream("");
			user.setTutorial("");
			user.setPermissionLevel(UserPermissionLevel.STUDENT);
			
			userDao.add(user);
		}
		return user;
	}

	/**
	 * Helper method
	 * 
	 * @see pasta.service.UserManager#giveExtension(PASTAUser, String, Date)
	 * @param username the username of the student getting an extension
	 * @param assessmentId the id of the assessment
	 * @param extension the new due date for the assessment
	 */
	public void giveExtension(String username, long assessmentId, Date extension) {
		PASTAUser user = getUser(username);
		if(user != null){
			giveExtension(user, assessmentId, extension);
		}
	}
	
	/**
	 * Give a student an extension to a given date.
	 * <p>
	 * Updates the extensions in the cahced user and writes them to disk
	 * in the folder $ProjectLocation$/submissions/$username$/user.extensions
	 *  using the following format: "$assessmentId$>yyyy-MM-dd'T'HH-mm-ss"
	 * 
	 * @param user the user of the student getting an extension
	 * @param assessmentId the id of the assessment
	 * @param extension the new due date for the assessment
	 */
	public void giveExtension(PASTAUser user, long assessmentId, Date extension) {
		user.getExtensions().put(assessmentId, extension);
		
		// update the files
		try {
			PrintWriter out = new PrintWriter(new File(ProjectProperties.getInstance().getSubmissionsLocation() +
					user.getUsername() + "/user.extensions"));
			for (Entry<Long, Date> ex : user.getExtensions().entrySet()) {
				out.println(ex.getKey() + ">" + PASTAUtil.formatDate(ex.getValue()));
			}
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not save extension information for " + user.getUsername());
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
	 * @see pasta.repository.UserDAO#replaceStudents(List)
	 * @param users the list of students to replace the current list with
	 */
	public void replaceStudents(List<PASTAUser> users){
		userDao.replaceStudents(users);
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#updateStudents(List)
	 * @param users the list of students to update the current list with
	 */
	public void updateStudents(List<PASTAUser> users){
		userDao.updateStudents(users);
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#replaceTutors(List)
	 * @param users the list of tutors and instructors to replace the current list with
	 */
	public void replaceTutors(List<PASTAUser> users){
		userDao.replaceTutors(users);
	}
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.UserDAO#replaceTutors(List)
	 * @param users the list of tutors and instructors to update the current list with
	 */
	public void updateTutors(List<PASTAUser> users){
		userDao.updateTutors(users);
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
	
	
}
