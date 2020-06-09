package controllers;

import controllers.eSecurePlay1.Auth.Check;
import core.services.ApiService.AuthorizedHttpVerb;
import core.services.QuestionService;
import models.User.Authorizations;
import play.Logger;
import utils.StringUtil;

@Check({ Authorizations.TEST_MANAGEMENT, Authorizations.MEDIA_MANAGEMENT })
public class QuestionController extends ApiLoggedController {

	private static QuestionService questionService = QuestionService.instance();

	public static void question(String questionId, Boolean complete, String TestId) {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(questionId)) {
					jsonResponse = questionService.questionsList(TestId, false);
				} else {
					jsonResponse = questionService.getQuestion(questionId, complete);
				}
			} else if (isPost()) {
				if (StringUtil.isNil(questionId)) {
					jsonResponse = questionService.createQuestion();
				} else {
					jsonResponse = questionService.updateQuestion(questionId);
				}
			} else if (isDelete()) {
				jsonResponse = questionService.deleteQuestion(questionId);
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

	public static void importQuestion() {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS);
			request.format = "json";
			if (isPost()) {
				jsonResponse = questionService.importQuestion();
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