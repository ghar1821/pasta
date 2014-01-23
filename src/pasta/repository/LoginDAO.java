package pasta.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.PASTALoginUser;

@Repository("loginDAO")
public class LoginDAO extends HibernateDaoSupport{
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	public void save(PASTALoginUser user) {
		getHibernateTemplate().save(user);
	}

	public void update(PASTALoginUser user) {
		getHibernateTemplate().update(user);
	}
	
	public void delete(PASTALoginUser user) {
		getHibernateTemplate().delete(user);
	}

	public boolean authenticate(String username, String hashedPassword) {
		Object[] parameters = {username, hashedPassword};
		return !(getHibernateTemplate().find("FROM PASTALoginUser WHERE USERNAME=? AND HASHEDPASSWORD=?", parameters).isEmpty());
	}
	
	public boolean hasPassword(String username) {
		Object[] parameters = {username};
		return !(getHibernateTemplate().find("FROM PASTALoginUser WHERE USERNAME=?", parameters).isEmpty());
	}
	
	public void updatePassword(String username, String hashedPassword){
		PASTALoginUser user = new PASTALoginUser();
		user.setUsername(username);
		user.setHashedPassword(hashedPassword);
		if(hasPassword(username)){
			logger.info("updated password");
			getHibernateTemplate().update(user);
		}
		else{
			logger.info("saved password");
			getHibernateTemplate().save(user);
		}
	}

}
