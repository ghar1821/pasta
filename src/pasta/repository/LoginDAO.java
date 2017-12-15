package pasta.repository;

import io.jsonwebtoken.impl.crypto.MacProvider;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.PASTALoginUser;
import pasta.domain.security.AuthenticationSettings;

/**
 * Data Access Object for Authentication if using {@link pasta.login.DBAuthValidator}.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-02-25
 */
@Transactional
@Repository("loginDAO")
public class LoginDAO {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private SessionFactory sessionFactory;

	public void save(PASTALoginUser user) {
		sessionFactory.getCurrentSession().save(user);
	}

	public void update(PASTALoginUser user) {
		sessionFactory.getCurrentSession().update(user);
	}
	
	public void delete(PASTALoginUser user) {
		sessionFactory.getCurrentSession().delete(user);
	}

	public boolean authenticate(String username, String hashedPassword) {
		return !sessionFactory.getCurrentSession()
				.createQuery("FROM PASTALoginUser WHERE USERNAME=:username AND HASHEDPASSWORD=:password")
				.setParameter("username",  username)
				.setParameter("password", hashedPassword)
				.list().isEmpty();
	}
	
	public boolean hasPassword(String username) {
		return !sessionFactory.getCurrentSession()
				.createQuery("FROM PASTALoginUser WHERE USERNAME=:username")
				.setParameter("username",  username)
				.list().isEmpty();
	}
	
	public void updatePassword(String username, String hashedPassword){
		PASTALoginUser user = new PASTALoginUser();
		user.setUsername(username);
		user.setHashedPassword(hashedPassword);
		if(hasPassword(username)){
			logger.info("updated password");
			sessionFactory.getCurrentSession().update(user);
		}
		else{
			logger.info("saved password");
			sessionFactory.getCurrentSession().save(user);
		}
	}

	public AuthenticationSettings getAuthSettings() {
		@SuppressWarnings("unchecked")
		List<AuthenticationSettings> settings = sessionFactory.getCurrentSession().createCriteria(AuthenticationSettings.class).list();
		if(settings.isEmpty()) {
			return null;
		}
		if(settings.size() > 1) {
			for(int i = 1; i < settings.size(); i++) {
				sessionFactory.getCurrentSession().delete(settings.get(i));
			}
		}
		return settings.get(0);
	}
	
	public AuthenticationSettings createAuthSettings(String authType, List<String> serverAdresses) {
		AuthenticationSettings newSettings = new AuthenticationSettings();
		newSettings.setKey(MacProvider.generateKey().getEncoded());
		newSettings.setType(authType);
		newSettings.setServerAddresses(serverAdresses);
		sessionFactory.getCurrentSession().save(newSettings);
		return newSettings;
	}

	public void updateAuthSettings(AuthenticationSettings authSettings) {
		sessionFactory.getCurrentSession().update(authSettings);
	}
}
