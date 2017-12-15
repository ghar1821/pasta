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

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.ReplicationMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.BaseEntity;

@Transactional
@Repository("baseDAO")
public class BaseDAO {

	protected final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	protected SessionFactory sessionFactory;
	
	public void saveOrUpdate(BaseEntity entity) {
		Long id = entity.getId();
		sessionFactory.getCurrentSession().saveOrUpdate(entity);
		logger.info((id == entity.getId() ? "Updated " : "Created ") + entity.toString());
	}
	
	public Long save(BaseEntity entity) {
		sessionFactory.getCurrentSession().save(entity);
		logger.info("Created " + entity.toString());
		return entity.getId();
	}
	
	public void update(BaseEntity entity) {
		sessionFactory.getCurrentSession().update(entity);
		logger.info("Updated " + entity.toString());
	}
	
	public void delete(BaseEntity entity) {
		String desc = entity.toString();
		sessionFactory.getCurrentSession().delete(entity);
		logger.info("Deleted " + desc);
	}
	
	public BaseEntity get(Class<? extends BaseEntity> clazz, Long id) {
		return (BaseEntity) sessionFactory.getCurrentSession().get(clazz, id);
	}
}
