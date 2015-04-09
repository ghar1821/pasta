package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.template.UnitTest;

@Repository("unitTestDAO")
public class UnitTestDAO extends HibernateDaoSupport {

	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	public void save(UnitTest test) {
		getHibernateTemplate().save(test);
		logger.info("Created unit test " + test.getName());
	}
	
	public void update(UnitTest test) {
		getHibernateTemplate().update(test);
		logger.info("Updated unit test " + test.getName());
	}
	
	public void delete(UnitTest test) {
		getHibernateTemplate().delete(test);
		logger.info("Deleted unit test " + test.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<UnitTest> getAllUnitTests() {
		return getHibernateTemplate().find("FROM UnitTest");
	}
	
	@Deprecated
	public UnitTest getUnitTest(String name) {
		@SuppressWarnings("unchecked")
		List<UnitTest> results = getHibernateTemplate().find("FROM UnitTest WHERE name = ?", name);
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	public UnitTest getUnitTest(long id) {
		return getHibernateTemplate().get(UnitTest.class, id);
	}
}