package pasta.repository;


import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Collection;
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
 * @author Alex
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
						usersByTutorial.get(oldUser.getTutorial()).remove(oldUser.getUsername());
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
						usersByStream.get(oldUser.getStream()).remove(oldUser.getUsername());
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

	public void save(PASTAUser user) {
		updateCachedUser(user);
		getHibernateTemplate().save(user);
	}

	public void update(PASTAUser user) {
		updateCachedUser(user);
		getHibernateTemplate().update(user);
	}
	
	public void delete(PASTAUser user) {
		deleteCachedUser(user);
		getHibernateTemplate().delete(user);
	}
	
	public void replaceStudents(List<PASTAUser> users){
		for(PASTAUser user: allUsers.values()){
			if(!user.isTutor()){
				delete(user);
			}
		}
		
		for(PASTAUser user: users){
			save(user);
		}
		
		loadUsersFromDB();
	}
	
	public void updateStudents(List<PASTAUser> users){
		for(PASTAUser user: users){
			if(allUsers.containsKey(user.getUsername())){
				update(user);
			}
			else{
				save(user);
			}
		}

		loadUsersFromDB();
	}
	
	public void replaceTutors(List<PASTAUser> users){
		for(PASTAUser user: allUsers.values()){
			if(user.isTutor()){
				delete(user);
			}
		}
		
		for(PASTAUser user: users){
			save(user);
		}
		
		loadUsersFromDB();
	}
	
	public void updateTutors(List<PASTAUser> users){
		for(PASTAUser user: users){
			if(allUsers.containsKey(user.getUsername())){
				update(user);
			}
			else{
				save(user);
			}
		}
		
		loadUsersFromDB();
	}
	
	// calculated methods
	
	public PASTAUser getUser(String username){
		if(allUsers == null){
			loadUsersFromDB();
		}
		return allUsers.get(username.toLowerCase());
	}
	
	public Collection<PASTAUser> getUserList(){
		return allUsers.values();
	}
	
	public Collection<PASTAUser> getUserListByTutorial(String tutorialName){
		Set<PASTAUser> users = new TreeSet<PASTAUser>();
		if(usersByTutorial.get(tutorialName) != null){
			for(String user: usersByTutorial.get(tutorialName)){
				users.add(allUsers.get(user));
			}
		}
		return users;
	}
	
	public Collection<PASTAUser> getUserListByStream(String streamName){
		Set<PASTAUser> users = new TreeSet<PASTAUser>();
		if(usersByTutorial.get(streamName) != null){
			for(String user: usersByStream.get(streamName)){
				users.add(allUsers.get(user));
			}
		}
		return users;
	}
	
	public Map<String, Collection<String>> getTutorialByStream(){
		return tutorialByStream;
	}
	
	
	public void add(PASTAUser user){
		allUsers.put(user.getUsername().toLowerCase(), user);
		save(user);
	}
	
	private void loadUsersFromDB(){
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

	public void deleteSingleUser(PASTAUser toDelete) {
		delete(toDelete);
		PASTAUser fullUser = allUsers.get(toDelete.getUsername());
		if(fullUser != null){
			usersByTutorial.get(fullUser.getTutorial()).remove(fullUser.getUsername());
			usersByStream.get(fullUser.getStream()).remove(fullUser.getUsername());
			allUsers.remove(toDelete.getUsername());
		}
	}
}
