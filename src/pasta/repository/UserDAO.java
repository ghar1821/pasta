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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.PASTAUser;
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
	
	Map<String, PASTAUser> allUsers = null;
	Map<String, Set<String>> usersByTutorial = null;
	Map<String, Set<String>> usersByStream = null;
	Map<String, Collection<String>> tutorialByStream = null;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	/**
	 * Update the cached user.
	 * <p>
	 * Add them to the correct maps, handle stream or tutorial changing.
	 * @param user the user to be updated
	 */
	public void updateCachedUser(PASTAUser user){
		if (user != null) {
			PASTAUser oldUser = allUsers.get(user.getUsername());
			
			if(user.isTutor()){
				// TUTOR
				if(oldUser == null){
					// add like normal
					allUsers.put(user.getUsername(), user);
				}
				else{
					// update
					oldUser.setTutorial(user.getTutorial());
					oldUser.setStream(user.getStream());
				}
			}
			else{
				// STUDENT
				if(oldUser == null){
					// add new
					allUsers.put(user.getUsername(), user);
					// tutorial cache
					if(!usersByStream.containsKey(user.getStream())){
						usersByStream.put(user.getStream(), new TreeSet<String>());
					}
					usersByStream.get(user.getStream()).add(user.getUsername());
					// stream cache
					if(!usersByTutorial.containsKey(user.getTutorial())){
						usersByTutorial.put(user.getTutorial(), new TreeSet<String>());
					}
					usersByTutorial.get(user.getTutorial()).add(user.getUsername());
				}
				else{
					// update

					// check if tutorial has changed
					if(!user.getTutorial().equals(oldUser.getTutorial())){
						// update caching
						if(usersByTutorial.containsKey(oldUser.getTutorial())){
							usersByTutorial.get(oldUser.getTutorial()).remove(oldUser.getUsername());
						}
						if(usersByTutorial.get(oldUser.getTutorial()).isEmpty()){
							usersByTutorial.remove(oldUser.getTutorial());
						}
						oldUser.setTutorial(user.getTutorial());
						if(!usersByTutorial.containsKey(user.getTutorial())){
							usersByTutorial.put(user.getTutorial(), new TreeSet<String>());
						}
						usersByTutorial.get(user.getTutorial()).add(oldUser.getUsername());
					}
					// check if stream has changed
					if(!user.getStream().equals(oldUser.getStream())){
						// remove from caching
						if(usersByStream.containsKey(oldUser.getStream())){
							usersByStream.get(oldUser.getStream()).remove(oldUser.getUsername());
						}
						if(usersByStream.get(oldUser.getStream()).isEmpty()){
							usersByStream.remove(oldUser.getStream());
						}
						oldUser.setStream(user.getStream());
						if(!usersByStream.containsKey(user.getStream())){
							usersByStream.put(user.getStream(), new TreeSet<String>());
						}
						usersByStream.get(user.getStream()).add(oldUser.getUsername());
					}
				}
			}
		}
	}
	
	/**
	 * Delete the a user from the cache.
	 * <p>
	 * Remove them from all of the maps, but do not change any information on the disk
	 * @param user the user getting removed
	 */
	public void deleteCachedUser(PASTAUser user){
		if (user != null) {
			PASTAUser oldUser = allUsers.get(user.getUsername());
			if(!user.isTutor()){
				// clean up after the old user
				if (oldUser.getTutorial() != null && 
						usersByTutorial.containsKey(oldUser.getTutorial())) {
					usersByTutorial.get(oldUser.getTutorial()).remove(oldUser.getUsername());
				}
				if (oldUser.getStream() != null && 
						usersByStream.containsKey(oldUser.getStream())) {
					usersByStream.get(oldUser.getStream()).remove(oldUser.getUsername());
				}
			}
		}
	}

	/**
	 * Save the user to the cache and the database.
	 * 
	 * @param user the user being saved
	 */
	public void save(PASTAUser user) {
		updateCachedUser(user);
		getHibernateTemplate().save(user);
	}

	/**
	 * Update the user in the cache and the database.
	 * 
	 * @param user the user being updated
	 */
	public void update(PASTAUser user) {
		updateCachedUser(user);
		getHibernateTemplate().update(user);
	}
	
	/**
	 * Delete the user from the cache and the database.
	 * 
	 * @param user the user being deleted
	 */
	public void delete(PASTAUser user) {
		deleteCachedUser(user);
		getHibernateTemplate().delete(user);
	}
	
	/**
	 * Replace the current list of students with a new one.
	 * 
	 * @param users the list of users which will replace the current list
	 */
	public void replaceStudents(List<PASTAUser> users){
		for(PASTAUser user: allUsers.values()){
			if(!user.isTutor()){
				delete(user);
			}
		}
		
		for(PASTAUser user: users){
			save(user);
		}
		
		loadUsers();
	}
	
	/**
	 * Update the current list of students
	 * 
	 * @param users the list of users which will be updated
	 */
	public void updateStudents(List<PASTAUser> users){
		for(PASTAUser user: users){
			if(allUsers.containsKey(user.getUsername())){
				update(user);
			}
			else{
				save(user);
			}
		}

		loadUsers();
	}
	
	/**
	 * Replace the current list of teaching staff with a new one.
	 * 
	 * @param users the list of users which will replace the current list
	 */
	public void replaceTutors(List<PASTAUser> users){
		for(PASTAUser user: allUsers.values()){
			if(user.isTutor()){
				delete(user);
			}
		}
		
		for(PASTAUser user: users){
			save(user);
		}
		
		loadUsers();
	}
	
	/**
	 * Update the current list of teaching staff
	 * 
	 * @param users the list of teaching staff which will be updated
	 */
	public void updateTutors(List<PASTAUser> users){
		for(PASTAUser user: users){
			if(allUsers.containsKey(user.getUsername())){
				update(user);
			}
			else{
				save(user);
			}
		}
		
		loadUsers();
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
		if(allUsers == null){
			loadUsers();
		}
		return allUsers.get(username.toLowerCase());
	}
	
	/**
	 * Get the collection of all users.
	 * <p>
	 * This list includes students, tutors and instructors.
	 * @return the collection of all users registered in the system
	 */
	public Collection<PASTAUser> getUserList(){
		return allUsers.values();
	}
	
	/**
	 * Get the collection of students
	 * 
	 * @return the collection of all students registered in the system
	 */
	public Collection<PASTAUser> getStudentList(){
		Collection<PASTAUser> users = new LinkedList<PASTAUser>();
		for(PASTAUser user: allUsers.values()){
			if(!user.isTutor()){
				users.add(user);
			}
		}
		return users;
	}
	
	/**
	 * Get the set of users in a given tutorial
	 * <p>
	 * Will never return null.
	 * @param tutorialName the name of the tutorial	
	 * @return the collection of students in a given tutorial
	 */
	public Collection<PASTAUser> getUserListByTutorial(String tutorialName){
		Set<PASTAUser> users = new TreeSet<PASTAUser>();
		if(usersByTutorial.get(tutorialName) != null){
			for(String user: usersByTutorial.get(tutorialName)){
				users.add(allUsers.get(user));
			}
		}
		return users;
	}
	
	/**
	 * Get the set of users in a given stream
	 * <p>
	 * Will never return null.
	 * @param streamName the name of the stream
	 * @return the collection of users in a given stream
	 */
	public Collection<PASTAUser> getUserListByStream(String streamName){
		Set<PASTAUser> users = new TreeSet<PASTAUser>();
		if(usersByStream.get(streamName) != null){
			for(String user: usersByStream.get(streamName)){
				users.add(allUsers.get(user));
			}
		}
		return users;
	}
	
	/**
	 * Get the map of tutorials by the streams they are in.
	 * 
	 * @return a map with key as the stream name and value as the collection of tutorial names
	 */
	public Map<String, Collection<String>> getTutorialByStream(){
		return tutorialByStream;
	}
	
	/**
	 * Add a user to the system.
	 * 
	 * @param user the user to be added.
	 */
	public void add(PASTAUser user){
		allUsers.put(user.getUsername().toLowerCase(), user);
		save(user);
	}
	
	/**
	 * Load all of the users into cache.
	 * <p>
	 * <ol>
	 * 	<li>Load the users from the database</li>
	 * 	<li>Load any extensions the students may have</li>
	 * 	<li>Put them in the correct caching maps</li>
	 * </ol>
	 */
	private void loadUsers(){
		List<PASTAUser> users = getHibernateTemplate().loadAll(PASTAUser.class);
		allUsers = new TreeMap<String, PASTAUser>();
		usersByTutorial = new TreeMap<String, Set<String>>();
		usersByStream = new TreeMap<String, Set<String>>();
		tutorialByStream = new TreeMap<String, Collection<String>>();
		if(users != null){
			for(PASTAUser user: users){
				allUsers.put(user.getUsername().toLowerCase(), user);
				
				if(!usersByStream.containsKey(user.getStream())){
					usersByStream.put(user.getStream(), new TreeSet<String>());
					tutorialByStream.put(user.getStream(), new TreeSet<String>());
				}
				usersByStream.get(user.getStream()).add(user.getUsername());
				// ensure you don't get grouping of tutorials (e.g. tutors have multiple tutorials
				if(!user.isTutor()){
					if(!user.getTutorial().contains(",")){
						tutorialByStream.get(user.getStream()).add(user.getTutorial());
					}
					if(!usersByTutorial.containsKey(user.getTutorial())){
						usersByTutorial.put(user.getTutorial(), new TreeSet<String>());
					}
					usersByTutorial.get(user.getTutorial()).add(user.getUsername());
				}
				
				
				// load extension file
				Scanner in;
				try {
					in = new Scanner(new File(ProjectProperties.getInstance().getProjectLocation() + "/submissions/" +
							user.getUsername() + "/user.extensions"));
					while(in.hasNextLine()){
						String[] line = in.nextLine().split(">");
						if(line.length == 2){
							try {
								user.getExtensions().put(line[0], PASTAUtil.parseDate(line[1]));
							} catch (ParseException e) {
								// ignore
							}
						}
					}
					in.close();
				} catch (FileNotFoundException e) {
					// no extensions given
				}
			}
		}
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
		PASTAUser fullUser = allUsers.get(toDelete.getUsername());
		if(fullUser != null){
			if(usersByTutorial.containsKey(fullUser.getTutorial())){
				usersByTutorial.get(fullUser.getTutorial()).remove(fullUser.getUsername());
			}
			if(usersByStream.containsKey(fullUser.getStream())){
				usersByStream.get(fullUser.getStream()).remove(fullUser.getUsername());
			}
			allUsers.remove(toDelete.getUsername());
		}
	}
}
