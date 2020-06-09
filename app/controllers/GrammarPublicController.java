package controllers;

import core.services.ApiService.AuthorizedHttpVerb;
import core.services.GrammarService;
import core.services.TestService;
import models.Test.TestType;
import models.User.Authorizations;
import play.Logger;
import play.mvc.Util;
import utils.StringUtil;

public class GrammarPublicController extends BaseController {

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
					jsonResponse = grammarService.getGrammar(grammarId, isPlay, isComplete);
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
					jsonResponse = grammarService.getGrammarPart(grammarPartId, isPlay);
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

}