package controllers;

import controllers.eSecurePlay1.Auth.Check;
import core.services.ApiService.AuthorizedHttpVerb;
import core.services.GrammarService;
import core.services.TestService;
import models.Test.TestType;
import models.User.Authorizations;
import play.Logger;
import play.mvc.Util;
import utils.StringUtil;

@Check({ Authorizations.TEST_MANAGEMENT, Authorizations.MEDIA_MANAGEMENT })
public class GrammarController extends ApiLoggedController {

	private static GrammarService grammarService = GrammarService.instance();
	
	public static void grammar(String grammarId, Boolean isPlay, Boolean isComplete, String orderby, String ordertype) {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(grammarId)) {
					jsonResponse = grammarService.grammarList(orderby, ordertype);
				} else {
					jsonResponse = grammarService.getGrammar(grammarId, isPlay, isComplete);
				}
			} else if (isPost()) {
				if (StringUtil.isNil(grammarId)) {
					//jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
					jsonResponse = grammarService.createGrammar();
				} else {
					jsonResponse = grammarService.updateGrammar(grammarId);
				}
			} else if (isDelete()) {
				jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
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
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(grammarPartId)) {
					jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
				} else {
					jsonResponse = grammarService.getGrammarPart(grammarPartId, isPlay);
				}
			} else if (isPost()) {
				if (StringUtil.isNil(grammarPartId)) {
					jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
				} else {
					jsonResponse = grammarService.updateGrammarPart(grammarPartId);
				}
			} else if (isDelete()) {
				jsonResponse = apiService.buildHttpVerbNotAllowedResponse();
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