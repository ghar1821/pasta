package pasta.repository;


import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.PASTAUser;
import pasta.util.ProjectProperties;
/**
 * The Data access object for the User class.
 * @author Alex
 *
 */
@Repository("userDAO")
public class UserDAO extends HibernateDaoSupport{
	
	HashMap<String, PASTAUser> allUsers = null;
	HashMap<String, Collection<PASTAUser>> usersByTutorial = null;
	HashMap<String, Collection<PASTAUser>> usersByStream = null;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	public void save(PASTAUser user) {
		getHibernateTemplate().save(user);
	}

	public void update(PASTAUser user) {
		getHibernateTemplate().update(user);
	}
	
	public void delete(PASTAUser user) {
		getHibernateTemplate().delete(user);
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
		return usersByTutorial.get(tutorialName);
	}
	
	public Collection<PASTAUser> getUserListByStream(String streamName){
		return usersByStream.get(streamName);
	}
	
	public void add(PASTAUser user){
		allUsers.put(user.getUsername().toLowerCase(), user);
		save(user);
	}
	
	private void loadUsersFromDB(){
		List<PASTAUser> users = getHibernateTemplate().loadAll(PASTAUser.class);
		allUsers = new HashMap<String, PASTAUser>();
		usersByTutorial = new HashMap<String, Collection<PASTAUser>>();
		usersByStream = new HashMap<String, Collection<PASTAUser>>();
		if(users != null){
			for(PASTAUser user: users){
				allUsers.put(user.getUsername().toLowerCase(), user);
				
				if(!usersByStream.containsKey(user.getStream())){
					usersByStream.put(user.getStream(), new ArrayList<PASTAUser>());
				}
				usersByStream.get(user.getStream()).add(user);
				
				if(!usersByTutorial.containsKey(user.getTutorial())){
					usersByTutorial.put(user.getTutorial(), new ArrayList<PASTAUser>());
				}
				usersByTutorial.get(user.getTutorial()).add(user);
				
				// load extension file
				Scanner in;
				try {
					in = new Scanner(new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/" +
							user.getUsername() + "/user.extensions"));
					while(in.hasNextLine()){
						String[] line = in.nextLine().split(">");
						if(line.length == 2){
							try {
								user.getExtensions().put(line[0], ProjectProperties.parseDate(line[1]));
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
}
