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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedField;
import pasta.domain.template.WeightedHandMarking;

@Transactional
@Repository("handMarkingDAO")
public class HandMarkingDAO extends BaseDAO {

	protected final Log logger = LogFactory.getLog(getClass());
	
	@SuppressWarnings("unchecked")
	public List<HandMarking> getAllHandMarkings() {
		return sessionFactory.getCurrentSession().createCriteria(HandMarking.class).list();
	}

	public HandMarking getHandMarking(long id) {
		return (HandMarking) sessionFactory.getCurrentSession().get(HandMarking.class, id);
	}
	
	public WeightedField getWeightedField(long id) {
		return (WeightedField) sessionFactory.getCurrentSession().get(WeightedField.class, id);
	}
	
	public WeightedHandMarking getWeightedHandMarking(long id) {
		return (WeightedHandMarking) sessionFactory.getCurrentSession().get(WeightedHandMarking.class, id);
	}
	
	/**
	 * Create and store a new blank row/column
	 * 
	 * @return the newly created row/column
	 */
	public WeightedField createNewWeightedField() {
		Long newId = save(new WeightedField());
		return getWeightedField(newId);
	}
}
