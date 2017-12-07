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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.UserPermissionLevel;
import pasta.domain.template.Assessment;
import pasta.domain.template.AssessmentExtension;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;

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
@Transactional
@Repository("userDAO")
public class UserDAO extends BaseDAO {
	
	/**
	 * "Delete" the user from the database by setting them as inactive.
	 * 
	 * @param user the user being deleted
	 */
	public void delete(PASTAUser user) {
		if(user instanceof PASTAGroup) {
			((PASTAGroup) user).setAssessment(null);
			((PASTAGroup) user).removeAllMembers();
			update(user);
			super.delete(user);
		} else {
			user.setActive(false);
			update(user);
		}
	}
	
	/**
	 * "Undelete" the user from the database by setting them as active.
	 * 
	 * @param user the user being undeleted
	 */
	public void undelete(PASTAUser user) {
		user.setActive(true);
		update(user);
	}
	
	/**
	 * Update the current list of users
	 * 
	 * @param users the list of users which will be updated
	 */
	public void updateUsers(Set<PASTAUser> users){
		Map<String, PASTAUser> currentUsers = getAllUserMap();
		for(PASTAUser user: users){
			if(currentUsers.containsKey(user.getUsername())){
				PASTAUser toUpdate = currentUsers.get(user.getUsername());
				toUpdate.setPermissionLevel(user.getPermissionLevel());
				toUpdate.setStream(user.getStream());
				toUpdate.setTutorial(user.getTutorial());
				toUpdate.setActive(true);
				update(toUpdate);
			}
			else{
				save(user);
				currentUsers.put(user.getUsername(), getUser(user.getUsername()));
			}
		}
	}
	
	/**
	 * Replace the current list of users with a new one.
	 * 
	 * @param users the list of users which will replace the current list
	 * @param tutors whether or not you're replacing the list of teaching staff
	 */
	public void replaceUsers(Set<PASTAUser> users, boolean tutors){
		Map<String, PASTAUser> currentUsers;
		if(tutors)
			currentUsers = getAllTutorMap();
		else
			currentUsers = getAllStudentMap();
		
		Set<String> existingUsernames = new HashSet<>(currentUsers.keySet());
		for(PASTAUser user: users){
			existingUsernames.remove(user.getUsername());
		}
		for(String username : existingUsernames) {
			delete(currentUsers.get(username));
			currentUsers.remove(username);
		}
		updateUsers(users);
	}
	
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
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAUser.class);
		cr.add(Restrictions.eq("username", username));
		@SuppressWarnings("unchecked")
		List<PASTAUser> results = cr.list();
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	public PASTAUser getUser(long id){
		return (PASTAUser) sessionFactory.getCurrentSession().get(PASTAUser.class, id);
	}
	
	/**
	 * Get the collection of all active users.
	 * <p>
	 * This list includes students, tutors and instructors.
	 * @return the collection of all active users registered in the system
	 */
	public List<PASTAUser> getUserList(){
		return getUserList(false, false, null);
	}
	
	/**
	 * Get the collection of all active users and groups.
	 * <p>
	 * This list includes students, tutors, instructors and potentially groups.
	 * @param includingGroups whether to include groups in the list.
	 * @return the collection of all active users registered in the system
	 */
	public List<PASTAUser> getUserList(boolean includingGroups){
		return getUserList(false, includingGroups, null);
	}
	
	/**
	 * Get the collection of all users (including inactive).
	 * <p>
	 * This list includes students, tutors and instructors.
	 * @return the collection of all users registered in the system
	 */
	public List<PASTAUser> getAllUserList(){
		return getUserList(true, false, null);
	}
	
	/**
	 * Get the collection of students
	 * 
	 * @return the collection of all active students registered in the system
	 */
	public List<PASTAUser> getStudentList(){
		return getUserList(false, false, UserPermissionLevel.STUDENT);
	}
	
	/**
	 * Get the collection of all students (including inactive ones)
	 * 
	 * @return the collection of all active students registered in the system
	 */
	public List<PASTAUser> getAllStudentList(){
		return getUserList(true, false, UserPermissionLevel.STUDENT);
	}
	
	/**
	 * Get the collection of all active tutors and instructors.
	 * 
	 * @return the collection of all active tutors and instructors registered in the system
	 */
	public List<PASTAUser> getTutorList(){
		return getUserList(false, false, UserPermissionLevel.TUTOR);
	}
	
	/**
	 * Get the collection of all tutors and instructors (including inactive).
	 * 
	 * @return the collection of all tutors and instructors registered in the system
	 */
	public List<PASTAUser> getAllTutorList(){
		return getUserList(true, false, UserPermissionLevel.TUTOR);
	}
	
	@SuppressWarnings("unchecked")
	private List<PASTAUser> getUserList(boolean includeInactive, boolean includeGroups, UserPermissionLevel permissionLevel) {
		if(includeInactive && includeGroups && permissionLevel == null) {
			return sessionFactory.getCurrentSession().createCriteria(PASTAUser.class).list();
		} else {
			Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAUser.class);
			if(!includeInactive)
				cr.add(Restrictions.eq("active", true));
			if(!includeGroups)
				cr.add(Restrictions.ne("permissionLevel", UserPermissionLevel.GROUP));
			if(permissionLevel != null) {
				switch(permissionLevel) {
				case TUTOR:
				case INSTRUCTOR:
					cr.add(
							Restrictions.or(
									Restrictions.eq("permissionLevel", UserPermissionLevel.TUTOR), 
									Restrictions.eq("permissionLevel", UserPermissionLevel.INSTRUCTOR)));
					break;
				default:
					cr.add(Restrictions.eq("permissionLevel", permissionLevel));
				}
			}
			return cr.list();
		}
	}
	
	public boolean hasActiveUsers() {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAUser.class);
		cr.setProjection(Projections.rowCount());
		cr.add(Restrictions.eq("active", true));
		cr.add(Restrictions.ne("permissionLevel", UserPermissionLevel.GROUP));
		return ((Number) cr.uniqueResult()).longValue() > 0;
	}
	
	public Map<String, PASTAUser> getUserMap(){
		return getUserMap(false, false, null);
	}
	public Map<String, PASTAUser> getAllUserMap(){
		return getUserMap(true, false, null);
	}
	public Map<String, PASTAUser> getStudentMap(){
		return getUserMap(false, false, UserPermissionLevel.STUDENT);
	}
	public Map<String, PASTAUser> getAllStudentMap(){
		return getUserMap(true, false, UserPermissionLevel.STUDENT);
	}
	public Map<String, PASTAUser> getTutorMap(){
		return getUserMap(false, false, UserPermissionLevel.TUTOR);
	}
	public Map<String, PASTAUser> getAllTutorMap(){
		return getUserMap(true, false, UserPermissionLevel.TUTOR);
	}
	
	private Map<String, PASTAUser> getUserMap(boolean includeInactive, boolean includeGroups, UserPermissionLevel permissionLevel) {
		Map<String, PASTAUser> userMap = new HashMap<String, PASTAUser>();
		List<PASTAUser> users = getUserList(includeInactive, includeGroups, permissionLevel);
		for(PASTAUser user : users) {
			userMap.put(user.getUsername(), user);
		}
		return userMap;
	}
	
	/**
	 * Get the set of users in a given stream and tutorial
	 * <p>
	 * Will never return null.
	 * @param streamName the name of the stream; null for all streams
	 * @param tutorialName the name of the tutorial; null for all tutorials
	 * @return the collection of students in a given stream and tutorial
	 */	
	@SuppressWarnings("unchecked")
	public List<PASTAUser> getUserListByTutorialAndStream(String streamName, String tutorialName) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAUser.class);
		cr.add(Restrictions.eq("permissionLevel", UserPermissionLevel.STUDENT));
		if(streamName != null) {
			cr.add(Restrictions.eq("stream", streamName));
		}
		if(tutorialName != null) {
			cr.add(Restrictions.eq("tutorial", tutorialName));
		}
		cr.add(Restrictions.eq("active", true));
		return cr.list();
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

	@SuppressWarnings("unchecked")
	public List<String> getAllStudentTutorials() {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAUser.class);
		cr.add(Restrictions.eq("permissionLevel", UserPermissionLevel.STUDENT));
		cr.setProjection(Projections.distinct(Projections.property("tutorial")));
		return cr.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getAllStudentStreams() {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAUser.class);
		cr.add(Restrictions.eq("permissionLevel", UserPermissionLevel.STUDENT));
		cr.setProjection(Projections.distinct(Projections.property("stream")));
		return cr.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getAllStudentUsernames() {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAUser.class);
		cr.add(Restrictions.eq("permissionLevel", UserPermissionLevel.STUDENT));
		cr.setProjection(Projections.distinct(Projections.property("username")));
		return cr.list();
	}

	public PASTAGroup getGroup(long id) {
		return (PASTAGroup) sessionFactory.getCurrentSession().get(PASTAGroup.class, id);
	}
	
	public int getGroupCount() {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAGroup.class);
		cr.setProjection(Projections.rowCount());
		return DataAccessUtils.intResult(cr.list());
	}

	@SuppressWarnings("unchecked")
	public List<PASTAGroup> getGroups(Assessment assessment) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAGroup.class);
		cr.add(Restrictions.eq("assessment", assessment));
		return cr.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<PASTAGroup> getAssessmentGroups(long assessmentId) {
		Criteria cr = sessionFactory.getCurrentSession()
				.createCriteria(PASTAGroup.class)
				.createCriteria("assessment")
					.add(Restrictions.eq("id", assessmentId));
		return cr.list();
	}

	public PASTAGroup getGroup(PASTAUser user, Assessment assessment) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAGroup.class);
		cr.add(Restrictions.eq("assessment", assessment));
		cr.createAlias("members", "user");
		cr.add(Restrictions.eq("user.id", user.getId()));
		@SuppressWarnings("unchecked")
		List<PASTAGroup> results = cr.list();
		if(results.isEmpty()) {
			return null;
		}
		if(results.size() > 1) {
			logger.warn("Ignoring multiple groups for user " + user.getId() + ", assessment " + assessment.getId());
		}
		return results.get(0);
	}

	public PASTAGroup getGroup(PASTAUser user, long assessmentId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAGroup.class);
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		cr.createAlias("members", "user");
		cr.add(Restrictions.eq("user.id", user.getId()));
		@SuppressWarnings("unchecked")
		List<PASTAGroup> results = cr.list();
		if(results.isEmpty()) {
			return null;
		}
		if(results.size() > 1) {
			logger.warn("Ignoring multiple groups for user " + user.getId() + ", assessment " + assessmentId);
		}
		return results.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<PASTAGroup> getAllUserGroups(PASTAUser user) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAGroup.class);
		cr.createAlias("members", "user");
		cr.add(Restrictions.eq("user.id", user.getId()));
		return cr.list();
	}

	@SuppressWarnings("unchecked")
	public List<PASTAGroup> getGroups(Collection<PASTAUser> users, long assessmentId) {
		if(users == null || users.isEmpty()) {
			return new ArrayList<PASTAGroup>();
		}
		Set<Long> ids = new HashSet<Long>();
		for(PASTAUser user : users) {
			ids.add(user.getId());
		}
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAGroup.class);
		cr.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		if(assessmentId > 0) {
			cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		}
		cr.createAlias("members", "user");
		cr.add(Restrictions.in("user.id", ids));
		cr.addOrder(Order.asc("assessment"));
		cr.addOrder(Order.asc("number"));
		return cr.list();
	}
	
	@SuppressWarnings("unchecked")
    public List<Long[]> getAllUserGroups(Collection<PASTAUser> users) {
        if(users == null || users.isEmpty()) {
        	return new ArrayList<Long[]>();
        }
    	Set<Long> ids = new HashSet<Long>();
        for(PASTAUser user : users) {
            ids.add(user.getId());
        }
        Criteria cr = sessionFactory.getCurrentSession().createCriteria(PASTAGroup.class);
        cr.createAlias("members", "user");
        cr.add(Restrictions.in("user.id", ids));
        cr.setProjection(Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("user.id"))
        );
        return cr.list();
    }
    
	@SuppressWarnings("unchecked")
	public List<PASTAGroup> getAllGroups() {
		return sessionFactory.getCurrentSession()
				.createCriteria(PASTAGroup.class).list();
	}

	public void giveExtension(PASTAUser user, Assessment assessment, Date extension) {
		AssessmentExtension curExt = getAssessmentExtension(user, assessment);
		if(curExt == null) {
			AssessmentExtension ext = new AssessmentExtension(user, assessment, extension);
			save(ext);
		} else {
			curExt.setNewDueDate(extension);
			update(curExt);
		}
	}

	@SuppressWarnings("unchecked")
	public AssessmentExtension getAssessmentExtension(PASTAUser user, Assessment assessment) {
		return (AssessmentExtension) DataAccessUtils.uniqueResult(
				sessionFactory.getCurrentSession().createCriteria(AssessmentExtension.class)
				.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("assessment", assessment))
				.list());
	}
	
	public Date getExtension(PASTAUser user, Assessment assessment) {
		AssessmentExtension ext = getAssessmentExtension(user, assessment);
		return ext == null ? null : ext.getNewDueDate();
	}

	@SuppressWarnings("unchecked")
	public List<AssessmentExtension> getAllExtensionsForAssessment(long assessmentId) {
		return sessionFactory.getCurrentSession().createCriteria(AssessmentExtension.class)
				.createCriteria("assessment")
				.add(Restrictions.eq("id", assessmentId))
				.list();
	}
}
