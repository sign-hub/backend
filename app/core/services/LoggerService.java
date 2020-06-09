package core.services;

import models.LogEntry;

public class LoggerService {

	private static enum LoggerServiceSingleton {
		INSTANCE;

		LoggerService singleton = new LoggerService();

		public LoggerService getSingleton() {
			return singleton;
		}
	}

	public static LoggerService instance() {
		return LoggerService.LoggerServiceSingleton.INSTANCE.getSingleton();
	}

	private LoggerService() {
		// TODO Auto-generated constructor stub
	}

	public void log(String type, String status, Object obj) {
		LogEntry le = new LogEntry(type, status, obj);
		le.save();
	}
	
	
	
	
}
