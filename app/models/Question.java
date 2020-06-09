package models;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import core.services.OptionService;
import core.services.SlideService;
import play.Logger;

@Entity
public class Question extends ModelUuid {

	public static enum TransitionType {
		time, click, enter, action, answer;

		public static final String TRANSITION_TYPES_SEPARATOR = " ";

		public static List<TransitionType> tryBuildTransitionTypesFromValues(List<String> vals) {
			if (vals == null || vals.isEmpty()) {
				return null;
			}

			List<TransitionType> ret = new LinkedList<>();

			TransitionType[] allEnumVals = values();
			for (String val : vals) {
				boolean ok = false;
				for (TransitionType transitionType : allEnumVals) {
					if (transitionType.name().equals(val)) {
						ok = true;
						ret.add(transitionType);
						break;
					}
				}
				if (!ok) {
					return null;
				}
			}
			return ret;
		}

		public static List<TransitionType> buildTransitionTypesListFromString(String transitionTypes) {

			List<String> tempsToList = buildTransitionTypesStringListFromString(transitionTypes);

			List<TransitionType> ret = TransitionType.tryBuildTransitionTypesFromValues(tempsToList);

			return ret;

		}

		public static String buildTransitionTypesStringFromList(List<TransitionType> vals) {
			if (vals == null) {
				return null;
			}

			String ret = "";
			String sep = "";
			for (TransitionType transitionType : vals) {
				ret += sep + transitionType.name();
				sep = TransitionType.TRANSITION_TYPES_SEPARATOR;
			}
			return ret;
		}

		public static List<String> buildTransitionTypesStringListFromString(String transitionTypes) {
			List<String> ret = new LinkedList<String>();

			if (transitionTypes == null || transitionTypes.isEmpty()) {
				return ret;
			}

			String[] temps = transitionTypes.split(TransitionType.TRANSITION_TYPES_SEPARATOR);
			if (temps == null || temps.length <= 0) {
				return ret;
			}

			for (String temp : temps) {
				ret.add(temp);
			}

			return ret;
		}

	}

	/* @Basic
	 @Enumerated(EnumType.STRING)
	 private TransitionType transitionType;*/

	@Basic
	private String transitionType;

	@OneToMany(mappedBy="question")
	private Set<Option> options = new LinkedHashSet<Option>();

	@OneToMany(mappedBy="question")
	private Set<Slide> slides = new LinkedHashSet<Slide>();

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="test_uuid")
	private Test test;

	@Basic
	private Boolean deleted = false;

	@Basic
	private String name;
	
	@Basic
	private Date creationDate = new Date();
	
	@Basic
	private Integer orderNum = 0;

	@Transient
	private Map<String, String> optionsMap;

	public String getTransitionType() {
		return transitionType;
	}

	public List<TransitionType> retrieveTransitionTypes() {
		String transitionTypes = getTransitionType();
		List<TransitionType> ret = TransitionType.buildTransitionTypesListFromString(transitionTypes);
		return ret;
	}

	public void setupTransitionTypes(List<TransitionType> transitionTypesList) {
		String transitionTypes = TransitionType.buildTransitionTypesStringFromList(transitionTypesList);
		setTransitionType(transitionTypes);
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Integer getOrder() {
		return orderNum;
	}

	public void setOrder(Integer order) {
		this.orderNum = order;
	}

	public void setTransitionType(String transitionType) {
		this.transitionType = transitionType;
	}

	public Set<Option> getOptions() {
		return options;
	}

	public void setOptions(Set<Option> options) {
		this.options = options;
	}

	public Set<Slide> getSlides() {
		return orderSlide(getNotDeleted());
	}
	
	private Set<Slide> getNotDeleted() {
		Set<Slide> ret = new HashSet<Slide>();
		for(Slide s : slides) {
			if(s!= null && s.getDeleted() == false)
				ret.add(s);
		}
		return ret;
	}

	private Set<Slide> orderSlide(Set<Slide> slides) {
		Slide[] tmp= new Slide[slides.size()];
		LinkedList<Slide> ret = new LinkedList<Slide>();
		ret.addAll(slides);
		Collections.sort(ret, new Comparator<Slide>() {

			@Override
			public int compare(Slide o1, Slide o2) {
				if(o1 == null && o2 == null)
					return 0;
				if(o1 == null ||o1.getOrder() == null)
					return -1;
				if( o2 == null || o2.getOrder() == null)
					return 1;
				return o1.getOrder().compareTo(o2.getOrder());
			}
			
		});
		int lastOrder = -1;
		boolean needRecalc = false;
		for(int i = 0; i < slides.size(); i++) {
			Slide s = ret.get(i);
			if(s.getOrder()==null) {
				s.setOrder(i);
				s.save();
			}
			if(s.getOrder() == lastOrder) {
				needRecalc = true;
			}
			lastOrder = s.getOrder();
		}
		if(needRecalc) {
			return reorderSlidesComparator();
		}
		slides = new LinkedHashSet<Slide>();
		slides.addAll(ret);
		return slides;
	}
	
	private Set<Slide> reorderSlidesComparator(){
		LinkedList<Slide> ordered = new LinkedList<Slide>();
		for(Slide q: slides) {
			if(q == null || q.getDeleted())
				continue;
			ordered.add(q);
		}
		
		Collections.sort(ordered, new Comparator<Slide>(){

			@Override
			public int compare(Slide o1, Slide o2) {
				if(o1==null && o2 == null)
					return -1;
				if(o2==null)
					return 1;
				if(o1.getOrder()==null)
					o1.setOrder(0);
				if(o2.getOrder()==null)
					o2.setOrder(0);
				if(o1.getOrder().compareTo(o2.getOrder())!=0)
					return o1.getOrder().compareTo(o2.getOrder());
				if(o1.getCreationDate()!=null && o2.getCreationDate()!=null &&
						o1.getCreationDate().compareTo(o2.getCreationDate()) != 0)
					return o1.getCreationDate().compareTo(o2.getCreationDate());
				return o1.getUuid().compareTo(o2.getUuid());
			}
			
		});
		
		for(int i = 0; i < ordered.size(); i++){
			Slide q = ordered.get(i);
			q.setOrder(i);
			q.save();	
		}
		Set<Slide> ret = new LinkedHashSet<Slide>();
		ret.addAll(ordered);
		return ret;
	}

	public List<Slide> retrieveSlidesByDeleted(Boolean deleted) {
		List<Slide> ret = new LinkedList<Slide>();
		if (deleted == null) {
			ret.addAll(getSlides());
		} else {
			for (Slide slide : getSlides()) {
				if (slide.getDeleted() == deleted) {
					ret.add(slide);
				}
			}
		}
		return ret;
	}

	public void setSlides(Set<Slide> slides) {
		this.slides = slides;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean addOption(Option option) {
		if (option == null)
			return false;
		if (this.options.contains(option)) {
			return false;
		}
		return this.options.add(option);
	}

	public boolean addSlide(Slide slide) {
		if (slide == null)
			return false;
		if (this.slides.contains(slide)) {
			return false;
		}
		slide.setOrder(this.slides.size());
		slide.save();
		return this.slides.add(slide);
	}

	/*flag dontAddQuestionToTestQuestionList serves because, without, in Test clonation
	//is thrown a ConcurrentModification Exception*/
	public Question clone(Test test, boolean addQuestionToTestQuestionList) {
		if (test == null)
			throw new RuntimeException("Test is null!");

		try {
			OptionService optionService = OptionService.instance();
			SlideService slideService = SlideService.instance();
			Question clonedQuestion = new Question();
			clonedQuestion.setDeleted(this.getDeleted());
			clonedQuestion.setName(this.getName());
			clonedQuestion.setTest(test);
			clonedQuestion.setOrder(this.getOrder());
			clonedQuestion.setTransitionType(this.getTransitionType());
			clonedQuestion.save();
			
			if(addQuestionToTestQuestionList){
				test.addQuestion(clonedQuestion);
				test.save();
			}
			
			Set<Option> clonedOptions = optionService.cloneOptionsList(null, clonedQuestion, null, null,
					this.getOptions());
			clonedQuestion.setOptions(clonedOptions);
			Logger.warn("Test clone; cq " + clonedQuestion.getUuid());
			Set<Slide> clonedSlides = slideService.cloneSlidesList(clonedQuestion, slides);
			clonedQuestion.setSlides(clonedSlides);

			clonedQuestion.save();
			return clonedQuestion;
		} catch (Exception ex) {
			Logger.error(ex, "Exception during Question clonation...return null");
			return null;
		}
	}
	
	public Map<String, String> getMapOptions() {
		if(this.optionsMap != null)
			return this.optionsMap;
		Map<String, String> opts = new HashMap<String, String>();
		for(Option o : options) {
			opts.put(o.getKey(), o.getValue());
		}
		this.optionsMap = opts;
		return opts;
	}
	
	public String getFromOption(String key) {
		if(this.optionsMap == null)
			this.optionsMap = this.getMapOptions();
		return this.optionsMap.get(key);
	}

	public Set<Slide> getSlidesWithoutReorder() {
		return this.slides;
	}
}