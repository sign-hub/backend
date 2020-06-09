package models;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import utils.StringUtil;

@Entity
public class SlideContentComponent extends ModelUuid {
	public static enum ComponentType {
		BUTTON, CLICK_AREA, IMAGE, VIDEO, TEXT, RANGE, UPLOADS, CHECKABLETABLE,
		TEXTAREA, RADIO, CHECKBOX, TEXTBLOCK, AUDIO, TXTFILE, CUSTOM_CLICK_AREA, VIDEO_RECORD;

		public static ComponentType tryBuildComponentTypeFromValue(String val) {
			if (StringUtil.isNil(val)) {
				return null;
			}

			for (ComponentType componentType : values()) {
				if (componentType.name().equals(val)) {
					return componentType;
				}
			}

			return null;
		}
	}

	@Basic
	@Enumerated(EnumType.STRING)
	private ComponentType componentType;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="media_uuid")
	private Media media;

	@Basic
	private String pos;

	@Basic
	private String dim;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="slide_uuid")
	private Slide slide;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="test_uuid")
	private Test test;

	@OneToMany(mappedBy="slideContentComponent")
	private Set<Option> options = new LinkedHashSet<Option>();
	
	@Transient
	private Map<String, String> optionsMap;

	public ComponentType getComponentType() {
		return componentType;
	}

	public void setComponentType(ComponentType componentType) {
		this.componentType = componentType;
	}

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getDim() {
		return dim;
	}

	public void setDim(String dim) {
		this.dim = dim;
	}

	public Set<Option> getOptions() {
		return options;
	}

	public void setOptions(Set<Option> options) {
		this.options = options;
	}

	public Slide getSlide() {
		return slide;
	}

	public void setSlide(Slide slide) {
		this.slide = slide;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	public boolean addOption(Option option) {
		if (option == null)
			return false;
		if (this.options.contains(option)) {
			return false;
		}
		return this.options.add(option);
	}

	public static int deleteBySlide(Slide slide) {
		int dels = delete("slide =?", slide);
		return dels;
	}
	
	public static int findAndDeleteBySlide(Slide slide) {
		List<SlideContentComponent> ret = find("slide =:slide").setParameter("slide", slide).fetch();
		for(SlideContentComponent ssc : ret) {
			deleteBySlideContentComponent(ssc);
		}
		return deleteBySlide(slide);
	}
	
	public static void deleteBySlideContentComponentComplete(SlideContentComponent ssc) {
		if(ssc == null)
			return;
		deleteBySlideContentComponent(ssc);
		delete("uuid=?", ssc.getUuid());
	}

	public static void deleteBySlideContentComponent(SlideContentComponent ssc) {
		if(ssc.getOptions()!=null) {
			ssc.getOptions().clear();
			ssc.save();
		}
		Option.deleteBySlideContentComponent(ssc);
	}
	
	public static List<SlideContentComponent> findBySlide(Slide slide) {
		List<SlideContentComponent> ret = find("slide =:slide").setParameter("slide", slide).fetch();
		return ret;
	}

	public static SlideContentComponent findBySlideAndMedia(Slide slide, Media media) {
		SlideContentComponent ret = find("slide =:slide AND media=:media").setParameter("slide", slide)
				.setParameter("media", media).first();
		return ret;
	}

	public static SlideContentComponent findBySlideAndMediaId(Slide slide, String mediaId) {
		SlideContentComponent ret = find("slide =:slide AND media.uuid=:mediaId").setParameter("slide", slide)
				.setParameter("mediaId", mediaId).first();
		return ret;
	}

	public static List<SlideContentComponent> findByTest(Test test) {
		/*List<SlideContentComponent> ret = find("test =:test").setParameter("test", test).fetch();*/
		List<SlideContentComponent> ret = new LinkedList<SlideContentComponent>();
		for(Question q : test.getQuestions()) {
			if(q==null)
				continue;
			for(Slide s : q.getSlides()) {
				if(s == null)
					continue;
				ret.addAll(s.getSlideContent());
			}
		}
		return ret;
	}

	public static List<SlideContentComponent> findByTestId(String testId) {
		/*List<SlideContentComponent> ret = find("test.uuid =:testId").setParameter("testId", testId).fetch();*/
		Test t = Test.findById(testId);
		return findByTest(t);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
		result = prime * result + ((dim == null) ? 0 : dim.hashCode());
		result = prime * result + ((media == null) ? 0 : media.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + ((slide == null) ? 0 : slide.hashCode());
		result = prime * result + ((test == null) ? 0 : test.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof SlideContentComponent))
			return false;
		SlideContentComponent other = (SlideContentComponent) obj;
		if (componentType != other.componentType)
			return false;
		if (dim == null) {
			if (other.dim != null)
				return false;
		} else if (!dim.equals(other.dim))
			return false;
		if (media == null) {
			if (other.media != null)
				return false;
		} else if (!media.equals(other.media))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		if (slide == null) {
			if (other.slide != null)
				return false;
		} else if (!slide.equals(other.slide))
			return false;
		if (test == null) {
			if (other.test != null)
				return false;
		} else if (!test.equals(other.test))
			return false;
		return true;
	}

	public String getName(String forceField) {
		
		Boolean forceName = false;
		Boolean forceGroup = false;
		if(!StringUtil.isNil(forceField)) {
			if(forceField.equals("name"))
				forceName = true;
			if(forceField.equals("group"))
				forceGroup = true;
		}
		String name = null;
		String groupName = null;
		for(Option o : this.options) {
			if(o.getKey().equals("name")) {
				name =  o.getValue();
				if(forceName)
					return name;
			}
			if(o.getKey().equals("groupName"))
				groupName =  o.getValue();
			if(forceGroup)
				return groupName;
		}
		if(!StringUtil.isNil(name))
			return name;
		if(!StringUtil.isNil(groupName))
			return groupName;
		return this.getUuid();
	}

	public String getLabel() {
		for(Option o : this.options) {
			if(o.getKey().equals("label"))
				return o.getValue();
		}
		return this.getUuid();
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
	
	
}