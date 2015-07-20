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

package pasta.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.result.AssessmentResult;
import pasta.domain.result.CompetitionMarks;
import pasta.domain.result.CompetitionResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Competition;
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
@Repository("executionScheduler")
public class ExecutionScheduler extends HibernateDaoSupport {

	private List<AssessmentJob> jobQueueCache;
	private Date lastQueueUpdate;
	
	@Autowired
	private ResultManager resultManager;
	
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	public void scheduleJob(PASTAUser user, long assessmentId, AssessmentResult result, Date runDate) {
		result.setWaitingToRun(true);
		resultManager.update(result);
		save(new AssessmentJob(user, assessmentId, runDate, result));
	}
	
	public void scheduleJob(Competition comp, Arena arena, Date runDate) {
		CompetitionMarks marks = new CompetitionMarks();
		marks.setCompetition(comp);
		CompetitionResult result = new CompetitionResult();
		result.setArena(arena);
		result.setCompetition(comp);
		save(new CompetitionJob(comp, arena, marks, result, runDate));
	}
	
	public void scheduleJob(Competition comp, Date runDate) {
		scheduleJob(comp, null, runDate);
	}

	public void save(Job job) {
		try{
			getHibernateTemplate().save(job);
		}
		catch (Exception e){
			logger.error("Exception while saving job", e);
		}
	}

	public void update(Job job) {
		try{
			getHibernateTemplate().update(job);
		}
		catch (Exception e){
			logger.error("Exception while updating job", e);
		}
	}
	
	public void delete(Job job) {
		try{
			getHibernateTemplate().delete(job);
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
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentJob.class);
		cr.add(Restrictions.le("runDate", new Date()));
		cr.addOrder(Order.asc("runDate"));
		return getHibernateTemplate().findByCriteria(cr);
	}
	
	/**
	 * Get the outstanding {@link pasta.scheduler.CompetitionJob} for competitions.
	 *  
	 * @return the list of competition jobs that are outstanding
	 */
	public List<CompetitionJob> getOutstandingCompetitionJobs(){
		DetachedCriteria cr = DetachedCriteria.forClass(CompetitionJob.class);
		cr.add(Restrictions.le("runDate", new Date()));
		cr.add(Restrictions.isNull("arena"));
		cr.addOrder(Order.asc("runDate"));
		return getHibernateTemplate().findByCriteria(cr);
	}
	
	/**
	 * Get the outstanding {@link pasta.scheduler.CompetitionJob} for competitions.
	 *  
	 * @return the list of competition jobs that are outstanding
	 */
	public List<CompetitionJob> getOutstandingArenaJobs(){
		DetachedCriteria cr = DetachedCriteria.forClass(CompetitionJob.class);
		cr.add(Restrictions.le("runDate", new Date()));
		cr.add(Restrictions.isNotNull("arena"));
		cr.addOrder(Order.asc("runDate"));
		return getHibernateTemplate().findByCriteria(cr);
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
