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

	public boolean authenticate(String unikey, String hashedPassword) {
		Object[] parameters = {unikey, hashedPassword};
		return !(getHibernateTemplate().find("FROM PASTALoginUser WHERE USERNAME=? AND HASHEDPASSWORD=?", parameters).isEmpty());
	}

}
