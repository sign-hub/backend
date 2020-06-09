package controllers;

import java.io.File;
import java.util.List;

import controllers.eSecurePlay1.Auth.Play_Session_Keys;
import core.services.ApiService;
import core.services.ApiService.AuthorizedHttpVerb;
import core.services.ContactService;
import core.services.MediaService;
import core.services.ReportService;
import core.services.TestService;
import core.services.UserService;
import models.Media;
import models.Question;
import models.Report;
import models.Slide;
import models.Test;
import models.Test.TestType;
import play.Logger;
import play.Play;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Header;
import play.mvc.results.RenderJson;
import play.mvc.Util;
import utils.StringUtil;

public class BaseController extends Controller {

	protected static UserService userService = UserService.instance();
	protected static ApiService apiService = ApiService.instance();

	// IMPORTANTE! Devo parsare per prima cosa la request (altrimenti il play!
	// "la consuma preventivamente...")!
	@Before(priority = 0)
	public static void getJSONFromRequest() {
		// addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS,
		// AuthorizedHttpVerb.GET,
		// AuthorizedHttpVerb.DELETE, AuthorizedHttpVerb.PUT);
		try {
			//Logger.warn("HTTP Method: " + request.method);
			String action = request.action;
			//Logger.warn("Called EndPoint: " + request.path);

			// se sto caricando un media non parso la request altrimenti il
			// play! nel MediaController
			// non mi valorizza il parametro file e perdo tutte le info relative
			// al media
			if ((action.equals("MediaController.media") && isPost())
					|| (action.equals("MediaController.testingtoolMedia") && isPost())
					|| (action.equals("MediaController.atlasMedia") && isPost())
					|| (action.equals("MediaController.grammarMedia") && isPost())
					|| (action.equals("MediaController.retrieve") && isGet())) {
				return;
			} else {
				String json = apiService.getJsonFromRequest(request);
				if (!StringUtil.isNil(json)) {
					renderArgs.put(Play_Session_Keys.CURR_PARSED_JSON.getValue(), json);
				}
			}
		} catch (Exception ex) {
			Logger.error(ex, "Exception during getJSONFromRequest()");
			renderJSON(apiService.buildGenericErrorResponse());
		}
	}

	@Before(priority = 1)
	public static void renderBasicArgs() {
		renderArgs.put("isProd", Play.mode.isProd());
	}

	/**
	 * @return
	 */
	protected static boolean isPost() {
		return request.method.equalsIgnoreCase("post");
	}

	/**
	 * @return
	 */
	protected static boolean isGet() {
		return request.method.equalsIgnoreCase("get");
	}

	/**
	 * @return
	 */
	protected static boolean isDelete() {
		return request.method.equalsIgnoreCase("delete");
	}

	/**
	 * @return
	 */
	protected static boolean isPut() {
		return request.method.equalsIgnoreCase("put");
	}

	/**
	 * @return
	 */
	protected static boolean isPatch() {
		return request.method.equalsIgnoreCase("patch");
	}

	/**
	 * @return
	 */
	protected static boolean isOptions() {
		return request.method.equalsIgnoreCase("options");
	}

	private static void disableAllMessages() {
		flash.remove("__mesgErrorActive");
		flash.remove("__mesgSuccessActive");
		flash.remove("__mesgWarningActive");
		flash.remove("__mesgNoticeActive");
	}

	@Util
	public static void setError(String msg) {
		disableAllMessages();
		flash.put("__mesgError", msg);
		flash.put("__mesgErrorActive", true);
	}

	@Util
	public static void setSuccess(String msg) {
		disableAllMessages();
		flash.put("__mesgSuccess", msg);
		flash.put("__mesgSuccessActive", true);
	}

	@Util
	public static void setWarning(String msg) {
		disableAllMessages();
		flash.put("__mesgWarning", msg);
		flash.put("__mesgWarningActive", true);
	}

	@Util
	public static void setNotice(String msg) {
		disableAllMessages();
		flash.put("__mesgNotice", msg);
		flash.put("__mesgNoticeActive", true);
	}

	@After
	@Util
	public static void addVeryImportantHeaders() {
		Header h;
		
		Header header = request.headers.get("origin");
		if(header !=null){
			List<String> vv = header.values;
			String s = vv.get(0);
			h = new Header("Access-Control-Allow-Origin", s);
			response.headers.put("Access-Control-Allow-Origin", h);
		} else {
			h = new Header("Access-Control-Allow-Origin", "*");
			response.headers.put("Access-Control-Allow-Origin", h);
		}

		h = new Header("Access-Control-Allow-Credentials", "true");
		response.headers.put("Access-Control-Allow-Credentials", h);

		h = new Header("Access-Control-Max-Age", "3600");
		response.headers.put("Access-Control-Max-Age", h);

		h = new Header("Access-Control-Allow-Headers",
				"X-Requested-With, Content-Type, Authorization, Origin, Accept, Access, Access-Control-Allow-Origin, Access-Control-Allow-Credentials, Access-Control-Allow-Origin, Access-Control-Max-Age, Access-Control-Allow-Methods, authtoken");
		response.headers.put("Access-Control-Allow-Headers", h);
	}

	@Util
	protected static void addOptions(AuthorizedHttpVerb... httpVerbs) {
		if (httpVerbs == null || httpVerbs.length <= 0)
			return;
		List<String> authorizedHttpVerbs = apiService.buildAuthorizedHttpVerbs(httpVerbs);
		String s = "";
		String del = "";
		for(String ss : authorizedHttpVerbs){
			s += del + ss;
			del = " ";
		}
		Header h = new Header("Access-Control-Allow-Methods", s);
		response.headers.put("Access-Control-Allow-Methods", h);
	}

	public static void retrievePublic(String code, Boolean base64, String mediaUuid) {
		Logger.warn(""+request.querystring);
		Logger.warn(""+request.url);
		if(base64 == null)
			base64 = false;
		if(!StringUtil.isNil(code))
			MediaService.instance().retrievePublic(code, base64);
		if(!StringUtil.isNil(mediaUuid))
			MediaService.instance().retrievePublicMedia(mediaUuid, base64);
		throw new RenderJson("{\"error\":\"error\"}");
	}
	
	public static void retrievePublicCsv(String code) {
		File f = ReportService.getCsvFromCode(code);
		if(f!=null)
			renderBinary(f);
	}

	
	
	public static void cleanAll(){
		if(true) {
			cleanOptions();
			return;
		}
		List<Test> all = Test.all().fetch();
		for(Test t : all){
			if(t.getTestType().equals(TestType.TESTINGTOOL)) {
				List<Report> reports = Report.findAllByTest(t);
				for(Report r : reports) {
					r.delete();
				}
				for(Question q : t.getQuestions()){
					for(Slide s : q.getSlidesWithoutReorder()) {
						//s.delete();
						List<models.SlideContentComponent> sccl = models.SlideContentComponent.findBySlide(s);
						for(models.SlideContentComponent scc : sccl) {
							if(s.getSlideContent().contains(scc))
								continue;
							List<models.Option> ol = models.Option.findBySlideContentComponent(scc);
							for(models.Option o : ol) {
								if(scc.getOptions().contains(o))
									continue;
								o.delete();
							}
							scc.delete();
						}
					}
					q.getSlides().clear();
					//q.delete();
				}
				t.getQuestions().clear();
				t.delete();
			}
		}
	}
	
	public static boolean cleanOptions() {
		
		List<Test> all = Test.all().fetch();
		for(Test t : all){
			if(t.getTestType().equals(TestType.TESTINGTOOL)) {
				for(Question q : t.getQuestions()){
					for(Slide s : q.getSlidesWithoutReorder()) {
						List<models.SlideContentComponent> sccl = models.SlideContentComponent.findBySlide(s);
						for(models.SlideContentComponent scc : sccl) {
							for(models.Option o : scc.getOptions()) {
								if(o.getSlideContentComponent().equals(scc))
									continue;
								o.setSlideContentComponent(scc);
								o.save();
							}
						}
						for(models.SlideContentComponent scc : s.getSlideContent()) {
							for(models.Option o : scc.getOptions()) {
								if(o.getSlideContentComponent().equals(scc))
									continue;
								o.setSlideContentComponent(scc);
								o.save();
							}
						}
						
						for(models.Option o : s.getOptions()) {
							if(o.getSlide().equals(s))
								continue;
							o.setSlide(s);
							o.save();
						}
					}
					
					for(models.Option o : q.getOptions()) {
						if(o.getQuestion().equals(q))
							continue;
						o.setQuestion(q);
						o.save();
					}
				}
				
				for(models.Option o : t.getOptions()) {
					if(o.getTest().equals(t))
						continue;
					o.setTest(t);
					o.save();
				}
			}
		}
		return true;
	}
}
