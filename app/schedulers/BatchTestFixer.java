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

public class BatchTestFixer extends Job {
	
	private List<String> testListUuid;
	private Integer index;
	private List<Test> testList;

	public BatchTestFixer(List<String> list, Integer index) {
		this.testListUuid = list;
		this.index = index;
		testList = new ArrayList<Test>();
	}

	@Override
	public void doJob() {
		Logger.info("BatchTestFixer START " + index);
		fixtSlidesComponentsTest();
		Logger.info("BatchTestFixer END "  + index);
	}
	
	private void fixtSlidesComponentsTest() {
		for(String uuid : testListUuid) {
			if(uuid == null)
				continue;
			Test t = Test.findById(uuid);
			testList.add(t);
		}
		fixtSlidesComponentsTest1(testList);
	}
	
	private void fixtSlidesComponentsTest1(List<Test> testList) {
		for(Test t : testList){
			
			Logger.info("fixSlidesComponents TEST:" + t.getUuid() + " " + index);
			List<Question> questionList = t.getQuestions();
			for(Question q : questionList){
				if(q.getSlides() == null)
					continue;
				Logger.info("fixSlidesComponents QUESTION:" + q.getName() + " " + index);
				for(Slide s : q.getSlides()) {
					fixSlideComponents(s);
				}
			}
		}
	}

	private void fixSlideComponents(Slide s) {
		if(s==null)
			return;
		Logger.info("fixSlidesComponents SLIDE:" + s.getUuid() + " " + index);
		//PRIMA OPERAZIONE: RIPULIRE SLIDE CONTENT COMPONENTS CHE NON SONO CONTENUTI NELLA TABELLA DI JOIN
		Set<SlideContentComponent> components;
		if(s.getSlideContent() == null) {
			components = new HashSet<SlideContentComponent>();
		} else {
			components = s.getSlideContent();
		}
		Logger.info("fixSlidesComponents SLIDE components:" + components.size() + " " + index);
		List<SlideContentComponent> components1 = SlideContentComponent.findBySlide(s);
		Logger.info("fixSlidesComponents SLIDE components1:" + components1.size() + " " + index);
		
		Set<SlideContentComponent> todelete = new HashSet<SlideContentComponent>();
		if(components.isEmpty()) {
			todelete.addAll(components1);
		} else {
			for(SlideContentComponent comp : components1) {
				if(!components.contains(comp)) {
					todelete.add(comp);
					Logger.info("fixSlidesComponents SLIDE todelete adding:" + comp.getUuid() + " " + index);
				}
			}
		}
		Logger.info("fixSlidesComponents SLIDE todelete:" + todelete.size() + " " + index);
		for(SlideContentComponent comp : todelete) {
			deleteSingleSlideContentComponent(comp);
		}
//		deleteSlideContentComponent(todelete);
		//SECONDA OPERAZIONE: ACCORPARE SLIDE CONTENT COMPONENT DUPLICATI
		Map<String, SlideContentComponent> contentComponents = new HashMap<String, SlideContentComponent>();
		todelete = new HashSet<SlideContentComponent>();
		Set<SlideContentComponent> newComponents = new HashSet<SlideContentComponent>();
		for(SlideContentComponent comp : components) {
			String hash = calculateHash(comp);
			if(contentComponents.containsKey(hash)) {
				Logger.info("fixSlidesComponents SLIDE contentComponents contains: " + hash + " " + comp.getUuid() + " " + index);
				todelete.add(comp);
			} else {
				Logger.info("fixSlidesComponents SLIDE contentComponents put: " + hash + " " + index);
				contentComponents.put(hash, comp);
				newComponents.add(comp);
			}
		}
		Logger.info("fixSlidesComponents SLIDE newComponents: " + newComponents.size() + " " + index);
		Logger.info("fixSlidesComponents SLIDE todelete2: " + todelete.size() + " " + index);
		s.setSlideContent(newComponents);
		s.save();
		
//		for(SlideContentComponent comp : todelete) {
//			deleteSingleSlideContentComponent(comp);
//		}
//		deleteSlideContentComponent(todelete);
	}

	private void deleteSlideContentComponent(Set<SlideContentComponent> todelete) {
		List<String> uuids = new ArrayList<String>();
		int i = 0;
		for(SlideContentComponent comp : todelete) {
			uuids.add(comp.getUuid());
			if(uuids.size() == 50) {
				i++;
				BatchSlideContentComponentFixer bsccf = new BatchSlideContentComponentFixer(uuids, index, i);
				bsccf.now();
			}
		}
		if(uuids.size() > 0) {
			i++;
			BatchSlideContentComponentFixer bsccf = new BatchSlideContentComponentFixer(uuids, index, i);
			bsccf.now();
		}
	}

	private String calculateHash(SlideContentComponent comp) {
		if(comp == null)
			return null;
		String ret = comp.getComponentType().name() + "-" + comp.getPos();
		return ret;
	}

	private void deleteSingleSlideContentComponent(SlideContentComponent comp) {
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
