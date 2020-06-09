package schedulers;

import core.services.MediaService;
import play.jobs.Every;
import play.jobs.Job;

@Every("1h")
public class OneHour extends Job {
	
	@Override
	public void doJob() throws Exception {
		MediaService.instance().removeOldPublic();
	}

}
