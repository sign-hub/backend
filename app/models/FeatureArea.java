package models;

import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
public class FeatureArea extends ModelUuid {

	@Basic
	private String name;
	
	@Basic
	private String areaDescription;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAreaDescription() {
		return areaDescription;
	}

	public void setAreaDescription(String areaDescription) {
		this.areaDescription = areaDescription;
	}

	public static FeatureArea findByName(String name) {
		return find("name=:name").setParameter("name", name).first();
	}
	
	
}
