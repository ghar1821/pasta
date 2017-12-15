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

package pasta.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.result.AssessmentResult;
import pasta.domain.user.PASTAUser;
import pasta.service.ResultManager;
import pasta.util.PASTAUtil;

/**
 * Class that handles the execution of jobs.
 * <p>
 * This class interacts with the database and is used to query
 * the database for any outstanding jobs.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-12-07
 * 
 */
@Transactional
@Repository("executionScheduler")
public class ExecutionScheduler {
	private static Logger logger = Logger.getLogger(ExecutionScheduler.class);
	
	private List<AssessmentJob> jobQueueCache;
	private Date lastQueueUpdate;
	
	@Autowired
	private ResultManager resultManager;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void scheduleJob(PASTAUser user, long assessmentId, AssessmentResult result, Date runDate) {
		if(result.isWaitingToRun()) {
			return;
		}
		result.setWaitingToRun(true);
		resultManager.update(result);
		save(new AssessmentJob(user, assessmentId, runDate, result));
	}
	
	public void save(Job job) {
		try{
			sessionFactory.getCurrentSession().save(job);
		}
		catch (Exception e){
			logger.error("Exception while saving job", e);
		}
	}

	public void update(Job job) {
		try{
			sessionFactory.getCurrentSession().update(job);
		}
		catch (Exception e){
			logger.error("Exception while updating job", e);
		}
	}
	
	public void delete(Job job) {
		try{
			sessionFactory.getCurrentSession().delete(job);
		}
		catch (Exception e){
			logger.error("Exception while deleting job", e);
		}
	}

	/**
	 * Get the outstanding {@link pasta.scheduler.AssessmentJob} for assessments.
	 * <p>
	 * For a job to be outstanding, it has to have a run date later than now.
	 * Pretty much all jobs in the list will fall into this category currently
	 * as there is not currently possible to set the execution of an assessment
	 * job in the future.
	 *  
	 * @return the list of assessment jobs that are outstanding
	 */
	public List<AssessmentJob> getOutstandingAssessmentJobs(){
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentJob.class);
		cr.add(Restrictions.le("runDate", new Date()));
		cr.addOrder(Order.asc("runDate"));

		@SuppressWarnings("unchecked")
		List<AssessmentJob> list = cr.list();
		return list;
	}
	
	/**
	 * Gets the outstanding list of assessment jobs, with a cache that dirties
	 * every 3 seconds. Cache designed to cope with many async requests.
	 * 
	 * @return the cached list of outstanding assessment jobs
	 */
	public List<AssessmentJob> getAssessmentQueue() {
		boolean dirtyCache = 
				jobQueueCache == null ||
				lastQueueUpdate == null ||
				PASTAUtil.elapseTime(lastQueueUpdate, Calendar.SECOND, 3).before(new Date());
		
		if(dirtyCache) {
			jobQueueCache = getOutstandingAssessmentJobs();
			lastQueueUpdate = new Date();
		}
		
		return jobQueueCache;
	}
	public void clearJobCache() {
		jobQueueCache = null;
		lastQueueUpdate = null;
	}
}
