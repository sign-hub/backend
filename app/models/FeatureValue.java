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

import utils.StringUtil;

@Entity
public class FeatureValue extends ModelUuid  {
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	private Feature feature;
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	private SignLanguage signLanguage;
	
	@Basic
	private String value;

	@Basic
	private Boolean personalJudgment = false;

	public FeatureValue(Feature f, String v, SignLanguage sl) {
		this.feature = f;
		this.value = v;
		this.signLanguage = sl;
	}
	public FeatureValue() {
		// TODO Auto-generated constructor stub
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public SignLanguage getSignLanguage() {
		return signLanguage;
	}

	public void setSignLanguage(SignLanguage signLanguage) {
		this.signLanguage = signLanguage;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static List<FeatureValue> findByFeatureAndValue(Feature f, String v) {
		return find("feature=:f and value=:v").setParameter("f", f).setParameter("v", v).fetch();
	}
	
	public static List<FeatureValue> findByFeature(Feature f) {
		return find("feature=:f").setParameter("f", f).fetch();
	}
	
	public static List<FeatureValue> findBySignLanguage(SignLanguage sl) {
		return find("signLanguage=:sl").setParameter("sl", sl).fetch();
	}
	
	public static void cleanByFeature(Feature feature) {
		delete("feature=?",feature);
	}
	public static void cleanByFeatureAndSignLanguage(Feature feature, SignLanguage sl) {
		delete("feature=? and signLanguage=?",feature, sl);
	}
	
	public static void deleteAllByFeature(Feature ft) {
		delete("feature=?", ft);	
	}
	public void setPersonalJudgment(Boolean personalJudgmentVal) {
		this.personalJudgment = personalJudgmentVal;
	}
	public Boolean getPersonalJudgment() {
		return this.personalJudgment;
	}
	
	
}
