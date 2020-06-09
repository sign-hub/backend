package core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.google.common.collect.Sets;

import core.services.ApiService.OKResponseObject;
import core.services.GrammarService.GrammarPartObject;
import core.services.TestService.SignLanguageWrapper;
import models.Feature;
import models.Feature.FeatureType;
import models.FeatureArea;
import models.FeatureChoiche;
import models.FeatureOption;
import models.FeatureOptionRules;
import models.FeatureValue;
import models.Grammar;
import models.GrammarPart;
import models.Option;
import models.Question;
import models.Report;
import models.SignLanguage;
import models.Slide;
import models.SlideContentComponent;
import models.SlideContentComponent.ComponentType;
import models.Test;
import play.Logger;
import utils.StringUtil;

public class FeatureService {
	private static enum TopicServiceSingleton {
		INSTANCE;

		FeatureService singleton = new FeatureService();

		public FeatureService getSingleton() {
			return singleton;
		}
	}

	public static FeatureService instance() {
		return FeatureService.TopicServiceSingleton.INSTANCE.getSingleton();
	}

	private FeatureService() {
		// TODO Auto-generated constructor stub
	}

	private static ApiService apiService = ApiService.instance();
	
	public static class FeatureOptionWrapper {
		public String key;
		public String value;
		public FeatureOptionWrapper(FeatureOption opt) {
			this.key = opt.optionKey;
			this.value = opt.optionValue;
		}
		
	}
	
	public static class FeatureWrapper {

		public String code;
		public String name;
		public String featureDescription;
		public FeatureType featureType;
		public Boolean personalJudgment = false;
		public Map<String, String> options;
		//public List<FeatureOptionWrapper> options;
		public FeatureArea area;
		public String testName;
		public String sectionName;
		public String slideName;
		public String groupName;
		public String bluePrintSection;
		public String chapterName;
		public String chapterUuid;
		public String uuid;

		public FeatureWrapper(Feature f) {
			this.uuid = f.getUuid();
			this.code = f.getCode();
			this.name = f.getName();
			this.featureDescription = f.getFeatureDescription();
			this.featureType = f.getFeatureType();
			this.personalJudgment = f.getPersonalJudgment();
			this.options = this.buildFeatureOptionsWrappersMap(f.getOptions());
			this.area = f.getArea();
			this.testName = f.getTestName();
			this.sectionName = f.getSectionName();
			this.slideName = f.getSlideName();
			this.groupName = f.getGroupName();
			this.bluePrintSection = f.getBluePrintSection();
			this.chapterName = f.getChapterName();
			this.chapterUuid = f.getChapterUuid();
		}

		private Map<String, String> buildFeatureOptionsWrappersMap(List<FeatureOption> options) {
			Map<String, String> ret = new LinkedHashMap<String, String>();
			for(FeatureOption opt: options) {
				ret.put(opt.optionKey, opt.optionValue);
			}
			return ret;
		}

		private List<FeatureOptionWrapper> buildFeatureOptionsWrappers(List<FeatureOption> options) {
			List<FeatureOptionWrapper> ret = new LinkedList<FeatureOptionWrapper>();
			for(FeatureOption opt: options) {
				FeatureOptionWrapper fw = new FeatureOptionWrapper(opt);
				ret.add(fw);
			}
			return ret;
		}
		
	}

	public List<FeatureWrapper> getFeatures() {
		List<Feature> features = Feature.all().fetch();
		List<FeatureWrapper> ret = new LinkedList<FeatureWrapper>();
		for(Feature f : features) {
			ret.add(new FeatureWrapper(f));
		}
		return ret;
	}

	public Map<String, List<FeatureWrapper>> getFeaturesMap() {
		List<Feature> all = Feature.all().fetch();
		Map<String, List<FeatureWrapper>> ret = new HashMap<String, List<FeatureWrapper>>();

		for (Feature f : all) {
			if (!ret.containsKey(f.getArea().getName()))
				ret.put(f.getArea().getName(), new LinkedList<FeatureWrapper>());
			ret.get(f.getArea().getName()).add(new FeatureWrapper(f));
		}
		return ret;
	}

	public void parseCsv(String path, String filename, String filename1) {
		String csvFile = buildFilePath(path, filename);
		String csvFile1 = buildFilePath(path, filename1);
		parseCsv(new File(csvFile), new File(csvFile1));
	}

	private void parseCsv(File f, File f1) {
		if (!f.exists() || !f1.exists()) {
			Logger.error("Error parsing features files: " + f.getAbsolutePath() + " " + f1.getAbsolutePath());
			return;
		}
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";

		try {

			br = new BufferedReader(new FileReader(f));
			int index = 0;
			while ((line = br.readLine()) != null) {

				String[] fields = line.split(cvsSplitBy);

				if (fields.length > 0) {
					index++;
					String area = fields[0];
					String testName = fields[1];
					String sectionName = fields[2];
					String slideName = fields[3];
					String groupName = fields[4];
					String personalJudgment = fields[5];
					String bluePrintSection = fields[6];

					String description = groupName;
					if (fields.length == 8) {
						description = fields[7];
					} else {
						description = groupName;
					}

					area = area.trim();
					if (StringUtil.isEmpty(area)) {
						Logger.warn("Skip area ");
						continue;
					}
					FeatureArea a = FeatureArea.findByName(area);
					if (a == null) {
						a = new FeatureArea();
						a.setName(area);
						a.setAreaDescription(area);
						a.save();
					}

					Boolean pj = false;
					if (!StringUtil.isNil(personalJudgment) && personalJudgment.equalsIgnoreCase("yes"))
						pj = true;
					Feature feature = new Feature();
					feature.setArea(a);
					feature.setFeatureDescription(description);
					feature.setName(groupName);
					feature.setBluePrintSection(bluePrintSection);
					feature.setGroupName(groupName);
					feature.setPersonalJudgment(pj);
					feature.setSectionName(sectionName);
					feature.setSlideName(slideName);
					feature.setTestName(testName);
					feature.setFeatureType(FeatureType.SINGLE);
					//Map<String, String> options;
					List<FeatureOption> options;
					/*
					 * options = new HashMap<String, String>(); if(!StringUtil.isNil(values)) {
					 * String[] vals = values.split("\\$");
					 * 
					 * for(String val : vals) { val = val.trim(); if(val.contains("=")) { String[]
					 * vv = val.split("="); options.put(vv[0].trim(), vv[1].trim()); } else {
					 * options.put(val, val); } } }
					 */
					feature.save();
					options = this.parseOptionsCsv(f1, index, feature);
					feature.setOptions(options);
					//feature.setSingleChoiches(choiches);
					feature.save();
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private List<FeatureOption> parseOptionsCsv(File f, int index, Feature feature) {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";
		List<FeatureOption> options;
		options = new LinkedList<FeatureOption>();
		try {

			br = new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null) {

				String[] fields = line.split(cvsSplitBy);

				if (fields.length > 0) {
					String numFeature = fields[0];
					String value = fields[1];
					String label = fields[2];
					try {
						int n = Integer.parseInt(numFeature);
						if (n == index) {
							//options.put(label, value);
							FeatureOption fo = new FeatureOption();
							fo.optionKey = label;
							fo.optionValue = value;
							fo.setFeature(feature);
							fo.save();
							options.add(fo);
						}
					} catch (Exception e) {
						Logger.error(e, "Error parsing number " + numFeature);
					}
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return options;
	}

	/*
	 * This function build the absolute path;
	 */
	private String buildFilePath(String path, String filename) {
		String ret = "";
		if (!StringUtil.isNil(path)) {
			ret += path;
			if (!path.endsWith("/"))
				ret += "/";
		}
		if (!StringUtil.isNil(filename)) {
			if (filename.startsWith("/"))
				filename = filename.substring(1);
			ret += filename;
		}
		return ret;
	}

	public void createFeatures(String path, String filename, String filename1, boolean override) {
		if (StringUtil.isNil(path) || StringUtil.isNil(filename))
			return;
		File f = new File(path, filename);
		if (!f.exists()) {
			Logger.warn("file don't exists " + f.getAbsolutePath());
			return;
		}
		if (!f.isFile() || f.isDirectory()) {
			Logger.warn("file is not a file??? " + f.getAbsolutePath());
			return;
		}

		File f1 = new File(path, filename1);
		if (!f1.exists()) {
			Logger.warn("file don't exists " + f1.getAbsolutePath());
			return;
		}
		if (!f1.isFile() || f1.isDirectory()) {
			Logger.warn("file is not a file??? " + f1.getAbsolutePath());
			return;
		}
		if (override) {
			this.deleteAllFeature(FeatureType.SINGLE);
		}
		this.parseCsv(f, f1);
	}

	private void deleteAllFeature(FeatureType ft) {
		if (ft == null)
			return;
		List<Feature> list = Feature.findByType(ft);
		List<Feature> toDelete = new ArrayList<Feature>();
		Iterator<Feature> it = list.iterator();
		Feature el = null;
		while (it.hasNext()) {
			el = it.next();
			if (el == null)
				continue;
			if(!el.getFeatureType().equals(ft))
				continue;
			el.setOptions(null);
			el.save();
			FeatureValue.cleanByFeature(el);
			FeatureOption.deleteAllByFeature(el);
			FeatureOptionRules.deleteAllByFeature(el);
			FeatureChoiche.deleteAllByFeature(el);
			//el.delete();
		}
		
		Feature.deleteAllByType(ft);
		/*
		 * for(Feature el : list) { if(!el.getFeatureType().equals(ft)) continue;
		 * el.setOptions(null); el.save(); toDelete.add(el); }
		 */
		// Feature.deleteAll();
	}

	public static class SearchFeatureRequest {
		List<SearchFeatureDetail> request;
	}

	public static class SearchFeatureDetail {
		String area;
		String id;
		String name;
		List<String> idOption;
		List<String> options;
	}

	public static class FeatureTuples {
		public FeatureTuples(List<FeatureTupla> tp, Set<SignLanguage> languages) {
			options = tp;
			this.languages = this.convertLanguages(languages);
		}

		private Set<SignLanguageReduced> convertLanguages(Set<SignLanguage> languages) {
			if (languages == null)
				return null;
			Set<SignLanguageReduced> ret = new HashSet<SignLanguageReduced>();
			for (SignLanguage lang : languages) {
				if (lang == null)
					continue;
				ret.add(new SignLanguageReduced(lang));
			}
			return ret;
		}

		List<FeatureTupla> options;
		Set<SignLanguageReduced> languages;
	}

	public static class SignLanguageReduced {
		public SignLanguageReduced(SignLanguage lang) {
			this.id = lang.getUuid();
			this.code = lang.getCode();
			this.codeLanguage = lang.getCode();
			this.name = lang.getName();
			this.coords = this.convertCoords(lang.getCoordinates());
		}

		private Coords convertCoords(String coordinates) {
			if (StringUtil.isNil(coordinates))
				return null;
			Coords ret = new Coords();
			String[] splitted = coordinates.substring(1, coordinates.length() - 1).split(",");
			String lat = splitted[0];
			String lon = splitted[1];
			// Logger.warn("lat: " + lat + " lon: " + lon);

			if (!StringUtil.isNil(lat)) {
				try {
					ret.lat = Float.parseFloat(lat);
				} catch (Exception e) {
					Logger.error(e, "Errore nella conversione delle coordinate lat " + lat);
					ret.lat = null;
				}
			}
			if (!StringUtil.isNil(lon)) {
				try {
					ret.lon = Float.parseFloat(lon);
				} catch (Exception e) {
					Logger.error(e, "Errore nella conversione delle coordinate lon " + lon);
					ret.lon = null;
				}
			}
			// Logger.warn("lat: " + ret.lat + " lon: " + ret.lon);
			return ret;
		}

		public SignLanguageReduced() {
		}

		String id;
		String code;
		String codeLanguage;
		String name;
		Coords coords;
	}

	public static class Coords {
		Float lon;
		Float lat;
	}

	public static class FeatureTupla {
		public FeatureTupla(Feature f, String optionId) {
			this.feature = new FeatureReduced(f);
			this.value = new FeatureVal(f, optionId);
		}

		public FeatureTupla() {
		}

		FeatureReduced feature;
		FeatureVal value;
	}

	public static class FeatureVal {
		public FeatureVal(Feature f, String optionId) {
			this.value = optionId;
			this.valueName = optionId;
			for(FeatureOption fo : f.getOptions()) {
				if(fo.optionValue.equals(optionId)){
					this.valueName = fo.optionKey;
					break;
				}
					
			}
			/*for (String k : f.getOptions().keySet()) {
				String v = f.getOptions().get(k);
				if (v.equals(optionId)) {
					this.valueName = k;
					break;
				}
			}*/
		}

		String value;
		String valueName;
	}

	public static class FeatureReduced {

		String featureUuid;
		String featureName;

		public FeatureReduced(Feature f) {
			this.featureName = f.getName();
			this.featureUuid = f.getUuid();
		}

	}

	public static class SearchFeatureResponse extends OKResponseObject {
		Set<FeatureTuples> response;
	}

	public String searchByFeatures() {
		Logger.warn("searchByFeatures: ");

		SearchFeatureRequest req = null;
		try {
			req = (SearchFeatureRequest) apiService.buildObjectFromJson(apiService.getCurrentJson(),
					SearchFeatureRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			if (req.request == null || req.request.isEmpty()) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			List<HashSet<FeatureTupla>> sets = new ArrayList<HashSet<FeatureTupla>>();
			for (SearchFeatureDetail sfd : req.request) {
				Logger.warn("SearchFeatureDetail: " + sfd.id + " " + sfd.idOption);
				if (sfd == null || StringUtil.isNil(sfd.id))
					continue;
				Feature f = Feature.findById(sfd.id);
				if (f == null)
					continue;
				Logger.warn("SearchFeatureDetail: " + sfd.id + " " + sfd.idOption);
				HashSet<FeatureTupla> set = new HashSet<FeatureTupla>();
				for (String optionId : sfd.idOption) {
					if (StringUtil.isNil(optionId))
						continue;
					FeatureTupla ft = new FeatureTupla(f, optionId);
					Logger.warn("adding in set " + f.getUuid() + " " + optionId);
					set.add(ft);
				}
				sets.add(set);
			}
			SearchFeatureResponse response = new SearchFeatureResponse();
			response.response = new HashSet<FeatureTuples>();
			// Set<FeatureTuples> tuples = new HashSet<FeatureTuples>();
			Set<List<FeatureTupla>> product = Sets.cartesianProduct(sets);
			for (List<FeatureTupla> tp : product) {
				Logger.warn("product------");
				for (FeatureTupla ft : tp)
					Logger.warn("p: " + ft.feature.featureUuid + " " + ft.value);
				response.response.add(new FeatureTuples(tp, elaborateTupla(tp)));
			}
			return response.toJson();

		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
		// return null;
	}

	private Set<SignLanguage> elaborateTupla(List<FeatureTupla> tp) {
		Logger.warn("elaborateTupla");
		if (tp == null)
			return null;
		Set<SignLanguage> ret = null;
		for (FeatureTupla ft : tp) {
			Set<SignLanguage> ret1 = new HashSet<SignLanguage>();
			Logger.warn("elaborate tupla " + ft.feature.featureUuid + " " + ft.value.valueName);
			List<FeatureValue> featureValues = FeatureValue
					.findByFeatureAndValue(Feature.findById(ft.feature.featureUuid), ft.value.valueName);
			for (FeatureValue fv : featureValues) {
				ret1.add(fv.getSignLanguage());
			}
			if (ret == null) {
				ret = new HashSet<SignLanguage>();
				ret.addAll(ret1);
			}
			ret.retainAll(ret1);
		}
		return ret;
	}

	public ParseReportResponse extractFeaturesFromReport(Report r) {
		if (r == null || StringUtil.isNil(r.getReportCsvPath()))
			return null;
		Test t = r.getReportTest();
		if (t == null)
			return null;
		String csvPath = r.getReportCsvPath();
		if(!StringUtil.isNil(csvPath) && csvPath.endsWith("xlsx")) {
			csvPath = csvPath.substring(0, csvPath.length()-4) + "csv";
		}
		Logger.warn("csv path: " + csvPath);
		File csv = new File(csvPath);
		if (!csv.exists())
			return null;
		List<Feature> features = Feature.findByTestName(t.getTestName());
		if (features == null || features.isEmpty())
			return null;
		SignLanguage sl;
		if (r.getIsComplessive()) {
			sl = null;
		} else {
			String languageName = r.getLanguageName();
			sl = SignLanguage.findByName(languageName);
		}
		cleanFeaturesValues(features, sl);
		Map<String, Feature> featureMap = buildMappingFeatureMap(features);
		ParseReportResponse ret = parseReportCsv(csv, featureMap, sl, t);
		ret.reportId = r.getUuid();
		return ret;
	}

	private Map<String, Feature> buildMappingFeatureMap(List<Feature> features) {
		Map<String, Feature> ret = new HashMap<String, Feature>();
		String featureId;
		for (Feature f : features) {
			featureId = buildFeatureId(f.getSectionName(), f.getSlideName(), f.getGroupName());
			ret.put(featureId, f);
		}
		return ret;
	}

	private String buildFeatureId(String sectionName, String slideName, String groupName) {
		if (StringUtil.isNil(sectionName) || StringUtil.isNil(slideName) || StringUtil.isNil(groupName))
			return null;
		return sectionName + "-" + slideName + "-" + groupName;
	}

	private void cleanFeaturesValues(List<Feature> features, SignLanguage sl) {
		for (Feature feature : features) {
			if (feature == null)
				continue;
			FeatureValue.cleanByFeatureAndSignLanguage(feature, sl);
		}
	}
	
	public static class ParseReportResponse {
		public String signLanguage;
		public String reportId;
		public Set<FeatureValueWrapper> values;
		public Set<ParseReportResponseError> errors;
		
		public void setValues(Set<FeatureValue> retVal) {
			this.values = new HashSet<FeatureValueWrapper>();
			for(FeatureValue fv: retVal) {
				this.values.add(new FeatureValueWrapper(fv));
			}
		}
	}
	
	public static class FeatureValueWrapper {
		public FeatureWrapper feature;
		public SignLanguageWrapper signLanguage;
		public String id;
		public String value;

		public FeatureValueWrapper() {
		}
		
		public FeatureValueWrapper(FeatureValue fv) {
			this.id = fv.getUuid();
			this.feature = new FeatureWrapper(fv.getFeature());
			this.signLanguage = new SignLanguageWrapper(fv.getSignLanguage());
			this.value = fv.getValue();
		}
	}
	
	public static class ParseReportResponseError {
		public FeatureWrapper f;
		public String error;
	}

	private ParseReportResponse parseReportCsv(File reportFile, Map<String, Feature> featureMap, SignLanguage sl, Test t) {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";
		Set<FeatureValue> retVal;
		ParseReportResponse ret = new ParseReportResponse();
		ret.errors = new HashSet<ParseReportResponseError>();
		retVal = new HashSet<FeatureValue>();
		Map<Feature, Map<String, String>> multiFeatureMap = new HashMap<Feature, Map<String, String>>();
		try {

			br = new BufferedReader(new FileReader(reportFile));
			while ((line = br.readLine()) != null) {
				//Logger.warn("line: " + line);
				String[] fields = line.split(cvsSplitBy);

				if (fields.length >= 5) {
					String section = fields[0];
					String slide = fields[1];
					String group = fields[2];
					String label = fields[3];
					String value = fields[4];
					String featureId = buildFeatureId(section, slide, group);
					if (StringUtil.isNil(featureId)) {
						continue;
					}
					Logger.warn("featureId: " + featureId);
					if (!featureMap.containsKey(featureId)) {
						FeatureChoiche fc = FeatureChoiche.findByGroupName(group);
						if(fc != null) {
							Feature fcf = fc.getFeature();
							if(!multiFeatureMap.containsKey(fcf))
								multiFeatureMap.put(fcf, new HashMap<String, String>());
							multiFeatureMap.get(fcf).put(group, value);
						}
						continue;
					}
					
					Feature feature = featureMap.get(featureId);
					FeatureValue fv = null;
					if(feature.getFeatureType().equals(FeatureType.SINGLE)) {
						String vv = null;
						Slide s = null;
						for(Question q : t.getQuestions()) {
							if(!q.getName().equals(section))
								continue;
							boolean found = false;
							for(Slide ss : q.getSlides()) {
								Option o = null;
								for(Option oo : ss.getOptions()) {
									if(oo.getKey().equals("name")) {
										o = oo;
										break;
									}
								}
								if(o != null && o.getValue().equals(slide)) {
									s = ss;
									found = true;
									break;
								}
							}
							if(found)
								break;
						}
						if(s != null) {
							SlideContentComponent scc = null;
							for(SlideContentComponent sccc : s.getSlideContent()) {
								Option o = null;
								for(Option oo : sccc.getOptions()) {
									if(oo.getKey().equals("groupName")) {
										o = oo;
										break;
									}
								}
								if(o != null && o.getValue().equals(group)) {
									scc = sccc;
									break;
								}
							}
							
							if(scc != null) {
								if(scc.getComponentType().equals(ComponentType.RADIO)) {
									String index = null;
									for(Option o : scc.getOptions()) {
										if(o.getKey().startsWith("radioComponentLabel") && o.getValue().equals(value)) {
											String key = o.getKey();
											index = key.split("_")[1];
											break;
										}
									}
									if(index != null) {
										index = "radioComponentValue_" + index;
										for(Option o : scc.getOptions()) {
											if(o.getKey().equals(index)) {
												vv = o.getValue();
												break;
											}
										}
									}
								}
							}
						} else {
							Logger.error("Slide not found???");
						}
						
						if(vv == null) {
							Logger.error("Come mai vv è null???");
						} else {
							value = vv;
						}
						
						Logger.warn("feature: " + feature.getUuid());
						fv = getSingleTypeFeatureValue(feature, sl, value);
					} /*else if(feature.getFeatureType().equals(FeatureType.MULTIPLE)) {
						fv = getMultipleTypeFeatureValue(feature, sl, value);
					}*/
					if(fv != null)
						retVal.add(fv);
					else {
						ParseReportResponseError e = new ParseReportResponseError();
						e.error = "No valid value found " + value;
						e.f = new FeatureWrapper(feature);
						ret.errors.add(e);
					}
				}

			}
			
			if(multiFeatureMap != null) {
				FeatureValue fv = null;
				for(Feature f : multiFeatureMap.keySet()) {
					fv = getMultipleTypeFeatureValue(f, sl, multiFeatureMap.get(f));
					if(fv != null)
						retVal.add(fv);
					else {
						ParseReportResponseError e = new ParseReportResponseError();
						e.error = "No valid value found for multiple choices";
						e.f = new FeatureWrapper(f);
						ret.errors.add(e);
					}
						
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		ret.signLanguage = sl.getName();
		ret.setValues(retVal);
		return ret;
	}

	private FeatureValue getMultipleTypeFeatureValue(Feature feature, SignLanguage sl, Map<String, String> map) {
		// TODO Auto-generated method stub
		List<FeatureChoiche> choiches = FeatureChoiche.findByFeature(feature);
		int[] comb = createBaseFilter(choiches.size());
		FeatureChoiche fc = null;
		String v = null;
		for(int i = 0 ; i < choiches.size(); i++) {
			fc = choiches.get(i);
			v = map.get(fc.getGroupName());
			if(v == null) {
				Logger.error("Attenzione! nel report non c'è un valore per la feature choiche: " + fc.getUuid());
			}
			if(v != null && !v.equals("N/A"))
				comb[i] = 1;
		}
		List<FeatureOptionRules> featureOptionRules = FeatureOptionRules.findByFeature(feature);
		for(FeatureOptionRules singleOptionRule: featureOptionRules) {
			boolean check = false;
			for(int[] sf : singleOptionRule.getIntFilter()) {
				if(sf != null) {
					if(this.checkFilter(comb,sf));
					check = true;
				}
			}
			if(check) {
				String label = singleOptionRule.getLabel();
				for(FeatureOption fo : feature.getOptions()) {
					if(fo.getOptionKey().equals(label)) {
						FeatureValue fv = new FeatureValue();
						fv.setFeature(feature);
						fv.setSignLanguage(sl);
						fv.setValue(fo.getOptionKey());
						fv.save();
						return fv;
					}
				}
			}
				
		}
		return null;
	}
	
	private boolean checkFilter(int[] filter1, int[] filter2) {
		if(filter1.length != filter2.length)
			return false;
		for(int i = 0 ; i < filter1.length; i++) {
			if(filter1[i] != filter2[i])
				return false;
		}
		return true;
	}

	private FeatureValue getSingleTypeFeatureValue(Feature feature, SignLanguage sl, String value) {
		FeatureValue fv = new FeatureValue();
		fv.setFeature(feature);
		fv.setSignLanguage(sl);
		String val = null;
		for (FeatureOption fo : feature.getOptions()) {
			if (fo.optionValue.equals(value)) {
				val = fo.optionKey;
				break;
			}
		}
		
		
		if (val==null) {
			Logger.error("value not found "+ feature.getUuid() + " " + sl.getName() + " " + value);
			return null;
		}
//		for (String fok : feature.getOptions().keySet()) {
//			if (fok.equals(value)) {
//				val = feature.getOptions().get(value);
//				break;
//			}
//		}
		fv.setValue(val);
		fv.save();
		return fv;
	}

	public static class FeatValOpt {
		String label;
		String val;

		public FeatValOpt() {
			this.label = "N/A";
			this.val = "N/A";
		}
	}

	public static class FeatVal {

		
		public FeatVal(Feature f, FeatureValue featureValue) {
			this.uuid = f.getUuid();
			this.code = f.getCode();
			this.name = f.getName();
			this.featureDescription = f.getFeatureDescription();
			this.featureType = f.getFeatureType();
			this.personalJudgment = f.getPersonalJudgment();
			this.area = f.getArea();
			this.active = f.getActive();
			this.bluePrintSection = f.getBluePrintSection();
			this.chapterName = f.getChapterName();
			this.chapterUuid = f.getChapterUuid();
			this.val = new FeatValOpt();
			if (featureValue != null) {
				for( FeatureOption fo : f.getOptions()) {
					if (fo.optionValue.equals(featureValue.getValue())) {
						this.val.label = fo.optionKey;
						this.val.val = fo.optionValue;
						break;
					}
				}
//				for (String k : f.getOptions().keySet()) {
//					String v = f.getOptions().get(k);
//					if (v.equals(featureValue.getValue())) {
//						this.val.label = k;
//						this.val.val = v;
//						break;
//					}
//				}
			}
		}

		public FeatVal() {
			// TODO Auto-generated constructor stub
		}

		public String uuid;
		public String code;
		public String name;
		public String featureDescription;
		public FeatureType featureType;
		public Boolean personalJudgment = false;
		public FeatureArea area;
		public Boolean active = true;
		public String bluePrintSection;
		public FeatValOpt val;
		public String chapterName;
		public String chapterUuid;
	}

	public Map<String, List<FeatVal>> getFeaturesMapByLanguage(String code) {
		SignLanguage sl = SignLanguage.findByCode(code);
		if (sl == null) {
			return null;
		}
		Map<String, List<FeatVal>> ret = new HashMap<String, List<FeatVal>>();

		List<Feature> all = Feature.all().fetch();

		Map<String, FeatureValue> featureValueMap = new HashMap<String, FeatureValue>();
		List<FeatureValue> values = FeatureValue.findBySignLanguage(sl);
		for (FeatureValue fv : values) {
			if (fv == null)
				continue;
			featureValueMap.put(fv.getFeature().getUuid(), fv);
		}
		FeatVal fv;
		for (Feature f : all) {
			if (!ret.containsKey(f.getArea().getName())) {
				ret.put(f.getArea().getName(), new LinkedList<FeatVal>());
			}
			fv = new FeatVal(f, featureValueMap.get(f.getUuid()));
			ret.get(f.getArea().getName()).add(fv);
		}

		return ret;
	}

	public void createFeaturesMultiple(String path, String filename, String filename1, String filename2,
			boolean override) {
		if (StringUtil.isNil(path) || StringUtil.isNil(filename))
			return;
		File f = new File(path, filename);
		if (!f.exists()) {
			Logger.warn("file don't exists " + f.getAbsolutePath());
			return;
		}
		if (!f.isFile() || f.isDirectory()) {
			Logger.warn("file is not a file??? " + f.getAbsolutePath());
			return;
		}

		File f1 = new File(path, filename1);
		if (!f1.exists()) {
			Logger.warn("file don't exists " + f1.getAbsolutePath());
			return;
		}
		if (!f1.isFile() || f1.isDirectory()) {
			Logger.warn("file is not a file??? " + f1.getAbsolutePath());
			return;
		}

		File f2 = new File(path, filename2);
		if (!f2.exists()) {
			Logger.warn("file don't exists " + f2.getAbsolutePath());
			return;
		}
		if (!f2.isFile() || f2.isDirectory()) {
			Logger.warn("file is not a file??? " + f2.getAbsolutePath());
			return;
		}

		if (override) {
			this.deleteAllFeature(FeatureType.MULTIPLE);
		}
		this.parseCsvMultiple(f, f1, f2);
	}

	private void parseCsvMultiple(File f, File f1, File f2) {
		if (!f.exists() || !f1.exists() || !f2.exists()) {
			Logger.error("Error parsing features files: " + f.getAbsolutePath() + " " + f1.getAbsolutePath() + " "
					+ f2.getAbsolutePath());
			return;
		}
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";

		try {

			br = new BufferedReader(new FileReader(f));
			int index = 0;
			while ((line = br.readLine()) != null) {

				String[] fields = line.split(cvsSplitBy);

				if (fields.length > 0) {
					index++;
					Logger.warn("parseCsvMultiple read new line " + index);
					String area = fields[0];
					String testName = fields[1];
					String sectionName = fields[2];
					String slideName = fields[3];
					String groupName = fields[4];
					String personalJudgment = fields[5];
					String bluePrintSection = fields[6];

					String description = groupName;
					if (fields.length == 8) {
						description = fields[7];
					} else {
						description = groupName;
					}

					area = area.trim();
					if (StringUtil.isEmpty(area)) {
						Logger.warn("Skip area ");
						continue;
					}
					FeatureArea a = FeatureArea.findByName(area);
					if (a == null) {
						a = new FeatureArea();
						a.setName(area);
						a.setAreaDescription(area);
						a.save();
					}

					Boolean pj = false;
					if (!StringUtil.isNil(personalJudgment) && personalJudgment.equalsIgnoreCase("yes"))
						pj = true;
					Feature feature = new Feature();
					feature.setArea(a);
					feature.setFeatureDescription(description);
					feature.setName(groupName);
					feature.setBluePrintSection(bluePrintSection);
					feature.setGroupName(groupName);
					feature.setPersonalJudgment(pj);
					feature.setSectionName(sectionName);
					feature.setSlideName(slideName);
					feature.setTestName(testName);
					feature.setFeatureType(FeatureType.MULTIPLE);
					feature.save();
					FeatureMultipleOptionsElements res = this.parseOptionsCsvMultiple(f1, f2, index, feature);

					feature.setOptions(res.options);
					feature.setRules(res.roules);
					feature.save();
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static class FeatureOptionMultiple {
		public String groupName;
		public String label;
		public String partition;
		public Integer position;

		public FeatureOptionMultiple() {
			// TODO Auto-generated constructor stub
		}

		

		public FeatureOptionMultiple(String groupName, String label, String partition, Integer position) {
			super();
			this.groupName = groupName;
			this.label = label;
			this.partition = partition;
			this.position = position;
		}



		public static List<FeatureChoiche> getChoiches(List<FeatureOptionMultiple> rules, Feature f) {
			List<FeatureChoiche> ret = new LinkedList<FeatureChoiche>();
			for(FeatureOptionMultiple r : rules) {
				FeatureChoiche fc = new FeatureChoiche();
				fc.setFeature(f);
				fc.setGroupName(r.groupName);
				fc.setLabel(r.label);
				fc.setPartition(r.partition);
				fc.setPosition(r.position);
				fc.save();
				ret.add(fc);
			}
			return ret;
		}

	}

	public static class FeatureMultipleOptionsElements {
		List<FeatureOption> options;
		List<FeatureOptionRules> roules;
		public FeatureMultipleOptionsElements() {
			options = new LinkedList<FeatureOption>();
			roules = new LinkedList<FeatureOptionRules>();
		}
	}

	private FeatureMultipleOptionsElements parseOptionsCsvMultiple(File f, File f1, int index, Feature feature) {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";
		FeatureMultipleOptionsElements ret = new FeatureMultipleOptionsElements();
		List<FeatureOptionMultiple> rules = new LinkedList<FeatureOptionMultiple>();
		try {

			Map<String, List<FeatureOptionMultiple>> partitions = new HashMap<String, List<FeatureOptionMultiple>>();

			br = new BufferedReader(new FileReader(f));
			int fileindex = 0;
			while ((line = br.readLine()) != null) {
				fileindex++;
				Logger.warn("parseOptionsCsvMultiple1 newline " + fileindex);
				String[] fields = line.split(cvsSplitBy);

				if (fields.length > 0) {
					String numFeature = fields[0];
					String groupName = fields[1];
					String label = fields[2];
					String partition = fields[3];
					try {
						int n = Integer.parseInt(numFeature);
						if (n == index) {
							FeatureOptionMultiple r = new FeatureOptionMultiple(groupName, label, partition, null);
							rules.add(r);

						}
					} catch (Exception e) {
						Logger.error(e, "Error parsing number " + numFeature);
					}
				}

			}
			if (!rules.isEmpty()) {
				Collections.sort(rules, new Comparator<FeatureOptionMultiple>() {
					@Override
					public int compare(FeatureOptionMultiple o1, FeatureOptionMultiple o2) {
						if (o1 == null && o2 == null)
							return 0;
						if (o1 == null)
							return 1;
						if (o2 == null)
							return -1;
						return o1.groupName.compareTo(o2.groupName);
					}

				});
				FeatureOptionMultiple fom = null;
				for (int i = 0; i < rules.size(); i++) {
					fom = rules.get(i);
					fom.position = i;
					if (partitions.get(fom.partition) == null)
						partitions.put(fom.partition, new LinkedList<FeatureOptionMultiple>());
					partitions.get(fom.partition).add(fom);
				}
				feature.setChoiches(FeatureOptionMultiple.getChoiches(rules, feature));
				feature.save();
				ret = parseOptionsCsvMultiple(f1, index, partitions, rules, feature);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					Logger.warn("parseOptionsCsvMultiple br close");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	private FeatureMultipleOptionsElements parseOptionsCsvMultiple(File f1, int index,
			Map<String, List<FeatureOptionMultiple>> partitions, List<FeatureOptionMultiple> rules, Feature feature) {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";
		FeatureMultipleOptionsElements ret = new FeatureMultipleOptionsElements();
		int filterDimension = rules.size();
		try {
			br = new BufferedReader(new FileReader(f1));
			int lineindex = 0;
			while ((line = br.readLine()) != null) {
				lineindex++;
				Logger.warn("parseOptionsCsvMultiple read new line " + lineindex);
				String[] fields = line.split(cvsSplitBy);

				if (fields.length > 0) {
					String numFeature = fields[0];
					String partition = fields[1];
					String clusters = fields[2];
					String clustersName = "";
					if(fields.length > 3)
						clustersName = fields[3];
					try {
						int n = Integer.parseInt(numFeature);
						if (n == index) {
							List<FeatureOptionMultiple> p = partitions.get(partition);
							if (clusters.trim().equals("*")) {
								int[] arr = new int[p.size()];
								List<int[]> combinations = new LinkedList<int[]>();
								generateAllBinaryStrings(p.size(), arr, 0, combinations, 0, p.size());
								//List<int[]> filters = generateFilters(filterDimension, p, combinations);
								//options.put(label, value);
								String label = "";
								String sep = "";
								for(int[] comb : combinations) {
									String str = "";
									for(int a : comb)
										str += a;
									Logger.warn("feature* " + feature.getName() + " combination " + str);
									List<int[]> filters = new LinkedList<int[]>();
									int[] filter = createBaseFilter(filterDimension);
									label = "";
									sep = "";
									for(int i = 0; i < comb.length; i++) {
										if(comb[i] == 1) {
											filter[p.get(i).position] = 1;
											label += sep + p.get(i).label;
											sep = ", ";
										}
									}
									filters.add(filter);
									FeatureOptionRules e = new FeatureOptionRules();
									e.setIntFilter(filters);
									if(label.isEmpty())
										label = "no answer";
									e.setLabel(label);
									e.setFeature(feature);
									e.save();
									//ret.options.put(label, e.getUuid());
									FeatureOption fo = new FeatureOption();
									fo.optionKey = label;
									fo.optionValue = e.getUuid();
									fo.setFeature(feature);
									fo.save();
									ret.options.add(fo);
									ret.roules.add(e);
								}
							} else {
								String[] splitted = clusters.split(",");
								
								int base = 1;
								Logger.warn("feature " + feature.getName() + " splitted.length " + splitted.length);
								for (int j = 0; j <= splitted.length; j++) {
									int last = -1;
									if(j < splitted.length)
										last = Integer.parseInt(splitted[j]);
									else
										last = p.size();
									int[] arr = new int[p.size()];
									List<int[]> combinations = new LinkedList<int[]>();
									generateAllBinaryStrings(p.size(), arr, 0, combinations, base, last);
									String label = "";
									if(base == last)
										label = base + " " + clustersName;
									else
										label = base + ".."+ last + " " + clustersName;
									List<int[]> filters = new LinkedList<int[]>();
									for(int[] comb : combinations) {
										int[] filter = createBaseFilter(filterDimension);
										
										for(int i = 0; i < comb.length; i++) {
											if(comb[i] == 1) {
												filter[p.get(i).position] = 1;
											}
										}
										filters.add(filter);
									}
									Logger.warn("feature " + feature.getName() + " combination " + filters);
									FeatureOptionRules e = new FeatureOptionRules();
									e.setIntFilter(filters);
									e.setLabel(label);
									e.setFeature(feature);
									e.save();
									FeatureOption fo = new FeatureOption();
									fo.optionKey = label;
									fo.optionValue = e.getUuid();
									fo.setFeature(feature);
									fo.save();
									ret.options.add(fo);
									//ret.options.put(label, e.getUuid());
									ret.roules.add(e);
									base = last+1;
								}
							}
						}
					} catch (Exception e) {
						Logger.error(e, "Error parsing number " + numFeature);
					}
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					Logger.warn("parseOptionsCsvMultiple br close");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	private int[] createBaseFilter(int filterDimension) {
		int[] ret = new int[filterDimension];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = 0;
		}
		return ret;
	}

	static void generateAllBinaryStrings(int n, int arr[], int i, List<int[]> ret, int minNum, int maxNum) {
		if (i == n) {
			int[] arr1 = new int[n];
			int elNum = 0;
			for (int j = 0; j < n; j++)  
		    { 
		        arr1[j] = arr[j];
		        if(arr1[j] == 1)
		        	elNum++;
		    }
			if(elNum >= minNum && elNum <= maxNum)
				ret.add(arr1);
			return;
		}
		arr[i] = 0;
		generateAllBinaryStrings(n, arr, i + 1, ret, minNum, maxNum);
		arr[i] = 1;
		generateAllBinaryStrings(n, arr, i + 1, ret, minNum, maxNum);
	}
	
	public static class GrammarObject {

		public String uuid;
		public String grammarName;
		public String grammarSignHubSeries;
		public Integer grammarSignHubSeriesNumber;
		public String grammarOtherSignHubSeries;
		public String grammarEditorialInfo;
		public String grammarCopyrightInfo;
		public String grammarISBNInfo;
		public String grammarBibliographicalReference;
		public String creationDate;
		public String revisionDate;
		public String grammarStatus;
		public Boolean isDeleted;
		public List<GrammarPartObject> parts;
		// public List<String> parts;
		public String author;
		public List<String> editors;
		public List<String> contentProviders;
		public String status;
		public boolean htmlAvailable;
		public boolean pdfAvailable;
		public String frontMatter;

		public GrammarObject(Grammar g) {
			this.uuid = g.getUuid();
			this.grammarName = g.getGrammarName();
			this.grammarBibliographicalReference = g.getGrammarBibliographicalReference();
			this.grammarCopyrightInfo = g.getGrammarCopyrightInfo();
			this.grammarEditorialInfo = g.getGrammarEditorialInfo();
			this.grammarISBNInfo = g.getGrammarISBNInfo();
			this.grammarOtherSignHubSeries = g.getGrammarOtherSignHubSeries();
			this.grammarSignHubSeries = g.getGrammarSignHubSeries();
			this.grammarSignHubSeriesNumber = g.getGrammarSignHubSeriesNumber();
			this.creationDate = StringUtil.date(g.getCreationDate(), g.CREATION_DATE_FORMAT);
			this.revisionDate = StringUtil.date(g.getRevisionDate(), g.REVISION_DATE_FORMAT);
			this.grammarStatus = g.getGrammarStatus().name();
			this.isDeleted = g.getDeleted();
			this.parts = buildListGrammarPartsObjectFromPartList(g, g.getParts());
			this.status = g.getGrammarStatus().name();
			this.htmlAvailable = StringUtil.isNil(g.getHtmlPath()) ? false : true;
			this.pdfAvailable = StringUtil.isNil(g.getPdfPath()) ? false : true;
		}

		public GrammarObject() {
			// TODO Auto-generated constructor stub
		}

	}
	
	public static class GrammarPartObject {

		protected Integer grammarPartOrder;
		public Float completePartOrder;
		public String uuid;
		public Boolean isDeleted;
		public String elementNumber;
		public String name;
		public String status;
		public String type;
		public String html;
		public String parent;
		public String creationDate;
		public String revisionDate;
		public List<GrammarPartObject> parts;
		public List<FeatureWrapper> features;
		// public List<String> parts;
		public String author;
		public List<String> editors;
		public List<String> contentProviders;
		public Map<String, String> options;
		private Float completePartOrderNow;

		public GrammarPartObject(GrammarPart part) {
			this.uuid = part.getUuid();
			this.isDeleted = part.getDeleted();
			this.elementNumber = part.getElementNumber();
			this.grammarPartOrder = part.getGrammarPartOrder();
			this.completePartOrder = part.getCompleteOrder();
			this.completePartOrderNow = part.getCompleteOrderNow();
			this.name = part.getGrammarPartName();
			this.status = part.getGrammarPartStatus().name();
			this.type = part.getGrammarPartTYpe().name();
			this.parent = null;
			if (part.getParent() != null)
				this.parent = part.getParent().getUuid();
			this.creationDate = StringUtil.date(part.getCreationDate(), part.CREATION_DATE_FORMAT);
			this.revisionDate = StringUtil.date(part.getRevisionDate(), part.REVISION_DATE_FORMAT);
			this.parts = buildListGrammarPartsObjectFromPartList(part.getGrammar(),part.getParts());
			this.features = buildListFeatureWrapperFromFeatureList(part.getFeatures());
		}

		private List<FeatureWrapper> buildListFeatureWrapperFromFeatureList(List<Feature> features) {
			List<FeatureWrapper> ret = new LinkedList<FeatureWrapper>();
			if(features != null) {
				for(Feature f : features) {
					ret.add(new FeatureWrapper(f));
				}
			}
			return ret;
		}

	}
	
	private static List<GrammarPartObject> buildListGrammarPartsObjectFromPartList(Grammar g, List<GrammarPart> parts) {
		List<GrammarPartObject> ret = new LinkedList<GrammarPartObject>();

		for (GrammarPart part : parts) {
			if(part.getGrammar() == null) {
				part.setGrammar(g);
				part.save();
			}
			ret.add(new GrammarPartObject(part));
		}
		Collections.sort(ret, new Comparator<GrammarPartObject>() {

			@Override
			public int compare(GrammarPartObject o1, GrammarPartObject o2) {
				return o1.grammarPartOrder.compareTo(o2.grammarPartOrder);
			}
		});
		return ret;
	}
	
	public GrammarObject getFeaturesTree() {
		Grammar g = Grammar.findSystemToc();
		if(g==null)
			return null;
		GrammarObject grammarObj = new GrammarObject(g);
		return grammarObj;
	}


}
