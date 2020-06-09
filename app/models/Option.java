package models;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

//TODO INIBIRE CREAZIONE CON UNIQUE CON KEY-SLIDE OPPURE KEY-QUESTION ETC...

@Entity
public class Option extends ModelUuid {

	@Basic
	@Column(name = "keyfield")
	private String key;

	@Basic
	@Column(name = "valuefield")
	private String value;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="question_uuid")
	private Question question;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="test_uuid")
	private Test test;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="slide_uuid")
	private Slide slide;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="slideContentComponent_uuid")
	private SlideContentComponent slideContentComponent;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	public Slide getSlide() {
		return slide;
	}

	public void setSlide(Slide slide) {
		this.slide = slide;
	}

	public SlideContentComponent getSlideContentComponent() {
		return slideContentComponent;
	}

	public void setSlideContentComponent(SlideContentComponent slideContentComponent) {
		this.slideContentComponent = slideContentComponent;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public static Option findByTestAndKey(Test test, String key) {
		Option ret = find("test =:test AND key=:key").setParameter("test", test).setParameter("key", key).first();
		return ret;
	}

	public static Option findByQuestionAndKey(Question question, String key) {
		Option ret = find("question =:question AND key=:key").setParameter("question", question)
				.setParameter("key", key).first();
		return ret;
	}

	public static Option findBySlideAndKey(Slide slide, String key) {
		Option ret = find("slide =:slide AND key=:key").setParameter("slide", slide).setParameter("key", key).first();
		return ret;
	}

	public static Option findBySlideContentComponentAndKey(SlideContentComponent slideContentComponent, String key) {
		Option ret = find("slideContentComponent =:slideContentComponent AND key=:key")
				.setParameter("slideContentComponent", slideContentComponent).setParameter("key", key).first();
		return ret;
	}
	
	public static List<Option> findBySlideContentComponent(SlideContentComponent slideContentComponent) {
		List<Option> ret = find("slideContentComponent =:slideContentComponent")
				.setParameter("slideContentComponent", slideContentComponent).fetch();
		return ret;
	}

	public Option clone(Test test, Question question, Slide slide, SlideContentComponent slideContentComponent) {
		Option clonedOption = new Option();
		clonedOption.setKey(this.getKey());
		clonedOption.setValue(this.getValue());
		if (question != null) {
			clonedOption.setQuestion(question);
		} else if (test != null) {
			clonedOption.setTest(test);
		} else if (slide != null) {
			clonedOption.setSlide(slide);
		} else if (slideContentComponent != null) {
			clonedOption.setSlideContentComponent(slideContentComponent);
		} else {
			throw new RuntimeException();
		}
		clonedOption.save();
		return clonedOption;
	}

	public static int deleteBySlideContentComponent(SlideContentComponent slideContentComponent) {
		if (slideContentComponent == null)
			return -1;
		int dels = delete("slideContentComponent =?", slideContentComponent);
		return dels;
	}
}