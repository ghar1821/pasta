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

@Service("userManager")
@Repository
/**
 * User manager.
 * 
 * Manages interaction between controller and data.
 * 
 * @author Alex
 *
 */
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
	
	
	public PASTAUser getUser(String username) {
		return userDao.getUser(username);
	}
	
	public Collection<PASTAUser> getUserList() {
		return userDao.getUserList();
	}
	
	public Collection<PASTAUser> getStudentList() {
		return userDao.getStudentList();
	}
	
	public Map<String, Collection<String>> getTutorialByStream(){
		return userDao.getTutorialByStream();
	}
	
	public Collection<PASTAUser> getUserListByTutorial(String className) {
		return userDao.getUserListByTutorial(className);
	}
	
	public Collection<PASTAUser> getUserListByStream(String stream) {
		return userDao.getUserListByStream(stream);
	}
	
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

	public void giveExtension(String username, String assessmentName, Date extension) {
		PASTAUser user = getUser(username);
		user.getExtensions().put(assessmentName, extension);
		
		// update the files
		try {
			PrintWriter out = new PrintWriter(new File(ProjectProperties.getInstance().getProjectLocation() + "/submissions/" +
					username + "/user.extensions"));
			for (Entry<String, Date> ex : user.getExtensions().entrySet()) {
				out.println(ex.getKey() + ">" + PASTAUtil.formatDate(ex.getValue()));
			}
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not save extension information for " + username);
		}
	}

	public LoginDAO getLoginDao() {
		return loginDao;
	}
	
	public void replaceStudents(List<PASTAUser> users){
		userDao.replaceStudents(users);
	}
	
	public void updateStudents(List<PASTAUser> users){
		userDao.updateStudents(users);
	}
	
	public void replaceTutors(List<PASTAUser> users){
		userDao.replaceTutors(users);
	}
	
	public void updateTutors(List<PASTAUser> users){
		userDao.updateTutors(users);
	}

	public void deleteUser(PASTAUser toDelete) {
		userDao.deleteSingleUser(toDelete);
	}

	public void updatePassword(String username, String newPassword) {
		loginDao.updatePassword(username, DigestUtils.md5Hex(newPassword));
	}
	
	public void giveExtension(PASTAUser user, String assessmentName, Date extension) {
		user.getExtensions().put(assessmentName, extension);
		
		// update the files
		try {
			PrintWriter out = new PrintWriter(new File(ProjectProperties.getInstance().getProjectLocation() + "/submissions/" +
					user.getUsername() + "/user.extensions"));
			for (Entry<String, Date> ex : user.getExtensions().entrySet()) {
				out.println(ex.getKey() + ">" + PASTAUtil.formatDate(ex.getValue()));
			}
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not save extension information for " + user.getUsername());
		}
	}

}
