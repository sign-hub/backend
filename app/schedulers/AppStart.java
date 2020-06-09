/**
 *
 * Original file name: AppStart.java
 * Created by luca 
 */
package schedulers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.services.ApiService;
import core.services.GrammarService;
import core.services.TestService;
import core.services.TestService.Language;
import models.Feature;
import models.FeatureOption;
import models.FeatureValue;
import models.Grammar;
import models.Grammar.GrammarStatus;
import models.GrammarPart;
import models.Option;
import models.GrammarPart.GrammarPartStatus;
import models.GrammarPart.GrammarPartType;
import models.Question;
import models.SignLanguage;
import models.Slide;
import models.SlideContentComponent;
import models.Test;
import models.User;
import models.User.Authorizations;
import models.User.Roles;
import net.eclettica.esecure.manager.EsAccountManager;
import net.eclettica.esecure.models.EsAuth;
import net.eclettica.esecure.models.EsRole;
import play.Logger;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPABase;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import utils.cfg.CfgUtil;
import utils.cfg.ConfigManager;
import utils.cfg.PlayConfigAdapter;

/**
 * 
 * @author luca
 * 
 */

@OnApplicationStart
public class AppStart extends Job {

	public static HashMap<String, Integer> map;
	
	public void doJob() {
		ConfigManager.instance().setAdapter(PlayConfigAdapter.instance());
		EsAccountManager esdao = configureESecurePlay1();
		if (esdao == null) {
			throw new RuntimeException();
		}
		initializeAuthsAndRoles(esdao);
		checkMediaDirectory();
		checkDirectories();
		//fixQuestions();
		//createGrammar();
		//AppStart_prova.createGrammar();
		//fixSlidesComponents();
		//GrammarService.instance().grammarPdfCreator("UUID-GRMM-1b66b300-7e6e-4a22-97e3-be6946132a34");
		//createLanguages();
		//createFakeFeatureValues();
		List<Grammar> gl = Grammar.findAll();
		for(Grammar g : gl) {
			//GrammarService.instance().getGrammar(g.getUuid(), true, true);
			//GrammarService.instance().rebuildCompleteOrderNow(g);
		}
		/*for(Grammar g : gl) {
			GrammarService.instance().fixGrammarParts(g);
		}
		for(Grammar g : gl) {
			GrammarService.instance().fixGrammarParts1(g);
		}
		for(Grammar g : gl) {
			GrammarService.instance().fixIntonation(g);
			GrammarService.instance().fixTense(g);
		}*/
		GrammarService.instance().fixNames();
		boolean found = false;
		for(Grammar g : gl) {
			if(g.getGrammarStatus().equals(GrammarStatus.SYSTEMTOC)) {
				found = true;
				break;
			}
		}
		if(!found) {
			Grammar g = AppStart_prova.createGrammar();
			g.setGrammarStatus(GrammarStatus.SYSTEMTOC);
			g.setGrammarName("GRAMMAR SYSTEM TOC");
			g.save();
		}
	}

	

	private void fixQuestions() {
		List<Test> testList = Test.all().fetch();
		for(Test t : testList){
			List<Question> questionList = t.getQuestions();
			for(Question q : questionList){
				q.setTest(t);
				q.save();
			}
		}
	}

	private void checkMediaDirectory() {
		ApiService.instance().createMediaDirectory();
	}
	
	private void checkDirectories() {
		checkDirectory("grammar.basepath", "/usr/cini/grammar-repository/grammar");
	}
	private void checkDirectory(String configKey, String defaultVal) {
		String path = CfgUtil.getString(configKey, defaultVal);
		if(path == null)
			return;
		File destDir = new File(path);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
	}

	private EsAccountManager configureESecurePlay1() {
		String clazz = CfgUtil.getString("esecure.dao.class", "net.eclettica.esecure.dao.EsDao");

		map = new HashMap<String, Integer>();
		map.put("authconfig.minpassword", 6);
		map.put("authconfig.session", 172800);
		//map.put("authconfig.session", 5);
		map.put("authconfig.refreshSessionOnVerify", 0);

		try {
			Class c = Class.forName(clazz);
			EsAccountManager esdao = (EsAccountManager) c.newInstance();

			net.eclettica.esecure.services.AuthService.instance().associateManager(esdao);
			net.eclettica.esecure.conf.AuthConfig.getInstance().configure(map);

			return esdao;
		} catch (Exception ex) {
			Logger.error(ex, "Startup error");
			return null;
		}
	}

	private void initializeAuthsAndRoles(EsAccountManager esdao) {
		List<EsAuth> adminAuths = new LinkedList<EsAuth>();
		List<EsAuth> contentProviderAuths = new LinkedList<EsAuth>();

		// for (Authorizations auth : Authorizations.values()) {
		// EsAuth esAuth = esdao.findAuthByName(auth.name());
		// if (esAuth != null) {
		// continue;
		// }
		// esdao.createAuth(auth.name(), auth.name());
		// esAuth = esdao.findAuthByName(auth.name());
		// if (esAuth == null) {
		// continue;
		// }
		// adminAuths.add(esAuth);
		// if (auth.equals(Authorizations.TEST_MANAGEMENT) ||
		// auth.equals(Authorizations.MEDIA_MANAGEMENT)
		// || auth.equals(Authorizations.REPORT_MANAGEMENT)) {
		// contentProviderAuths.add(esAuth);
		// }
		// }

		for (String auth : Authorizations.values()) {
			EsAuth esAuth = esdao.findAuthByName(auth);
			adminAuths.add(esAuth);
			if (esAuth != null) {
				continue;
			}
			esdao.createAuth(auth, auth);
			esAuth = esdao.findAuthByName(auth);
			if (esAuth == null) {
				continue;
			}
			
			if (auth.equals(Authorizations.TEST_MANAGEMENT) || auth.equals(Authorizations.MEDIA_MANAGEMENT)
					|| auth.equals(Authorizations.REPORT_MANAGEMENT)) {
				contentProviderAuths.add(esAuth);
			}
		}

		for (String role : Roles.values()) {
			EsRole esRole = esdao.findRole(role);
			if (esRole != null) {
				continue;
			}
			esdao.createRole(role, role);
		}

		setAuthsToRole(esdao, adminAuths, Roles.ADMIN, false);
		setAuthsToRole(esdao, contentProviderAuths, Roles.CONTENT_PROVIDER, false);
		Logger.info("AppStart - setAuthsToRole");
		for (String auth : Authorizations.values()) {
			EsAuth esAuth = esdao.findAuthByName(auth);
			if (esAuth == null) {
				continue;
			}
			if (auth.equals(Authorizations.TEST_MANAGEMENT) || auth.equals(Authorizations.MEDIA_MANAGEMENT)
					|| auth.equals(Authorizations.REPORT_MANAGEMENT)) {
				contentProviderAuths.add(esAuth);
			}
			
		}
		setAuthsToRole(esdao, contentProviderAuths, Roles.TESTING_EDITOR, false);
		setAuthsToRole(esdao, contentProviderAuths, Roles.TESTING_USER, false);
		
		
		setAuthsToRole(esdao, adminAuths, Roles.ATLAS_ADMINISTRATOR, false);
		setAuthsToRole(esdao, adminAuths, Roles.TESTING_ADMINISTRATOR, false);
		setAuthsToRole(esdao, adminAuths, Roles.GRAMMAR_ADMINISTRATOR, false);
		setAuthsToRole(esdao, adminAuths, Roles.STREAMING_ADMINISTRATOR, false);
		setAuthsToRole(esdao, contentProviderAuths, Roles.GRAMMAR_CONTENT_PROVIDER, false);
		setAuthsToRole(esdao, contentProviderAuths, Roles.GRAMMAR_ADMIN, false);
		
//		EsRole admin = null;
//		admin = esdao.findRole(Roles.ADMIN);
//		if (admin == null) {
//			throw new RuntimeException("ADMIN Role is null!!!");
//		}
//		for (EsAuth adminAuth : adminAuths) {
//			if (admin == null) {
//				
//			}
//			esdao.addAuthRoles(adminAuth, admin);
//			esdao.addRoleAuths(adminAuth, admin);
//		}

//		EsRole contentProvider = null;
//		for (EsAuth contentProviderAuth : contentProviderAuths) {
//			if (contentProvider == null) {
//				contentProvider = esdao.findRole(Roles.CONTENT_PROVIDER);
//				if (contentProvider == null) {
//					throw new RuntimeException("CONTENT_PROVIDER Role is null!!!");
//				}
//			}
//			esdao.addAuthRoles(contentProviderAuth, contentProvider);
//			esdao.addRoleAuths(contentProviderAuth, contentProvider);
//		}
	}

	private void setAuthsToRole(EsAccountManager esdao, List<EsAuth> adminAuths, String role, boolean b) {
		if(b)
			Logger.info("AppStart - setAuthsToRole " + role + " " + adminAuths);
		EsRole admin = null;
		admin = esdao.findRole(role);
		if (admin == null) {
			throw new RuntimeException("AppStart - setAuthsToRoleADMIN Role is null!!!");
		}
		for (EsAuth adminAuth : adminAuths) {
			if(esdao.findAuthByRole(admin.getName()) == null || !esdao.findAuthByRole(admin.getName()).contains(adminAuth)) {
				esdao.addRoleAuths(adminAuth, admin);
				if(b)
					Logger.info("AppStart - setAuthsToRole adding addRoleAuths" + adminAuth.getName() + " " + admin.getName());
			}
			if(esdao.findRoleByAuth(adminAuth.getName()) == null || !esdao.findRoleByAuth(adminAuth.getName()).contains(admin)) {
				esdao.addAuthRoles(adminAuth, admin);
				if(b)
					Logger.info("AppStart - setAuthsToRole adding addAuthRoles" + adminAuth.getName() + " " + admin.getName());
			}
			
			if(b)
				Logger.info("AppStart - setAuthsToRole added" + adminAuth.getName() + " " + admin.getName());
		}
	}
	
	private void fixSlidesComponents() {
		Logger.info("fixSlidesComponents START");
		List<Test> testList = Test.all().fetch();
		List<String> sublist = new ArrayList<String>();
		int i = 0;
		for(Test t : testList) {
			sublist.add(t.getUuid());
			if(sublist.size()==60) {
				i++;
				BatchTestFixer btf = new BatchTestFixer(sublist, i);
				btf.now();
				sublist = new ArrayList<String>();
			}
		}
		if(!sublist.isEmpty()) {
			i++;
			BatchTestFixer btf = new BatchTestFixer(sublist, i);
			btf.now();
		}
		//fixtSlidesComponentsTest(testList);
		Logger.info("fixSlidesComponents END");
	}

	private void createLanguages() {
		List<SignLanguage> langs = SignLanguage.all().fetch();
		if(langs == null || langs.isEmpty()) {
			List<Language> languages = TestService.instance().getLanguages();
			for(Language l : languages) {
				SignLanguage sl = new SignLanguage();
				sl.setCode(l.code);
				sl.setName(l.name);
				sl.save();
			}
		}
	}
	
	
	private void createFakeFeatureValues() {
		/*List<FeatureValue> list = FeatureValue.findAll();
		if(list != null && !list.isEmpty())
			return;*/
		SignLanguage italian = SignLanguage.findByCode("ise");
		//italian.setCoordinates("[42.6384261,12.674297]");
		//italian.save();
		SignLanguage french = SignLanguage.findByCode("fsl");
		//french.setCoordinates("[46.603354,1.8883335]");
		//french.save();
		SignLanguage german = SignLanguage.findByCode("deu");
		//german.setCoordinates("[51.0834196,10.4234469]");
		//german.save();
		SignLanguage frenchafrican = SignLanguage.findByCode("gus");
		
		List<Feature> features = Feature.all().fetch();
		
		for(Feature f: features) {
			//Map<String, String> opts = f.getOptions();
			List<FeatureOption> opts = f.getOptions();
			if(opts == null || opts.size()<=0)
				continue;
			List<FeatureValue> list = FeatureValue.findByFeature(f);
			if(list != null && !list.isEmpty())
				continue;
			FeatureValue fv1 = new FeatureValue(f, getRandomOption(opts), italian);
			fv1.save();
			fv1 = new FeatureValue(f, getRandomOption(opts), french);
			fv1.save();
			fv1 = new FeatureValue(f, getRandomOption(opts), german);
			fv1.save();
			fv1 = new FeatureValue(f, getRandomOption(opts), frenchafrican);
			fv1.save();
		}


//		Feature f1 = Feature.findById("UUID-FTR-05f8b0a5-154c-4977-8199-5d614b58a8c1"); //30 e 31
//		Feature f2 = Feature.findById("UUID-FTR-1f5090d9-cb59-4071-8e5c-42a8c326b789"); //10
//
//		FeatureValue fv1 = new FeatureValue(f1, "30", italian);
//		fv1.save();
//		fv1 = new FeatureValue(f1, "30", french);
//		fv1.save();
//		fv1 = new FeatureValue(f1, "31", german);
//		fv1.save();
//		fv1 = new FeatureValue(f1, "31", frenchafrican);
//		fv1.save();
//		
//		fv1 = new FeatureValue(f2, "10", italian);
//		fv1.save();
//		fv1 = new FeatureValue(f2, "10", french);
//		fv1.save();
//		fv1 = new FeatureValue(f2, "10", german);
//		fv1.save();
//		fv1 = new FeatureValue(f2, "11", frenchafrican);
//		fv1.save();
		
	}



	private String getRandomOption(List<FeatureOption> opts) {
		/*List<String> keys = new LinkedList<String>();
		for(FeatureOption fo : opts) {
			keys.add(fo.optionKey);
		}
		//keys.addAll(opts.keySet());
		if(keys.size() <=0)
			return null;*/
		int idx = 0;
		idx += Math.round(Math.random() * (opts.size()-1));
		return opts.get(idx).optionValue;
	}



	
}