package schedulers;


import java.util.ArrayList;

import notifiers.Mails;

import org.apache.log4j.spi.LoggingEvent;

import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import utils.StringUtil;

import com.eclettica.appender.AttachmentSmtpAppender;

//@Every("5mn")
@On("0 0/5 * * * ?")
public class ErrNotificationScheduler extends Job {
	
	@Override
	public void doJob() {
		Logger.info("Error Notification Scheduler execution");
		Logger.warn("Error Notification Scheduler execution");
		emailLogControl();
	}
	
	public static void emailLogControl(){
		//ArrayList<LoggingEvent> toRemove = new ArrayList<LoggingEvent>();
		Logger.info("Error Notification Scheduler - Numero eventi: " +AttachmentSmtpAppender.eventsList.size());
		Logger.warn("Error Notification Scheduler - Numero eventi: " +AttachmentSmtpAppender.eventsList.size());
		boolean send = false;
		if(AttachmentSmtpAppender.eventsList.size()>0){
			for(LoggingEvent le : AttachmentSmtpAppender.eventsList){
				if(!StringUtil.isNil(le.getRenderedMessage()))
					send = true;
			}
		}

		if(send){
			ArrayList<LoggingEvent> list = new ArrayList<LoggingEvent>();
			list.addAll(AttachmentSmtpAppender.eventsList);
			AttachmentSmtpAppender.eventsList.clear();
			Mails.logEvent(list, Play.mode.name());
		}
	}
	
}
