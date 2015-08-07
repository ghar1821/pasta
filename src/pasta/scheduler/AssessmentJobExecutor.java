package pasta.scheduler;

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

import pasta.service.ExecutionManager;
import pasta.util.ProjectProperties;

public class AssessmentJobExecutor extends ThreadPoolExecutor {

	protected Logger logger = Logger.getLogger(getClass());
	
	@Autowired private ExecutionScheduler scheduler;
	@Autowired protected ExecutionManager executionManager;

	private ConcurrentSkipListSet<Long> processingIds;
	private ConcurrentMap<String, Lock> locks;
	
	public AssessmentJobExecutor(int corePoolSize, int maxPoolSize) {
		super(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		locks = new ConcurrentHashMap<String, Lock>();
		processingIds = new ConcurrentSkipListSet<Long>();
	}
	
	public boolean offer(AssessmentJob job) {
		if(executionManager == null) {
			logger.warn("Rejecting job: no execution manager.");
			return false;
		}
		if(hasJob(job)) {
			return false;
		}
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
		task.lock();
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		AssessmentJobTask task = (AssessmentJobTask) r;
		if(t != null) {
			logger.error("Error running job: ", t);
		}
		task.job.getResults().setWaitingToRun(false);
		try {
			ProjectProperties.getInstance().getResultDAO().update(task.job.getResults());
		} catch(Exception e) {
			logger.error("Unable to update results from assessment job #" + task.job.getId(), e);
		}
		scheduler.delete(task.job);
		processingIds.remove(task.job.getId());
		task.unlock();
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
		
		public void lock() {
			this.lock.lock();
		}
		
		public void unlock() {
			this.lock.unlock();
		}
		
		@Override
		public void run() {
			manager.executeNormalJob(job);
		}
	}
}
