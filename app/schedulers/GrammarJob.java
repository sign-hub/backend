package schedulers;


import core.services.GrammarService;
import core.services.MediaService;
import core.services.ReportService;
import models.Grammar;
import models.Test;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;


public class GrammarJob extends WorkerJob {
	


	@Override
	public void doJob() throws Exception {
		try {
			Grammar grammar = Grammar.findById(this.grammarId);
			this.grammar = grammar;
			Logger.info("GrammarJob do Job " + this.getId() + " grammar: " + this.grammar.getUuid());
			this.startJob();
			//Thread.sleep(60000);
			GrammarService.instance().grammarPdfCreator(this.grammar);
			this.completeTask(false);
			Logger.info("GrammarJob complete Job " + this.getId());
		} catch(Exception e) {
			e.printStackTrace();
			this.completeTask(true);
		}
	}

}
