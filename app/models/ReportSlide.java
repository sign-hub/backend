package models;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import models.Question.TransitionType;

@Entity
public class ReportSlide extends ModelUuid {
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private Slide slide;

	@Basic
	private Integer presentationOrder;

	@Basic
	private Date startedAt;

	@Basic
	private Date endedAt;

	@Basic
	// endedAt - startedAt
	private Date effectiveElapsedDate;

	@Basic
	@Enumerated(EnumType.STRING)
	private TransitionType transitionPerformed;

	@Basic
	private String answers;

	@Basic
	private String expectedAnswers;

	// the array of all position of the mouse (granularity 10px?)
	@Basic
	private String mouseTracking;

	public Slide getSlide() {
		return slide;
	}

	public void setSlide(Slide slide) {
		this.slide = slide;
	}

	public Integer getPresentationOrder() {
		return presentationOrder;
	}

	public void setPresentationOrder(Integer presentationOrder) {
		this.presentationOrder = presentationOrder;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(Date endedAt) {
		this.endedAt = endedAt;
	}

	public Date getEffectiveElapsedDate() {
		return effectiveElapsedDate;
	}

	public void setEffectiveElapsedDate(Date effectiveElapsedDate) {
		this.effectiveElapsedDate = effectiveElapsedDate;
	}

	public TransitionType getTransitionPerformed() {
		return transitionPerformed;
	}

	public void setTransitionPerformed(TransitionType transitionPerformed) {
		this.transitionPerformed = transitionPerformed;
	}

	public String getAnswers() {
		return answers;
	}

	public void setAnswers(String answers) {
		this.answers = answers;
	}

	public String getExpectedAnswers() {
		return expectedAnswers;
	}

	public void setExpectedAnswers(String expectedAnswers) {
		this.expectedAnswers = expectedAnswers;
	}

	public String getMouseTracking() {
		return mouseTracking;
	}

	public void setMouseTracking(String mouseTracking) {
		this.mouseTracking = mouseTracking;
	}

}