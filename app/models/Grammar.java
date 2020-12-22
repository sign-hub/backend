package models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import utils.StringUtil;

@Entity
public class Grammar extends ModelUuid {

	public static enum GrammarStatus {
		NEW, DRAFT, PUBLISHED, SYSTEMTOC;

		public static GrammarStatus tryBuildGrammarStatusFromName(String name) {
			if (StringUtil.isNil(name)) {
				return null;
			}

			for (GrammarStatus testStatus : values()) {
				if (testStatus.name().equals(name)) {
					return testStatus;
				}
			}

			return null;
		}
	}

	@Basic
	private String grammarName;

	@Basic
	private String grammarSignHubSeries;
	@Basic
	private Integer grammarSignHubSeriesNumber;

	@Lob
	private String grammarOtherSignHubSeries;
	@Lob
	private String grammarEditorialInfo;
	@Lob
	private String grammarCopyrightInfo;
	@Lob
	private String grammarISBNInfo;
	@Lob
	private String grammarBibliographicalReference;
	@Lob
	private String editedBy;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name = "signlanguage_uuid")
	SignLanguage signLanguage;

	@Basic
	String languageVersion;

	@Basic
	@Enumerated(EnumType.STRING)
	private GrammarStatus grammarStatus;

	@Basic
	private Date revisionDate = new Date();

	@Basic
	private Date creationDate = new Date();

	@Transient
	public static final String REVISION_DATE_FORMAT = "dd_MM_yyyy_HH:mm:ss:SSS";

	@Transient
	public static final String CREATION_DATE_FORMAT = "yyyy MM dd HH:mm:ss:SSS";

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private User author;

	@Basic
	private Boolean deleted = false;

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinTable(name = "grammar_editoruser")
	private List<User> editorList;

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinTable(name = "grammar_contentprovideruser")
	private List<User> contentProviderList;

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	private List<GrammarPart> parts = new LinkedList<GrammarPart>();

	@Basic
	private String htmlPath;

	@Basic
	private String pdfPath;

	public String getGrammarName() {
		return grammarName;
	}

	public void setGrammarName(String grammarName) {
		this.grammarName = grammarName;
	}

	public GrammarStatus getGrammarStatus() {
		return grammarStatus;
	}

	public void setGrammarStatus(GrammarStatus grammarStatus) {
		this.grammarStatus = grammarStatus;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

	public void setRevisionDate(Date revisionDate) {
		this.revisionDate = revisionDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public List<User> getEditorList() {
		return editorList;
	}

	public void setEditorList(List<User> editorList) {
		this.editorList = editorList;
	}

	public List<User> getContentProviderList() {
		return contentProviderList;
	}

	public void setContentProviderList(List<User> contentProviderList) {
		this.contentProviderList = contentProviderList;
	}

	public List<GrammarPart> getParts() {
		return parts;
	}

	public void setParts(LinkedList<GrammarPart> parts) {
		this.parts = parts;
	}

	public String getHtmlPath() {
		return htmlPath;
	}

	public void setHtmlPath(String htmlPath) {
		this.htmlPath = htmlPath;
	}

	public String getPdfPath() {
		return pdfPath;
	}

	public void setPdfPath(String pdfPath) {
		this.pdfPath = pdfPath;
	}

	public static List<Grammar> findAllOrdered(String orderby, String ordertype) {
		if (StringUtil.isNil(orderby) || StringUtil.isNil(ordertype))
			return Grammar.find("deleted=:deleted and grammarStatus!=:status").setParameter("deleted", false)
					.setParameter("status", GrammarStatus.SYSTEMTOC).fetch();
		return Grammar.find("deleted=:deleted and grammarStatus!=:status order by " + orderby + " " + ordertype)
				.setParameter("deleted", false).setParameter("status", GrammarStatus.SYSTEMTOC).fetch();

	}

	public static List<Grammar> findAllForUserOrdered(User currentUserLogged, String orderby, String ordertype) {
		if (StringUtil.isNil(orderby) || StringUtil.isNil(ordertype))
			return Grammar.find("deleted=:deleted and grammarStatus!=:status").setParameter("deleted", false)
					.setParameter("status", GrammarStatus.SYSTEMTOC).fetch();
		return Grammar.find("deleted=:deleted and grammarStatus!=:status  order by " + orderby + " " + ordertype)
				.setParameter("deleted", false).setParameter("status", GrammarStatus.SYSTEMTOC).fetch();
	}

	public static List<Grammar> findAllPublished(String orderby, String ordertype) {
		return Grammar.find("grammarStatus=:status  order by " + orderby + " " + ordertype)
				.setParameter("status", GrammarStatus.PUBLISHED).fetch();
	}

	public static List<Grammar> findAllForUserEditor(User currentUserLogged, String orderby, String ordertype) {
		if (true)
			return Grammar.find("deleted=:deleted and grammarStatus!=:status").setParameter("deleted", false)
					.setParameter("status", GrammarStatus.SYSTEMTOC).fetch();
		String q = "(author=:user or :user MEMBER OF editorList) order by " + orderby + " " + ordertype;

		return Grammar.find(q).setParameter("user", currentUserLogged).fetch();
	}

	public boolean isPublished() {
		if (this.grammarStatus.equals(GrammarStatus.PUBLISHED))
			return true;
		return false;
	}

	public String getGrammarSignHubSeries() {
		return grammarSignHubSeries;
	}

	public void setGrammarSignHubSeries(String grammarSignHubSeries) {
		this.grammarSignHubSeries = grammarSignHubSeries;
	}

	public Integer getGrammarSignHubSeriesNumber() {
		return grammarSignHubSeriesNumber;
	}

	public void setGrammarSignHubSeriesNumber(Integer grammarSignHubSeriesNumber) {
		this.grammarSignHubSeriesNumber = grammarSignHubSeriesNumber;
	}

	public String getGrammarOtherSignHubSeries() {
		return grammarOtherSignHubSeries;
	}

	public void setGrammarOtherSignHubSeries(String grammarOtherSignHubSeries) {
		this.grammarOtherSignHubSeries = grammarOtherSignHubSeries;
	}

	public String getGrammarEditorialInfo() {
		return grammarEditorialInfo;
	}

	public void setGrammarEditorialInfo(String grammarEditorialInfo) {
		this.grammarEditorialInfo = grammarEditorialInfo;
	}

	public String getGrammarCopyrightInfo() {
		return grammarCopyrightInfo;
	}

	public void setGrammarCopyrightInfo(String grammarCopyrightInfo) {
		this.grammarCopyrightInfo = grammarCopyrightInfo;
	}

	public String getGrammarISBNInfo() {
		return grammarISBNInfo;
	}

	public void setGrammarISBNInfo(String grammarISBNInfo) {
		this.grammarISBNInfo = grammarISBNInfo;
	}

	public String getGrammarBibliographicalReference() {
		return grammarBibliographicalReference;
	}

	public void setGrammarBibliographicalReference(String grammarBibliographicalReference) {
		this.grammarBibliographicalReference = grammarBibliographicalReference;
	}

	public String getEditedBy() {
		return editedBy;
	}

	public void setEditedBy(String editedBy) {
		this.editedBy = editedBy;
	}

	public SignLanguage getSignLanguage() {
		return signLanguage;
	}

	public void setSignLanguage(SignLanguage signLanguage) {
		this.signLanguage = signLanguage;
	}

	public String getLanguageVersion() {
		return languageVersion;
	}

	public void setLanguageVersion(String languageVersion) {
		this.languageVersion = languageVersion;
	}

	public static List<Grammar> findBySignLanguage(SignLanguage sl) {
		return find("signLanguage=:sl").setParameter("sl", sl).fetch();
	}

	public static Grammar findBySignLanguageAndVersion(SignLanguage sl, String version) {
		return find("signLanguage=:sl and languageVersion=:languageVersion").setParameter("sl", sl)
				.setParameter("languageVersion", version).first();
	}

	public static Grammar findSystemToc() {
		return Grammar.find("grammarStatus=:grammarStatus").setParameter("grammarStatus", GrammarStatus.SYSTEMTOC)
				.first();
	}

}
