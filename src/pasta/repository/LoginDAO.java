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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.PASTALoginUser;

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

}
