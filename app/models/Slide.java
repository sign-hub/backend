package models;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import core.services.OptionService;
import models.Question.TransitionType;
import play.Logger;
import utils.StringUtil;

@Entity
public class Slide extends ModelUuid {

	public static enum SlideType {
		BLANK, INFO, STIMULUS, DISTRACTION, QUESTION, ANSWER;

		public static SlideType tryBuildSlideTypeFromValue(String val) {
			if (StringUtil.isNil(val)) {
				return null;
			}

			for (SlideType slideType : values()) {
				if (slideType.name().equals(val)) {
					return slideType;
				}
			}

			return null;
		}
	}

	@Basic
	@Enumerated(EnumType.STRING)
	@Column(name = "typefield")
	private SlideType type;

	@Basic
	private String transitionType;

	@OneToMany(mappedBy="slide")
	private Set<Option> options = new LinkedHashSet<Option>();

	@OneToMany(mappedBy="slide")
	private Set<SlideContentComponent> slideContent = new LinkedHashSet<SlideContentComponent>();

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="question_uuid")
	private Question question;

	@Basic
	private Boolean deleted = false;
	
	@Basic
	private Date creationDate = new Date();
	
	@Basic
	private Integer orderNum;
	
	@Transient
	private Map<String, String> optionsMap;

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

	public SlideType getType() {
		return type;
	}

	public void setType(SlideType type) {
		this.type = type;
	}

	public String getTransitionType() {
		return transitionType;
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

	public Set<SlideContentComponent> getSlideContent() {
		return slideContent;
	}

	public void setSlideContent(Set<SlideContentComponent> slideContent) {
		this.slideContent = slideContent;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public boolean addOption(Option option) {
		if (option == null)
			return false;
		if (this.options.contains(option)) {
			return false;
		}
		return this.options.add(option);
	}

	public boolean addSlideContentComponent(SlideContentComponent slideContent) {
		if (slideContent == null)
			return false;
		if (this.slideContent.contains(slideContent)) {
			return false;
		}
		return this.slideContent.add(slideContent);
	}

	public Slide clone(Question question) throws Exception {
		if (question == null)
			throw new RuntimeException("Question is null!");

		try {
			OptionService optionService = OptionService.instance();
			Slide clonedSlide = new Slide();
			clonedSlide.setQuestion(question);
			clonedSlide.setType(this.getType());
			clonedSlide.setTransitionType(this.getTransitionType());
			clonedSlide.setDeleted(this.getDeleted());
			clonedSlide.save();
			question.addSlide(clonedSlide);
			question.save();

			Set<Option> clonedOptions = optionService.cloneOptionsList(null, null, clonedSlide, null,
					this.getOptions());
			clonedSlide.setOptions(clonedOptions);
			clonedSlide.save();

			Set<SlideContentComponent> clonedSlideContentComponents = new LinkedHashSet<SlideContentComponent>();
			for (SlideContentComponent originalSlideContentComponent : this.getSlideContent()) {
				SlideContentComponent clonedSlideContentComponent = new SlideContentComponent();
				clonedSlideContentComponent.setComponentType(originalSlideContentComponent.getComponentType());
				clonedSlideContentComponent.setDim(originalSlideContentComponent.getDim());
				clonedSlideContentComponent.setMedia(originalSlideContentComponent.getMedia());
				clonedSlideContentComponent.setPos(originalSlideContentComponent.getPos());
				clonedSlideContentComponent.setSlide(clonedSlide);
				clonedSlideContentComponent.setTest(question.getTest());
				clonedSlideContentComponent.save();
				clonedOptions = optionService.cloneOptionsList(null, null, null, clonedSlideContentComponent,
						originalSlideContentComponent.getOptions());
				clonedSlideContentComponent.setOptions(clonedOptions);
				clonedSlideContentComponent.save();
				clonedSlideContentComponents.add(clonedSlideContentComponent);
			}
			clonedSlide.setSlideContent(clonedSlideContentComponents);
			clonedSlide.save();

			return clonedSlide;
		} catch (Exception ex) {
			Logger.error(ex, "Exception during Slide clonation...return null");
			return null;
		}
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

	public Test getTest() {
		return this.getQuestion().getTest();
	}
}