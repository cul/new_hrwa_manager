package edu.columbia.ldpd.hrwa.tasks;

import edu.columbia.ldpd.hrwa.HrwaManager;

public abstract class AbstractTask {
	
	long startTime;
	
	public abstract void taskImpl();
	
	public void runTask() {
		beginTask();
		taskImpl();
		endTask();
	}
	
	
	public void beginTask() {
		this.startTime = System.currentTimeMillis() / 1000L;
		HrwaManager.logger.info("Started Task: " + this.getClass().getSimpleName());
	}
	
	public void endTask() {
		HrwaManager.logger.info("Finished Task: " + this.getClass().getSimpleName() + " in " + ((System.currentTimeMillis() / 1000L) - this.startTime) + " seconds");
	}
	
}
