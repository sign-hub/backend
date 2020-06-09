package controllers;

import controllers.eSecurePlay1.Auth.Check;
import core.services.ApiService.AuthorizedHttpVerb;
import core.services.TestService;
import models.Test.TestType;
import models.User.Authorizations;
import play.Logger;
import play.mvc.Util;
import utils.StringUtil;

@Check({ Authorizations.TEST_MANAGEMENT, Authorizations.MEDIA_MANAGEMENT })
public class TestController extends ApiLoggedController {

	private static TestService testService = TestService.instance();

	public static void testingtoolTest(String testId, Boolean complete, Boolean isPlay, String orderby, String ordertype) {
		if(StringUtil.isNil(orderby)){
			orderby = "testName";
			ordertype = "ASC";
		}
		test(testId, complete, TestType.TESTINGTOOL, isPlay, orderby, ordertype);
	}
	
	public static void atlasTest(String testId, Boolean complete, Boolean isPlay, String orderby, String ordertype) {
		test(testId, complete, TestType.ATLAS, isPlay, orderby, ordertype);
	}
	
	@Util
	public static void test(String testId, Boolean complete, TestType type, Boolean isPlay, String orderby, String ordertype) {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(testId)) {
					jsonResponse = testService.testList(type, orderby, ordertype, complete);
				} else {
					Logger.info("Get test called..." + testId + " " + complete + " " + isPlay);
					jsonResponse = testService.getTest(testId, complete, isPlay);
				}
			} else if (isPost()) {
				if (StringUtil.isNil(testId)) {
					jsonResponse = testService.createTest(type);
				} else {
					jsonResponse = testService.updateTest(testId);
				}
			} else if (isDelete()) {
				jsonResponse = testService.deleteTest(testId);
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

	public static void cloneTest() {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			request.format = "json";
			if (isPost()) {
				jsonResponse = testService.cloneTest();
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
	
	public static void sortQuestions() {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST);
			request.format = "json";
			if (isPost()) {
				jsonResponse = testService.sortQuestions();
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
	
	public static void sortSlides() {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST);
			request.format = "json";
			if (isPost()) {
				jsonResponse = testService.sortSlides();
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
	
	public static void atlasManagement() {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST);
			request.format = "json";
			if (isPost()) {
				jsonResponse = testService.atlasManagement();
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