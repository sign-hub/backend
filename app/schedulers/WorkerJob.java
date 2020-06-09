package schedulers;

import java.util.Calendar;
import java.util.Date;

import models.Grammar;
import models.Test;
import play.jobs.Job;

public class WorkerJob extends Job{
	
	protected Boolean isWorking;
	protected Boolean isStarted;
	protected Boolean isCompleted;
	protected Boolean isError;
	protected String id;
	protected Test test;
	protected String testId;
	protected String grammarId;
	protected Grammar grammar;
	protected Date toDelete;
	
	
	public Boolean getIsWorking() {
		return isWorking;
	}
	public void setIsWorking(Boolean isWorking) {
		this.isWorking = isWorking;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Test getTest() {
		return test;
	}
	public void setTest(Test test) {
		this.test = test;
	}
	public Boolean getIsStarted() {
		return isStarted;
	}
	public void setIsStarted(Boolean isStarted) {
		this.isStarted = isStarted;
	}
	public Boolean getIsCompleted() {
		return isCompleted;
	}
	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}
	public Boolean getIsError() {
		return isError;
	}
	public void setIsError(Boolean isError) {
		this.isError = isError;
	}
	
	
	
	
	
	public Grammar getGrammar() {
		return grammar;
	}
	public void setGrammar(Grammar grammar) {
		this.grammar = grammar;
	}
	public String getGrammarId() {
		return grammarId;
	}
	public void setGrammarId(String grammarId) {
		this.grammarId = grammarId;
	}
	public Date getToDelete() {
		return toDelete;
	}
	public void setToDelete(Date toDelete) {
		this.toDelete = toDelete;
	}
	
	public void startJob() {
		this.isStarted = true;
		this.isWorking = true;
		this.isError = false;
		this.isCompleted = false;
	}
	
	public void completeTask(boolean isError) {
		this.isWorking = false;
		this.isCompleted = true;
		this.isError = isError;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 120);
		this.toDelete = c.getTime();
	}
	
	public String getCurrentStatus() {
		String ret = null;
		if(!this.getIsStarted())
			 ret = "WAITING";
		 else if(this.getIsCompleted()) {
			 if(this.getIsError())
				 ret = "ERROR";
			 else
				 ret = "COMPLETE";
		 } else 
			 ret = "WORKING";
		return ret;
	}
	public String getTestId() {
		return testId;
	}
	public void setTestId(String testId) {
		this.testId = testId;
	}
	
	
	
}
