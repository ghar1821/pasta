/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
