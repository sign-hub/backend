package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import utils.StringUtil;

@Entity
public class SignLanguage extends ModelUuid  {
	@Basic
	private String code;
	@Basic
	private String officialCode;
	@Basic
	private String name;
	@Basic
	private String usersDescription;
	@Basic
	private String deafCulture;
	@Basic
	private String deafEducation;
	@Basic
	private String status;
	@Basic
	private String linguisticStudies;
	@Basic
	private String author;
	@Basic
	private String countries;
	@Basic
	private String area;
	
	@Basic
	private String coordinates;
	
	@Transient
	private String grammarReference;
	
	@Basic
	private String cpPHON;
	
	@Lob
	private String ackPHON;
	
	@Basic
	private String cpLEX;
	
	@Lob
	private String ackLEX;
	
	@Basic
	private String cpMORPH;
	
	@Lob
	private String ackMORPH;
	
	@Basic
	private String cpSYN;
	
	@Lob
	private String ackSYN;
	
	@Basic
	private String cpPRAG;
	
	@Lob
	private String ackPRAG;
	
	
	
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
	public String getUsersDescription() {
		return usersDescription;
	}
	public void setUsersDescription(String usersDescription) {
		this.usersDescription = usersDescription;
	}
	public String getDeafCulture() {
		return deafCulture;
	}
	public void setDeafCulture(String deafCulture) {
		this.deafCulture = deafCulture;
	}
	public String getDeafEducation() {
		return deafEducation;
	}
	public void setDeafEducation(String deafEducation) {
		this.deafEducation = deafEducation;
	}
	public String getLinguisticStudies() {
		return linguisticStudies;
	}
	public void setLinguisticStudies(String linguisticStudies) {
		this.linguisticStudies = linguisticStudies;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public List<String> getCountriesList() {
		List<String> ret = null;
		if(!StringUtil.isNil(countries)) {
			String[] arr = countries.split("||");
			ret = Arrays.asList(arr);	
		}
		return ret;
	}
	
	public void setCountriesList(List<String> countries) {
		String cc = null;
		if(!StringUtil.isNil(countries)) {
			cc = "";
			String sep ="";
			for(String c : countries) {
				cc += sep + c;
				sep = "||";
			}
		}
		this.countries = cc;
	}
	
	public String getCountries() {
		return countries;
	}
	
	public void setCountries(String countries) {
		this.countries = countries;
	}
	public static SignLanguage findByCode(String code) {
		SignLanguage ret = find("code=:code").setParameter("code", code).first();
		return ret;
	}
	public static SignLanguage findByName(String name) {
		SignLanguage ret = find("name=:name").setParameter("name", name).first();
		return ret;
	}
	public String getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public void setGrammarReference(String uuid) {
		this.grammarReference = uuid;
	}
	
	public String getGrammarReference() {
		return this.grammarReference;
	}
	public String getOfficialCode() {
		return officialCode;
	}
	public void setOfficialCode(String officialCode) {
		this.officialCode = officialCode;
	}
	public String getCpPHON() {
		return cpPHON;
	}
	public void setCpPHON(String cpPHON) {
		this.cpPHON = cpPHON;
	}
	public String getAckPHON() {
		return ackPHON;
	}
	public void setAckPHON(String ackPHON) {
		this.ackPHON = ackPHON;
	}
	public String getCpLEX() {
		return cpLEX;
	}
	public void setCpLEX(String cpLEX) {
		this.cpLEX = cpLEX;
	}
	public String getAckLEX() {
		return ackLEX;
	}
	public void setAckLEX(String ackLEX) {
		this.ackLEX = ackLEX;
	}
	public String getCpMORPH() {
		return cpMORPH;
	}
	public void setCpMORPH(String cpMORPH) {
		this.cpMORPH = cpMORPH;
	}
	public String getAckMORPH() {
		return ackMORPH;
	}
	public void setAckMORPH(String ackMORPH) {
		this.ackMORPH = ackMORPH;
	}
	public String getCpSYN() {
		return cpSYN;
	}
	public void setCpSYN(String cpSYN) {
		this.cpSYN = cpSYN;
	}
	public String getAckSYN() {
		return ackSYN;
	}
	public void setAckSYN(String ackSYN) {
		this.ackSYN = ackSYN;
	}
	public String getCpPRAG() {
		return cpPRAG;
	}
	public void setCpPRAG(String cpPRAG) {
		this.cpPRAG = cpPRAG;
	}
	public String getAckPRAG() {
		return ackPRAG;
	}
	public void setAckPRAG(String ackPRAG) {
		this.ackPRAG = ackPRAG;
	}
	
	
	
}
