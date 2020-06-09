package controllers;

import core.services.ReportService;
import core.services.TestService;
import core.services.TopicService;
import core.services.UserService;
import models.Grammar;
import models.GrammarPart;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.services.ApiService.AuthorizedHttpVerb;
import core.services.ContactService;
import core.services.FeatureService;
import play.Logger;
import play.Play;
import utils.StringUtil;

public class UserNotLoggedController extends BaseController {

	public static void index() {
		String ret = "";
		ret += "remoteAddress " + request.remoteAddress + "\n";
		ret += "url " + request.url + "\n";
		ret += request.headers.get("host").value() + request.url+ "\n";
		ret += "getBase " + request.getBase() + "\n";
		ret += "path " + request.path + "\n";
		ret += "action " + request.action + "\n";
		ret += "pool " + Play.configuration.getProperty("play.pool");
		renderText("BACK-END STARTED " + ret);
	}

	public static void login() {
		String jsonResponse = "{}";
		try {
			addOptions(AuthorizedHttpVerb.POST);
			if (isPost())
				jsonResponse = userService.login();
			else if (isOptions()) {
				System.out.println("options...");
				renderJSON(new HashMap<String, String>());
			}
			renderJSON(jsonResponse);
		} catch (Throwable thr) {
			Logger.error(thr, "Error...");
			jsonResponse = apiService.buildGenericErrorResponse();
		}
		renderJSON(jsonResponse);
	}

	public static void loginRecovery() {
		String jsonResponse = "{}";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS);
			if (isPost())
				jsonResponse = userService.loginRecovery();
			else if (isOptions()) {
				/* continue*/
			}
			renderJSON(jsonResponse);
		} catch (Throwable thr) {
			Logger.error(thr, "Error...");
			jsonResponse = apiService.buildGenericErrorResponse();
		}
		renderJSON(jsonResponse);
	}

	public static void passwordReset() {
		String jsonResponse = "{}";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS);
			if (isPost())
				jsonResponse = userService.passwordReset();
			else if (isOptions()) {
				/* continue */
			}
			renderJSON(jsonResponse);
		} catch (Throwable thr) {
			Logger.error(thr, "Error...");
			jsonResponse = apiService.buildGenericErrorResponse();
		}
		renderJSON(jsonResponse);
	}
	
	public static void createTopics(String path, String filename){
		Logger.info("createTopics..." + path + " " + filename);
		TopicService.instance().createTopics(path, filename);
		
		Logger.info("Topics created");
	}
	
	/*public static void createFeatures(String path, String filename, String filename1){
		Logger.info("createFeatures..." + path + " " + filename);
		FeatureService.instance().createFeatures(path, filename, filename1, true);
		
		Logger.info("Topics created");
	}*/
	

	
	
	
	public static void getUploadedMediaList(String reportUuid) {
		if(reportUuid == null)
			renderJSON(apiService.buildGenericErrorResponse());
		String res = ReportService.instance().getUploadedMediaList(reportUuid);
		if(res == null)
			renderJSON(apiService.buildGenericErrorResponse());
		renderJSON(res);
	}
	
	public static void getLanguages() {
		renderJSON(TestService.instance().getLanguages());
	}
	
	public static void getSignLanguages() {
		renderJSON(TestService.instance().getSignLanguages());
	}
	
	public static void getSignLanguage(String id, String code) {
		renderJSON(TestService.instance().getSignLanguage(id, code));
	}
	
	public static void cleanGrammars() {
		/*List<Grammar> list = Grammar.findAll();
		for(Grammar g : list) {
			for(GrammarPart gp : g.getParts()) {
				deleteGrammarPart(gp);
			}
			g.delete();
		}*/
	}
	
	public static void cleanGrammarParts() {
		/*List<GrammarPart> list = GrammarPart.findAll();
		for(GrammarPart g : list) {
			if(g.getParent() != null)
				continue;
			for(GrammarPart gp : g.getParts()) {
				deleteGrammarPart(gp);
			}
			g.delete();
		}*/
	}
	
	private static void deleteGrammarPart(GrammarPart gp) {
		for(GrammarPart gpp : gp.getParts()) {
			deleteGrammarPart(gpp);
		}
		gp.delete();
	}
	
	public static void getPublicGrammarHtml(String grammarId) {
		if(StringUtil.isNil(grammarId))
			renderJSON("ERROR");
		Grammar g = Grammar.findById(grammarId);
		if(StringUtil.isNil(g.getHtmlPath()))
			renderJSON("NOT PUBLISHED");
		File f = new File(g.getHtmlPath());
		if(!f.exists())
			renderJSON("FILE NOT EXIST");
		renderBinary(f);
	}
	
	public static void getPublicGrammarPdf(String grammarId) {
		if(StringUtil.isNil(grammarId))
			renderJSON("ERROR");
		Grammar g = Grammar.findById(grammarId);
		if(StringUtil.isNil(g.getPdfPath()))
			renderJSON("NOT PUBLISHED");
		File f = new File(g.getPdfPath());
		if(!f.exists())
			renderJSON("FILE NOT EXIST");
		renderBinary(f);
	}
	
	public static void requestRegistration() {
		System.out.println("request Registration");
		/*Map<String, String> res = new HashMap<String, String>();
				res.put("result", "OK");*/
		String res = ContactService.instance().registerRegistrationRequest();
		renderJSON(res);
	}
	
	public static void createFeatures() {
		FeatureService.instance().createFeatures("/tmp", "single-choice-features.csv", "single-choice-features-values.csv", true);
		//FeatureService.instance().parseCsv("/tmp", "single-choice-features.csv", "single-choice-features-values.csv");
	}
	
	public static void createMultipleFeatures() {
		FeatureService.instance().createFeaturesMultiple("/tmp", "multiple-choice-features.csv", "multiple-choice-features-choices.csv", "multiple-choice-features-clusters.csv", true);
	}

}
