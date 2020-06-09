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

import models.Grammar.GrammarStatus;
import play.Logger;
import utils.StringUtil;

@Entity
public class GrammarPart extends ModelUuid {

	public static enum GrammarPartStatus {
		NEW, DRAFT, PUBLISHED;

		public static GrammarPartStatus tryBuildGrammarPartStatusFromName(String name) {
			if (StringUtil.isNil(name)) {
				return null;
			}

			for (GrammarPartStatus testStatus : values()) {
				if (testStatus.name().equals(name)) {
					return testStatus;
				}
			}

			return null;
		}
	}
	
	public static enum GrammarPartType {
		PART, CHAPTER, PARAGRAPH, ARTICLE;

		public static GrammarPartType tryBuildGrammarPartTypeFromName(String name) {
			if (StringUtil.isNil(name)) {
				return null;
			}

			for (GrammarPartType testStatus : values()) {
				if (testStatus.name().equals(name)) {
					return testStatus;
				}
			}

			return null;
		}
	}
	
	@Basic
	private String grammarPartName;
	
	@Basic
	@Enumerated(EnumType.STRING)
	private GrammarPartStatus grammarPartStatus;
	
	@Basic
	@Enumerated(EnumType.STRING)
	private GrammarPartType grammarPartTYpe;
	
	@Basic
	private Integer grammarPartOrder;
	
	public Integer getGrammarPartOrder() {
		return grammarPartOrder;
	}

	public void setGrammarPartOrder(Integer grammarPartOrder) {
		this.grammarPartOrder = grammarPartOrder;
	}

	public void setParts(List<GrammarPart> parts) {
		this.parts = parts;
	}

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
	
	//@OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@OneToMany(mappedBy="parent")
	private List<GrammarPart> parts = new LinkedList<GrammarPart>();
	
	@OneToMany(mappedBy="grammarPart")
	private List<Feature> features = new LinkedList<Feature>();
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="parent_uuid")
	private GrammarPart parent;
	
	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinTable(name="grammarpart_editoruser")
	private List<User> editorList;
	
	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinTable(name="grammarpart_contentprovideruser")
	private List<User> contentProviderList;
	
	@Lob
	private String html;
	
	@Basic
	private String elementNumber;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="grammar_uuid")
	private Grammar grammar;

	@Basic
	private Float completeOrder;
	
	@Basic
	private Float completeOrderNow;

	public String getGrammarPartName() {
		return grammarPartName;
	}

	public void setGrammarPartName(String grammarPartName) {
		this.grammarPartName = grammarPartName;
	}

	public GrammarPartStatus getGrammarPartStatus() {
		return grammarPartStatus;
	}

	public void setGrammarPartStatus(GrammarPartStatus grammarPartStatus) {
		this.grammarPartStatus = grammarPartStatus;
	}

	public GrammarPartType getGrammarPartTYpe() {
		return grammarPartTYpe;
	}

	public void setGrammarPartTYpe(GrammarPartType grammarPartTYpe) {
		this.grammarPartTYpe = grammarPartTYpe;
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

	public List<GrammarPart> getParts() {
		return parts;
	}

	public void setParts(LinkedList<GrammarPart> parts) {
		this.parts = parts;
	}

	public GrammarPart getParent() {
		return parent;
	}

	public void setParent(GrammarPart parent) {
		this.parent = parent;
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

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getElementNumber() {
		return elementNumber;
	}

	public void setElementNumber(String elementNumber) {
		this.elementNumber = elementNumber;
	}

	public static List<GrammarPart> search(Grammar g, String q) {
		return find("deleted=false and grammar=:grammar and html like :q").setParameter("grammar", g).setParameter("q", "%"+q+"%").fetch();
	}

	public void setGrammar(Grammar g) {
		this.grammar = g;
	}

	public Grammar getGrammar() {
		return this.grammar;
	}

	public static GrammarPart findPartsByName(String name, Grammar g) {
		return find("grammarPartName=:name and grammar=:g").setParameter("name", name).setParameter("g", g).first();
	}

	public Float getCompleteOrder() {
		if(this.completeOrder != null)
			return this.completeOrder;
		calculateCompleteOrder();
		return this.completeOrder;
	}
	
	public void calculateCompleteOrder() {
		String co = this.getCompleteOrderString();
		co = "0." + co;
		this.completeOrder = Float.parseFloat(co);
		this.save();
	}
	
	public Float getCompleteOrderNow() {
		if(this.completeOrderNow != null)
			return this.completeOrderNow;
		calculateCompleteOrderNow();
		return this.completeOrderNow;
	}

	public void calculateCompleteOrderNow() {
		try {
		String co = this.getCompleteOrderNowString();
		co = "0." + co;
		this.completeOrderNow = Float.parseFloat(co);
		Logger.warn("calculate complete order: " + getGrammarPartName() + " " + co);
		this.save();
		} catch(Exception w) {
			Logger.warn(getGrammarPartName() + " " + getUuid());
		}
	}
	
	public String getCompleteOrderString() {
		String p = "";
		if(this.getParent() != null) {
			Logger.warn("parent.uuid " + this.getParent().getUuid());
			p = this.getParent().getCompleteOrderString();
		}
		p = p + this.getGrammarPartOrder();
		return p;
	}
	
	public String getCompleteOrderString2() {
		String p = "";
		String sep = "";
		if(this.getParent() != null) {
			p = this.getParent().getCompleteOrderString2();
			sep = "-";
		}
		p = p + sep + this.getGrammarPartOrder();
		return p;
	}
	
	public String getCompleteOrderNowString() {
		if(thisIsPart()) {
			String[] aplitted = this.getGrammarPartName().split(" ");
			Integer p = Integer.parseInt(aplitted[1]);
			p += 4;
			return ""+p;
		} else if(thisIsChapter()) {
			String[] aplitted = this.getGrammarPartName().split(" ");
			return this.getParent().getCompleteOrderNowString() + aplitted[1].replaceAll("\\.", "");
		} else if(thisIsParagraph()) {
			String[] aplitted = this.getGrammarPartName().split(" ");
			GrammarPart part = this.getPart();
			return part.getCompleteOrderNowString() + aplitted[0].replaceAll("\\.", "");
		} else {
			String ret = "";
			if(this.getParent()!= null) {
				ret = this.getParent().getCompleteOrderNowString();
				int listSize = getParent().getParts().size();
				int order = -1;
				if(grammarPartName.equals("Information on data and consultants")) {
					order = listSize - 2;
				} else if(grammarPartName.equals("References")) {
					order = listSize - 1;
				} else if(grammarPartName.equals("Authorship information")) {
					order = listSize;
				}
				this.setGrammarPartOrder(order);
				this.setElementNumber(""+order);
				this.save();
			}
			return ret + this.getGrammarPartOrder();
		}
	}
	
	public String getCompleteOrderNowString2() {
		if(thisIsPart()) {
			String[] aplitted = this.getGrammarPartName().split(" ");
			Integer p = Integer.parseInt(aplitted[1]);
			p += 4;
			return ""+p;
		} else if(thisIsChapter()) {
			String[] aplitted = this.getGrammarPartName().split(" ");
			String ret = this.getParent().getCompleteOrderNowString2() + "-" + aplitted[1].replaceAll("\\.", "-");
			if(ret.endsWith("-"))
				ret = ret.substring(0, ret.length()-1);
			return ret;
		} else if(thisIsParagraph()) {
			String[] aplitted = this.getGrammarPartName().split(" ");
			GrammarPart part = this.getPart();
			String ret = part.getCompleteOrderNowString2() + "-" + aplitted[0].replaceAll("\\.", "-");
			if(ret.endsWith("-"))
				ret = ret.substring(0, ret.length()-1);
			return ret;
		} else {
			return this.getParent().getCompleteOrderNowString2() + "-" + this.getGrammarPartOrder();
		}
	}

	public boolean thisIsParagraph() {
		return Character.isDigit(this.getGrammarPartName().charAt(0));
	}

	public boolean thisIsChapter() {
		return this.getGrammarPartName().startsWith("Chapter");
	}

	public boolean thisIsPart() {
		return this.getGrammarPartName().startsWith("PART");
	}
	
	public boolean thisIsArticle() {
		return !thisIsPart() && !thisIsChapter() && !thisIsParagraph();
	}

	private GrammarPart getPart() {
		if(this.getParent()!=null)
			return getParent().getPart();
		return this;
	}

	public static String getChapterNameByBlueprintSectionOld(String bluePrintSection) {
		String[] parts = bluePrintSection.split("\\.");
		Logger.warn("parts " + parts.length);
		if(parts.length > 1 && parts[0].length() == 2) {
			char part = parts[0].charAt(0);
			char chapter = parts[0].charAt(1);
			Logger.warn("part: " + part + " chapter " + chapter);
			GrammarPart p = find("elementNumber=:elNum and parent is null")
					.setParameter("elNum", ""+convertPartChar(part)).first();
			if(p == null) {
				Logger.warn("part is null");
				return "chapter TODO";
			}
			Logger.warn("part " + p.getUuid());
			GrammarPart gp = find("elementNumber=:elNum and parent=:p")
					.setParameter("elNum", ""+chapter).setParameter("p", p).first();

			if(gp!=null) {
				Logger.warn("chapter " + gp.getUuid());
				return gp.grammarPartName;
			} else {
				Logger.warn("chapter null");
			}
		}
		return "chapter TODO";
	}
	
	public static String getChapterNameByBlueprintSection(String bluePrintSection) {
		GrammarPart gp = getChapterByBlueprintSection(bluePrintSection);
		if(gp==null)
			return "chapter TODO";
		return gp.grammarPartName;
	}
	
	public static GrammarPart getChapterByBlueprintSection(String bluePrintSection) {
		String[] parts = bluePrintSection.split("\\.");
		Logger.warn("parts " + parts.length);
		Grammar g = Grammar.findSystemToc();
		if(parts.length > 1 && parts[0].length() == 2) {
			char part = parts[0].charAt(0);
			char chapter = parts[0].charAt(1);
			Logger.warn("part: " + part + " chapter " + chapter);
			GrammarPart p = find("elementNumber=:elNum and parent is null and grammar=:grammar")
					.setParameter("elNum", ""+convertPartChar(part))
					.setParameter("grammar", g).first();
			if(p == null) {
				Logger.warn("part is null");
				return null;
			}
			Logger.warn("part " + p.getUuid());
			GrammarPart gp = find("elementNumber=:elNum and parent=:p and grammar=:grammar")
					.setParameter("elNum", ""+chapter).setParameter("p", p)
					.setParameter("grammar", g).first();

			if(gp!=null) {
				Logger.warn("chapter " + gp.getUuid());
				return gp;
			} else {
				Logger.warn("chapter null");
			}
		}
		return null;
	}
	
	public static GrammarPart getPartByBlueprintSection(String bluePrintSection) {
		//bluePrintSection=B1.5.2
		String[] parts = bluePrintSection.split("\\.");
		// B1 5 2
		Grammar g = Grammar.findSystemToc();
		Logger.warn("parts " + parts.length);
		if(parts.length > 1 && parts[0].length() == 2) {
			char part = parts[0].charAt(0); //B
			char chapter = parts[0].charAt(1); //1
			Logger.warn("part: " + part + " chapter " + chapter);
			GrammarPart p = find("elementNumber=:elNum and parent is null and grammar=:grammar")
					.setParameter("elNum", ""+(convertPartChar(part)+4))
					.setParameter("grammar", g).first();
			if(p == null) {
				Logger.warn("part is null");
				return null;
			}
			GrammarPart gp = p;
			parts[0] = chapter+"";
			for(int i = 0 ; i < parts.length; i++) {
				gp = find("elementNumber=:elNum and parent=:p")
						.setParameter("elNum", ""+parts[i]).setParameter("p", gp).first();
			}

			if(gp!=null) {
				Logger.warn("chapter " + gp.getUuid());
				return gp;
			} else {
				Logger.warn("chapter null");
			}
		}
		return null;
	}
	

	private static Integer convertPartChar(char part) {
		if(part == 'A')
			return 1;
		if(part == 'B')
			return 2;
		if(part == 'C')
			return 3;
		if(part == 'D')
			return 4;
		if(part == 'E')
			return 5;
		if(part == 'F')
			return 6;
		if(part == 'G')
			return 7;
		if(part == 'H')
			return 8;
		return null;
	}

	public void recalculateOrders() {
		this.calculateCompleteOrder();
		this.calculateCompleteOrderNow();
		for(GrammarPart gp : getParts()) {
			gp.recalculateOrders();
		}
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}
	
}
