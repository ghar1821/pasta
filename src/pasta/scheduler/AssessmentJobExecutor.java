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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import pasta.repository.ResultDAO;
import pasta.service.ExecutionManager;

public class AssessmentJobExecutor extends ThreadPoolExecutor {

	protected Logger logger = Logger.getLogger(getClass());
	
	@Autowired private ResultDAO resultDAO;
	@Autowired private ExecutionScheduler scheduler;
	@Autowired protected ExecutionManager executionManager;

	private ConcurrentSkipListSet<Long> processingIds;
	private ConcurrentSkipListSet<Long> executingIds;
	private ConcurrentMap<String, Lock> locks;
	
	public AssessmentJobExecutor(int corePoolSize, int maxPoolSize) {
		super(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		locks = new ConcurrentHashMap<String, Lock>();
		processingIds = new ConcurrentSkipListSet<Long>();
		executingIds = new ConcurrentSkipListSet<Long>();
	}
	
	public boolean offer(AssessmentJob job) {
		logger.trace("Offering " + job.toString());
		if(executionManager == null) {
			logger.warn("Rejecting job: no execution manager.");
			return false;
		}
		if(hasJob(job)) {
			logger.trace("Rejecting job - already have it");
			return false;
		}
		logger.trace("Accepting job");
		processingIds.add(job.getId());
		execute(new AssessmentJobTask(job, executionManager, locks));
		return true;
	}
	
	public boolean hasJob(AssessmentJob job) {
		return processingIds.contains(job.getId());
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		AssessmentJobTask task = (AssessmentJobTask) r;
		executingIds.add(task.job.getId());
		task.job.setRunning(true);
		scheduler.update(task.job);
		logger.debug("Starting execution of " + r.toString());
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		logger.debug("Finishing execution of " + r.toString());
		AssessmentJobTask task = (AssessmentJobTask) r;
		if(t != null) {
			logger.error("Error running job: ", t);
		}
		task.job.getResults().setWaitingToRun(false);
		try {
			resultDAO.update(task.job.getResults());
		} catch(Exception e) {
			logger.error("Unable to update results from assessment job #" + task.job.getId(), e);
		}
		task.job.setRunning(false);
		scheduler.update(task.job);
		processingIds.remove(task.job.getId());
		executingIds.remove(task.job.getId());
		scheduler.delete(task.job);
	}
	
	public void clearAllTasks() {
		Iterator<Runnable> it = this.getQueue().iterator();
		while(it.hasNext()) {
			it.next();
			it.remove();
		}
		processingIds.clear();
		executingIds.clear();
		locks.clear();
	}
	
	public boolean isExecuting(AssessmentJob job) {
		return executingIds.contains(job.getId());
	}
	
	static class AssessmentJobTask implements Runnable {

		private AssessmentJob job;
		private String lockKey;
		private ExecutionManager manager;
		private Lock lock;
		
		public AssessmentJobTask(AssessmentJob job, ExecutionManager manager, ConcurrentMap<String, Lock> locks) {
			this.job = job;
			this.lockKey = job.getUser().getUsername() + "|" + job.getAssessmentId();
			this.manager = manager;
			this.lock = locks.get(this.lockKey);
			if(this.lock == null) {
				this.lock = new ReentrantLock();
				locks.put(this.lockKey, this.lock);
			}
		}
		
		public boolean is(AssessmentJob job) {
			if(this.job == null && job == null) {
				return true;
			}
			if(this.job == null || job == null) {
				return false;
			}
			return this.job.getId() == job.getId();
		}

		public AssessmentJob getJob() {
			return job;
		}
		
		public String getLockKey() {
			return lockKey;
		}
		
		@Override
		public void run() {
			this.lock.lock();
			manager.executeNormalJob(job);
			this.lock.unlock();
		}
		
		@Override
		public String toString() {
			return "Execution task: " + job.toString();
		}
	}
}
