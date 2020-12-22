package controllers;

import core.services.FeatureService;
import core.services.GrammarService;
import core.services.TestService;
import models.Report;
import core.services.ApiService.AuthorizedHttpVerb;
import play.Logger;
import play.mvc.results.RenderText;
import utils.StringUtil;

public class SignHubPublicController extends BaseController {
	
	private static GrammarService grammarService = GrammarService.instance();
	
	public static void grammar(String grammarId, Boolean isPlay, Boolean isComplete, String orderby, String ordertype) {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(grammarId)) {
					jsonResponse = grammarService.grammarList(orderby, ordertype);
				} else {
					jsonResponse = grammarService.getGrammar(grammarId, isPlay, isComplete, true);
				}
			} else if (isOptions()) {
				// continue
			} else {
				jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
			}
		} catch (Throwable thr) {
			Logger.error(thr, "Error...");
			jsonResponse = apiService.buildGenericErrorResponse();
		}

		renderJSON(jsonResponse);
	}
	
	
	public static void grammarPart(String grammarPartId, Boolean isPlay) {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(grammarPartId)) {
					jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
				} else {
					jsonResponse = grammarService.getGrammarPart(grammarPartId, true);
				}
			} else if (isOptions()) {
				// continue
			} else {
				jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
			}
		} catch (Throwable thr) {
			Logger.error(thr, "Error...");
			jsonResponse = apiService.buildGenericErrorResponse();
		}

		renderJSON(jsonResponse);
	}
	
	public static void grammarSearch(String grammarId, String q) {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(grammarId)) {
					jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
				} else {
					jsonResponse = grammarService.search(grammarId, q);
				}
			} else if (isPost()) {
				jsonResponse = grammarService.searchPost();
			} else if (isOptions()) {
				// continue
			} else {
				jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
			}
		} catch (Throwable thr) {
			Logger.error(thr, "Error...");
			jsonResponse = apiService.buildGenericErrorResponse();
		}

		renderJSON(jsonResponse);
	}

	public static void getFeatures() {
		renderJSON(FeatureService.instance().getFeatures());
	}
	public static void getFeaturesMap() {
		renderJSON(FeatureService.instance().getFeaturesMap());
	}
	
	public static void searchByFeatures() {
		Logger.warn("searchByFeatures");
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.POST);
			request.format = "json";
			if (isPost()) {
					jsonResponse = FeatureService.instance().searchByFeatures();
			} else if (isOptions()) {
				// continue
			} else {
				jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
			}
		} catch (Throwable thr) {
			Logger.error(thr, "Error...");
			jsonResponse = apiService.buildGenericErrorResponse();
		}

		renderJSON(jsonResponse);
	}
	
	public static void getFeaturesMapByLanguage(String code, String id) {
		renderJSON(FeatureService.instance().getFeaturesMapByLanguage(code, id));
	}
	
	public static void extractFeatures(String reportUuid) {
		if(StringUtil.isNil(reportUuid))
			renderText("reportUuid null");
		Report r = Report.findById(reportUuid);
		if(r==null)
			renderText("report null");
		//FeatureService.instance().extractFeaturesFromReport(r);
		renderJSON(FeatureService.instance().extractFeaturesFromReport(r));
		//renderText("OK");
	}
	
	public static void extractSignLanguages() {
		TestService.instance().extractSignLanguages("/tmp", "sign-language-list.csv");
		renderText("OK");
	}
	
	public static void getFeaturesTree() {
		renderJSON(FeatureService.instance().getFeaturesTree());
	}
}




