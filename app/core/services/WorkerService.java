package core.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Grammar;
import models.Test;
import schedulers.GrammarJob;
import schedulers.ReportJob;
import schedulers.WorkerJob;

public class WorkerService {

    private static WorkerService instance = null; 
   
    private WorkerService() { 
    	testWorkerMap = new HashMap<String, Map<Class, String>>();
    	workerJobMap = new HashMap<String, WorkerJob>();
    } 
  

    public static WorkerService getInstance() { 
        if (instance == null) 
        	instance = new WorkerService(); 
        return instance; 
    } 
    
    
    private Map<String, Map<Class, String>> testWorkerMap;
    private Map<String, WorkerJob> workerJobMap;

    
    public ReportJob generateReportJob(Test t) {
    	ReportJob rj = getReportJob(t);
    	if(rj!=null)
    		return rj;
    	rj = new ReportJob();
		rj.setId(generateWorkerId());
		rj.setIsWorking(false);
		rj.setTest(t);
		rj.setTestId(t.getUuid());
		if(!this.testWorkerMap.containsKey(t.getUuid()))
			this.testWorkerMap.put(t.getUuid(), new HashMap<Class, String>());
		this.testWorkerMap.get(t.getUuid()).put(ReportJob.class, rj.getId());
		this.workerJobMap.put(rj.getId(), rj);
		return rj;
    }
    
    
	public ReportJob getReportJob(Test t) {
		if(t == null)
			return null;
		if(testWorkerMap.containsKey(t.getUuid())) {
			Map<Class, String> m = testWorkerMap.get(t.getUuid());
			if(m.containsKey(ReportJob.class)) {
				if(workerJobMap.containsKey(m.get(ReportJob.class))) {		
					return (ReportJob) workerJobMap.get(m.get(ReportJob.class));
				}
			}
		}
		return null;	
	}


	private String generateWorkerId() {
		return UUID.randomUUID().toString();
	}


	public WorkerJob getReportJob(String workerId) {
		return workerJobMap.get(workerId);
	}
	
	
	public GrammarJob generateGrammarJob(Grammar g) {
    	GrammarJob rj = getGrammarJob(g);
    	if(rj!=null)
    		return rj;
    	rj = new GrammarJob();
		rj.setId(generateWorkerId());
		rj.setIsWorking(false);
		rj.setGrammar(g);
		rj.setGrammarId(g.getUuid());
		if(!this.testWorkerMap.containsKey(g.getUuid()))
			this.testWorkerMap.put(g.getUuid(), new HashMap<Class, String>());
		this.testWorkerMap.get(g.getUuid()).put(GrammarJob.class, rj.getId());
		this.workerJobMap.put(rj.getId(), rj);
		return rj;
    }
    
    
	public GrammarJob getGrammarJob(Grammar g) {
		if(g == null)
			return null;
		if(testWorkerMap.containsKey(g.getUuid())) {
			Map<Class, String> m = testWorkerMap.get(g.getUuid());
			if(m.containsKey(GrammarJob.class)) {
				if(workerJobMap.containsKey(m.get(GrammarJob.class))) {		
					return (GrammarJob) workerJobMap.get(m.get(GrammarJob.class));
				}
			}
		}
		return null;	
	}

	public WorkerJob getWorkerJob(String workerId) {
		return workerJobMap.get(workerId);
	}
	
	
}
