package controllers;

import java.io.File;

import controllers.eSecurePlay1.Auth.Check;
import core.services.ApiService.AuthorizedHttpVerb;
import core.services.ReportService;
import core.services.TestService;
import models.User.Authorizations;
import play.Logger;
import utils.StringUtil;

@Check({ Authorizations.TEST_MANAGEMENT, Authorizations.MEDIA_MANAGEMENT })
public class ReportController extends ApiLoggedController {

	private static ReportService reportService = ReportService.instance();

	public static void report(String reportId, String type) {
		Logger.info("type: " + type);
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			request.format = "json";
			if (isGet()) {
				if (StringUtil.isNil(reportId)) {
					jsonResponse = reportService.reportList(type);
				} else {
					jsonResponse = apiService.buildGenericErrorResponse();
					//jsonResponse = reportService.getReport(reportId);
				}
			} else if (isPost()) {
				if (StringUtil.isNil(reportId)) {
					jsonResponse = reportService.createReport();
				} else {
					jsonResponse = apiService.buildGenericErrorResponse();
				}
			} else if (isDelete()) {
				jsonResponse = apiService.buildGenericErrorResponse();
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
	
	/*public static void reportCsvFile(String reportId) {
		File f = ReportService.getCsvFile(reportId);
		if(f!=null){
			renderBinary(f);
		} else {
			renderJSON(null);
		}
	}*/
	
	public static void reportCsv(String reportId) {
		String url = ReportService.getCsvPublic(reportId);
		if(StringUtil.isEmpty(url))
			renderJSON("{}");
		String ret ="{\"url\":\""+ url +"\"}";
		renderJSON(ret);
	}
	
	public static void reportJson(String reportId) {
		String json = ReportService.getJson(reportId);
		if(StringUtil.isEmpty(json))
			renderJSON("{}");
		renderJSON(json);
	}
	
	
	public static void generateReport(String testId) {
		if (isOptions()) {
			renderJSON("");
			return;
		}
		String json = ReportService.generateReport(testId);
		if(StringUtil.isEmpty(json))
			renderJSON("{}");
		renderJSON(json);
	}
	
	public static void checkReport(String reportId) {
		if (isOptions()) {
			renderJSON("");
			return;
		}
		String json = ReportService.checkReport(reportId);
		if(StringUtil.isEmpty(json))
			renderJSON("{}");
		renderJSON(json);
	}
	
	public static void downloadReport(String reportId) {
		if (isOptions()) {
			renderJSON("");
			return;
		}
		String url = ReportService.downloadPublicReportPublic(reportId);
		if(StringUtil.isEmpty(url))
			renderJSON("{}");
		String ret ="{\"url\":\""+ url +"\"}";
		renderJSON(ret);
	}
	
	public static void generateFeatures(String testId) {
		if (isOptions()) {
			renderJSON("");
			return;
		}
		String json = ReportService.generateFeatures(testId);
		if(StringUtil.isEmpty(json))
			renderJSON("{}");
		renderJSON(json);
	}
	
	public static void checkFeatures(String workerId) {
		if (isOptions()) {
			renderJSON("");
			return;
		}
		String json = ReportService.checkFeatures(workerId);
		if(StringUtil.isEmpty(json))
			renderJSON("{}");
		renderJSON(json);
	}

}