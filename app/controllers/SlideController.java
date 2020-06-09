package controllers;

import controllers.eSecurePlay1.Auth.Check;
import core.services.ApiService.AuthorizedHttpVerb;
import core.services.SlideService;
import models.User.Authorizations;
import play.Logger;
import utils.StringUtil;

@Check({ Authorizations.TEST_MANAGEMENT, Authorizations.MEDIA_MANAGEMENT })
public class SlideController extends ApiLoggedController {

	private static SlideService slideService = SlideService.instance();

	public static void slide(String slideId, String questionId) {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(slideId)) {
					jsonResponse = slideService.slideList(questionId);
				} else {
					jsonResponse = slideService.getSlide(slideId);
				}
			} else if (isPost()) {
				if (StringUtil.isNil(slideId)) {
					jsonResponse = slideService.createSlide();
				} else {
					jsonResponse = slideService.slideUpdate(slideId);
				}
			} else if (isDelete()) {
				jsonResponse = slideService.deleteSlide(slideId);
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

	public static void cloneSlide() {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS);
			request.format = "json";
			if (isPost()) {
				jsonResponse = slideService.cloneSlide();
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