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
		try{
			getHibernateTemplate().save(job);
		}
		catch (Exception e){
			logger.info("Exception while saving job " + e);
		}
	}

	public void update(Job job) {
		try{
			getHibernateTemplate().update(job);
		}
		catch (Exception e){
			logger.info("Exception while updating job " + e);
		}
	}
	
	public void delete(Job job) {
		try{
			getHibernateTemplate().delete(job);
		}
		catch (Exception e){
			logger.info("Exception while deleting job " + e);
		}
	}

	public List<Job> getOutstandingAssessmentJobs(){
		return getHibernateTemplate().find("FROM Job WHERE runDate <= NOW() AND NOT username = 'PASTACompetitionRunner' GROUP BY runDate");
	}
	
	public List<Job> getOutstandingCompetitionJobs(){
		return getHibernateTemplate().find("FROM Job WHERE runDate <= NOW() AND username = 'PASTACompetitionRunner' GROUP BY runDate");
	}
}
