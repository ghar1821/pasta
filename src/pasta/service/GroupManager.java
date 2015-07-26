package pasta.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.form.UpdateGroupsForm;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.repository.UserDAO;

@Service("groupManager")
@Repository
public class GroupManager {
	public static final Logger logger = Logger.getLogger(GroupManager.class);
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private UserDAO userDAO;
	
	public PASTAGroup getGroup(long id) {
		return userDAO.getGroup(id);
	}
	
	public Set<PASTAGroup> getGroups(Assessment assessment) {
		TreeSet<PASTAGroup> allGroups = new TreeSet<PASTAGroup>(userDAO.getGroups(assessment));
		while(allGroups.size() < assessment.getGroupCount()) {
			PASTAGroup newGroup = newGroup(assessment, allGroups);
			allGroups.add(newGroup);
			userDAO.save(newGroup);
		}
		
		return allGroups;
	}
	
	private PASTAGroup newGroup(Assessment assessment, Collection<PASTAGroup> existingGroups) {
		if(existingGroups == null) {
			existingGroups = getGroups(assessment);
		}
		int number = 1;
		for(PASTAGroup group : existingGroups) {
			if(group.getNumber() > number) {
				break;
			}
			number++;
		}
		PASTAGroup newGroup = new PASTAGroup();
		newGroup.setAssessment(assessment);
		newGroup.setNumber(number);
		return newGroup;
	}
	
	public PASTAGroup getGroup(PASTAUser user, Assessment assessment) {
		return userDAO.getGroup(user, assessment);
	}
	
	public PASTAGroup getGroup(PASTAUser user, long assessmentId) {
		return userDAO.getGroup(user, assessmentId);
	}
	
	public List<PASTAGroup> getAllUserGroups(PASTAUser user) {
		return userDAO.getAllUserGroups(user);
	}
	
	public List<PASTAGroup> getAllGroups() {
		return userDAO.getAllGroups();
	}
	
	public Map<PASTAUser, Map<Long, PASTAGroup>> getAllUserGroups(Collection<PASTAUser> users) {
		Map<Long, PASTAGroup> groupLookup = new HashMap<>();
		for(PASTAGroup group : getAllGroups()) {
			groupLookup.put(group.getId(), group);
		}
		Map<Long, PASTAUser> userLookup = new HashMap<>();
		for(PASTAUser user : userDAO.getAllStudentList()) {
			userLookup.put(user.getId(), user);
		}
		List<Long[]> joins = userDAO.getAllUserGroups(users);
		Map<PASTAUser, Map<Long, PASTAGroup>> results = new TreeMap<>();
		for(Object[] join : joins) {
			PASTAUser user = userLookup.get(join[0]);
			PASTAGroup group = groupLookup.get(join[1]);
			if(user != null) {
				Map<Long, PASTAGroup> userGroups = results.get(user);
				if(userGroups == null) {
					userGroups = new HashMap<>();
					results.put(user, userGroups);
				}
				userGroups.put(group.getAssessment().getId(), group);
			}
		}
		return results;
	}

	public int getUsedGroupCount(Assessment assessment) {
		List<PASTAGroup> groups = userDAO.getGroups(assessment);
		int count = 0;
		for(PASTAGroup group : groups) {
			if(!group.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	public boolean leaveCurrentGroup(PASTAUser user, Assessment assessment) {
		if(assessment.isGroupsLocked()) {
			return false;
		}
		PASTAGroup myGroup = getGroup(user, assessment);
		if(myGroup == null) {
			return true;
		}
		if(myGroup.removeMember(user)) {
			userDAO.update(myGroup);
			return true;
		}
		return false;
	}

	public boolean joinGroup(PASTAUser user, Assessment assessment, PASTAGroup group) {
		if(assessment.isGroupsLocked() || group.isLocked()) {
			return false;
		}
		if(assessment.isUnlimitedGroupSize() || group.getSize() < assessment.getGroupSize()) {
			if(leaveCurrentGroup(user, assessment)) {
				if(group.addMember(user)) {
					userDAO.update(group);
					return true;
				}
			}
		}
		return false;
	}

	public boolean addGroup(Assessment assessment) {
		long currentCount = userDAO.getGroupCount();
		if(assessment.isUnlimitedGroupCount() || 
				currentCount < assessment.getGroupCount()) {
			PASTAGroup newGroup = newGroup(assessment, null);
			userDAO.save(newGroup);
			return true;
		}
		return false;
	}

	public void removeExtraGroups(Assessment assessment, int numToRemove) {
		Set<PASTAGroup> groups = getGroups(assessment);
		ListIterator<PASTAGroup> it = new LinkedList<PASTAGroup>(groups).listIterator(groups.size());
		int removed = 0;
		while(it.hasPrevious() && removed < numToRemove) {
			PASTAGroup group = it.previous();
			if(group.isEmpty()) {
				it.remove();
				userDAO.delete(group);
				removed++;
			}
		}
	}

	public int getGroupCount(Assessment assessment) {
		return userDAO.getGroupCount();
	}

	public void saveAllGroups(Assessment assessment, UpdateGroupsForm form) {
		List<PASTAGroup> existingGroups = userDAO.getGroups(assessment);
		Map<String, PASTAUser> users = userDAO.getAllStudentMap();
		
		Map<Long, List<String>> newMembers = form.getGroupMembers();
		Set<PASTAGroup> toUpdate = new TreeSet<PASTAGroup>();
		for(PASTAGroup group : existingGroups) {
			List<String> newGroupMembers = newMembers.get(group.getId());
			if(newGroupMembers == null) {
				if(!group.isEmpty()) {
					group.removeAllMembers();
					toUpdate.add(group);
				}
				continue;
			}
			
			// Cannot use the Set.remove() method... see comments in PASTAGroup
			Iterator<PASTAUser> it = group.getMembers().iterator();
			while(it.hasNext()) {
				PASTAUser existingUser = it.next();
				if(newGroupMembers.contains(existingUser.getUsername())) {
					users.remove(existingUser.getUsername());
				} else {
					it.remove();
					toUpdate.add(group);
				}
			}
		}
		for(PASTAGroup group : toUpdate) {
			userDAO.update(group);
		}
		toUpdate.clear();
		
		for(PASTAGroup group : existingGroups) {
			List<String> newGroupMembers = newMembers.get(group.getId());
			if(newGroupMembers == null) {
				continue;
			}
			for(String username : newGroupMembers) {
				PASTAUser newMember = users.get(username);
				if(newMember != null) {
					group.addMember(newMember);
					users.remove(username); // Prevents users in multiple groups
					toUpdate.add(group);
				}
			}
		}
		for(PASTAGroup group : toUpdate) {
			userDAO.update(group);
		}
	}

	public List<PASTAGroup> getGroups(Collection<PASTAUser> users, long assessmentId) {
		return userDAO.getGroups(users, assessmentId);
	}
	
	public List<PASTAGroup> getGroups(Collection<PASTAUser> users) {
		return userDAO.getGroups(users, -1);
	}

	public void toggleLock(PASTAUser user, PASTAGroup group, boolean lock) {
		boolean isLocked = group.isLocked();
		if(isLocked == lock) {
			return;
		}
		if(!user.isTutor() && !group.isMember(user)) {
			return;
		}
		group.setLocked(lock);
		userDAO.update(group);
	}
}
