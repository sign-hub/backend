package models;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import core.services.UserService;
import play.db.jpa.GenericModel;
import play.db.jpa.JPABase;
import utils.UuidGenerator;

@MappedSuperclass
public class ModelUuid extends GenericModel {

	@Id
	protected String uuid;
	
	@Basic
	private Date updateDate = new Date();
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	private User updatedBy;
	

	@PrePersist
	public void generateId() {
		if (uuid == null || uuid.equals("")) {
			uuid = generateId(this.getClass());
		}
	}
	
	/*@PreUpdate
	public void preUpdate() {
		this.setInformations();
	}*/

	private void setInformations() {
		this.updateDate = new Date();
		this.updatedBy = UserService.instance().getCurrentUserLogged();
	}

	/**
	 * @param class1
	 * @return
	 */
	public static String generateId(Class class1) {
		String mm = class1.getSimpleName();
		mm = mm.substring(mm.lastIndexOf(".") + 1).replaceAll("[aeiou_]++", "").toUpperCase();
		if (mm.length() > 4) {
			mm = mm.substring(0, 4);
		}
		mm = "UUID-" + mm;
		return generateId(mm);
	}

	public static String generateId(String mm) {
		mm = mm + "-";
		return UuidGenerator.generate(mm);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public Object _key() {
		return getUuid();
	}

	@Override
	public int hashCode() {
		/*final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;*/
		
		if(uuid == null)
			return super.hashCode();
		return this.uuid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof ModelUuid))
			return false;
		ModelUuid other = (ModelUuid) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	@Override
	public <T extends JPABase> T save() {
		this.setInformations();
		return super.save();
	}
	
}