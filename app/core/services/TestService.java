/**
 *  
 */
package core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Transient;

import com.google.gson.Gson;

import core.services.ApiService.OKResponseObject;
import core.services.FeatureService.FeatureMultipleOptionsElements;
import core.services.QuestionService.QuestionObjectComplete;
import models.Feature;
import models.FeatureArea;
import models.Grammar;
import models.GrammarPart;
import models.Option;
import models.Question;
import models.Report;
import models.SignLanguage;
import models.Slide;
import models.Test;
import models.Test.TestStatus;
import models.Test.TestType;
import models.User;
import models.Feature.FeatureType;
import models.User.Roles;
import models.User.ToolsTypes;
import play.Logger;
import utils.StringUtil;

/**
 *
 * @author luca
 */
public class TestService {

	private static enum TestServiceSingleton {
		INSTANCE;

		TestService singleton = new TestService();

		public TestService getSingleton() {
			return singleton;
		}
	}

	public static TestService instance() {
		return TestService.TestServiceSingleton.INSTANCE.getSingleton();
	}

	private List<SignLanguage> signLanguages;
	
	private TestService() {
		this.languages = createLanguages();
	}

	private static ApiService apiService = ApiService.instance();
	private static OptionService optionService = OptionService.instance();
	private static QuestionService questionService = QuestionService.instance();
	private static UserService userService = UserService.instance();
	
	private static List<Language> languages;

	public static class TestObjectSmall {
		public String TestId;
		public String TestName;
		public String authorId;
		public Boolean toEdit;
		public Boolean deleted;
		public String state;
		public String revId;
		public Map<String, String> options;
		public Map<String, String> questions;
		public String creationDate;
		public String revisionDate;
		public List<String> editors;
		public List<String> contentProviders;
		public String reportId;
		public String globalReportId;
		public boolean canEdit;
	}

	public static class TestObjectComplete {

		public String TestId;

		public String TestName;

		public String authorId;

		public Boolean deleted;

		public String state;

		public String revId;

		public Boolean toEdit;

		public List<QuestionObjectComplete> questions;

		public Map<String, String> options;
		
		public List<String> editors;
		public List<String> contentProviders;

		public String reportId;

	}

	public static class CreateTestRequest {
		public TestObjectComplete test;
	}

	public static class CreateTestResponse extends OKResponseObject {
		public TestObjectComplete response;
	}

	public static class UpdateTestResponse extends OKResponseObject {
		public TestObjectSmall response;
	}

	public String createTest(TestType type) {
		Test test = new Test();
		test.setAuthor(userService.getCurrentUserLogged());
		test.setRevisionDate(new Date());
		test.save();
		test.setTestName("NEW_TEST_" + test.getUuid());
		test.setType(type);
		if(!test.getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}
		test.save();
		TestObjectComplete testObjectComplete = buildTestObjectCompleteFromTest(test, false);
		CreateTestResponse res = new CreateTestResponse();
		res.response = testObjectComplete;
		String ret = res.toJson();
		return ret;

	}


	public String updateTest(String testId) {
		if (StringUtil.isNil(testId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		Test test = Test.findById(testId);
		if (test == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(test))) {
			if (test.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}
		if(!test.getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}
		/*if (!userService.hasCurrentUserLoggedAdminRole()) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(test.getAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}*/

		String json = apiService.getCurrentJson();
		CreateTestRequest req;
		TestObjectComplete testToUpdate;
		try {
			req = (CreateTestRequest) apiService.buildObjectFromJson(json, CreateTestRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			testToUpdate = req.test;
			if (testToUpdate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		// potrebbero arrivare stati non presenti nella enum
		TestStatus testStatus = TestStatus.tryBuildTestStatusFromName(testToUpdate.state);
		if (testStatus != null) {
			test.setState(testStatus);
		} else {
			return apiService.buildWrongTestStatusFieldErrorResponse();
		}

		if (testToUpdate.deleted != null) {
			test.setDeleted(testToUpdate.deleted);
		}

		if (!StringUtil.isNil(testToUpdate.TestName)) {
			test.setTestName(testToUpdate.TestName);
		}

		if (testToUpdate.options != null) {
			optionService.updateOptions(test, testToUpdate.options);
		}
		
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(test))) {
			if(testToUpdate.contentProviders!=null)
				test.setContentProviderList(buildUserListFromString(testToUpdate.contentProviders));
			if(testToUpdate.editors!=null)
				test.setEditorList(buildUserListFromString(testToUpdate.editors));
		}
		

		// DISABILITATO PERCHE' DALL'INTERFACCIA UTENTE NON C'E MODO DI EDITARE
		// LE QUESTIONS
		// ALL'ATTO DELLA MODIFICA DI UN TEST (SE SI RIATTIVA OCCORRE TESTARE E,
		// SOPRATTUTTO,
		// EFFETTUARE LA VALIDAZIONE DI OGNI SOTTOOGGETTO [Es. SlideObject,
		// etc.]!)
		// if (testToUpdate.questions != null) {
		// questionService.updateQuestions(test, testToUpdate.questions);
		// }

		// TODO creare una nuova entity con new Date a revision date e mettere a
		// deleted la precedente + legare i vari test con un id comune
		// (transazionale)
		test.setRevisionDate(new Date());
		test.save();

		UpdateTestResponse res = new UpdateTestResponse();
		res.response = buildTestObjectSmallFromTest(test);
		String ret = res.toJson();
		return ret;
	}

	public static class TestListResponse extends OKResponseObject {
		public List<TestObjectSmall> response;
	}

	
	public String testList(TestType type, String orderby, String ordertype, Boolean complete) {
		Logger.warn("test list:" + type.name() + " " + orderby);
		List<Test> testObjs = new LinkedList<Test>();
		Map<String,Report> rl = null;
		Map<String,Report> rgl = null;
//		if (userService.hasCurrentUserLoggedAdminRole()) {
//			testObjs = Test.findAllOrdered(type, orderby, ordertype);
//		} else {
			if(type.equals(TestType.ATLAS)) { 
				if(userService.userCan(Auths.AT_MANAGE_TESTS))
					testObjs = Test.findAllByDeletedOrdered(false, type, orderby, ordertype);
				else if(userService.userCan(Auths.AT_PLAY_TESTS))
						testObjs = Test.findAllByContentProviderOrdered(userService.getCurrentUserLogged(), type, orderby, ordertype);
				else if(/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(ToolsTypes.ATLAS)) {
					testObjs = Test.findAllOrdered(type, orderby, ordertype);
				}
				Report r = null;		
				rl = new HashMap<String, Report>();
				rgl = new HashMap<String, Report>();
				for(Test t: testObjs) {
					r = Report.findByTestAndAuthor(userService.getCurrentUserLogged(), t);
					if(r!=null)
						rl.put(t.getUuid(), r);
					r = Report.findGlobalByTest(t);
					if(r!=null)
						rgl.put(t.getUuid(), r);
				}
			} else {
				List<Test> testObjsL = Test.findAllByDeletedOrdered(false, type, orderby, ordertype);
				testObjs = new LinkedList<Test>();
				for(Test to : testObjsL) {
					if(to.getCanView(userService))
						testObjs.add(to);
				}
			}
//		}
		List<TestObjectSmall> testObjsSmall = buildTestObjectSmallListFromTestList(testObjs, rl, rgl, userService.getCurrentUserLogged(), complete);
		TestListResponse res = new TestListResponse();
		res.response = testObjsSmall;
		String ret = res.toJson();
		return ret;
	}

	public List<TestObjectSmall> buildTestObjectSmallListFromTestList(List<Test> tests, Map<String, Report> reportList, Map<String, Report> globalReportList, User user, Boolean complete) {
		if (complete == null)
			complete = true;
		List<TestObjectSmall> testObjsSmall = new LinkedList<TestObjectSmall>();
		if (tests != null && !tests.isEmpty()) {
			for (Test test : tests) {
				TestObjectSmall testObjSmall = buildTestObjectSmallFromTest(test, complete);
				if(reportList!= null && reportList.containsKey(test.getUuid()))
					testObjSmall.reportId = reportList.get(test.getUuid()).getUuid();
				if(globalReportList!= null && globalReportList.containsKey(test.getUuid()))
					testObjSmall.globalReportId = globalReportList.get(test.getUuid()).getUuid();
				testObjSmall.canEdit = test.getCanEdit(userService);
				testObjsSmall.add(testObjSmall);
			}
		}
		return testObjsSmall;
	}

	public static class GetTestObjectSmallResponse extends OKResponseObject {
		public TestObjectSmall response;
	}

	public static class GetTestObjectCompleteResponse extends OKResponseObject {
		public TestObjectComplete response;
	}

	public String getTest(String testId, Boolean complete, Boolean isPlay) {
		if(StringUtil.isNil(isPlay))
			isPlay = false;
		if (StringUtil.isNil(testId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Test test = Test.findById(testId);
		if (test == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(test))) {
			if (test.isDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (complete == null) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Report r = Report.findByTestAndAuthor(userService.getCurrentUserLogged(), test);
		String ret;
		if (complete) {
			GetTestObjectCompleteResponse res = new GetTestObjectCompleteResponse();
			res.response = buildTestObjectCompleteFromTest(test, isPlay);
			if(r!=null) {
				res.response.reportId = r.getUuid();
			}
			ret = res.toJson();
		} else {
			GetTestObjectSmallResponse res = new GetTestObjectSmallResponse();
			res.response = buildTestObjectSmallFromTest(test);
			if(r!=null) {
				res.response.reportId = r.getUuid();
			}
			ret = res.toJson();
		}
		return ret;
	}
	
	public TestObjectSmall buildTestObjectSmallFromTest(Test test) {
		return buildTestObjectSmallFromTest(test, true);
	}

	
	public TestObjectSmall buildTestObjectSmallFromTest(Test test, Boolean complete) {
		TestObjectSmall ret = new TestObjectSmall();
		ret.authorId = test.getAuthor().getUuid();
		ret.TestId = test.getUuid();
		ret.TestName = test.getTestName();
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(test))) {
			ret.toEdit = true;
		} else {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (currentUserLogged.equals(test.getAuthor())) {
				ret.toEdit = true;
			} else {
				ret.toEdit = false;
			}
		}
		ret.toEdit = test.getCanEdit(userService);
		ret.deleted = test.isDeleted();
		ret.state = test.getState().name();
		Set<Option> s = test.getOptions();
		ret.options = optionService.buildOptionsMapFromOptionsList(s);
		if(complete != null && complete)
			ret.questions = questionService.buildOptionsMapFromQuestionsList(test.getQuestions());
		ret.creationDate = StringUtil.date(test.getCreationDate(), Test.SMALL_DATE_FORMAT);
		ret.revisionDate = StringUtil.date(test.getRevisionDate(), Test.SMALL_DATE_FORMAT);
		ret.revId = test.buildRevisionDateFormatted();
		ret.editors = buildUsersList(test.getEditorList());
		ret.contentProviders = buildUsersList(test.getContentProviderList());
		
		return ret;
	}
	
	private List<String> buildUsersList(List<User> userList) {
		List<String> ret = new LinkedList<String>();
		if(userList!=null){
			for(User u : userList)
				ret.add(u.getUuid());
		}
		return ret;
	}

	private List<User> buildUserListFromString(List<String> userUuids) {
		List<User> ret = new LinkedList<User>();
		if(userUuids!=null)
			for(String uuid : userUuids){
				User u = User.findById(uuid);
				if(u!=null)
					ret.add(u);
			}
		return ret;
	}

	public TestObjectComplete buildTestObjectCompleteFromTest(Test test, Boolean isPlay) {
		TestObjectComplete ret = new TestObjectComplete();
		if (test != null) {
			ret.authorId = test.getAuthor().getUuid();
			ret.deleted = test.isDeleted();
			Set<Option> oo = test.getOptions();
			ret.options = optionService.buildOptionsMapFromOptionsList(oo);
			List<Question> qq = test.getQuestions();
			ret.questions = questionService.buildQuestionObjectCompleteListFromQuestionList(qq, isPlay);
			ret.revId = test.buildRevisionDateFormatted();
			ret.state = test.getState().name();
			ret.TestId = test.getUuid();
			ret.TestName = test.getTestName();
			ret.editors = buildUsersList(test.getEditorList());
			ret.contentProviders = buildUsersList(test.getContentProviderList());
			
			if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(test))) {
				ret.toEdit = true;
			} else {
				User currentUserLogged = userService.getCurrentUserLogged();
				if (currentUserLogged.equals(test.getAuthor())) {
					ret.toEdit = true;
				} else {
					ret.toEdit = false;
				}
			}
		}
		return ret;
	}

	public static class CloneTestRequest {
		public String testId;
	}

	public static class CloneTestResponse extends OKResponseObject {
		public TestObjectSmall response;
	}

	public static class DeleteTestResponse extends OKResponseObject {
		public Object response = new Object();
	}

	public String deleteTest(String testId) {
		if (StringUtil.isNil(testId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		Test test = Test.findById(testId);
		if (test == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(QuestionService.getToolType(test))) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(test.getAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		if (test.isDeleted()) {
			return apiService.buildEntityAlreadyDeletedErrorResponse();
		}

		test.setDeleted(true);
		test.save();

		DeleteTestResponse res = new DeleteTestResponse();

		String ret = res.toJson();
		return ret;
	}

	public String cloneTest() {

		String json = apiService.getCurrentJson();
		CloneTestRequest req;
		try {
			req = (CloneTestRequest) apiService.buildObjectFromJson(json, CloneTestRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (StringUtil.isNil(req.testId)) {
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		Test toClone = Test.findById(req.testId);
		if (toClone == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		try {
			Test cloned = toClone.clone();
			if (cloned == null) {
				return apiService.buildGenericErrorResponse();
			}
			CloneTestResponse res = new CloneTestResponse();
			TestObjectSmall testObjectSmall = buildTestObjectSmallFromTest(cloned);
			res.response = testObjectSmall;
			Logger.warn("Test cloned! orig " + toClone.getUuid() + " cloned " + cloned.getUuid());
			String ret = res.toJson();
			return ret;
		} catch (Exception ex) {
			Logger.error(ex, "Exception during Test clonation (test: %s)", req.testId);
			return apiService.buildGenericErrorResponse();
		}

	}

	public static class GetStandaloneTestRequest {

	}

	public static class GetStandaloneTestResponse extends OKResponseObject {

	}

	// TODO
	public GetStandaloneTestResponse getStandaloneTest(GetStandaloneTestRequest req) {
		return null;
	}

	public String sortQuestions() {
		String json = apiService.getCurrentJson();
		try {
			TestOrderQuestionRequest req = (TestOrderQuestionRequest) apiService.buildObjectFromJson(json, TestOrderQuestionRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			String testToUpdate = req.testId;
			if (testToUpdate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			Test test = Test.findById(testToUpdate);
			if(test == null)
				return apiService.buildEntityDoesNotExistsErrorResponse();
			
			if(!test.getCanEdit(userService)) {
				return apiService.buildNotAuthorizedResponse();
			}
			
			List<Question> tosave = new ArrayList<Question>();
			for(int i = 0; i<req.questions.size(); i++) {
				Question q = Question.findById(req.questions.get(i).question);
				if(q == null)
					return apiService.buildNotWellFormedJsonErrorResponse();
				q.setOrder(req.questions.get(i).order);
				tosave.add(q);
			}
			
			for(Question question : tosave){
				question.save();
			}
			
			return instance().getTest(testToUpdate, true, false);
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
	}
	
	

	public static class TestOrderQuestionRequest {
		String testId;
		List<TestOrderQuestionOrderRequest> questions;
	}
	
	public static class TestOrderQuestionOrderRequest {
		String question;
		Integer order;
	}
	
	public String sortSlides() {
		String json = apiService.getCurrentJson();
		try {
			TestOrderSlidesRequest req = (TestOrderSlidesRequest) apiService.buildObjectFromJson(json, TestOrderSlidesRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			String questionToUpdate = req.questionId;
			if (questionToUpdate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			Question q = Question.findById(questionToUpdate);
			if (q == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			if(!q.getTest().getCanEdit(userService)) {
				return apiService.buildNotAuthorizedResponse();
			}
			List<Slide> tosave = new ArrayList<Slide>();
			for(int i = 0; i<req.slides.size(); i++) {
				Slide s = Slide.findById(req.slides.get(i).slide);
				if(s == null)
					return apiService.buildNotWellFormedJsonErrorResponse();
				if(!s.getQuestion().equals(q))
					return apiService.buildNotWellFormedJsonErrorResponse();
				s.setOrder(req.slides.get(i).order);
				tosave.add(s);
			}
			
			for(Slide slide : tosave){
				slide.save();
			}
			
			return instance().getTest(q.getTest().getUuid(), true, false);
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
	}
	
	public static class TestOrderSlidesRequest {
		String questionId;
		List<TestOrderSlidesOrderRequest> slides;
	}
	
	public static class TestOrderSlidesOrderRequest {
		String slide;
		Integer order;
	}
	
	public static class Language {
		public String code;
		public String name;
		
		public Language(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public Language(SignLanguage l) {
			this.code = l.getCode();
			this.name = l.getName();
		}
	}
	
	List<Language> createLanguages() {
        List<Language> list = new LinkedList<Language>();
        list.add(new Language("ise", "Italian Sign Language"));
        list.add(new Language("fsl", "French Sign Language"));
		list.add(new Language("syy", "Al-Sayyid Bedouin Sign Language"));
		list.add(new Language("dmr", "Adamarobe Sign Language"));
		list.add(new Language("ase", "American Sign Language"));
		list.add(new Language("aed", "Argentine Sign Language"));
		list.add(new Language("asf", "Australian Sign Language"));
		list.add(new Language("asw", "Australian Aboriginal Sign Language"));
		list.add(new Language("asq", "Austrian Sign Language"));
		list.add(new Language("bfk", "Ban Khor Sign Language"));
		list.add(new Language("bzs", "Brazilian Sign Language"));
		list.add(new Language("bfi", "British Sign Language"));
		list.add(new Language("bqn", "Bulgarian Sign Language"));
		list.add(new Language("cbd", "Cambodian Sign Language"));
		list.add(new Language("csc", "Catalan Sign Language"));
		list.add(new Language("csg", "Chilean Sign Language"));
		list.add(new Language("cls", "Chinese Sign Language"));
		list.add(new Language("cst", "Cistercian Sign Language"));
		list.add(new Language("csn", "Colombian Sign Language"));
		list.add(new Language("cng", "Congolese Sign Language"));
		list.add(new Language("hrv", "Croatian Sign Language"));
		list.add(new Language("cse", "Czech Sign Language"));
		list.add(new Language("dls", "Danish Sign Language"));
		list.add(new Language("esl", "Egyptian Sign Language"));
		list.add(new Language("eth", "Ethiopian Sign Language"));
		list.add(new Language("fil", "Filipino Sign Language"));
		list.add(new Language("fse", "Finnish Sign Language"));
		list.add(new Language("nld", "Flemish Sign Language"));
		list.add(new Language("gus", "French-African Sign Language"));
		list.add(new Language("deu", "German Sign Language"));
		list.add(new Language("gss", "Greek Sign Language"));
		list.add(new Language("guy", "Guyana Sign Language"));
		list.add(new Language("haf", "Hai Phong Sign Language"));
		list.add(new Language("hab", "Ha Noi Sign Language"));
		list.add(new Language("hsl", "Hausa Sign Language"));
		list.add(new Language("hks", "Hong Kong Sign Language"));
		list.add(new Language("hos", "Ho Chi Minh Sign Language"));
		list.add(new Language("icl", "Icelandic Sign Language"));
		list.add(new Language("ins", "Indian Sign Language"));
		list.add(new Language("isn", "Indo-Pakistani Sign Language"));
		list.add(new Language("ils", "International Sign Language"));
		list.add(new Language("iks", "Inuit Sign Language"));
		list.add(new Language("isg", "Irish Sign Language"));
		list.add(new Language("isr", "Israeli Sign Language"));
		list.add(new Language("jls", "Jamaican Sign Language"));
		list.add(new Language("jsl", "Japanese Sign Language"));
		list.add(new Language("jos", "Jordanian Sign Language"));
		list.add(new Language("xki", "Kenyan Sign Language"));
		list.add(new Language("kvk", "Korean Sign Language"));
		list.add(new Language("leb", "Lebanese Sign Language"));
		list.add(new Language("lbs", "Libyan Sign Language"));
		list.add(new Language("bog", "Malian Sign Language"));
		list.add(new Language("bga", "Malinese Sign Language"));
		list.add(new Language("mrd", "Mardin Sign Language"));
		list.add(new Language("nrs", "Maritime Sign Language"));
		list.add(new Language("mre", "Martha’s Vineyard Sign Language"));
		list.add(new Language("lsy", "Mauritian Sign Language"));
		list.add(new Language("mfs", "Mexican Sign Language"));
		list.add(new Language("vsi", "Moldova Sign Language"));
		list.add(new Language("xms", "Moroccan Sign Language"));
		list.add(new Language("nzs", "New Zealand Sign Language"));
		list.add(new Language("ncs", "Nicaraguan Sign Language"));
		list.add(new Language("ncd", "North Central Desert Sign Language"));
		list.add(new Language("nls", "Norwegian Sign Language"));
		list.add(new Language("pys", "Paraguayan Sign Language"));
		list.add(new Language("psd", "Plains Indian Sign Language"));
		list.add(new Language("pso", "Polish Sign Language"));
		list.add(new Language("psr", "Portuguese Sign Language"));
		list.add(new Language("prz", "Providence Island Sign Language"));
		list.add(new Language("psl", "Puerto Rican Sign Language"));
		list.add(new Language("fcs", "Quebec Sign Language"));
		list.add(new Language("rus", "Russian Sign Language"));
		list.add(new Language("rwd", "Rwandan Sign Language"));
		list.add(new Language("swm", "Sawmill Sign Language"));
		list.add(new Language("bqy", "Desa Kolok (Kata Kolok) Sign Language"));
		list.add(new Language("ngt", "Netherlands Sign Language"));
		list.add(new Language("svk", "Slovakian Sign Language"));
		list.add(new Language("sfs", "South African Sign Language"));
		list.add(new Language("ssp", "Spanish Sign Language"));
		list.add(new Language("swl", "Swedish Sign Language"));
		list.add(new Language("sgg", "Swiss-German Sign Language"));
		list.add(new Language("tss", "Taiwan Sign Language"));
		list.add(new Language("tsq", "Thai Sign Language"));
		list.add(new Language("tsm", "Turkish Sign Language"));
		list.add(new Language("ugn", "Ugandan Sign Language"));
		list.add(new Language("ukl", "Ukrainian Sign Language"));
		list.add(new Language("ugy", "Uruguayan Sign Language"));
		list.add(new Language("vsl", "Venezuelan Sign Language"));
		list.add(new Language("ygs", "Yolngu Sign Language"));
        return list;
    }
	
	public List<Language> getLanguages(){
		if(this.languages == null) {
			//this.languages = this.createLanguages();
			List<SignLanguage> languages = SignLanguage.all().fetch();
			this.languages = buildLanguagesFromModelList(languages);
		}
		return this.languages;
	}
	
	public static class SignLanguageWrapper {
		public String code;
		public String name;
		public String usersDescription;
		public String deafCulture;
		public String deafEducation;
		public String linguisticStudies;
		public String countries;
		public String area;
		public String coordinates;
		public String grammarReference;
		public String uuid;
		public String cpPHON;
		public String cpLEX;
		public String cpMORPH;
		public String cpSYN;
		public String cpPRAG;
		public String ack;
		
		public SignLanguageWrapper(SignLanguage sl) {
			this.code = sl.getCode();
			this.name = sl.getName();
			this.usersDescription = sl.getUsersDescription();
			this.deafCulture = sl.getDeafCulture();
			this.deafEducation = sl.getDeafEducation();
			this.linguisticStudies = sl.getLinguisticStudies();
			this.countries = sl.getCountries();
			this.area = sl.getArea();
			this.coordinates = sl.getCoordinates();
			this.grammarReference = sl.getGrammarReference();
			this.uuid = sl.getUuid();
			this.cpPHON = sl.getCpPHON();
			this.cpLEX = sl.getCpLEX();
			this.cpMORPH = sl.getCpMORPH();
			this.cpSYN = sl.getCpSYN();
			this.cpPRAG = sl.getCpPRAG();
			this.ack = "";
			String sep = "";
			if(!StringUtil.isNil(sl.getAckPHON())) {
				ack += sep + sl.getAckPHON();
				sep += "</br>";
			}
			if(!StringUtil.isNil(sl.getAckLEX())) {
				ack += sep + sl.getAckLEX();
				sep += "</br>";
			}
			if(!StringUtil.isNil(sl.getAckMORPH())) {
				ack += sep + sl.getAckMORPH();
				sep += "</br>";
			}
			if(!StringUtil.isNil(sl.getAckSYN())) {
				ack += sep + sl.getAckSYN();
				sep += "</br>";
			}
			if(!StringUtil.isNil(sl.getAckPRAG())) {
				ack += sep + sl.getAckPRAG();
				sep += "</br>";
			}
			
		}
		
		public SignLanguageWrapper() {
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public List<SignLanguageWrapper> getSignLanguages(){
		if(this.signLanguages == null) {
			this.signLanguages = SignLanguage.all().fetch();
		}
		List<SignLanguageWrapper> ret = new LinkedList<SignLanguageWrapper>();
		for(SignLanguage sl : this.signLanguages) {
			sl = createFakeInfo(sl);
			ret.add(new SignLanguageWrapper(sl));
		}
		return ret;
	}
	
	private SignLanguage createFakeInfo(SignLanguage sl) {
		if(sl == null)
			return null;
		if(StringUtil.isNil(sl.getCoordinates()))
			sl.setCoordinates("[42.6384261,12.674297]");
		if(StringUtil.isNil(sl.getCountries()))
			sl.setCountries("country");
		
//		sl.setUsersDescription("85.000 deaf people, 150.000 hard-of hearing people");
//		sl.setDeafCulture("Theater, Deaf clubs");
//		sl.setDeafEducation("pre-school, high school");
//		sl.setLinguisticStudies("corpus data by...");
//		sl.setStatus("sign language law, National Congress of Brazil, since 2002");
//		sl.setArea("Europe");
//		sl.setAuthor("Ronice Müller de Quadros");
		if(StringUtil.isNil(sl.getUsersDescription()))
			sl.setUsersDescription("N/A");
		if(StringUtil.isNil(sl.getDeafCulture()))
			sl.setDeafCulture("N/A");
		if(StringUtil.isNil(sl.getDeafEducation()))
			sl.setDeafEducation("N/A");
		if(StringUtil.isNil(sl.getLinguisticStudies()))
			sl.setLinguisticStudies("N/A");
		if(StringUtil.isNil(sl.getStatus()))
			sl.setStatus("N/A");
		if(StringUtil.isNil(sl.getArea()))
			sl.setArea("N/A");
		if(StringUtil.isNil(sl.getAuthor()))
			sl.setAuthor("N/A");
		
		return sl;
	}

	public static class SignLanguageResponse extends OKResponseObject {
		SignLanguageWrapper response;
	}
	
	public SignLanguageResponse getSignLanguage(String id, String code){
		SignLanguageResponse res = new SignLanguageResponse();
		SignLanguage sl = null;
		if(!StringUtil.isNil(id)) {
			sl = SignLanguage.findById(id);
		} else if(!StringUtil.isNil(code)) {
			sl = SignLanguage.findByCode(code);
		}
		if(sl == null)
			return new SignLanguageResponse();
		sl = createFakeInfo(sl);
		Grammar g = Grammar.findBySignLanguageAndVersion(sl, "English");
		if(g!=null) {
			GrammarPart gp = GrammarPart.findPartsByName("Chapter 2. The sign language community", g);
			for(GrammarPart gpp : gp.getParts()) {
				if(gpp == null)
					continue;
				switch (gpp.getGrammarPartName()) {
					case "2.1. Community characteristics":
						sl.setUsersDescription(gpp.getHtml());
						break;
					case "2.2. Sign language users":
						sl.setUsersDescription(gpp.getHtml());
						break;
					case "2.3. Deaf culture":
						sl.setDeafCulture(gpp.getHtml());
						break;
					case "2.4. Deaf education":
						sl.setDeafEducation(gpp.getHtml());
						break;
					case "Information on data and consultants":
						//sl.setUsersDescription(gpp.getHtml());
						break;
					case "References":
						//sl.setUsersDescription(gpp.getHtml());
						break;
					case "Authorship information":
						sl.setAuthor(gpp.getHtml());
						break;
						
				}
			}
			sl.setGrammarReference(g.getUuid());
		}
		res.response = new SignLanguageWrapper(sl);
		return res;
	}

	private List<Language> buildLanguagesFromModelList(List<SignLanguage> languages) {
		List<Language> ret = new ArrayList<Language>();
		for(SignLanguage l : languages) {
			if(l == null)
				continue;
			ret.add(new Language(l));
		}
		return ret;
	}

	public static class AtlasManagementRequest {
		List<AtlasManagementDetailReq> management; 
	}
	
	public static class AtlasManagementDetailReq {
		String testId;
		List<String> contentProviderIds;
	}
	
	public String atlasManagement() {
		String json = apiService.getCurrentJson();
		try {
			AtlasManagementRequest req = (AtlasManagementRequest) apiService.buildObjectFromJson(json, AtlasManagementRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			
			for(AtlasManagementDetailReq amr : req.management) {
				String testId = amr.testId;
				if(StringUtil.isNil(testId)) {
					Logger.warn("Test Service atlasManagement skipping");
					continue;
				}
				Test t = Test.findById(testId);
				if(t == null) {
					Logger.warn("Test Service atlasManagement skipping testId: " + testId);
					continue;
				}
				t.getContentProviderList().clear();
				t.save();
				for(String cid : amr.contentProviderIds) {
					if(StringUtil.isNil(cid)) {
						Logger.warn("Test Service atlasManagement skipping cid");
						continue;
					}
					User u = User.findById(cid);
					if(u == null) {
						Logger.warn("Test Service atlasManagement skipping user id: " + cid);
						continue;
					}
					t.getContentProviderList().add(u);
				}
				t.save();
			}
			return apiService.buildGenericPositiveResponse();
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
	}


	public void extractSignLanguages(String path, String filename) {
		if (StringUtil.isNil(path) || StringUtil.isNil(filename))
			return;
		File f = new File(path, filename);
		if (!f.exists()) {
			Logger.info("file don't exists " + f.getAbsolutePath());
			return;
		}
		if (!f.isFile() || f.isDirectory()) {
			Logger.info("file is not a file??? " + f.getAbsolutePath());
			return;
		}
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";

		try {
			Gson gson = new Gson();
			br = new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null) {

				String[] fields = line.split(cvsSplitBy);

				if (fields.length > 0) {
					String realName = fields[0];
					String realCode = fields[1];
					String latitude = fields[2];
					String longitude = fields[3];
					String oldname = null;
					if(fields.length == 5)
						oldname = fields[4];
					Logger.warn("Elaboro: " + realName + " " + oldname);
					SignLanguage sl = null;
					if(!StringUtil.isNil(oldname)) {
						sl = SignLanguage.findByName(oldname.trim());
						Logger.error("E' segnalato un vecchio valore... ma non è stato trovato");
					} 
					if(sl == null) {
						sl = SignLanguage.findByName(realName.trim());
					}
					
					if(sl == null) {
						Logger.warn("Non trovata");
						sl = new SignLanguage();
						sl.setCode(realCode.trim());
					}
					sl.setOfficialCode(realCode.trim());
					sl.setName(realName.trim());
					sl.setCoordinates("["+latitude.trim()+","+longitude.trim()+"]");
					Logger.warn(gson.toJson(sl));
					sl.save();
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}