package models;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class FeatureChoiche extends ModelUuid {

	@Basic
	public String groupName;
	
	@Basic
	public String label;
	
	@Basic
	@Column(name="partitionName")
	public String partition;
	
	@Basic
	public Integer position;
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="feature_uuid")
	private Feature feature;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	public static void deleteAllByFeature(Feature ft) {
		delete("feature=?", ft);	
	}

	public static FeatureChoiche findByGroupName(String group) {
		return find("groupName=?", group).first();
	}

	public static List<FeatureChoiche> findByFeature(Feature ft) {
		return find("feature=? order by position ASC", ft).fetch();
	}
	
}
