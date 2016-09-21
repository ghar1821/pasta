package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;

@Transactional
@Repository("unitTestDAO")
public class UnitTestDAO {

	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void save(UnitTest test) {
		sessionFactory.getCurrentSession().save(test);
		logger.info("Created unit test " + test.getName());
	}
	
	public void update(UnitTest test) {
		sessionFactory.getCurrentSession().update(test);
		logger.info("Updated unit test " + test.getName());
	}
	
	public void delete(UnitTest test) {
		sessionFactory.getCurrentSession().delete(test);
		logger.info("Deleted unit test " + test.getName());
	}
	
	public void deleteTestTests(UnitTest test) {
		sessionFactory.getCurrentSession().delete(test.getTestResult());
		logger.info("Deleted unit test test result" + test.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<UnitTest> getAllUnitTests() {
		return sessionFactory.getCurrentSession().createCriteria(UnitTest.class).list();
	}

	public UnitTest getUnitTest(long id) {
		return (UnitTest) sessionFactory.getCurrentSession().get(UnitTest.class, id);
	}
	
	public WeightedUnitTest getWeightedUnitTest(long id) {
		return (WeightedUnitTest) sessionFactory.getCurrentSession().get(WeightedUnitTest.class, id);
	}
}
