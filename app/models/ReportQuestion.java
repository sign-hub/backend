package models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class ReportQuestion extends ModelUuid {

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private Question question;

	// the presentation order of the question
	@Basic
	@Column(name = "orderfield")
	private Integer order;

	@Basic
	private Date startedAt;

	@Basic
	private Date endedAt;

	@Basic
	// endedAt - startedAt
	private Date effectiveElapsedDate;

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private List<ReportSlide> slides = new LinkedList<ReportSlide>();

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private Report report;

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
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

	public List<ReportSlide> getSlides() {
		return slides;
	}

	public void setSlides(List<ReportSlide> slides) {
		this.slides = slides;
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	public boolean addReportSlide(ReportSlide reportSlide) {
		if (reportSlide == null)
			return false;
		if (this.slides.contains(reportSlide)) {
			return false;
		}
		return this.slides.add(reportSlide);
	}
}