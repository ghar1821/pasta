package pasta.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.result.UnitTestResult;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;

@Transactional
@Repository("unitTestDAO")
public class UnitTestDAO extends BaseDAO {

	public void deleteTestTests(UnitTest test) {
		UnitTestResult testResult = test.getTestResult();
		test.setTestResult(null);
		update(test);
		delete(testResult);
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
