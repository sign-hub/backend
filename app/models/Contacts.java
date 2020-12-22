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

import models.Feature.FeatureType;
import models.Test.TestStatus;
import utils.StringUtil;

@Entity
public class Contacts extends ModelUuid  {
	
	public static enum ContactTool {
		TESTING, ATLAS, GRAMMAR, STREAMING, GENERAL, TECH;
		
		public static ContactTool tryBuildContactToolFromName(String name) {
			if (StringUtil.isNil(name)) {
				return null;
			}

			for (ContactTool contactTool : values()) {
				if (contactTool.name().equals(name)) {
					return contactTool;
				}
			}

			return null;
		}
	}
	
	@Basic
	@Enumerated(EnumType.STRING)
	private ContactTool tool;
	
	@Basic
	private String email;

	
	public Contacts() {
		super();
	}

	public Contacts(String email, ContactTool tool) {
		this.email = email;
		this.tool = tool;
	}

	public ContactTool getTool() {
		return tool;
	}

	public void setTool(ContactTool tool) {
		this.tool = tool;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public static List<Contacts> findByType(ContactTool type) {
		return find("tool=:tool").setParameter("tool", type).fetch();
	}

	public static Contacts findByMailAndTool(String email, ContactTool type) {
		return find("email=:email and tool=:tool").setParameter("email", email).setParameter("tool", type).first();
	}
	
}
