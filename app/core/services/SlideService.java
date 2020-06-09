/**
 *  
 */
package core.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import com.google.gson.Gson;

import core.services.ApiService.OKResponseObject;
import models.Media;
import models.Option;
import models.Question;
import models.Question.TransitionType;
import models.Slide;
import models.Slide.SlideType;
import models.SlideContentComponent;
import models.SlideContentComponent.ComponentType;
import models.Test;
import models.Topic;
import models.User;
import play.Logger;
import play.db.jpa.JPA;
import utils.StringUtil;

/**
 *
 * @author luca
 */
public class SlideService {

	private static enum SlideServiceSingleton {
		INSTANCE;

		SlideService singleton = new SlideService();

		public SlideService getSingleton() {
			return singleton;
		}
	}

	public static SlideService instance() {
		return SlideService.SlideServiceSingleton.INSTANCE.getSingleton();
	}

	
	private Map<String, Topic> topics;
	private Map<Integer, Map<String, Topic>> topicsMap;
	private int maxLength;
	
	private SlideService() {
		this.topics = TopicService.instance().getTopicMap();
		buildTopicsMap();
	}

	private void buildTopicsMap() {
		this.topicsMap = new HashMap<Integer, Map<String, Topic>>();
		this.maxLength = 0;
		String[] splitted;
		for(String str : this.topics.keySet()){
			System.out.println("toinsert " + str);
			splitted = null;
			splitted = str.split(" ");
			Integer len = splitted.length;
			if(!this.topicsMap.containsKey(len))
				this.topicsMap.put(len, new HashMap<String, Topic>());
			this.topicsMap.get(len).put(str, this.topics.get(str));
			if(len > this.maxLength)
				this.maxLength = len;
		}
	}


	private static OptionService optionService = OptionService.instance();
	private static ApiService apiService = ApiService.instance();
	private static UserService userService = UserService.instance();

	public static class SlideObject {
		public String slideId;
		public String type;
		public List<String> transitionType;
		public Map<String, String> options;
		public SlideContentObject slideContent;
		public Boolean toEdit;
		public Boolean deleted;
	}

	public static class SlideContentObject {
		public List<SingleComponentArrayFacade> componentArray;
		public Boolean toEdit;

	}

	public static class SingleComponentArrayFacade {
		public String componentType;
		public String mediaId;
		public String pos;
		public String dim;
		public Map<String, String> options;
		public String id;
	}

	public List<SlideObject> buildSlideObjectListFromSlideList(Collection<Slide> slides, Boolean isPlay) {
		System.out.println("buildSlideObjectListFromSlideList " + slides.size());
		List<SlideObject> ret = new LinkedList<SlideObject>();
		if (slides != null && !slides.isEmpty()) {
			Iterator<Slide> it = slides.iterator();
			while(it.hasNext()) {
				Slide slide = it.next();
				if(slide == null || slide.getDeleted() == true) {
					System.out.println("buildSlideObjectListFromSlideList slide is null??? " + (slide==null));
					continue;
				}
				SlideObject slideObject = buildSlideObjectFromSlide(slide, isPlay);
				ret.add(slideObject);
			}
			/*for (Slide slide : slides) {
				if(slide == null || slide.getDeleted() == true) {
					System.out.println("buildSlideObjectListFromSlideList slide is null??? " + (slide==null));
					continue;
				}
				SlideObject slideObject = buildSlideObjectFromSlide(slide, isPlay);
				ret.add(slideObject);
			}*/
		}
		return ret;
	}

	public SlideObject buildSlideObjectFromSlide(Slide slide, Boolean isPlay) {
		User author = slide.getQuestion().getTest().getAuthor();
		SlideObject slideObject = new SlideObject();
		Set<Option> oo = slide.getOptions();
		slideObject.options = optionService.buildOptionsMapFromOptionsList(oo);
		//System.out.println("AAAAAAAAAAAAAAAAA-" + slide.getUuid());
		slideObject.slideContent = buildSlideContentObjectFromSlideContentList(author, slide.getSlideContent(), isPlay, slide.getTest());
		slideObject.slideId = slide.getUuid();

		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(slide.getTest()))) {
			slideObject.toEdit = true;
		} else {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (currentUserLogged.equals(author)) {
				slideObject.toEdit = true;
			} else {
				slideObject.toEdit = false;
			}
		}

		if (slide.getTransitionType() != null)
			slideObject.transitionType = TransitionType
					.buildTransitionTypesStringListFromString(slide.getTransitionType());
		if (slide.getType() != null)
			slideObject.type = slide.getType().name();
		slideObject.deleted = slide.getDeleted();
		return slideObject;
	}

	public SlideContentObject buildSlideContentObjectFromSlideContentList(User author,
			Set<SlideContentComponent> slideContent, Boolean isPlay, Test test) {
		SlideContentObject ret = new SlideContentObject();
		List<SingleComponentArrayFacade> componentArray = new LinkedList<SingleComponentArrayFacade>();
		if (slideContent != null && !slideContent.isEmpty()) {
			for (SlideContentComponent slideContentComponent : slideContent) {
				SingleComponentArrayFacade singleComponentArrayFacade = new SingleComponentArrayFacade();
				singleComponentArrayFacade.id = slideContentComponent.getUuid();
				singleComponentArrayFacade.componentType = slideContentComponent.getComponentType().name();
				singleComponentArrayFacade.dim = slideContentComponent.getDim();
				if (slideContentComponent.getMedia() != null)
					singleComponentArrayFacade.mediaId = slideContentComponent.getMedia().getUuid();
				Set<Option> oo = slideContentComponent.getOptions();
				singleComponentArrayFacade.options = optionService
						.buildOptionsMapFromOptionsList(oo);
				singleComponentArrayFacade.pos = slideContentComponent.getPos();
				
				// Nel caso in cui sto effettuando il play e il componente è un text block devo costruire i tags
				if(isPlay && slideContentComponent.getComponentType().equals(ComponentType.TEXTBLOCK)){
					if(singleComponentArrayFacade.options.containsKey("text")){
						singleComponentArrayFacade.options.put("text", buildTextWithTopics(singleComponentArrayFacade, singleComponentArrayFacade.options.get("text")));
					}
					if(singleComponentArrayFacade.options.containsKey("htmlText")){
						singleComponentArrayFacade.options.put("htmlText", buildTextWithTopics(singleComponentArrayFacade, singleComponentArrayFacade.options.get("htmlText")));
					}
				}
				
				componentArray.add(singleComponentArrayFacade);
			}
		}
		ret.componentArray = componentArray;
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(test))) {
			ret.toEdit = true;
		} else {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (currentUserLogged.equals(author)) {
				ret.toEdit = true;
			} else {
				ret.toEdit = false;
			}
		}
		return ret;
	}

	private String buildTextWithTopicsOrig(SingleComponentArrayFacade singleComponentArrayFacade, String htmlTest) {
		String ret = "";
		String totest = htmlTest;
		
		String cleaned = Jsoup.parse(htmlTest).text();
		System.out.println(cleaned);
		String[] splitted = cleaned.split(" ");
		List<String> found = new LinkedList<String>();
		
		for(String s : splitted){
			if(this.topics.containsKey(cleanWord(s))){
				System.out.println("found: " + s);
				/*if(found.contains(s))
					found.remove(s);*/
				found.add(s);
			}
		}
		Map<String, Topic> topicFound = new HashMap<String, Topic>();
		if(!found.isEmpty()){
			for(int i = found.size()-1; i>=0; i--){
				String s = found.get(i);
				System.out.println("topicizing: " + s);
				s = cleanWord(s);
				int ind = totest.lastIndexOf(s);
				String sub = totest.substring(ind);
				totest = totest.substring(0, ind);
				
				String sub1 = sub.substring(0, s.length());
				String sub2 = sub.substring(s.length());
				Topic t = this.topics.get(s);
				sub = "<fra tid='" + t.getUuid()+"'>"+sub1+"</fra>" + sub2;
				//if(singleComponentArrayFacade.options.topics == null)
				topicFound.put(t.getUuid(), t);
				//System.out.println(sub);
				ret = sub + ret;
				
			}
		}
		ret = totest + ret;
		System.out.println(ret);
		Gson gson = new Gson();
		String foundTopicString = gson.toJson(topicFound);
		System.out.println(foundTopicString);
		singleComponentArrayFacade.options.put("topics", foundTopicString);
		return ret;
	}
	
	
	
	private String buildTextWithTopics(SingleComponentArrayFacade singleComponentArrayFacade, String htmlTest) {
		String ret = "";
		String totest = htmlTest;
		
		String cleaned = Jsoup.parse(htmlTest).text();
		System.out.println(cleaned);
		String[] splitted = cleaned.split(" ");
		List<String> found = new LinkedList<String>();
		String toCheck;
		String sep;
		for(int i = 0; i < splitted.length;){
			for(int y = this.maxLength; y>0; y--){
				if(i+y >  splitted.length)
					continue;
				toCheck = "";
				sep = "";
				for(int k = 0; k < y; k++){
					toCheck += sep + splitted[i+k];
					sep = " ";
				}
				toCheck = cleanChars(toCheck);
				System.out.println("tocheck "+ toCheck);
				if(this.topicsMap.get(y).containsKey(toCheck)){
					System.out.println("found: " + toCheck);
					found.add(toCheck);
					i = i + (y-1);
					break;
				}
			}
			i++;
		}
		
		
		
		Map<String, Topic> topicFound = new HashMap<String, Topic>();
		if(!found.isEmpty()){
			for(int i = found.size()-1; i>=0; i--){
				String s = found.get(i);
				System.out.println("topicizing: " + s);
				int ind = totest.lastIndexOf(s);
				if(ind>=0) {
					String sub = totest.substring(ind);
					totest = totest.substring(0, ind);
					
					String sub1 = sub.substring(0, s.length());
					String sub2 = sub.substring(s.length());
					Topic t = this.topics.get(s);
					sub = "<fra tid='" + t.getUuid()+"'>"+sub1+"</fra>" + sub2;
					//if(singleComponentArrayFacade.options.topics == null)
					topicFound.put(t.getUuid(), t);
					//System.out.println(sub);
					ret = sub + ret;
				} else {
					System.out.println("non topicized: " + s);
					/*Map<String, String> map = substitute(s, totest, ret);
					if(map.containsKey("totest")) {
						totest = map.get("totest");
						ret = map.get("ret");
						Topic t = this.topics.get(s);
						topicFound.put(t.getUuid(), t);
					}*/
				}
			}
		}
		ret = totest + ret;
		System.out.println(ret);
		Gson gson = new Gson();
		String foundTopicString = gson.toJson(topicFound);
		System.out.println(foundTopicString);
		singleComponentArrayFacade.options.put("topics", foundTopicString);
		return ret;
	}
	
	private String cleanChars(String toCheck) {
		if(!Character.isLetter(toCheck.charAt(0)))
			return toCheck.substring(1);
		if(!Character.isLetter(toCheck.charAt(toCheck.length()-1)))
			return toCheck.substring(0,toCheck.length()-1);
		return toCheck;
	}

	private Map<String, String> substitute(String base, String totest, String rr) {
		Map<String, String> ret = new HashMap<String, String>();
		String pattern = "";
		String[] ss = base.split(" ");
		String sep = ".*";
		Integer groups = ss.length -1;
		for(int i = 0; i< ss.length; i++) {
			pattern += sep + ss[i];
			sep = "(\\s+|(\\s*(<\\/?[a-zA-Z].*>)*\\s+)|(\\s+(<\\/?[a-zA-Z].*>)*\\s*))";
		}
		 // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);
	      // Now create matcher object.
	      Matcher m = r.matcher(totest);
	      
	      if(m.find()) {
	    	  String sub = totest.substring(m.start());
	    	  totest = totest.substring(0, m.start());
	    	  String sub1 = sub.substring(0, m.end());
			  String sub2 = sub.substring(m.end());
			  Topic t = this.topics.get(base);
			  String sub11 = "";
			  for(int i = 0; i< ss.length; i++) {
				  if(i>0)
					  sep = m.group(i-1);
				  sub11 += sep + ss[i];
				}
			  sub = "<fra tid='" + t.getUuid()+"'>"+sub11+"</fra>" + sub2;
			  rr = sub + rr;
			  ret.put("totest", totest);
			  ret.put("ret", rr);
	      }
		return ret;
	}

	private String cleanWord(String s) {
		String ret = "";
		for(char c : s.toCharArray()){
			if(Character.isDigit(c) || Character.isAlphabetic(c))
				ret += c;
		}
		return ret;
	}

	public void createSlides(Question question, List<SlideObject> slides) {
		if (slides != null && !slides.isEmpty()) {
			for (SlideObject slideObject : slides) {

				List<TransitionType> transitionTypes = TransitionType
						.tryBuildTransitionTypesFromValues(slideObject.transitionType);
				if (transitionTypes == null)
					continue;

				SlideType type = SlideType.tryBuildSlideTypeFromValue(slideObject.type);
				if (type == null)
					continue;

				Slide slide = new Slide();

				slide.setQuestion(question);
				slide.setupTransitionTypes(transitionTypes);
				slide.setType(type);
				slide.save();
				optionService.createOptions(slide, slideObject.options);

				if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(slide.getTest()))) {
					slideObject.toEdit = true;
				} else {
					User currentUserLogged = userService.getCurrentUserLogged();
					if (currentUserLogged.equals(question.getTest().getAuthor())) {
						slideObject.toEdit = true;
					} else {
						slideObject.toEdit = false;
					}
				}

				/* DISABILITATO PERCHE' DALL'INTERFACCIA UTENTE NON C'E MODO DI
				 CREARE SLIDE CONTENT
				 ALL'ATTO DELLA CREAZIONE DI UNA SLIDE (SE SI RIATTIVA OCCORRE
				 TESTARE E, SOPRATTUTTO,
				 EFFETTUARE LA VALIDAZIONE DI OGNI SOTTOOGGETTO SlideContent,
				 etc.]!)
				 createSlideContent(question.getTest(), slide,
				 slideObject.slideContent);*/
				slide.save();
			}
		}
	}

	public void updateSlides(List<SlideObject> slides) {
		if (slides != null && !slides.isEmpty()) {
			for (SlideObject slideObject : slides) {
				Slide slide = Slide.findById(slideObject.slideId);
				if (slide == null) {
					continue;
				}

				if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(slide.getTest()))) {
					if (slide.getDeleted()) {
						continue;
					}
				}

				if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(slide.getTest()))) {
					User currentUserLogged = userService.getCurrentUserLogged();
					if (!currentUserLogged.equals(slide.getQuestion().getTest().getAuthor())) {
						continue;
					}
				}

				List<TransitionType> transitionTypes = TransitionType
						.tryBuildTransitionTypesFromValues(slideObject.transitionType);
				if (transitionTypes == null)
					continue;

				SlideType type = SlideType.tryBuildSlideTypeFromValue(slideObject.type);
				if (type == null)
					continue;

				optionService.updateOptions(slide, slideObject.options);

				/* DISABILITATO PERCHE' DALL'INTERFACCIA UTENTE NON C'E MODO DI
				 EDITARE SLIDE CONTENT
				 ALL'ATTO DELL'EDIT DI UNA SLIDE (SE SI RIATTIVA OCCORRE
				 TESTARE E, SOPRATTUTTO,
				 EFFETTUARE LA VALIDAZIONE DI OGNI SOTTOOGGETTO SlideContent,
				 etc.]!)
				 updateSlideContent(slide, slideObject.slideContent);*/
				slide.setupTransitionTypes(transitionTypes);
				slide.setType(type);
				slide.save();
			}
		}
	}

	private void updateSlideContent(Slide slide, SlideContentObject slideContent) {
		if (slideContent != null) {
			Set<SlideContentComponent> slideContents = slide.getSlideContent();
			/*if (slideContents != null) {
				for (SlideContentComponent scc : slideContents) {
					try {
						Logger.info("SLIDE SERVICE updateSlideContent deleting " + scc.getUuid());
						scc.delete();
						scc._delete();
						Logger.info("SLIDE SERVICE updateSlideContent deleted" );
					} catch (Exception ex) {
						Logger.error(ex, "Error during SlideContentComponent delete (uuid:%s)", scc.getUuid());
						continue;
					}
				}
			}*/
			/*
			slide.getSlideContent().clear();
			slide.save();
			SlideContentComponent.findAndDeleteBySlide(slide);
			*/
			HashSet<String> alreadyPresent = new HashSet<String>();
			HashSet<SlideContentComponent> toRemove = new HashSet<SlideContentComponent>();
			for(SingleComponentArrayFacade ca : slideContent.componentArray) {
				alreadyPresent.add(ca.id);
			}
			for(SlideContentComponent scc : slide.getSlideContent()) {
				if(!alreadyPresent.contains(scc.getUuid()))
					toRemove.add(scc);
			}
			slide.getSlideContent().removeAll(toRemove);
			slide.save();
			Iterator<SlideContentComponent> it = toRemove.iterator();
			SlideContentComponent scc;
			while(it.hasNext()) {
				scc = it.next();
				if(scc!= null)
					SlideContentComponent.deleteBySlideContentComponentComplete(scc);
				scc = null;
			}
			slideContentComponentsCreation(slide, slideContent.componentArray);
			
			
			//NUOVO METODO... POTREBBE ESSERE PIU' LENTO...
			/*List<SlideContentComponent> toRemove = new ArrayList<SlideContentComponent>();
			boolean toDel = true;
			for(SlideContentComponent scc : slide.getSlideContent()) {
				toDel = true;
				for(SingleComponentArrayFacade sccn : slideContent.componentArray) {
					if(sccn.id != null && sccn.id.equals(scc.getUuid()))
						toDel = false;
				}
				if(toDel) {
					toRemove.add(scc);
				}
			}
			if(!toRemove.isEmpty()) {
				slide.getSlideContent().removeAll(toRemove);
				Iterator<SlideContentComponent> it = toRemove.iterator();
				SlideContentComponent tr = null;
				while(it.hasNext()) {
					tr = it.next();
					if(tr == null)
						continue;
					tr.delete();
				}
			}
			
			slideContentComponentsModificationOnCreation(slide, slideContent.componentArray);*/
		}
	}
	
	private void slideContentComponentsModificationOnCreation(Slide slide, List<SingleComponentArrayFacade> slideComponents) {
		if (slideComponents != null && !slideComponents.isEmpty()) {
			for (SingleComponentArrayFacade slideComponent : slideComponents) {
				if(slideContainsComponentById(slide, slideComponent.id)) {
					//TODO modificare il component esistente...
				} else {
					SlideContentComponent slideContentComponent = new SlideContentComponent();
					ComponentType componentType = ComponentType
							.tryBuildComponentTypeFromValue(slideComponent.componentType);
					if (componentType != null)
						slideContentComponent.setComponentType(componentType);
					slideContentComponent.setTest(slide.getQuestion().getTest());
					slideContentComponent.setDim(slideComponent.dim);
					slideContentComponent.setPos(slideComponent.pos);
					slideContentComponent.setSlide(slide);
					Media media = null;
					if (!StringUtil.isNil(slideComponent.mediaId)) {
						media = Media.findById(slideComponent.mediaId);
						slideContentComponent.setMedia(media);
					}
					slideContentComponent.save();
					slide.addSlideContentComponent(slideContentComponent);
					slide.save();
					optionService.createOptions(slideContentComponent, slideComponent.options);
				}
			}
		}
	}

	private boolean slideContainsComponentById(Slide slide, String id) {
		boolean ret = false;
		for(SlideContentComponent scc : slide.getSlideContent()) {
			if(scc.getUuid().equals(id)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	private void slideContentComponentsCreation(Slide slide, List<SingleComponentArrayFacade> slideComponents) {
		if (slideComponents != null && !slideComponents.isEmpty()) {
			for (SingleComponentArrayFacade slideComponent : slideComponents) {
				SlideContentComponent slideContentComponent = null;
				if(!StringUtil.isNil(slideComponent.id)) {
					slideContentComponent = SlideContentComponent.findById(slideComponent.id);
				}
				if(slideContentComponent == null)
					slideContentComponent = new SlideContentComponent();
				ComponentType componentType = ComponentType
						.tryBuildComponentTypeFromValue(slideComponent.componentType);
				if (componentType != null)
					slideContentComponent.setComponentType(componentType);
				slideContentComponent.setTest(slide.getQuestion().getTest());
				slideContentComponent.setDim(slideComponent.dim);
				slideContentComponent.setPos(slideComponent.pos);
				slideContentComponent.setSlide(slide);
				//if(!StringUtil.isNil(slideComponent.id))
				//	slideContentComponent.setUuid(slideComponent.id);
				Media media = null;
				if (!StringUtil.isNil(slideComponent.mediaId)) {
					media = Media.findById(slideComponent.mediaId);
					slideContentComponent.setMedia(media);
				}
				slideContentComponent.save();
				slide.addSlideContentComponent(slideContentComponent);
				slide.save();
				Option.deleteBySlideContentComponent(slideContentComponent);
				optionService.createOptions(slideContentComponent, slideComponent.options);
			}
		}
	}

	private void createSlideContent(Test test, Slide slide, SlideContentObject slideContent) {
		if (slideContent != null) {
			slideContentComponentsCreation(slide, slideContent.componentArray);
		}
	}

	public List<String> buildSlideIdListFromSlideList(Set<Slide> slides) {
		List<String> ret = new LinkedList<String>();
		if (slides != null && !slides.isEmpty()) {
			for (Slide slide : slides) {
				if(slide == null || slide.getDeleted() == true)
					continue;
				ret.add(slide.getUuid());
			}
		}
		return ret;
	}

	public static class SlideListResponse extends OKResponseObject {
		public List<SlideObject> response;
	}

	public String slideList(String questionId) {
		if (StringUtil.isNil(questionId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Question question = Question.findById(questionId);
		if (question == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(question.getTest()))) {
			if (question.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(question.getTest()))) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(question.getTest().getAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		SlideListResponse res = new SlideListResponse();
		Set<Slide> slidesList = new LinkedHashSet<Slide>();
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(question.getTest()))) {
			slidesList.addAll(question.getSlides());
		} else {
			slidesList.addAll(question.retrieveSlidesByDeleted(false));
		}
		List<SlideObject> slidesObjList = buildSlideObjectListFromSlideList(slidesList, false);
		res.response = slidesObjList;
		String ret = res.toJson();
		return ret;
	}

	public static class GetSlideRequest {
		public String slideId;
	}

	public static class GetSlideResponse extends OKResponseObject {
		public SlideObject response;
	}

	public String getSlide(String slideId) {

		if (StringUtil.isNil(slideId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Slide slide = Slide.findById(slideId);
		if (slide == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(slide.getTest()))) {
			if (slide.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		SlideObject slideObject = buildSlideObjectFromSlide(slide);

		GetSlideResponse res = new GetSlideResponse();
		res.response = slideObject;

		String ret = res.toJson();
		return ret;
	}

	private SlideObject buildSlideObjectFromSlide(Slide slide) {
		return buildSlideObjectFromSlide(slide, false);
	}

	public static class CreateSlideRequest {
		public String questionId;
		public SlideObject slide;
	}

	public static class CreateSlideResponse extends OKResponseObject {
		public SlideObject response;
	}

	public String createSlide() {
		String json = apiService.getCurrentJson();
		CreateSlideRequest req;
		SlideObject slideToCreate;
		try {
			req = (CreateSlideRequest) apiService.buildObjectFromJson(json, CreateSlideRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			slideToCreate = req.slide;
			/* if (slideToCreate == null) {
			 return apiService.buildNotWellFormedJsonErrorResponse();
			 } */
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (StringUtil.isNil(req.questionId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Question question = Question.findById(req.questionId);
		if (question == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}
		
		if(!question.getTest().getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(question.getTest()))) {
			if (question.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(question.getTest()))) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(question.getTest().getAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		Slide slide = new Slide();
		slide.setQuestion(question);
		
		Integer order = 0;
		if(question.getSlidesWithoutReorder() != null)
			order = question.getSlidesWithoutReorder().size();
		slide.setOrder(order);
		slide.save();
		
		if (slideToCreate != null) {
			
			if (slideToCreate.options != null) {
				optionService.createOptions(slide, slideToCreate.options);
			}
			
			if (!StringUtil.isNil(slideToCreate.transitionType)) {
				List<TransitionType> transitionTypes = TransitionType
						.tryBuildTransitionTypesFromValues(slideToCreate.transitionType);
				if (transitionTypes == null)
					return apiService.buildWrongTransitionTypeFieldErrorResponse();
				slide.setupTransitionTypes(transitionTypes);
			} 

			if (!StringUtil.isNil(slideToCreate.type)) {
				SlideType type = SlideType.tryBuildSlideTypeFromValue(slideToCreate.type);
				if (type == null)
					return apiService.buildWrongSlideTypeFieldErrorResponse();
				slide.setType(type);
			}

			if (slideToCreate.slideContent != null) {
				String ret = validateSlideContent(slideToCreate.slideContent);
				if (ret == null) {
					createSlideContent(question.getTest(), slide, slideToCreate.slideContent);
				} else {
					return ret;
				}
			}
		}
		
		if(slide.getTransitionType() == null || StringUtil.isEmpty(slide.getTransitionType())){
				System.out.println(question.getTransitionType());
				List<TransitionType> transitionTypes = question.retrieveTransitionTypes();
				if(transitionTypes!= null && !transitionTypes.isEmpty()){
					for(TransitionType tt : transitionTypes){
						System.out.println(tt.name());
						if(tt.equals(TransitionType.time)){
							for(Option opt : question.getOptions()){
								if(opt.getKey().equals("seconds")){
									System.out.println("seconds " + opt.getValue());
									Option opt1 = new Option();
									opt1.setKey("seconds");
									opt1.setValue(opt.getValue());
									opt1.setSlide(slide);
									opt1.save();
									slide.addOption(opt1);
								}
							}
						}
					}
					slide.setupTransitionTypes(transitionTypes);
				}
		}

		slide.save();
		question.addSlide(slide);
		question.save();

		CreateSlideResponse res = new CreateSlideResponse();
		res.response = buildSlideObjectFromSlide(slide);

		String ret = res.toJson();
		return ret;

	}

	private String validateSlideContent(SlideContentObject slideContent) {
		List<SingleComponentArrayFacade> array = slideContent.componentArray;
		if (array != null && !array.isEmpty()) {
			for (SingleComponentArrayFacade component : array) {
				if ((component == null)
						|| (StringUtil.isNil(component.componentType) && StringUtil.isNil(component.mediaId))
						|| (StringUtil.isNil(component.dim) && StringUtil.isNil(component.pos)))
					return apiService.buildMandatoryParamsErrorResponse();
				if (!StringUtil.isNil(component.mediaId)) {
					Media media = Media.findById(component.mediaId);
					if (media == null) {
						return apiService.buildEntityDoesNotExistsErrorResponse();
					}
				}
				if (!StringUtil.isNil(component.componentType)) {
					/* potrebbero arrivare stati non presenti nella enum */
					ComponentType componentType = ComponentType.tryBuildComponentTypeFromValue(component.componentType);
					if (componentType == null) {
						return apiService.buildWrongComponentTypeFieldErrorResponse();
					}
				}
			}
		}
		return null;
	}

	public String slideUpdate(String slideId) {

		if (StringUtil.isNil(slideId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Slide slide = Slide.findById(slideId);
		if (slide == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(slide.getTest()))) {
			if (slide.getDeleted()) {
				return apiService.buildNotAuthorizedResponse();
			}
		}
		
		if(!slide.getQuestion().getTest().getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}

//		if (!userService.hasCurrentUserLoggedAdminRole()) {
//			User currentUserLogged = userService.getCurrentUserLogged();
//			if (!currentUserLogged.equals(slide.getQuestion().getTest().getAuthor())) {
//				return apiService.buildNotAuthorizedResponse();
//			}
//		}

		String json = apiService.getCurrentJson();
		CreateSlideRequest req;
		SlideObject slideToUpdate;
		try {
			req = (CreateSlideRequest) apiService.buildObjectFromJson(json, CreateSlideRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			slideToUpdate = req.slide;
			if (slideToUpdate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		/* potrebbero arrivare stati non presenti nella enum */
		List<TransitionType> transitionTypes = TransitionType
				.tryBuildTransitionTypesFromValues(slideToUpdate.transitionType);
		if (transitionTypes == null)
			return apiService.buildWrongTransitionTypeFieldErrorResponse();

		/* potrebbero arrivare stati non presenti nella enum */
		SlideType type = SlideType.tryBuildSlideTypeFromValue(slideToUpdate.type);
		if (type == null)
			return apiService.buildWrongSlideTypeFieldErrorResponse();

		if (slideToUpdate.slideContent != null) {
			String ret = validateSlideContent(slideToUpdate.slideContent);
			if (ret == null) {
				updateSlideContent(slide, slideToUpdate.slideContent);
			} else {
				return ret;
			}
		}

		if (slideToUpdate.options != null) {
			optionService.updateOptions(slide, slideToUpdate.options);
		}

		slide.setDeleted(slideToUpdate.deleted);
		slide.setupTransitionTypes(transitionTypes);
		slide.setType(type);
		slide.save();

		CreateSlideResponse res = new CreateSlideResponse();
		res.response = buildSlideObjectFromSlide(slide);

		String ret = res.toJson();
		return ret;

	}

	public static class DeleteSlideResponse extends OKResponseObject {
		public Object response = new Object();
	}

	public String deleteSlide(String slideId) {

		if (StringUtil.isNil(slideId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		Slide slide = Slide.findById(slideId);
		if (slide == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(slide.getTest()))) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(slide.getQuestion().getTest().getAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		if (slide.getDeleted()) {
			return apiService.buildEntityAlreadyDeletedErrorResponse();
		}

		slide.setDeleted(true);
		slide.save();

		DeleteSlideResponse res = new DeleteSlideResponse();

		String ret = res.toJson();
		return ret;

	}

	public static class CloneSlideRequest {
		public String slideId;
		public String questionId;
	}

	public static class CloneSlideResponse extends OKResponseObject {
		public SlideObject response;
	}

	public String cloneSlide() {

		String json = apiService.getCurrentJson();
		CloneSlideRequest req;
		try {
			req = (CloneSlideRequest) apiService.buildObjectFromJson(json, CloneSlideRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (StringUtil.isNil(req.slideId) || StringUtil.isNil(req.questionId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Question question = Question.findById(req.questionId);
		if (question == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}
		
		if(!question.getTest().getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}

		Slide slide = Slide.findById(req.slideId);
		if (slide == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (question.getSlidesWithoutReorder().contains(slide)) {
			//return apiService.buildSlideAlreadyBelongsToQuestionErrorResponse();
		}

		try {
			Slide cloned = slide.clone(question);
			if (cloned == null) {
				return apiService.buildGenericErrorResponse();
			}
			CloneSlideResponse res = new CloneSlideResponse();
			res.response = buildSlideObjectFromSlide(cloned);

			String ret = res.toJson();
			return ret;
		} catch (Exception ex) {
			Logger.error(ex, "Exception during Slide clonation (slide: %s, question: %s)", req.slideId, req.questionId);
			return apiService.buildGenericErrorResponse();
		}
	}

	public Set<Slide> cloneSlidesList(Question question, Set<Slide> slides) {
		Set<Slide> ret = new LinkedHashSet<>();
		if (slides != null && !slides.isEmpty()) {
			Logger.warn("clone slide list for question " + question.getUuid() + " size " + slides.size());
			int i = 0;
			for (Slide slide : slides) {
				if(slide == null)
					continue;
				i++;
				if(slide.getDeleted()) {
					Logger.warn("Cloning question... skipping deleted slide");
					continue;
				}
				try {
					Slide clonedSlide = slide.clone(question);
					if (clonedSlide != null) {
						Logger.warn("Clone slide " + i + " s " + slide.getUuid() + " " + clonedSlide.getUuid());
						ret.add(clonedSlide);
					} else {
						throw new Exception("Clone slide return null");
					}
				} catch (Exception ex) {
					Logger.error(ex, "Exception while cloning slide %s for import into question %s", slide.getUuid(),
							question.getUuid());
					continue;
				}
			}
		}
		return ret;
	}
}