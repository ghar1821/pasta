package pasta.repository;


import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.PASTAUser;
/**
 * The Data access object for the User class.
 * @author Alex
 *
 */
@Repository("userDAO")
public class UserDAO extends HibernateDaoSupport{
	
	HashMap<String, PASTAUser> allUsers = null;
	
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
		return allUsers.get(username);
	}
	
	public void add(PASTAUser user){
		allUsers.put(user.getUsername(), user);
		save(user);
	}
	
	private void loadUsersFromDB(){
		List<PASTAUser> users = getHibernateTemplate().loadAll(PASTAUser.class);
		allUsers = new HashMap<String, PASTAUser>();
		if(users != null){
			for(PASTAUser user: users){
				allUsers.put(user.getUsername(), user);
			}
		}
	}
}
