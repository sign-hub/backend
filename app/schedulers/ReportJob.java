package schedulers;


import core.services.MediaService;
import core.services.ReportService;
import models.Test;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;


public class ReportJob extends WorkerJob {
	

	@Override
	public void doJob() throws Exception {
		try {
			Test t = Test.findById(this.testId);
			this.test = t;
			Logger.info("ReportJob do Job " + this.getId() + " test: " + this.test.getUuid());
			this.startJob();
			//Thread.sleep(60000);
			ReportService.instance().generateCompleteReport(this.test, this.id);
			this.completeTask(false);
			Logger.info("ReportJob complete Job " + this.getId());
		} catch(Exception e) {
			e.printStackTrace();
			this.completeTask(true);
		}
	}

}
