package schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Option;
import models.Question;
import models.Slide;
import models.SlideContentComponent;
import models.Test;
import play.Logger;
import play.jobs.Job;

public class BatchSlideContentComponentFixer extends Job {
	
	private List<String> slideContentComponentUuid;
	private Integer index;
	private List<SlideContentComponent> slideContentComponentList;
	private Integer i;

	public BatchSlideContentComponentFixer(List<String> list, Integer index, int i) {
		this.slideContentComponentUuid = list;
		this.index = index;
		this.i = i;
		slideContentComponentList = new ArrayList<SlideContentComponent>();
	}

	@Override
	public void doJob() {
		Logger.info("BatchSlideContentComponentFixer START " + index + " " + i);
		fixtSlidesComponentsTest();
		Logger.info("BatchSlideContentComponentFixer END "  + index + " " + i);
	}
	
	private void fixtSlidesComponentsTest() {
		for(String uuid : slideContentComponentUuid) {
			if(uuid == null)
				continue;
			SlideContentComponent t = SlideContentComponent.findById(uuid);
			if(t==null)
				continue;
			slideContentComponentList.add(t);
		}
		deleteComponents(slideContentComponentList);
	}
	
	private void deleteComponents(List<SlideContentComponent> slideContentComponentList2) {
		for(SlideContentComponent comp : slideContentComponentList2) {
			deleteSlideContentComponent(comp);
		}
	}


	private void deleteSlideContentComponent(SlideContentComponent comp) {
		String uuid = comp.getUuid();
		//Logger.info("fixSlidesComponents SLIDE deleting component " + uuid);
		if(comp.getOptions()!=null) {
			comp.getOptions().clear();
			comp.save();
		}
		//Logger.info("fixSlidesComponents SLIDE deleting component; OPTIONS " + comp.getOptions());
		Option.deleteBySlideContentComponent(comp);
		comp.delete();
		//Logger.info("fixSlidesComponents SLIDE deleted component ");
		
	}
}
