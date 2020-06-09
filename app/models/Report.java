package models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.Test.TestType;

@Entity
public class Report extends ModelUuid {

	@Basic
	// the date of Test submission
	private Date reportDate;

	// the path to download csv report
	@Basic
	private String reportCsvPath;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private Test reportTest;

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private List<Metadata> metadata = new LinkedList<Metadata>();

	// array of all question report for the test
	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private List<ReportQuestion> questions = new LinkedList<ReportQuestion>();

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private User author;

	@Basic
	private Date startedAt;

	@Basic
	private Date endedAt;

	@Basic
	// endedAt - startedAt
	private Date effectiveElapsedDate;
	
	@Lob
	private String jsonContent;
	
	@Basic
	private String jsonFilePath;
	
	@Basic
	private Boolean isComplessive = false;
	
	@Basic
	private String languageName;
	
	@Basic
	private String standaloneId;
	
	@Basic
	private String workerId;


	public Boolean getIsComplessive() {
		return isComplessive;
	}

	public void setIsComplessive(Boolean isComplessive) {
		this.isComplessive = isComplessive;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public String getReportCsvPath() {
		return reportCsvPath;
	}

	public void setReportCsvPath(String reportCsvPath) {
		this.reportCsvPath = reportCsvPath;
	}

	public Test getReportTest() {
		return reportTest;
	}

	public void setReportTest(Test reportTest) {
		this.reportTest = reportTest;
	}

	public List<Metadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadata> metadata) {
		this.metadata = metadata;
	}

	public List<ReportQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<ReportQuestion> questions) {
		this.questions = questions;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
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

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public boolean addMetadata(Metadata metadata) {
		if (metadata == null)
			return false;
		if (this.metadata.contains(metadata)) {
			return false;
		}
		return this.metadata.add(metadata);
	}

	public boolean addSlide(ReportQuestion reportQuestion) {
		if (reportQuestion == null)
			return false;
		if (this.questions.contains(reportQuestion)) {
			return false;
		}
		return this.questions.add(reportQuestion);
	}

	public static List<Report> findAllByType(String type) {
		if(type==null)
			return findAll();
		TestType tt = Test.TestType.tryBuildTestTypeFromName(type);
		return find("reportTest.testType=? and reportCsvPath is not null and isComplessive = false and reportCsvPath!='' order by reportTest.testName ASC, reportDate DESC", tt).fetch();
		//return null;
	}

	public static List<Report> findAllByTypeAndContentProvide(String type, User user) {
		if(type==null)
			return null;
		TestType tt = Test.TestType.tryBuildTestTypeFromName(type);
		return find("reportTest.testType=? and isComplessive = false and reportCsvPath is not null and reportCsvPath!='' and author=? order by reportTest.testName ASC, reportDate DESC", tt, user).fetch();
		
	}
	
	public static List<Report> findAllByTypeAndContentProviderList(String type, User user) {
		if(type==null)
			return null;
		TestType tt = Test.TestType.tryBuildTestTypeFromName(type);
		return find("reportTest.testType=:testtype and reportCsvPath is not null and reportCsvPath!='' and reportTest.contentProviderList IS NOT EMPTY and :user IN reportTest.contentProviderList order by reportTest.testName ASC, reportDate DESC")
				.setParameter("testtype", tt).setParameter("user", user).fetch();
		
	}

	public static List<Report> findAllByTest(Test t) {
		if(t == null)
			return null;
		return find("reportTest=:test and isComplessive = false").setParameter("test", t).fetch();
	}

	public String getJsonContent() {
		return jsonContent;
	}

	public void setJsonContent(String jsonContent) {
		this.jsonContent = jsonContent;
	}

	public String getJsonFilePath() {
		return jsonFilePath;
	}

	public void setJsonFilePath(String jsonFilePath) {
		this.jsonFilePath = jsonFilePath;
	}

	public String getLanguageName() {
		return languageName;
	}

	public void setLanguageName(String languageName) {
		this.languageName = languageName;
	}

	public String getStandaloneId() {
		return standaloneId;
	}

	public void setStandaloneId(String standaloneId) {
		this.standaloneId = standaloneId;
	}

	public static Report findByTestAndAuthor(User u, Test t) {
		Report ret = null;
		ret = find("reportTest=:test and author=:auth order by reportDate DESC").setParameter("test", t).setParameter("auth", u).first();
		return ret;
	}
	
	public static List<Report> findListByTestAndAuthor(User u, Test t) {
		return find("reportTest=:test and author=:auth order by reportDate DESC").setParameter("test", t).setParameter("auth", u).fetch();
	}

	public static Report findByWorker(String workerId) {
		Report ret = null;
		ret = find("workerId=:workerId").setParameter("workerId", workerId).first();
		return ret;
	}

	public static Report findGlobalByTest(Test t) {
		Report ret = null;
		ret = find("reportTest=:test and isComplessive =:isComplessive order by reportDate DESC").setParameter("test", t).setParameter("isComplessive", true).first();
		return ret;
	}
	
	public static List<Report> findGlobalListByTest(Test t) {
		return find("reportTest=:test and isComplessive =:isComplessive order by reportDate DESC").setParameter("test", t).setParameter("isComplessive", true).fetch();
	}

	public static Report findByStandalone(String standaloneId) {
		Report ret = null;
		ret = find("standaloneId=:standaloneId").setParameter("standaloneId", standaloneId).first();
		return ret;
	}


}