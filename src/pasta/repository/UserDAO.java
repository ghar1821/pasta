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

package pasta.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.PASTAUser;
import pasta.domain.UserPermissionLevel;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * The Data access object for the User class.
 * <p>
 * 
 * This class is responsible for all of the interaction
 * between the data layer (disk in this case) and the system
 * for managing users.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
@Repository("userDAO")
public class UserDAO extends HibernateDaoSupport{
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * Save the user to the cache and the database.
	 * 
	 * @param user the user being saved
	 */
	public void save(PASTAUser user) {
		getHibernateTemplate().save(user);
	}

	/**
	 * Update the user in the cache and the database.
	 * 
	 * @param user the user being updated
	 */
	public void update(PASTAUser user) {
		getHibernateTemplate().update(user);
	}
	
	/**
	 * Delete the user from the cache and the database.
	 * 
	 * @param user the user being deleted
	 */
	public void delete(PASTAUser user) {
		getHibernateTemplate().delete(user);
	}
	
	/**
	 * Replace the current list of students with a new one.
	 * 
	 * @param users the list of users which will replace the current list
	 */
	public void replaceStudents(List<PASTAUser> users){
		for(PASTAUser user: getUserList()){
			if(!user.isTutor()){
				delete(user);
			}
		}
		for(PASTAUser user: users){
			save(user);
		}
	}
	
	/**
	 * Update the current list of students
	 * 
	 * @param users the list of users which will be updated
	 */
	public void updateStudents(List<PASTAUser> users){
		Map<String, PASTAUser> currentUsers = getUserMap();
		for(PASTAUser user: users){
			if(currentUsers.containsKey(user.getUsername())){
				PASTAUser toUpdate = currentUsers.get(user.getUsername());
				toUpdate.setPermissionLevel(user.getPermissionLevel());
				toUpdate.setStream(user.getStream());
				toUpdate.setTutorial(user.getTutorial());
				update(toUpdate);
			}
			else{
				save(user);
			}
		}
	}
	
	/**
	 * Replace the current list of teaching staff with a new one.
	 * 
	 * @param users the list of users which will replace the current list
	 */
	public void replaceTutors(List<PASTAUser> users){
		for(PASTAUser user : getUserList()){
			if(user.isTutor()){
				delete(user);
			}
		}
		for(PASTAUser user: users){
			save(user);
		}
	}
	
	/**
	 * Update the current list of teaching staff
	 * 
	 * @param users the list of teaching staff which will be updated
	 */
	public void updateTutors(List<PASTAUser> users){
		updateStudents(users);
	}
	
	// calculated methods
	
	/**
	 * Get the user
	 * <p>
	 * Check if the list of all users exists, if not
	 * load it from the database.
	 * 
	 * @param username the name of the user
	 * @return the user or null if the user does not exist
	 */
	public PASTAUser getUser(String username){
		DetachedCriteria cr = DetachedCriteria.forClass(PASTAUser.class);
		cr.add(Restrictions.eq("username", username));
		@SuppressWarnings("unchecked")
		List<PASTAUser> results = getHibernateTemplate().findByCriteria(cr);
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	public PASTAUser getUser(long id){
		return getHibernateTemplate().get(PASTAUser.class, id);
	}
	
	/**
	 * Get the collection of all users.
	 * <p>
	 * This list includes students, tutors and instructors.
	 * @return the collection of all users registered in the system
	 */
	public List<PASTAUser> getUserList(){
		return getHibernateTemplate().loadAll(PASTAUser.class);
	}
	
	public Map<String, PASTAUser> getUserMap(){
		Map<String, PASTAUser> userMap = new HashMap<String, PASTAUser>();
		List<PASTAUser> users = getUserList();
		for(PASTAUser user : users) {
			userMap.put(user.getUsername(), user);
		}
		return userMap;
	}
	
	/**
	 * Get the collection of students
	 * 
	 * @return the collection of all students registered in the system
	 */
	@SuppressWarnings("unchecked")
	public List<PASTAUser> getStudentList(){
		DetachedCriteria cr = DetachedCriteria.forClass(PASTAUser.class);
		cr.add(Restrictions.eq("permissionLevel", UserPermissionLevel.STUDENT));
		return getHibernateTemplate().findByCriteria(cr);
	}
	
	/**
	 * Get the set of users in a given tutorial
	 * <p>
	 * Will never return null.
	 * @param tutorialName the name of the tutorial	
	 * @return the collection of students in a given tutorial
	 */
	@SuppressWarnings("unchecked")
	public List<PASTAUser> getUserListByTutorial(String tutorialName){
		DetachedCriteria cr = DetachedCriteria.forClass(PASTAUser.class);
		cr.add(Restrictions.eq("permissionLevel", UserPermissionLevel.STUDENT));
		cr.add(Restrictions.eq("tutorial", tutorialName));
		return getHibernateTemplate().findByCriteria(cr);
	}
	
	/**
	 * Get the set of users in a given stream
	 * <p>
	 * Will never return null.
	 * @param streamName the name of the stream
	 * @return the collection of users in a given stream
	 */
	@SuppressWarnings("unchecked")
	public List<PASTAUser> getUserListByStream(String streamName){
		DetachedCriteria cr = DetachedCriteria.forClass(PASTAUser.class);
		cr.add(Restrictions.eq("permissionLevel", UserPermissionLevel.STUDENT));
		cr.add(Restrictions.eq("stream", streamName));
		return getHibernateTemplate().findByCriteria(cr);
	}
	
	/**
	 * Get the map of tutorials by the streams they are in.
	 * 
	 * @return a map with key as the stream name and value as the collection of tutorial names
	 */
	public Map<String, Set<String>> getTutorialByStream(){
		Map<String, Set<String>> results = new HashMap<String, Set<String>>();
		
		for(PASTAUser user : getUserList()) {
			if(user.isTutor()) {
				continue;
			}
			String stream = user.getStream();
			Set<String> tutorials = results.get(stream);
			if(tutorials == null) {
				tutorials = new TreeSet<String>();
				results.put(stream, tutorials);
			}
			tutorials.add(user.getTutorial());
		}
		
		return results;
	}
	
	/**
	 * Add a user to the system.
	 * 
	 * @param user the user to be added.
	 */
	public void add(PASTAUser user){
		user.setUsername(user.getUsername().toLowerCase().trim());
		save(user);
	}

	/**
	 * Delete a single user.
	 * <p>
	 * Does not delete anything from disk.
	 *  
	 * @param toDelete the user to delete.
	 */
	public void deleteSingleUser(PASTAUser toDelete) {
		delete(toDelete);
	}
}
