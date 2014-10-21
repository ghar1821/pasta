/**
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
