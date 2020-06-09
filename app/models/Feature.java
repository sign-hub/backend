package models;

import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

import utils.StringUtil;

@Entity
public class Feature extends ModelUuid  {
	
	public static enum FeatureType {
		STRING, INTEGER, BOOLEAN, SINGLE, MULTIPLE;
	}
	@Basic
	private String code;
	@Basic
	private String name;
	@Basic
	private String featureDescription;
	@Basic
	@Enumerated(EnumType.STRING)
	private FeatureType featureType;
	
	@Basic
	private Boolean personalJudgment = false;
	
	@ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="feature_options", joinColumns=@JoinColumn(name="id"))
	private Map<String, String> optionsOld;
	
	@OneToMany(mappedBy="feature")
	@OrderColumn(name = "option_sequence")
	private List<FeatureOption> options;
	
	/*@ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="feature_options_new", joinColumns=@JoinColumn(name="id"))
	@OrderColumn(name = "option_sequence")
	private Map<String, String> optionsNew;*/
	
	@OneToMany(mappedBy="feature")
	private List<FeatureOptionRules> rules;
	
	@OneToMany(mappedBy="feature")
	private List<FeatureChoiche> choiches;
	
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	private FeatureArea area;
	
	@Basic
	private Boolean active = true;
	
	@Basic
	private String testName;
	
	@Basic
	private String sectionName;
	
	@Basic
	private String slideName;
	
	@Basic 
	private String groupName;
	
	@Basic
	private String bluePrintSection;
	
	@Basic
	private String chapterName;
	
	@Transient
	private String chapterUuid;
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name="grammarPart_uuid")
	private GrammarPart grammarPart;

	@PostLoad
	private void checkChapterName() {
		if(bluePrintSection != null && !bluePrintSection.trim().equals("") && (chapterName == null || "".equals(this.chapterName.trim()))) {
			chapterName = GrammarPart.getChapterNameByBlueprintSection(bluePrintSection);
			GrammarPart gp = GrammarPart.getChapterByBlueprintSection(bluePrintSection);
			if(gp != null) {
				chapterUuid = gp.getUuid();
				chapterName = gp.getGrammarPartName();
			}
			//save();
		}
		
		if(bluePrintSection != null && !bluePrintSection.trim().equals("") && (grammarPart == null)) {
			GrammarPart gp = GrammarPart.getPartByBlueprintSection(bluePrintSection);
			if(gp != null) {
				grammarPart = gp;
			}
			save();
		}
	}
	
	public String getChapterName() {
		return chapterName;
	}
	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFeatureDescription() {
		return featureDescription;
	}
	public void setFeatureDescription(String featureDescription) {
		this.featureDescription = featureDescription;
	}
	public FeatureType getFeatureType() {
		return featureType;
	}
	public void setFeatureType(FeatureType featureType) {
		this.featureType = featureType;
	}
	/*public Map<String, String> getOptions() {
		return options;
	}
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}*/
	
	public FeatureArea getArea() {
		return area;
	}
	public List<FeatureOption> getOptions() {
		return options;
	}

	public void setOptions(List<FeatureOption> options) {
		this.options = options;
	}

	public void setArea(FeatureArea area) {
		this.area = area;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public Boolean getPersonalJudgment() {
		return personalJudgment;
	}
	public void setPersonalJudgment(Boolean personalJudgment) {
		this.personalJudgment = personalJudgment;
	}
	public String getBluePrintSection() {
		return bluePrintSection;
	}
	public void setBluePrintSection(String bluePrintSection) {
		this.bluePrintSection = bluePrintSection;
	}
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	public String getSlideName() {
		return slideName;
	}
	public void setSlideName(String slideName) {
		this.slideName = slideName;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public static List<Feature> findByTestName(String testName) {
		return find("testName=:testName").setParameter("testName", testName).fetch();
	}

	public String getChapterUuid() {
		return chapterUuid;
	}

	public List<FeatureOptionRules> getRules() {
		return rules;
	}

	public void setRules(List<FeatureOptionRules> rules) {
		this.rules = rules;
	}

	public static List<Feature> findByType(FeatureType ft) {
		return find("featureType=:featureType").setParameter("featureType", ft).fetch();

	}

	public static void deleteAllByType(FeatureType ft) {
		delete("featureType=?", ft);
	}

	public List<FeatureChoiche> getChoiches() {
		return choiches;
	}

	public void setChoiches(List<FeatureChoiche> choiches) {
		this.choiches = choiches;
	}
	
	
	
}
