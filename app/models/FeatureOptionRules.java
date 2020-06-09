package models;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Transient;


@Entity
public class FeatureOptionRules extends ModelUuid  {
	
	@Lob
	private String filter;
	
	@Basic 
	private String label;
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="feature_uuid")
	private Feature feature;
	
	@Transient
	private List<int[]> intFilter;
	

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<int[]> getIntFilter() {
		if(intFilter == null)
			convertIntFilter();
		return intFilter;
	}

	@PostLoad
	private void convertIntFilter() {
		if(filter == null || filter.isEmpty())
			intFilter = null;
		intFilter = new LinkedList<>();
		String[] splitted = filter.split(" ");
		for(String s : splitted) {
			//Logger.warn("string to convert " + s + " " + s.length());
			int[] ifil = new int[s.length()];
			for(int i = 0 ; i < s.length(); i++) {
				ifil[i] = Integer.parseInt(Character.toString(s.charAt(i)));
			}
			intFilter.add(ifil);
		}
	}

	public void setIntFilter(List<int[]> intFilter) {
		this.intFilter = intFilter;
		String filter = "";
		String sep = "";
		for(int[] f : intFilter) {
			filter += sep;
			for(int i : f)
				filter += i;
			sep = " ";
		}
		this.setFilter(filter);
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

	public static List<FeatureOptionRules> findByFeature(Feature feature) {
		return find("feature=?", feature).fetch();
	}
	
	

}
