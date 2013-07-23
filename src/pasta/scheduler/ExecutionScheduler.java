package pasta.scheduler;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

@Repository("executionScheduler")
public class ExecutionScheduler extends HibernateDaoSupport {

	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	public void save(Job job) {
		getHibernateTemplate().save(job);
	}

	public void update(Job job) {
		getHibernateTemplate().update(job);
	}
	
	public void delete(Job job) {
		getHibernateTemplate().delete(job);
	}

	public List<Job> getOutstandingJobs(){
		return getHibernateTemplate().find("FROM Job WHERE runDate <= NOW() GROUP BY runDate");
	}
}
