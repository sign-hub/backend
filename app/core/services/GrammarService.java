package core.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;

import core.services.ApiService.OKResponseObject;
import models.Grammar;
import models.Grammar.GrammarStatus;
import models.GrammarPart;
import models.GrammarPart.GrammarPartStatus;
import models.GrammarPart.GrammarPartType;
import models.SignLanguage;
import models.Topic;
import models.User;
import models.User.ToolsTypes;
import play.Logger;
import play.Play;
import schedulers.AppStart_prova;
import schedulers.GrammarJob;
import utils.StringUtil;

public class GrammarService {

	private static enum GrammarServiceSingleton {
		INSTANCE;

		GrammarService singleton = new GrammarService();

		public GrammarService getSingleton() {
			return singleton;
		}
	}

	private static final int REDISTRIBUTE = 0;
	private static final int ISPROBLEM = 1;
	private static final int NOTPROBLEM = 2;

	public static GrammarService instance() {
		return GrammarService.GrammarServiceSingleton.INSTANCE.getSingleton();
	}

	private GrammarService() {
		this.topics = TopicService.instance().getTopicMap();
		buildTopicsMap();
	}

	private static ApiService apiService = ApiService.instance();
	private static UserService userService = UserService.instance();

	private Map<String, Topic> topics;
	private Map<Integer, Map<String, Topic>> topicsMap;

	private int maxLength;

	public static class GrammarObjectSmall {

		public String uuid;
		public String grammarName;
		public String creationDate;
		public String revisionDate;
		public String grammarStatus;
		public Boolean isDeleted;
		public Boolean htmlAvailable;
		public Boolean pdfAvailable;
		public SignLanguage signLanguage;

		public GrammarObjectSmall(Grammar g) {
			this.uuid = g.getUuid();
			this.grammarName = g.getGrammarName();
			this.creationDate = StringUtil.date(g.getCreationDate(), g.CREATION_DATE_FORMAT);
			this.revisionDate = StringUtil.date(g.getRevisionDate(), g.REVISION_DATE_FORMAT);
			this.grammarStatus = g.getGrammarStatus().name();
			this.isDeleted = g.getDeleted();
			this.htmlAvailable = StringUtil.isNil(g.getHtmlPath()) ? false : true;
			this.pdfAvailable = StringUtil.isNil(g.getPdfPath()) ? false : true;
			this.signLanguage = g.getSignLanguage();
		}

		public GrammarObjectSmall() {
			// TODO Auto-generated constructor stub
		}

	}

	private List<GrammarObjectSmall> buildGrammarObjectSmallListFromGrammarList(List<Grammar> grammarObjs) {
		List<GrammarObjectSmall> ret = new LinkedList<GrammarObjectSmall>();
		for (Grammar g : grammarObjs) {
			ret.add(new GrammarObjectSmall(g));
		}
		return ret;
	}

	public static class GrammarListResponse extends OKResponseObject {
		public List<GrammarObjectSmall> response;
	}

	public String grammarList(String orderby, String ordertype) {
		if (orderby == null)
			orderby = "grammarName";
		if (ordertype == null)
			ordertype = "ASC";
		Logger.info("grammar list:" + orderby);
		List<Grammar> grammarObjs = new LinkedList<Grammar>();
		if (/* userService.hasCurrentUserLoggedAdminRole() */ userService.checkAdminAndTool(ToolsTypes.GRAMMAR)
				|| userService.hasCurrentUserLoggedRole(User.Roles.GRAMMAR_ADMIN)) {
			grammarObjs = Grammar.findAllOrdered(orderby, ordertype);
		} else {
			if (userService.hasCurrentUserLoggedRole(User.Roles.GRAMMAR_CONTENT_PROVIDER)) {
				grammarObjs = Grammar.findAllForUserEditor(userService.getCurrentUserLogged(), orderby, ordertype);
			} else {
				grammarObjs = Grammar.findAllPublished(orderby, ordertype);
			}
		}
		List<GrammarObjectSmall> testObjsSmall = buildGrammarObjectSmallListFromGrammarList(grammarObjs);
		GrammarListResponse res = new GrammarListResponse();
		res.response = testObjsSmall;
		String ret = res.toJson();
		return ret;
	}

	public static class GrammarObject {

		public String uuid;
		public String grammarName;
		public String grammarSignHubSeries;
		public Integer grammarSignHubSeriesNumber;
		public String grammarOtherSignHubSeries;
		public String grammarEditorialInfo;
		public String grammarCopyrightInfo;
		public String grammarISBNInfo;
		public String grammarBibliographicalReference;
		public String creationDate;
		public String revisionDate;
		public String grammarStatus;
		public Boolean isDeleted;
		public List<GrammarPartObject> parts;
		// public List<String> parts;
		public String author;
		public List<String> editors;
		public List<String> contentProviders;
		public String status;
		public boolean htmlAvailable;
		public boolean pdfAvailable;
		public String frontMatter;

		public GrammarObject(Grammar g, Boolean isComplete) {
			this.uuid = g.getUuid();
			this.grammarName = g.getGrammarName();
			this.grammarBibliographicalReference = g.getGrammarBibliographicalReference();
			this.grammarCopyrightInfo = g.getGrammarCopyrightInfo();
			this.grammarEditorialInfo = g.getGrammarEditorialInfo();
			this.grammarISBNInfo = g.getGrammarISBNInfo();
			this.grammarOtherSignHubSeries = g.getGrammarOtherSignHubSeries();
			this.grammarSignHubSeries = g.getGrammarSignHubSeries();
			this.grammarSignHubSeriesNumber = g.getGrammarSignHubSeriesNumber();
			this.creationDate = StringUtil.date(g.getCreationDate(), g.CREATION_DATE_FORMAT);
			this.revisionDate = StringUtil.date(g.getRevisionDate(), g.REVISION_DATE_FORMAT);
			this.grammarStatus = g.getGrammarStatus().name();
			this.isDeleted = g.getDeleted();
			this.parts = buildListGrammarPartsObjectFromPartList(g, g.getParts(), isComplete, false);
			this.author = g.getAuthor() == null ? null : g.getAuthor().getUuid();
			this.editors = buildUsersList(g.getEditorList());
			this.contentProviders = buildUsersList(g.getContentProviderList());
			this.status = g.getGrammarStatus().name();
			this.htmlAvailable = StringUtil.isNil(g.getHtmlPath()) ? false : true;
			this.pdfAvailable = StringUtil.isNil(g.getPdfPath()) ? false : true;
		}

		public GrammarObject() {
			// TODO Auto-generated constructor stub
		}

	}

	private static List<String> buildUsersList(List<User> userList) {
		List<String> ret = new LinkedList<String>();
		if (userList != null) {
			for (User u : userList)
				ret.add(u.getUuid());
		}
		return ret;
	}

	private static List<GrammarPartObject> buildListGrammarPartsObjectFromPartList(Grammar g, List<GrammarPart> parts,
			Boolean isComplete, Boolean isPlay) {
		List<GrammarPartObject> ret = new LinkedList<GrammarPartObject>();

		for (GrammarPart part : parts) {
			if (part.getGrammar() == null) {
				part.setGrammar(g);
				part.save();
			}
			ret.add(new GrammarPartObject(part, isComplete, isPlay));
		}
		Collections.sort(ret, new Comparator<GrammarPartObject>() {

			@Override
			public int compare(GrammarPartObject o1, GrammarPartObject o2) {
				return o1.grammarPartOrder.compareTo(o2.grammarPartOrder);
			}
		});
		return ret;
	}

	/*
	 * private static List<String>
	 * buildListGrammarPartsObjectFromPartList(List<GrammarPart> parts) {
	 * List<String> ret = new LinkedList<String>(); for(GrammarPart part : parts){
	 * ret.add(part.getUuid()); } return ret; }
	 */

	public static class GrammarPartObject {

		protected Integer grammarPartOrder;
		public Float completePartOrder;
		public String uuid;
		public Boolean isDeleted;
		public String elementNumber;
		public String name;
		public String status;
		public String type;
		public String html;
		public String parent;
		public String creationDate;
		public String revisionDate;
		public List<GrammarPartObject> parts;
		// public List<String> parts;
		public String author;
		public List<String> editors;
		public List<String> contentProviders;
		public Map<String, String> options;
		private Float completePartOrderNow;

		public GrammarPartObject(GrammarPart part, Boolean isComplete, Boolean isPlay) {
			if (isComplete == null)
				isComplete = false;
			this.uuid = part.getUuid();
			this.isDeleted = part.getDeleted();
			this.elementNumber = part.getElementNumber();
			this.grammarPartOrder = part.getGrammarPartOrder();
			this.completePartOrder = part.getCompleteOrder();
			this.completePartOrderNow = part.getCompleteOrderNow();
			this.name = part.getGrammarPartName();
			this.status = part.getGrammarPartStatus().name();
			this.type = part.getGrammarPartTYpe().name();
			if (isComplete)
				this.html = part.getHtml();
			if (isPlay) {
				// this.html = GrammarService.instance().buildTextWithTopics(this, this.html);
			}
			this.parent = null;
			if (part.getParent() != null)
				this.parent = part.getParent().getUuid();
			this.creationDate = StringUtil.date(part.getCreationDate(), part.CREATION_DATE_FORMAT);
			this.revisionDate = StringUtil.date(part.getRevisionDate(), part.REVISION_DATE_FORMAT);
			this.parts = buildListGrammarPartsObjectFromPartList(part.getGrammar(), part.getParts(), isComplete,
					isPlay);
			this.author = part.getAuthor() == null ? null : part.getAuthor().getUuid();
			this.editors = buildUsersList(part.getEditorList());
			this.contentProviders = buildUsersList(part.getContentProviderList());
		}

		public GrammarPartObject(GrammarPart part) {
			this.uuid = part.getUuid();
			this.isDeleted = part.getDeleted();
			this.elementNumber = part.getElementNumber();
			this.grammarPartOrder = part.getGrammarPartOrder();
			this.completePartOrder = part.getCompleteOrder();
			this.completePartOrderNow = part.getCompleteOrderNow();
			this.name = part.getGrammarPartName();
			this.status = part.getGrammarPartStatus().name();
			this.type = part.getGrammarPartTYpe().name();
			this.parent = null;
			if (part.getParent() != null)
				this.parent = part.getParent().getUuid();
			this.creationDate = StringUtil.date(part.getCreationDate(), part.CREATION_DATE_FORMAT);
			this.revisionDate = StringUtil.date(part.getRevisionDate(), part.REVISION_DATE_FORMAT);
			this.author = part.getAuthor().getUuid();
			this.editors = buildUsersList(part.getEditorList());
			this.contentProviders = buildUsersList(part.getContentProviderList());
		}

		/*
		 * public GrammarPartObject(GrammarPart part, Boolean isComplete) { if
		 * (isComplete == null) isComplete = false; this.uuid = part.getUuid();
		 * this.isDeleted = part.getDeleted(); this.elementNumber =
		 * part.getElementNumber(); this.grammarPartOrder = part.getGrammarPartOrder();
		 * this.completePartOrder = part.getCompleteOrder(); this.name =
		 * part.getGrammarPartName(); this.status = part.getGrammarPartStatus().name();
		 * this.type = part.getGrammarPartTYpe().name(); if (isComplete) this.html =
		 * part.getHtml(); this.parent = null; if (part.getParent() != null) this.parent
		 * = part.getParent().getUuid(); this.creationDate =
		 * StringUtil.date(part.getCreationDate(), part.CREATION_DATE_FORMAT);
		 * this.revisionDate = StringUtil.date(part.getRevisionDate(),
		 * part.REVISION_DATE_FORMAT); this.parts =
		 * buildListGrammarPartsObjectFromPartList(part.getGrammar(), part.getParts(),
		 * isComplete); this.author = part.getAuthor().getUuid(); this.editors =
		 * buildUsersList(part.getEditorList()); this.contentProviders =
		 * buildUsersList(part.getContentProviderList()); }
		 */

	}

	public static class GrammarResponse extends OKResponseObject {
		public GrammarObject response;
	}

	public String getGrammar(String grammarId, Boolean isPlay, Boolean isComplete) {
		return getGrammar(grammarId, isPlay, isComplete, false);
	}

	public String getGrammar(String grammarId, Boolean isPlay, Boolean isComplete, Boolean isPublic) {
		if (StringUtil.isNil(isPlay))
			isPlay = false;
		if (StringUtil.isNil(isComplete))
			isComplete = false;
		if (StringUtil.isNil(grammarId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}
		Grammar grammar = Grammar.findById(grammarId);
		if (grammar == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}
		if (/* !userService.hasCurrentUserLoggedAdminRole() */ !userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			if (grammar.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (!checkUserPermission(userService.getCurrentUserLogged(), grammar, null, "GET")) {
			if (grammar.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		GrammarObject grammarObj = new GrammarObject(grammar, isComplete);
		grammarObj.frontMatter = createFrontMatter(grammar);
		GrammarResponse res = new GrammarResponse();
		res.response = grammarObj;
		return res.toJson();
	}

	private boolean checkUserPermission(User currentUserLogged, Grammar grammar, GrammarPart grammarPart,
			String operation) {
		if (/* !userService.hasCurrentUserLoggedAdminRole() */ userService.checkAdminAndTool(ToolsTypes.GRAMMAR)
				&& !userService.hasCurrentUserLoggedRole(User.Roles.GRAMMAR_ADMIN)) {
			if (operation.equals("GET")) {
				if (grammar != null) {
					if (!userService.hasCurrentUserLoggedRole(User.Roles.GRAMMAR_CONTENT_PROVIDER)
							|| !grammar.getEditorList().contains(userService.getCurrentUserLogged())) {
						if (!grammar.isPublished())
							return false;
					}
				} else if (grammarPart != null) {
					if (!userService.hasCurrentUserLoggedRole(User.Roles.GRAMMAR_CONTENT_PROVIDER)
							|| !grammarPart.getEditorList().contains(userService.getCurrentUserLogged())) {
						return false;
					}
				}
			} else if (operation.equals("UPDATE")) {
				if (grammar != null) {
					if (!userService.hasCurrentUserLoggedRole(User.Roles.GRAMMAR_CONTENT_PROVIDER)
							|| !grammar.getEditorList().contains(userService.getCurrentUserLogged())) {
						return false;
					}
				} else if (grammarPart != null) {
					if (!userService.hasCurrentUserLoggedRole(User.Roles.GRAMMAR_CONTENT_PROVIDER)
							|| !grammarPart.getEditorList().contains(userService.getCurrentUserLogged())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public String updateGrammar(String grammarId) {
		if (StringUtil.isNil(grammarId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		Grammar grammar = Grammar.findById(grammarId);
		if (grammar == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/* !userService.hasCurrentUserLoggedAdminRole() */ !userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			if (grammar.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (/* !userService.hasCurrentUserLoggedAdminRole() */ !userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!checkUserPermission(currentUserLogged, grammar, null, "GET")) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		String json = apiService.getCurrentJson();

		UpdateGrammarRequest req;
		GrammarObject grammarToUpdate;

		try {
			req = (UpdateGrammarRequest) apiService.buildObjectFromJson(json, UpdateGrammarRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			grammarToUpdate = req.grammar;
			if (grammarToUpdate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		GrammarStatus grammarStatus = GrammarStatus.tryBuildGrammarStatusFromName(grammarToUpdate.status);
		GrammarStatus oldGrammarStatus = grammar.getGrammarStatus();
		if (grammarStatus != null) {
			grammar.setGrammarStatus(grammarStatus);
		} else {
			return apiService.buildWrongTestStatusFieldErrorResponse();
		}

		if (grammarToUpdate.isDeleted != null) {
			grammar.setDeleted(grammarToUpdate.isDeleted);
		}

		if (!StringUtil.isNil(grammarToUpdate.grammarName)) {
			grammar.setGrammarName(grammarToUpdate.grammarName);
			grammar.setGrammarBibliographicalReference(grammarToUpdate.grammarBibliographicalReference);
			grammar.setGrammarCopyrightInfo(grammarToUpdate.grammarCopyrightInfo);
			grammar.setGrammarEditorialInfo(grammarToUpdate.grammarEditorialInfo);
			grammar.setGrammarISBNInfo(grammarToUpdate.grammarISBNInfo);
			grammar.setGrammarOtherSignHubSeries(grammarToUpdate.grammarOtherSignHubSeries);
			grammar.setGrammarSignHubSeries(grammarToUpdate.grammarSignHubSeries);
			grammar.setGrammarSignHubSeriesNumber(grammarToUpdate.grammarSignHubSeriesNumber);
		}

		grammar.setRevisionDate(new Date());

		if (/* userService.hasCurrentUserLoggedAdminRole() */ userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			grammar.setContentProviderList(buildUserListFromString(grammarToUpdate.contentProviders));
			grammar.setEditorList(buildUserListFromString(grammarToUpdate.editors));
		}
		/*
		 * Map<String, GrammarPartObject> map = new LinkedHashMap<String,
		 * GrammarPartObject>(); for(GrammarPartObject gpo : grammarToUpdate.parts){
		 * map.put(gpo.uuid, gpo); } updateGrammarParts(map, grammar.getParts());
		 */
		grammar.save();
		if (!oldGrammarStatus.equals(GrammarStatus.PUBLISHED)
				&& grammar.getGrammarStatus().equals(GrammarStatus.PUBLISHED)) {
			// grammarPdfCreator(grammar);
			GrammarJob gj = WorkerService.getInstance().generateGrammarJob(grammar);
			if (!gj.getIsWorking()) {
				Logger.info("Working GrammarJob");
				gj.in(60);
			} else {
				Logger.info("GrammarJob is already working");
			}
		}
		UpdateGrammarResponse res = new UpdateGrammarResponse();
		res.response = new GrammarObject(grammar, false);
		String ret = res.toJson();
		return ret;
	}

	private void updateGrammarParts(Map<String, GrammarPartObject> map, List<GrammarPart> list) {

		for (GrammarPart gp : list) {
			updateGrammarPart(gp, map.get(gp.getUuid()));
		}
	}

	public static class UpdateGrammarResponse extends OKResponseObject {
		public GrammarObject response;
	}

	public static class UpdateGrammarPartRequest {

		public GrammarPartObject grammarPart;

	}

	public static class UpdateGrammarPartResponse extends OKResponseObject {
		public GrammarPartObject response;
	}

	public String updateGrammarPart(String grammarPartId) {
		if (StringUtil.isNil(grammarPartId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		GrammarPart grammarPart = GrammarPart.findById(grammarPartId);
		if (grammarPart == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/* !userService.hasCurrentUserLoggedAdminRole() */ !userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			if (grammarPart.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (/* !userService.hasCurrentUserLoggedAdminRole() */ !userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!checkUserPermission(currentUserLogged, null, grammarPart, "GET")) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		String json = apiService.getCurrentJson();

		UpdateGrammarPartRequest req;
		GrammarPartObject grammarPartToUpdate;

		try {
			req = (UpdateGrammarPartRequest) apiService.buildObjectFromJson(json, UpdateGrammarPartRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			grammarPartToUpdate = req.grammarPart;
			if (grammarPartToUpdate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		updateGrammarPart(grammarPart, grammarPartToUpdate);

		UpdateGrammarPartResponse res = new UpdateGrammarPartResponse();
		res.response = new GrammarPartObject(grammarPart, false, false);
		String ret = res.toJson();
		return ret;

	}

	private void updateGrammarPart(GrammarPart gp, GrammarPartObject gpo) {
		if (gp == null || gpo == null)
			return;
		// gp.setContentProviderList(buildUserListFromString(gpo.contentProviders));
		if (gpo.isDeleted != null)
			gp.setDeleted(gpo.isDeleted);
		// gp.setEditorList(buildUserListFromString(gpo.editors));
		if (!StringUtil.isNil(gpo.elementNumber))
			gp.setElementNumber(gpo.elementNumber);
		if (!StringUtil.isNil(gpo.name))
			gp.setGrammarPartName(gpo.name);
		if (!StringUtil.isNil(gpo.status)) {
			try {
				GrammarPartStatus gps = GrammarPartStatus.tryBuildGrammarPartStatusFromName(gpo.status);
				gp.setGrammarPartStatus(gps);
			} catch (Exception ex) {
				System.out.println("Non è possibile effettuare la conversione dello stato " + gpo.status);
			}
		}
		if (!StringUtil.isNil(gpo.type)) {
			try {
				GrammarPartType gps = GrammarPartType.tryBuildGrammarPartTypeFromName(gpo.type);
				gp.setGrammarPartTYpe(gps);
			} catch (Exception ex) {
				System.out.println("Non è possibile effettuare la conversione dello tipo " + gpo.status);
			}
		}
		if (!StringUtil.isNil(gpo.html))
			gp.setHtml(gpo.html);
		/*
		 * if(!StringUtil.isNil(gpo.parent)){ GrammarPart parent =
		 * GrammarPart.findById(gpo.parent); gp.setParent(parent); }
		 */
		gp.setRevisionDate(new Date());

		/*
		 * Map<String, GrammarPartObject> map = new LinkedHashMap<String,
		 * GrammarPartObject>(); for(GrammarPartObject gpoo : gpo.parts){
		 * map.put(gpoo.uuid, gpoo); } updateGrammarParts(map, gp.getParts());
		 */

		if (/* userService.hasCurrentUserLoggedAdminRole() */ userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			gp.setContentProviderList(buildUserListFromString(gpo.contentProviders));
			gp.setEditorList(buildUserListFromString(gpo.editors));
			/*
			 * List<User> cpl = new LinkedList<User>(); List<User> el = new
			 * LinkedList<User>(); User u = null; for(String userUuid:
			 * gpo.contentProviders){ u = User.findById(userUuid); if(u!=null) cpl.add(u); }
			 * gp.setContentProviderList(cpl);
			 * 
			 * u = null; for(String userUuid: gpo.editors){ u = User.findById(userUuid);
			 * if(u!=null) el.add(u); } gp.setEditorList(el);
			 */
		}

		gp.save();
	}

	private List<User> buildUserListFromString(List<String> userUuids) {
		List<User> ret = new LinkedList<User>();
		if (userUuids != null)
			for (String uuid : userUuids) {
				User u = User.findById(uuid);
				if (u != null)
					ret.add(u);
			}
		return ret;
	}

	public static class UpdateGrammarRequest {

		public GrammarObject grammar;

	}

	public String getGrammarPart(String grammarPartId, Boolean isPlay) {
		if (StringUtil.isNil(isPlay))
			isPlay = false;
		if (StringUtil.isNil(grammarPartId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}
		GrammarPart grammarPart = GrammarPart.findById(grammarPartId);
		if (grammarPart == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}
		if (/* !userService.hasCurrentUserLoggedAdminRole() */ !userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			if (grammarPart.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (!checkUserPermission(userService.getCurrentUserLogged(), null, grammarPart, "GET")) {
			if (grammarPart.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}
		// grammarPart.calculateCompleteOrderNow();

		GrammarPartObject grammarPartObject = new GrammarPartObject(grammarPart, true, isPlay);
		GrammarPartResponse res = new GrammarPartResponse();
		res.response = grammarPartObject;
		return res.toJson();
	}

	public static class GrammarPartResponse extends OKResponseObject {
		public GrammarPartObject response;
	}

	public String createGrammar() {
		if (/* !userService.hasCurrentUserLoggedAdminRole() */ !userService.checkAdminAndTool(ToolsTypes.GRAMMAR)) {
			return apiService.buildHttpVerbNotAllowedResponse();
		}
		User u = userService.getCurrentUserLogged();
		Grammar g = AppStart_prova.createGrammar();
		g.setGrammarName("New Grammar");
		g.save();

		UpdateGrammarResponse res = new UpdateGrammarResponse();
		res.response = new GrammarObject(g, false);
		String ret = res.toJson();
		return ret;
	}

	public String grammarPdfCreator(String grammarId) {

		Boolean isComplete = true;
		if (StringUtil.isNil(grammarId)) {
			System.out.println("GrammarService grammarId is mandatory");
			return null;
		}
		Grammar grammar = Grammar.findById(grammarId);
		if (grammar == null) {
			System.out.println("GrammarService grammar not found");
			return null;
		}

		GrammarObject grammarObj = new GrammarObject(grammar, isComplete);

		return grammarPdfCreator(grammar);
	}

	public String grammarPdfCreator(Grammar grammar) {

		Logger.info("grammarPdfCreator start");
		String baseUri = Play.configuration.getProperty("fronthost", "https://testing02.eclettica.net");
		String basePath = Play.configuration.getProperty("grammar.basepath", "/usr/cini/cinibe-app/cinibe/grammar");

		File baseFile = new File(basePath);
		Logger.info("grammarPdfCreator " + baseFile.getAbsolutePath());
		grammar = Grammar.findById(grammar.getUuid());
		File f = new File(baseFile, grammar.getUuid() + ".pdf");
		Logger.info("grammarPdfCreator " + f.getAbsolutePath());
		String html = startGrammar(baseUri, grammar.getGrammarName());
		html += createFrontMatter(grammar);
		LinkedList<GrammarPart> parts = getOrderedGrammaParts(grammar.getParts());
		StringBuilder body = new StringBuilder("");
		StringBuilder toc = new StringBuilder(
				"<h2 style=\"page-break-before: always;\">Table of contents</h2><div class=\"toc_container\">");

		for (GrammarPart part : parts) {
			this.addText(body, toc, part);
		}
		toc.append("</div><p style=\"page-break-after: always;\">&nbsp;</p>");
		html += toc;

		html += "<p style=\"page-break-before: always;\">&nbsp;</p><div class=\"grammar_container\">" + body + "</div>";
		html = endGrammar(html);
		File out = new File(baseFile, grammar.getUuid() + ".html");
		Logger.info("grammarPdfCreator " + out.getAbsolutePath());
		try {
			FileWriter fw = new FileWriter(out);
			fw.write(html);
			fw.close();
			Logger.info("grammarPdfCreator html wrote!");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Logger.error(e1, "ERRORE!!!");
		}
		Logger.info("grammarPdfCreator set absolute path " + out.getAbsolutePath());
		grammar.setHtmlPath(out.getAbsolutePath());
		Logger.info("grammarPdfCreator saving...");
		try {
			grammar.save();
			Logger.info("grammarPdfCreator grammar saved");
		} catch (Exception e) {
			Logger.error(e, "ERRORE NEL SALVATAGGIO");
			e.printStackTrace();
		}
		/*
		 * Document document = new Document(); html = Jsoup.clean(html,
		 * Whitelist.basicWithImages()); //org.jsoup.nodes.Document doc =
		 * Jsoup.parse(html, baseUri, Parser.xmlParser()); org.jsoup.nodes.Document doc
		 * = Jsoup.parse(html, baseUri);
		 * doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.
		 * xml); String parsedHtml = doc.html();
		 * 
		 * Reader strReader = new StringReader(parsedHtml); try { PdfWriter writer =
		 * PdfWriter.getInstance(document, new FileOutputStream(f)); document.open();
		 * XMLWorkerHelper.getInstance().parseXHtml(writer, document, strReader);
		 * strReader.close(); } catch(Exception e) { e.printStackTrace();
		 * System.err.flush(); System.err.println(parsedHtml); } document.close();
		 * System.out.println("PDF path: " + f.getAbsolutePath());
		 */

		try {

			File htmlSource = new File("/usr/cini/grammar-repository/grammar/" + grammar.getUuid() + ".html");

			Document doc = Jsoup.parse(htmlSource, "UTF-8");
			Elements links = doc.select("a[href='javascript:void(0)']");

			for (Element link : links) {
				String text = link.text();
				link.replaceWith(new TextNode(text));
			}

			Elements videos = doc.select("video");
			for (Element video : videos) {
				video.tagName("a");
				video.removeAttr("class");
				video.removeAttr("controls");
				video.removeAttr("style");
				String src = video.attr("src");
				String href = baseUri + src.substring(src.indexOf("/api"), src.length());
				video.removeAttr("src");
				video.attr("href", href);
				video.attr("style", "text-decoration: none;");
				video.attr("target", "_blank");
				video.text("video");
			}

			File withoutHref = new File(baseFile, grammar.getUuid() + "-forPdf.html");
			FileWriter fwr = new FileWriter(withoutHref);
			fwr.write(doc.outerHtml());
			fwr.close();

			File pdfDest = new File("/usr/cini/grammar-repository/grammar/" + grammar.getUuid() + ".pdf");
			// pdfHTML specific code
			ConverterProperties converterProperties = new ConverterProperties();
			HtmlConverter.convertToPdf(new FileInputStream(withoutHref), new FileOutputStream(pdfDest),
					converterProperties);

//			Document document = new Document();
//		    PdfWriter writer = PdfWriter.getInstance(document, 
//		       new FileOutputStream("/usr/cini/grammar-repository/grammar/" + grammar.getUuid() + ".pdf"));
//		    document.open();
//		    XMLWorkerHelper.getInstance().parseXHtml(writer, document,
//		    		new FileInputStream("/usr/cini/grammar-repository/grammar/" + grammar.getUuid() + ".html"));
//		    document.close();
		} catch (Exception e) {
			Logger.error(e, "ERRORE NELLA CONVERSIONE");
			e.printStackTrace();
		}

		try {
			// String command = "google-chrome --headless --no-sandbox --disable-gpu
			// --print-to-pdf=\""+ f.getAbsolutePath() +"\" "+ out.getAbsolutePath() +"";
			// String command = "/usr/cini/bin/pdfConverter.sh";
			/*
			 * String[] command = { "/usr/cini/bin/pdfConverter.sh", "--headless",
			 * "--no-sandbox", "--disable-gpu", "--print-to-pdf=\""+ f.getAbsolutePath()
			 * +"\"", "\""+out.getAbsolutePath()+"\"" };
			 */
			/*
			 * String[] command = { "/usr/cini/bin/nodePdfConverter.sh",
			 * "--url=\"file:///usr/cini/grammar-repository/grammar/UUID-GRMM-1b66b300-7e6e-4a22-97e3-be6946132a34.html\"",
			 * "--pdf=\"/usr/cini/grammar-repository/grammar/UUID-GRMM-1b66b300-7e6e-4a22-97e3-be6946132a34.pdf\"",
			 * "--chrome-option=--no-sandbox" };
			 */
			String[] command = { "/usr/cini/bin/nodePdfConverter.sh",
					"/usr/cini/grammar-repository/grammar/" + grammar.getUuid() + ".html",
					"/usr/cini/grammar-repository/grammar/" + grammar.getUuid() + ".pdf" };
			/*
			 * String[] command = { "chrome-headless-render-pdf",
			 * "--url=\"file:///usr/cini/grammar-repository/grammar/UUID-GRMM-1b66b300-7e6e-4a22-97e3-be6946132a34.html\"",
			 * "--pdf=\"/usr/cini/grammar-repository/grammar/UUID-GRMM-1b66b300-7e6e-4a22-97e3-be6946132a34.pdf\"",
			 * "--chrome-option=--no-sandbox" };
			 */
			System.out.println("Execute command: " + command[0]);
//			Runtime run = Runtime.getRuntime();
//			Process pr = run.exec(command);
//			pr.waitFor();
//			{
//				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//				String line = "";
//				while ((line = buf.readLine()) != null) {
//					System.out.println(line);
//				}
//			}
//			{
//				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
//				String line = "";
//				while ((line = buf.readLine()) != null) {
//					System.out.println(line);
//				}
//			}
			System.out.println("Command executed");
			grammar.setPdfPath(f.getAbsolutePath());
			grammar.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f.getAbsolutePath();
	}

	private String checkNull(String field) {
		if (field == null) {
			return "";
		}
		return field;
	}

	private String preserveBreak(String field) {
		return field.replaceAll("(\r\n|\r|\n|\n\r)", "<br>");

	}

	private String createFrontMatter(Grammar grammar) {
		Set<User> users = new HashSet<User>();
		users.addAll(grammar.getContentProviderList());
		users.addAll(grammar.getEditorList());
		this.extractEditorsFromParts(grammar.getParts(), users);
		List<Grammar> gl = Grammar.findAll();
		String isbn = grammar.getGrammarISBNInfo();
		String linkCreativeCommons = "https://creativecommons.org/share-your-work/licensing-examples/#by-nc-sa";
		String firstPublished = "Publication year: 2020";
		String secondEdition = "Edition number: 1";
		String copyrightInfo = grammar.getGrammarCopyrightInfo(); // text area
		String bibliographicalReference = grammar.getGrammarBibliographicalReference(); // text area
		String editorialInfo = grammar.getGrammarEditorialInfo(); // text area
		String editedBy = grammar.getEditedBy(); // text area
		String grammarName = grammar.getGrammarName();
		String grammarOf = grammarName.substring(0, grammarName.indexOf("of") + 2);
		String grammarNameAfterOf = grammarName.substring(grammarName.indexOf("of") + 2, grammarName.length());
		String grammarOtherSignHubSeries = grammar.getGrammarOtherSignHubSeries();

		isbn = this.checkNull(isbn);
		linkCreativeCommons = this.checkNull(linkCreativeCommons);
		firstPublished = this.checkNull(firstPublished);
		secondEdition = this.checkNull(secondEdition);
		copyrightInfo = this.checkNull(copyrightInfo);
		copyrightInfo = this.preserveBreak(copyrightInfo);
		bibliographicalReference = this.checkNull(bibliographicalReference);
		bibliographicalReference = this.preserveBreak(bibliographicalReference);
		editedBy = this.checkNull(editedBy);
		editedBy = this.preserveBreak(editedBy);
		editorialInfo = this.checkNull(editorialInfo);
		editorialInfo = this.preserveBreak(editorialInfo);
		grammarOtherSignHubSeries = this.checkNull(grammarOtherSignHubSeries);
		grammarOtherSignHubSeries = this.preserveBreak(grammarOtherSignHubSeries);

		String ret = "<div class=\"frontmatter\">";
		ret += "<h1 class=\"nobreak\" >" + grammarOf + "<br>\n" + grammarNameAfterOf + "</h1>\n"
				+ "	<p style='font-size: 20pt;' class='edited-by'>Edited by<br>";
		ret += "<span class='pre' style='font-size: 20pt;'>" + editorialInfo + "</span>";
		/*
		 * for (User cp : users) { ret += cp.getSurname() + " " + cp.getName(); }
		 */
		ret += "</p>\n";
		ret += "<div class='logos'>\n"
				+ "	<img src='http://platform.sign-hub.eu/assets/img/logoFrontMatter.png' class='logoimg'>\n"
				+ "	<img src='http://platform.sign-hub.eu/assets/img/eu-logoFrontMatter.png' class='logoimg'>\n"
				+ "<div class='text-eu'>\n" + "Funded by the Horizon 2020 Framework Programme <br> \n"
				+ "of the European Union <br>\n" + "under grant agreement No 693349</div>" + "" + "</div>\n";
		ret += "<div style='font-size: 12pt;' class='isbn-info'>\n<span class='pre'>" + isbn + "</span><br>\n</div>\n";

		ret += "<div class='creative-commons'>\n"
				+ "<img src='http://platform.sign-hub.eu/assets/img/logo_creative_commons.png' class='logoimg'>"
				+ "<p style='font-size: 11pt;' class='margin-12'>This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 License.<br>"
				+ "For details go to <a href='" + linkCreativeCommons + "'>" + linkCreativeCommons + "</a>\n" + "</p>\n"
				+ "</div>\n";
		ret += "<div style='font-size: 10pt;' class='margin-12'>\n" + firstPublished + "<br>" + secondEdition
				+ "</div>\n";
		ret += "<div style='font-size: 10pt;' class='margin-12'>\n" + copyrightInfo + "</div>\n";
		ret += "<div style='font-size: 10pt;' class='margin-12'>\n"
				+ "The book is published with open access at <a href='https://www.sign-hub.eu/grammardetail/"
				+ grammar.getUuid() + "'>https://www.sign-hub.eu/grammardetail/" + grammar.getUuid() + "</a>\n"
				+ "</div>\n";
		ret += "<div style='font-size: 11pt;' class='biblio-reference'>\n"
				+ "We recommend that you cite the entire grammar or sub-parts of it as in the following examples.<br>"
				+ "<span class='pre'>" + bibliographicalReference + "</span></div>\n";
		ret += "<div class='published-list'>\n" + "<p style='font-size: 16pt;'>SIGN-HUB Sign Language Grammars</p>"
				+ "<p style='font-size: 14pt; position: relative; margin-top: -15px;'>Sign language grammars are available for these languages:</p>"
				+ "<p class='margin-14'>";

//		for (Grammar g : gl) {
//			if (g.isPublished()) {
//				ret += "<li style='list-style-type:none; font-size: 14pt;' class='margin-14'>" + g.getGrammarName()
//						+ "</li>";
//			}
//		}
		ret += grammarOtherSignHubSeries;
		ret += "</p>\n</div>\n";
		ret += "</div>";

		return ret;
	}

	private void extractEditorsFromParts(List<GrammarPart> parts, Set<User> users) {
		for (GrammarPart gp : parts) {
			users.addAll(gp.getContentProviderList());
			users.addAll(gp.getEditorList());
			if (gp.getParts() != null && !gp.getParts().isEmpty())
				this.extractEditorsFromParts(gp.getParts(), users);
		}
	}

	private LinkedList<GrammarPart> getOrderedGrammaParts(List<GrammarPart> parts) {
		LinkedList<GrammarPart> ret = new LinkedList();
		if (parts != null)
			ret.addAll(parts);
		Collections.sort((List<GrammarPart>) ret, new Comparator<GrammarPart>() {

			@Override
			public int compare(GrammarPart o1, GrammarPart o2) {
				if (o1 == null || o2 == null)
					return 0;
				// return o1.getCompleteOrder().compareTo(o2.getCompleteOrder());
				return o1.getGrammarPartOrder().compareTo(o2.getGrammarPartOrder());
			}
		});
		return ret;
	}

	private String startGrammar(String baseUri, String title) {
		String html = "<html><head>\n" + "<title>" + title + "</title>\n" + "<link rel='stylesheet' href='" + baseUri
				+ "/assets/webfontkit-handshape/stylesheet.css' type='text/css' charset='utf-8' />";
		html += "<style>\n" + "@media print {\n" + "  @page {\n" + "	  size: A4;\n"
				+ "	  margin: 3cm 2cm 2cm 2cm;\n" + "	}\n" + "	@page :left {\n" + "	  margin-left: 3cm;\n"
				+ "	}\n" + "\n" + "	@page :right {\n" + "	  margin-left: 4cm;\n" + "	}\n" + "	\n" + "}\n"
				+ "\n @media screen {\n" + "	body { width: 297mm;}\n" + "	.container {\n"
				+ "		padding: 3cm 2cm 2cm 2cm;\n" + "	}\n" + "}" + "hr {\n" + "	color: white;\n"
				+ "    border-color: white;\n" + "    border-width: 0;\n" + "}\n" + "h1 {\n"
				+ "	  page-break-before: always;\n" + "	}\n" + "	h1, h2, h3, h4, h5 {\n"
				+ "	  page-break-after: avoid;\n" + "	}\n" + "	table, figure {\n"
				+ "	  page-break-inside: avoid;\n" + "	}\n" + ".unbreakable\n" + "{\n" + "    display:inline-block;\n"
				+ "}\n" + ".unbreakable:after\n" + " {\n" + "    display:block;\n" + "    height:0px;\n"
				+ "    visibility: hidden;\n" + "}\n" + "\n" + "img {\n" + "	display:inline-block;\n" + "}\n" + "\n"
				+ "body {\n" + "	font-size: 12pt;\n" + "}\n" + "h1 {\n" + "	font-size: 18pt;\n"
				+ "	font-weight: bold;\n" + "}\n" + "h2 {\n" + "	font-size: 16pt;\n" + "	font-weight: bold;\n"
				+ "}\n" + "h3 {\n" + "	font-size: 14pt;\n" + "}\n" + ".text-eu {\n" + " display: inline-block;\n"
				+ " font-size: 10px;\n" + " vertical-align: middle;\n" + "}\n" + ".edited-by {\n" + " display: block;\n"
				+ " position: relative;\n" + " margin-top: 36px;\n" + " font-size: 20px;\n " + "}\n" + ".logos {\n"
				+ " display: block;\n" + " position: relative;\n" + " margin-top: 192px;\n" + "}\n" + "img.logoimg {\n"
				+ " object-fit: contain;\n" + "}\n" + ".nobreak {\n" + "	 page-break-before: unset;\n"
				+ " font-size: 36px;\n" + "}\n" + " video::-webkit-media-controls {\n" + " display: none;\n" + "}\n"
				+ ".isbn-info {\n" + "display: block;\n" + "position: relative;\n" + "margin-top: 144px;\n" + "}\n"
				+ ".creative-commons {\n" + "display: block;\n" + "position:relative;\n" + "margin-top: 12px;\n" + "}\n"
				+ ".margin-12 {\n" + "display: block;\n" + "position: relative;\n" + "margin-top: 12px;\n" + "}\n"
				+ ".margin-14 {\n" + "display: block;\n" + "position: relative;\n" + "margin-top: 14px;\n" + "}\n"
				+ ".biblio-reference {\n" + "display: block;\n" + " position: relative;\n" + " margin-top: 12px;\n"
				+ "}\n" + ".published-list {\n" + "display:block;\n" + "position: relative;\n" + "margin-top: 190px;\n"
				+ "text-align: center;\n" + "font-size: 14px;\n" + "}\n" + ".pre {\n"
				+ "    white-space: pre-wrap;       /* Since CSS 2.1 */\n"
				+ "    white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */\n"
				+ "    white-space: -pre-wrap;      /* Opera 4-6 */\n"
				+ "    white-space: -o-pre-wrap;    /* Opera 7 */\n"
				+ "    word-wrap: break-word;       /* Internet Explorer 5.5+ */\n" + ""
				+ "    font-family: sans-serif;\n" + "font-size: 11pt;\n" + "}\n" + "</style>";
		html += "</head>"
				// + "<body onload=\"window.print()\"><div class=\"container\">";
				+ "<body ><div class=\"container\">";
		// +"</body></html>";
		// org.jsoup.nodes.Document doc = Jsoup.parse(html, baseUri);
		// doc.body().attr("style", "margin: 0; color: #000; background-color: #fff;");
		// return doc.html();
		return html;
	}

	private String endGrammar(String html) {
		html += "</div></body></html>";
		return html;
	}

	private void addText(StringBuilder html, StringBuilder toc, GrammarPart part) {
		if (part == null)
			return;
		String head = "<p class='type_" + part.getGrammarPartTYpe().name() + "'>" + part.getGrammarPartName() + "</p>";
		String head1 = "<li>" + part.getGrammarPartName();
		switch (part.getGrammarPartTYpe()) {
		case PART:
			head = "<h1 class='type_" + part.getGrammarPartTYpe().name() + "'>" + part.getGrammarPartName() + "</h1>";
			break;
		case CHAPTER:
			head = "<h2 class='type_" + part.getGrammarPartTYpe().name() + "'>" + part.getGrammarPartName() + "</h2>";
			break;
		case PARAGRAPH:
			head = "<h3 class='type_" + part.getGrammarPartTYpe().name() + "'>" + part.getGrammarPartName() + "</h3>";
			break;
		case ARTICLE:
			head = "<h3 class='type_" + part.getGrammarPartTYpe().name() + "'>" + part.getGrammarPartName() + "</h3>";
			break;
		default:
			break;
		}
		String footer = "<div class='break_" + part.getGrammarPartTYpe().name() + "'></div>";
		toc.append(head1);
		html.append(head);
		if (!StringUtil.isNil(part.getHtml()))
			html.append(this.cleanBr(this.checkUrls(part.getHtml())));
		if (part.getParts() != null && !part.getParts().isEmpty()) {
			toc.append("<ul>");
			for (GrammarPart partIn : getOrderedGrammaParts(part.getParts())) {
				this.addText(html, toc, partIn);
			}
			toc.append("</ul>");
		}
		toc.append("</li>");
		html.append(footer);
	}

	private String cleanBr(String html) {
//		html = html.replaceAll("<br>","<br />");
//		html = html.replaceAll("<br style=\"mso-special-character:line-break\">", "<br />");
		return html;
	}

	private String checkUrls(String html) {
		if (html.contains("\"/api/rest/retrievePublic")) {
			String baseUri = Play.configuration.getProperty("fronthost", "https://testing02.eclettica.net");
			html = html.replaceAll("\"/api/rest/retrievePublic", "\"" + baseUri + "/api/rest/retrievePublic");
		}
		return html;
	}

	public static class GrammarSearchRequest {
		public String grammarId;
		public String q;
	}

	public String search(String grammarId, String q) {
		Logger.warn("searching... " + grammarId + " " + q);
		GrammarResponse gr = new GrammarResponse();
		GrammarObject go = new GrammarObject();
		Grammar g = Grammar.findById(grammarId);
		List<GrammarPart> parts = GrammarPart.search(g, q);
		Collections.sort(parts, new Comparator<GrammarPart>() {

			@Override
			public int compare(GrammarPart o1, GrammarPart o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;
				// se o1 < o2 deve restituire -1
				return o1.getCompleteOrder().compareTo(o2.getCompleteOrder());
			}

		});
		go.uuid = g.getUuid();
		go.grammarName = g.getGrammarName();
		go.grammarBibliographicalReference = g.getGrammarBibliographicalReference();
		go.grammarCopyrightInfo = g.getGrammarCopyrightInfo();
		go.grammarEditorialInfo = g.getGrammarEditorialInfo();
		go.grammarISBNInfo = g.getGrammarISBNInfo();
		go.grammarOtherSignHubSeries = g.getGrammarOtherSignHubSeries();
		go.grammarSignHubSeries = g.getGrammarSignHubSeries();
		go.grammarSignHubSeriesNumber = g.getGrammarSignHubSeriesNumber();
		go.creationDate = StringUtil.date(g.getCreationDate(), g.CREATION_DATE_FORMAT);
		go.revisionDate = StringUtil.date(g.getRevisionDate(), g.REVISION_DATE_FORMAT);
		go.grammarStatus = g.getGrammarStatus().name();
		go.isDeleted = g.getDeleted();
		go.parts = buildListGrammarPartsObjectFromPartListNoDeep(parts);
		go.author = g.getAuthor().getUuid();
		// go.editors = buildUsersList(g.getEditorList());
		// go.contentProviders = buildUsersList(g.getContentProviderList());
		go.status = g.getGrammarStatus().name();
		go.htmlAvailable = StringUtil.isNil(g.getHtmlPath()) ? false : true;
		go.pdfAvailable = StringUtil.isNil(g.getPdfPath()) ? false : true;
		gr.response = go;

		return gr.toJson();
	}

	private static List<GrammarPartObject> buildListGrammarPartsObjectFromPartListNoDeep(List<GrammarPart> parts) {
		List<GrammarPartObject> ret = new LinkedList<GrammarPartObject>();

		for (GrammarPart part : parts) {
			ret.add(new GrammarPartObject(part));
		}
		/*
		 * Collections.sort(ret, new Comparator<GrammarPartObject>() {
		 * 
		 * @Override public int compare(GrammarPartObject o1, GrammarPartObject o2) {
		 * return o1.grammarPartOrder.compareTo(o2.grammarPartOrder); } });
		 */
		return ret;
	}

	public String searchPost() {
		String json = apiService.getCurrentJson();

		GrammarSearchRequest req;
		try {
			req = (GrammarSearchRequest) apiService.buildObjectFromJson(json, GrammarSearchRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			if (req.grammarId == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			return search(req.grammarId, req.q);
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
	}

	private String buildTextWithTopics(GrammarPartObject grammarPartObject, String htmlTest) {
		String ret = "";
		String totest = htmlTest;

		String cleaned = Jsoup.parse(htmlTest).text();
		System.out.println(cleaned);
		String[] splitted = cleaned.split(" ");
		List<String> found = new LinkedList<String>();
		String toCheck;
		String sep;
		for (int i = 0; i < splitted.length;) {
			for (int y = this.maxLength; y > 0; y--) {
				if (i + y > splitted.length)
					continue;
				toCheck = "";
				sep = "";
				for (int k = 0; k < y; k++) {
					toCheck += sep + splitted[i + k];
					sep = " ";
				}
				toCheck = toCheck.trim();

				toCheck = cleanChars(toCheck);
				System.out.println("tocheck " + toCheck);
				if (this.topicsMap.get(y).containsKey(toCheck)) {
					System.out.println("found: " + toCheck);
					found.add(toCheck);
					i = i + (y - 1);
					break;
				}
			}
			i++;
		}

		Map<String, Topic> topicFound = new HashMap<String, Topic>();
		if (!found.isEmpty()) {
			for (int i = found.size() - 1; i >= 0; i--) {
				String s = found.get(i);
				System.out.println("topicizing: " + s);
				int ind = totest.lastIndexOf(s);
				if (ind >= 0) {
					String sub = totest.substring(ind);
					totest = totest.substring(0, ind);

					String sub1 = sub.substring(0, s.length());
					String sub2 = sub.substring(s.length());
					Topic t = this.topics.get(s);
					sub = "<mark tid='" + t.getUuid() + "'>" + sub1 + "</mark>" + sub2;
					// if(singleComponentArrayFacade.options.topics == null)
					topicFound.put(t.getUuid(), t);
					// System.out.println(sub);
					ret = sub + ret;
				} else {
					System.out.println("non topicized: " + s);
				}
			}
		}
		ret = totest + ret;
		System.out.println(ret);
		Gson gson = new Gson();
		String foundTopicString = gson.toJson(topicFound);
		System.out.println(foundTopicString);
		if (grammarPartObject.options == null)
			grammarPartObject.options = new HashMap<String, String>();
		grammarPartObject.options.put("topics", foundTopicString);
		return ret;
	}

	private String cleanChars(String toCheck) {
		if (StringUtil.isNil(toCheck))
			return toCheck;
		if (!Character.isLetter(toCheck.charAt(0)))
			return toCheck.substring(1);
		if (!Character.isLetter(toCheck.charAt(toCheck.length() - 1)))
			return toCheck.substring(0, toCheck.length() - 1);
		return toCheck;
	}

	private void buildTopicsMap() {
		this.topicsMap = new HashMap<Integer, Map<String, Topic>>();
		this.maxLength = 0;
		String[] splitted;
		for (String str : this.topics.keySet()) {
			System.out.println("toinsert " + str);
			splitted = null;
			splitted = str.split(" ");
			Integer len = splitted.length;
			if (!this.topicsMap.containsKey(len))
				this.topicsMap.put(len, new HashMap<String, Topic>());
			this.topicsMap.get(len).put(str, this.topics.get(str));
			if (len > this.maxLength)
				this.maxLength = len;
		}
	}

	public static void fixGrammarParts1(Grammar g) {
		List<GrammarPart> partsToFix = GrammarPart.find("grammar=:grammar and completeOrder != completeOrderNow")
				.setParameter("grammar", g).fetch();
		Logger.warn("Checking grammar: " + g.getGrammarName() + " number: " + partsToFix.size());
	}

	public static void fixGrammarParts(Grammar g) {
		List<GrammarPart> partsToFix = GrammarPart.find("grammar=:grammar and completeOrder != completeOrderNow")
				.setParameter("grammar", g).fetch();
		List<String> toRedistribute = new ArrayList<String>();
		List<String> toOrder = new ArrayList<String>();
		List<String> toOrderLast = new ArrayList<String>();
		for (GrammarPart gp : partsToFix) {
			Logger.warn("" + gp.getGrammarPartName() + " co:" + gp.getCompleteOrderString2() + " con:"
					+ gp.getCompleteOrderNowString2());
			int thisTheProblem = isThisTheProblem(gp);
			if (thisTheProblem == ISPROBLEM) {
				if (gp.thisIsArticle()) {
					toOrderLast.add(gp.getUuid());
				} else {
					toOrder.add(gp.getUuid());
				}
			} else if (thisTheProblem == REDISTRIBUTE) {
				toRedistribute.add(gp.getUuid());
			}
		}

		Iterator<String> it = toRedistribute.iterator();
		GrammarPart gp;
		String uuid;
		while (it.hasNext()) {
			uuid = it.next();
			gp = GrammarPart.findById(uuid);
			if (gp == null)
				continue;
			redistribute(gp);
			gp = null;
		}

		it = toOrder.iterator();
		while (it.hasNext()) {
			uuid = it.next();
			gp = GrammarPart.findById(uuid);
			if (gp == null)
				continue;
			String[] completeOrderNow = gp.getCompleteOrderNowString2().split("-");
			String s = completeOrderNow[completeOrderNow.length - 1];
			gp.setElementNumber(s);
			gp.setGrammarPartOrder(Integer.parseInt(s));
			gp.recalculateOrders();
			gp.save();
			gp = null;
		}

		it = toOrderLast.iterator();
		while (it.hasNext()) {
			uuid = it.next();
			gp = GrammarPart.findById(uuid);
			if (gp == null)
				continue;
			int listSize = gp.getParent().getParts().size();
			String grammarPartName = gp.getGrammarPartName();
			int order = -1;
			if (grammarPartName.equals("Information on data and consultants")) {
				order = listSize - 2;
			} else if (grammarPartName.equals("References")) {
				order = listSize - 1;
			} else if (grammarPartName.equals("Authorship information")) {
				order = listSize;
			} else {
				Logger.warn("ATTENZIONEEEEEEEE");
			}
			gp.setElementNumber(order + "");
			gp.setGrammarPartOrder(order);
			gp.recalculateOrders();
			gp.save();
			gp = null;
		}
	}

	private static void redistribute(GrammarPart gp) {
		// String[] completeOrder = gp.getCompleteOrderString2().split("-");

		String cpn = gp.getCompleteOrderNowString2();
		Logger.warn("REDISTRIBUTING " + cpn + " " + gp.getUuid());
		String[] completeOrderNow = gp.getCompleteOrderNowString2().split("-");
		GrammarPart basePart = getBasePart(gp);
		for (int i = 1; i < completeOrderNow.length - 1; i++) {
			basePart = getChildPart(basePart, i, completeOrderNow[i], gp);
			if (basePart == null) {
				Logger.warn("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA---------------- "
						+ gp.getCompleteOrderNowString2());
				return;
			}
		}
		gp.setParent(basePart);
		Logger.warn("REDISTRIBUTE per: " + cpn + " " + basePart.getCompleteOrderNowString2() + " "
				+ basePart.getCompleteOrderString2() + "-" + gp.getGrammarPartOrder() + " "
				+ gp.getCompleteOrderNowString2());

		int r = isThisTheProblem(gp);
		if (r == REDISTRIBUTE || r == ISPROBLEM) {
			String s = completeOrderNow[completeOrderNow.length - 1];
			gp.setElementNumber(s);
			gp.setGrammarPartOrder(Integer.parseInt(s));

		}
		gp.recalculateOrders();
		gp.save();

		// Logger.warn("REDISTRIBUTE per: " + gp.getUuid() + " " + basePart.getUuid());
		/*
		 * Logger.warn("REDISTRUBUTE check parents");
		 * Logger.warn("REDISTRUBUTE parent 0 " + basePart.getUuid()); GrammarPart gpp =
		 * basePart.getParent(); for(int i = 0; i < 10; i++) {
		 * Logger.warn("REDISTRUBUTE parent " + (i+1) + " " + gpp.getUuid()); gpp =
		 * gpp.getParent(); if(gpp == null) break; }
		 * Logger.warn("REDISTRUBUTE check parents ----");
		 */
		// gp.recalculateOrders();
		// gp.save();
	}

	private static GrammarPart getChildPart(GrammarPart basePart, int indice, String elemento, GrammarPart gp2) {
		Logger.warn("getChildPart indice:" + indice + " basePartUuid:" + basePart.getUuid() + " tofind:" + elemento);
		for (GrammarPart gp : basePart.getParts()) {
			String[] splitted = gp.getCompleteOrderNowString2().split("-");
			Logger.warn("checking indice:" + indice + " splitted:" + splitted[indice] + " tofind:" + elemento);
			if (splitted[indice].equals(elemento) && !gp2.equals(gp) && splitted.length == indice + 1) {
				Logger.warn("found: " + gp.getUuid());
				return gp;
			}
		}
		return null;
	}

	private static GrammarPart getBasePart(GrammarPart gp) {
		if (gp.getParent() == null)
			return gp;
		return getBasePart(gp.getParent());
	}

	private static int isThisTheProblem(GrammarPart gp) {
		String[] completeOrder = gp.getCompleteOrderString2().split("-");
		String[] completeOrderNow = gp.getCompleteOrderNowString2().split("-");
		return _isThisTheProblem(completeOrder, completeOrderNow);
		// return(!completeOrder[completeOrder.length-1].equals(completeOrderNow[completeOrderNow.length-1]));
	}

	public static int _isThisTheProblem(String[] completeOrder, String[] completeOrderNow) {
		if (completeOrder.length != completeOrderNow.length) {
			Logger.warn("ATTENZIONE!!!");
			return REDISTRIBUTE;
		}
		if (!completeOrder[completeOrder.length - 1].equals(completeOrderNow[completeOrderNow.length - 1])) {
			for (int i = 0; i < completeOrder.length - 1; i++) {
				if (!completeOrder[i].equals(completeOrderNow[i])) {
					Logger.warn("Doppio problema?");
					return REDISTRIBUTE;
				}
			}
		}
		if (!completeOrder[completeOrder.length - 1].equals(completeOrderNow[completeOrderNow.length - 1]))
			return ISPROBLEM;
		return NOTPROBLEM;
	}

	public void fixIntonation(Grammar g) {
		if (g == null)
			return;
		for (GrammarPart gp : g.getParts()) {
			if (!gp.getGrammarPartName().equals("PART 2 Phonology"))
				continue;
			for (GrammarPart gpc : gp.getParts()) {
				if (!gpc.getGrammarPartName().equals("Chapter 2. Prosody"))
					continue;
				boolean found = false;
				for (GrammarPart gpp : gpc.getParts()) {
					if (gpp.getGrammarPartName().equals("2.3. Intonation")) {
						found = true;
						break;
					}
				}
				if (!found) {
					GrammarPart gp2 = new GrammarPart();
					gp2.setAuthor(gpc.getAuthor());
					List<User> contentProviderList = new ArrayList<User>();
					contentProviderList.add(gpc.getAuthor());
					gp2.setContentProviderList(contentProviderList);
					gp2.setDeleted(false);
					List<User> editorList = new ArrayList<User>();
					editorList.add(gpc.getAuthor());
					gp2.setEditorList(editorList);
					gp2.setElementNumber("3");
					gp2.setGrammarPartName("2.3. Intonation");
					gp2.setGrammarPartOrder(3);
					gp2.setGrammarPartStatus(GrammarPartStatus.DRAFT);
					gp2.setGrammarPartTYpe(GrammarPartType.PARAGRAPH);
					gp2.setHtml("");
					gp2.setParent(gpc);
					gp2.setParts(new LinkedList<GrammarPart>());
					gp2.save();
					gpc.getParts().add(gp2);
					gpc.save();
				}
			}
		}
	}

	public void removePart(Grammar g) {
		if (g == null)
			return;
		for (GrammarPart gp : g.getParts()) {
			if (!gp.getGrammarPartName().equals("PART 2 Phonology"))
				continue;
			for (GrammarPart gpc : gp.getParts()) {
				if (!gpc.getGrammarPartName().equals("Chapter 2. Prosody"))
					continue;
				for (GrammarPart gpcc : gpc.getParts()) {
					if (!gpcc.getGrammarPartName().equals("2.2. Above the lexical level"))
						continue;
					for (GrammarPart gpp : gpcc.getParts()) {
						if (gpp.getGrammarPartName().equals("2.2.5. Attenuative")) {
							gpp.setDeleted(true);
							gpp.save();
							break;
						}
					}
				}
			}
		}
	}

	public void fixTense(Grammar g) {
		if (g == null)
			return;
		for (GrammarPart gp : g.getParts()) {
			if (!gp.getGrammarPartName().equals("PART 4 Morphology"))
				continue;
			for (GrammarPart gpc : gp.getParts()) {
				if (!gpc.getGrammarPartName().equals("Chapter 2. Derivation"))
					continue;
				for (GrammarPart gpcc : gpc.getParts()) {
					if (!gpcc.getGrammarPartName().equals("2.2. Non-manual markers of derivation"))
						continue;
					boolean found = false;
					for (GrammarPart gpp : gpcc.getParts()) {
						if (gpp.getGrammarPartName().equals("2.2.5. Attenuative")) {
							found = true;
							break;
						}
					}
					if (!found) {
						User u = gpc.getAuthor();
						List<User> contentProviderList = new ArrayList<User>();
						contentProviderList.add(u);
						List<User> editorList = new ArrayList<User>();
						editorList.add(u);
						GrammarPart gp3 = new GrammarPart();
						gp3.setAuthor(u);
						gp3.setContentProviderList(contentProviderList);
						gp3.setDeleted(false);
						gp3.setEditorList(editorList);
						gp3.setElementNumber("5");
						gp3.setGrammarPartName("2.2.5. Attenuative");
						gp3.setGrammarPartOrder(5);
						gp3.setGrammarPartStatus(GrammarPartStatus.DRAFT);
						gp3.setGrammarPartTYpe(GrammarPartType.PARAGRAPH);
						gp3.setHtml("");
						gp3.setParent(gpcc);
						gp3.setParts(new LinkedList<GrammarPart>());
						gp3.save();
						gpcc.getParts().add(gp3);
						gpcc.save();
					}
				}
			}
		}
	}

	public static void fixNames() {
		List<GrammarPart> parts = GrammarPart.find("grammarPartName=:grammarPartName")
				.setParameter("grammarPartName", "List of conventions observed in this book").fetch();
		if (parts != null)
			for (GrammarPart gp : parts) {
				gp.setGrammarPartName("List of notational conventions");
				gp.save();
			}
	}

	public void rebuildCompleteOrderNow(Grammar g) {
		for (GrammarPart gp : g.getParts()) {
			rebuildCompleteOrderNow(gp);
		}
	}

	private void rebuildCompleteOrderNow(GrammarPart gp) {
		if (gp == null)
			return;
		gp.calculateCompleteOrderNow();
		if (gp.getParts() != null) {
			for (GrammarPart gpp : gp.getParts()) {
				rebuildCompleteOrderNow(gpp);
			}
		}
	}

	public void fixGrammar(Grammar g) {
		if (g == null)
			return;
		int count = 0;
		for (int i = 0; i < g.getParts().size(); i++) {
			if (count == 0) {
				User u = g.getParts().get(i).getAuthor();
				List<User> contentProviderList = new ArrayList<User>();
				contentProviderList.add(u);
				List<User> editorList = new ArrayList<User>();
				editorList.add(u);
				GrammarPart newGp = new GrammarPart();
				newGp.setAuthor(u);
				newGp.setContentProviderList(contentProviderList);
				newGp.setDeleted(false);
				newGp.setEditorList(editorList);
				newGp.setElementNumber("1");
				newGp.setGrammarPartName("List of authors");
				newGp.setGrammarPartOrder(1);
				newGp.setGrammarPartStatus(GrammarPartStatus.DRAFT);
				newGp.setGrammarPartTYpe(GrammarPartType.ARTICLE);
				newGp.setHtml("");
				newGp.setParent(null);
				newGp.setParts(new LinkedList<GrammarPart>());
				newGp.save();
				g.getParts().add(newGp);
				g.save();
				count++;
			}

			if (!g.getParts().get(i).getGrammarPartName().equals("List of authors")) {

				int oldIndex = Integer.parseInt(g.getParts().get(i).getElementNumber());
				String newIndex = String.valueOf(oldIndex + 1);
				g.getParts().get(i).setElementNumber(newIndex);
				g.getParts().get(i).setGrammarPartOrder(g.getParts().get(i).getGrammarPartOrder() + 1);
				g.save();

			}

		}
	}

	public void fixAppendixAndDeleteOther() {
		List<GrammarPart> parts = GrammarPart.find("grammarPartName=:grammarPartName")
				.setParameter("grammarPartName", "Appendix 1").fetch();
		if (parts != null)
			for (GrammarPart gp : parts) {
				gp.setGrammarPartName("Appendices");
				gp.save();
			}

		for (int i = 2; i <= 5; i++) {
			List<GrammarPart> parts1 = GrammarPart.find("grammarPartName=:grammarPartName")
					.setParameter("grammarPartName", "Appendix " + i).fetch();
			if (parts1 != null)
				for (GrammarPart gp : parts1) {
					gp.setDeleted(true);
					gp.save();
				}
		}

		List<GrammarPart> parts2 = GrammarPart.find("grammarPartName=:grammarPartName")
				.setParameter("grammarPartName", "Affiliation info of authors").fetch();
		if (parts2 != null)
			for (GrammarPart gp : parts2) {
				gp.setDeleted(true);
				gp.save();
			}

		List<GrammarPart> parts21 = GrammarPart.find("grammarPartName=:grammarPartName")
				.setParameter("grammarPartName", "Index").fetch();
		if (parts21 != null)
			for (GrammarPart gp : parts21) {
				gp.setDeleted(true);
				gp.save();
			}

	}

	public void fixNameCompleteList() {
		List<GrammarPart> parts = GrammarPart.find("grammarPartName=:grammarPartName")
				.setParameter("grammarPartName", "Complete List of References/Bibliography").fetch();
		if (parts != null)
			for (GrammarPart gp : parts) {
				gp.setGrammarPartName("Complete list of references");
				int oldIndex = Integer.parseInt(gp.getElementNumber());
				String newIndex = String.valueOf(oldIndex - 4);
				gp.setElementNumber(newIndex);
				gp.setGrammarPartOrder(oldIndex - 4);
				gp.save();
			}
	}

	public void fixPosGrammaticalTerms() {
		List<GrammarPart> parts = GrammarPart.find("grammarPartName=:grammarPartName")
				.setParameter("grammarPartName", "Glossary of grammatical terms").fetch();
		if (parts != null)
			for (GrammarPart gp : parts) {
				int oldIndex = Integer.parseInt(gp.getElementNumber());
				String newIndex = String.valueOf(oldIndex - 4);
				gp.setElementNumber(newIndex);
				gp.setGrammarPartOrder(oldIndex - 4);
				gp.save();
			}
	}

	public static void fixNamesAbbreviations() {
		List<GrammarPart> parts = GrammarPart.find("grammarPartName=:grammarPartName")
				.setParameter("grammarPartName", "List of Abbreviations").fetch();
		if (parts != null)
			for (GrammarPart gp : parts) {
				gp.setGrammarPartName("List of abbreviations");
				gp.save();
			}
	}

}
