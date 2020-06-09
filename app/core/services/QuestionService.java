/**
 *  
 */
package core.services;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.services.ApiService.OKResponseObject;
import core.services.SlideService.SlideObject;
import models.Option;
import models.Question;
import models.Question.TransitionType;
import models.Slide;
import models.Test;
import models.Test.TestType;
import models.User;
import models.User.ToolsTypes;
import play.Logger;
import utils.StringUtil;

/**
 *
 * @author luca
 */
public class QuestionService {

	private static enum QuestionServiceSingleton {
		INSTANCE;

		QuestionService singleton = new QuestionService();

		public QuestionService getSingleton() {
			return singleton;
		}
	}

	public static QuestionService instance() {
		return QuestionService.QuestionServiceSingleton.INSTANCE.getSingleton();
	}

	private static ApiService apiService = ApiService.instance();
	private static OptionService optionService = OptionService.instance();
	private static SlideService slideService = SlideService.instance();
	private static UserService userService = UserService.instance();

	public static class QuestionObjectSmall {
		public String questionId;
		public List<String> slides;
		public List<String> transitionType;
		public Map<String, String> options;
		public Boolean toEdit;
		public Boolean deleted;
		public String name;
	}

	public static class QuestionObjectComplete {
		public String questionId;
		public List<SlideObject> slides;
		public List<String> transitionType;
		public Map<String, String> options;
		public Boolean toEdit;
		public Boolean deleted;
		public String name;
		public Integer order;
	}

	public static class CreateQuestionRequest {
		public String TestId;
		public QuestionObjectComplete question;
	}

	public static class CreateQuestionResponse extends OKResponseObject {
		public QuestionObjectSmall response;
	}

	public String createQuestion() {

		String json = apiService.getCurrentJson();
		CreateQuestionRequest req;
		QuestionObjectComplete questionToCreate;
		try {
			req = (CreateQuestionRequest) apiService.buildObjectFromJson(json, CreateQuestionRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			questionToCreate = req.question;
			// if (questionToCreate == null) {
			// return apiService.buildNotWellFormedJsonErrorResponse();
			// }
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (StringUtil.isNil(req.TestId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Test test = Test.findById(req.TestId);
		if (test == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}
		
		if(!test.getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(getToolType(test))) {
			if (test.isDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		// if (StringUtil.isNil(questionToCreate.name)) {
		// return apiService.buildMandatoryParamsErrorResponse();
		// }

		Question question = new Question();
		question.setTest(test);
		if (questionToCreate != null) {
			if (!StringUtil.isNil(questionToCreate.name)) {
				question.setName(questionToCreate.name);
			}

			if (!StringUtil.isNil(questionToCreate.transitionType)) {
				List<TransitionType> transitionTypes = TransitionType
						.tryBuildTransitionTypesFromValues(questionToCreate.transitionType);
				if (transitionTypes == null) {
					return apiService.buildWrongTransitionTypeFieldErrorResponse();
				}
				question.setupTransitionTypes(transitionTypes);
			}

			if (questionToCreate.options != null && !questionToCreate.options.isEmpty()) {
				optionService.createOptions(question, questionToCreate.options);
			}

			// DISABILITATO PERCHE' DALL'INTERFACCIA UTENTE NON C'E MODO DI
			// CREARE
			// GIA' LE SLIDE
			// ALL'ATTO DELLA CREAZIONE DI UNA QUESTION. SE DOVESSE ESSERE
			// AGGIUNTA
			// LA FUNZIONALITA'
			// OCCORRE VALIDARE OGNI SINGOLO SlideObject prima di proseguire!
			// if (questionToCreate.slides != null &&
			// !questionToCreate.slides.isEmpty()) {
			// slideService.createSlides(question, questionToCreate.slides);
			// }
		}

		question.save();

		if (questionToCreate == null || StringUtil.isNil(questionToCreate.name)) {
			question.setName("NEW_QUESTION_" + question.getUuid());
			question.save();
		}

		test.addQuestion(question);
		test.save();

		question.save();

		QuestionObjectSmall questionObjSmall = buildQuestionObjectSmallFromQuestion(question);
		CreateQuestionResponse res = new CreateQuestionResponse();
		res.response = questionObjSmall;
		String ret = res.toJson();
		return ret;
	}

	public static String getToolType(Test test) {
		if(test == null)
			return null;
		if(test.getType().equals(TestType.ATLAS))
			return ToolsTypes.ATLAS;
		else if(test.getType().equals(TestType.TESTINGTOOL))
			return ToolsTypes.TESTING;
		return null;
	}

	public String updateQuestion(String questionId) {

		if (StringUtil.isNil(questionId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Question question = Question.findById(questionId);
		if (question == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}
		
		if(!question.getTest().getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(question.getTest()))) {
			if (question.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(question.getTest()))) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(question.getTest().getAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		String json = apiService.getCurrentJson();
		CreateQuestionRequest req;
		QuestionObjectComplete questionToUpdate;
		try {
			req = (CreateQuestionRequest) apiService.buildObjectFromJson(json, CreateQuestionRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			questionToUpdate = req.question;
			if (questionToUpdate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		// potrebbero arrivare stati non presenti nella enum
		List<TransitionType> transitionTypes = TransitionType
				.tryBuildTransitionTypesFromValues(questionToUpdate.transitionType);
		if (transitionTypes != null) {
			question.setupTransitionTypes(transitionTypes);
		} else {
			return apiService.buildWrongTransitionTypeFieldErrorResponse();
		}

		if (!StringUtil.isNil(questionToUpdate.name)) {
			question.setName(questionToUpdate.name);
		}

		if (questionToUpdate.options != null && !questionToUpdate.options.isEmpty()) {
			optionService.updateOptions(question, questionToUpdate.options);
		}

		// DISABILITATO PERCHE' DALL'INTERFACCIA UTENTE NON C'E MODO DI EDITARE
		// LE SLIDE
		// ALL'ATTO DELL'EDIT DI UNA QUESTION. SE DOVESSE ESSERE AGGIUNTA
		// LA FUNZIONALITA'
		// OCCORRE VALIDARE OGNI SINGOLO SlideObject prima di proseguire!
		// slideService.updateSlides(questionToUpdate.slides);

		question.save();

		QuestionObjectSmall questionObjSmall = buildQuestionObjectSmallFromQuestion(question);
		CreateQuestionResponse res = new CreateQuestionResponse();
		res.response = questionObjSmall;
		String ret = res.toJson();
		return ret;
	}

	public List<QuestionObjectComplete> buildQuestionObjectCompleteListFromQuestionList(List<Question> questions, Boolean isPlay) {
		List<QuestionObjectComplete> ret = new LinkedList<QuestionObjectComplete>();
		if (questions != null && !questions.isEmpty()) {
			for (Question question : questions) {
				if(question==null || question.getDeleted())
					continue;
				QuestionObjectComplete questionObjectComplete = buildQuestionObjectCompleteFromQuestion(question, isPlay);
				ret.add(questionObjectComplete);
			}
		}
		return ret;
	}

	

	public void updateQuestions(Test test, List<QuestionObjectComplete> questions) {
		if (questions != null && !questions.isEmpty()) {
			for (QuestionObjectComplete questionObjectComplete : questions) {
				Question question = Question.findById(questionObjectComplete.questionId);
				if (question == null) {
					continue;
				}

				if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(question.getTest()))) {
					if (question.getDeleted()) {
						continue;
					}
				}

				if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(question.getTest()))) {
					User currentUserLogged = userService.getCurrentUserLogged();
					if (!currentUserLogged.equals(question.getTest().getAuthor())) {
						continue;
					}
				}

				// potrebbero arrivare stati non presenti nella enum
				List<TransitionType> transitionType = TransitionType
						.tryBuildTransitionTypesFromValues(questionObjectComplete.transitionType);
				if (transitionType == null)
					continue;

				question.setupTransitionTypes(transitionType);

				optionService.updateOptions(question, questionObjectComplete.options);
				slideService.updateSlides(questionObjectComplete.slides);

				test.save();
			}
		}
	}

	public static class QuestionsListResponse extends OKResponseObject {
		public List<QuestionObjectSmall> response;
	}

	public String questionsList(String TestId, boolean deleted) {
		if (StringUtil.isNil(TestId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Test test = Test.findById(TestId);
		if (test == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(test))) {
			if (test.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(test))) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(test.getAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		QuestionsListResponse res = new QuestionsListResponse();

		List<Question> optObjs = new LinkedList<Question>();
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(getToolType(test))) {
			//optObjs = test.getQuestions();
			optObjs = test.retrieveQuestionsByDeleted(deleted);
		} else {
			optObjs = test.retrieveQuestionsByDeleted(false);
		}

		List<QuestionObjectSmall> optObjsSmall = new LinkedList<QuestionObjectSmall>();
		if (optObjs != null && !optObjs.isEmpty()) {
			for (Question q : optObjs) {
				QuestionObjectSmall questionObjSmall = buildQuestionObjectSmallFromQuestion(q);
				optObjsSmall.add(questionObjSmall);
			}
		}
		res.response = optObjsSmall;
		String ret = res.toJson();
		return ret;
	}

	public QuestionObjectSmall buildQuestionObjectSmallFromQuestion(Question question) {
		QuestionObjectSmall questionObjSmall = new QuestionObjectSmall();
		Set<Option> oo = question.getOptions();
		questionObjSmall.options = optionService.buildOptionsMapFromOptionsList(oo);
		questionObjSmall.questionId = question.getUuid();
		questionObjSmall.slides = slideService.buildSlideIdListFromSlideList(question.getSlides());
		if (/*userService.hasCurrentUserLoggedAdminRole()*/userService.checkAdminAndTool(getToolType(question.getTest()))) {
			questionObjSmall.toEdit = true;
		} else {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (currentUserLogged.equals(question.getTest().getAuthor())) {
				questionObjSmall.toEdit = true;
			} else {
				questionObjSmall.toEdit = false;
			}
		}
		if (question.getTransitionType() != null)
			questionObjSmall.transitionType = TransitionType
					.buildTransitionTypesStringListFromString(question.getTransitionType());
		questionObjSmall.name = question.getName();

		questionObjSmall.deleted = question.getDeleted();

		return questionObjSmall;
	}

	public static class UpdateQuestionRequest {
		public String questionId;
		public QuestionObjectSmall question;
	}

	public static class UpdateQuestionResponse {
		public QuestionObjectSmall response;
	}

	public static class GetQuestionRequest {
		public String questionId;
		public Boolean complete;
	}

	public static class GetQuestionObjectSmallResponse extends OKResponseObject {
		public QuestionObjectSmall response;
	}

	public static class GetQuestionObjectCompleteResponse extends OKResponseObject {
		public QuestionObjectComplete response;
	}

	public String getQuestion(String questionId, Boolean complete) {
		if (StringUtil.isNil(questionId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Question question = Question.findById(questionId);
		if (question == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(question.getTest()))) {
			if (question.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (complete == null) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		String ret;
		if (complete) {
			GetQuestionObjectCompleteResponse res = new GetQuestionObjectCompleteResponse();
			res.response = buildQuestionObjectCompleteFromQuestion(question);
			ret = res.toJson();
		} else {
			GetQuestionObjectSmallResponse res = new GetQuestionObjectSmallResponse();
			res.response = buildQuestionObjectSmallFromQuestion(question);
			ret = res.toJson();
		}
		return ret;
	}

	private QuestionObjectComplete buildQuestionObjectCompleteFromQuestion(Question question) {
		return buildQuestionObjectCompleteFromQuestion(question, false);
	} 

	public QuestionObjectComplete buildQuestionObjectCompleteFromQuestion(Question question, Boolean isPlay) {
		QuestionObjectComplete questionObjectComplete = new QuestionObjectComplete();
		if (question != null) {
			Set<Option> ss = question.getOptions();
			questionObjectComplete.options = optionService.buildOptionsMapFromOptionsList(ss);
			questionObjectComplete.questionId = question.getUuid();
			Set<Slide> sss = question.getSlides();
			questionObjectComplete.slides = slideService.buildSlideObjectListFromSlideList(sss, isPlay);
			System.out.println("buildQuestionObjectCompleteFromQuestion " + questionObjectComplete.slides.size());
			if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(getToolType(question.getTest()))) {
				questionObjectComplete.toEdit = true;
			} else {
				User currentUserLogged = userService.getCurrentUserLogged();
				if (currentUserLogged.equals(question.getTest().getAuthor())) {
					questionObjectComplete.toEdit = true;
				} else {
					questionObjectComplete.toEdit = false;
				}
			}
			if (question.getTransitionType() != null)
				questionObjectComplete.transitionType = TransitionType
						.buildTransitionTypesStringListFromString(question.getTransitionType());

			questionObjectComplete.deleted = question.getDeleted();
			questionObjectComplete.name = question.getName();
			questionObjectComplete.order = question.getOrder();
		}
		return questionObjectComplete;
	}

	public static class DeleteQuestionRequest {
		public String questionId;
	}

	public static class DeleteQuestionResponse extends OKResponseObject {
		public Object response = new Object();
	}

	public String deleteQuestion(String questionId) {

		if (StringUtil.isNil(questionId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		Question question = Question.findById(questionId);
		if (question == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}
		
		if(!question.getTest().getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}

//		if (!userService.hasCurrentUserLoggedAdminRole()) {
//			User currentUserLogged = userService.getCurrentUserLogged();
//			if (!currentUserLogged.equals(question.getTest().getAuthor())) {
//				return apiService.buildNotAuthorizedResponse();
//			}
//		}

		if (question.getDeleted()) {
			return apiService.buildEntityAlreadyDeletedErrorResponse();
		}

		question.setDeleted(true);
		question.save();

		DeleteQuestionResponse res = new DeleteQuestionResponse();

		String ret = res.toJson();
		return ret;

	}

	public static class ImportQuestionRequest {
		public String questionId;
		public String testId;
	}

	public static class ImportQuestionResponse extends OKResponseObject {
		public QuestionObjectSmall response;
	}

	public String importQuestion() {

		String json = apiService.getCurrentJson();
		ImportQuestionRequest req;
		try {
			req = (ImportQuestionRequest) apiService.buildObjectFromJson(json, ImportQuestionRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (StringUtil.isNil(req.testId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Test test = Test.findById(req.testId);
		if (test == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(test))) {
			if (test.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}
		if(!test.getCanEdit(userService)) {
			return apiService.buildNotAuthorizedResponse();
		}

//		if (!userService.hasCurrentUserLoggedAdminRole()) {
//			User currentUserLogged = userService.getCurrentUserLogged();
//			if (!currentUserLogged.equals(test.getAuthor())) {
//				return apiService.buildNotAuthorizedResponse();
//			}
//		}

		if (StringUtil.isNil(req.questionId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		Question question = Question.findById(req.questionId);
		if (question == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(question.getTest()))) {
			if (question.getDeleted()) {
				return apiService.buildEntityDoesNotExistsErrorResponse();
			}
		}

		if (/*!userService.hasCurrentUserLoggedAdminRole()*/!userService.checkAdminAndTool(getToolType(question.getTest()))) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(question.getTest().getAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		/*if (test.getQuestions().contains(question)) {
			return apiService.buildSlideAlreadyBelongsToQuestionErrorResponse();
		}*/

		try {
			Question cloned = question.clone(test, true);
			if (cloned == null) {
				return apiService.buildGenericErrorResponse();
			}
			ImportQuestionResponse res = new ImportQuestionResponse();
			res.response = buildQuestionObjectSmallFromQuestion(cloned);

			String ret = res.toJson();
			return ret;

		} catch (Exception ex) {
			Logger.error(ex, "Exception during Question clonation (question: %s, testId: %s)", req.questionId,
					req.testId);
			return apiService.buildGenericErrorResponse();
		}
	}

	public Map<String, String> buildOptionsMapFromQuestionsList(List<Question> questions) {
		Map<String, String> ret = new LinkedHashMap<String, String>();
		if (questions != null && !questions.isEmpty()) {
			for (Question question : questions) {
				if(question!=null && question.getName()!=null)
					ret.put(question.getUuid(), question.getName());
			}
		}
		return ret;
	}

	public List<Question> cloneQuestionsList(Test test, List<Question> questions) {
		List<Question> ret = new LinkedList<>();
		if (questions != null && !questions.isEmpty()) {
			Logger.warn("Cloning " + questions.size() + " questions");
			int i = 0;
			for (Question question : questions) {
				if(question==null){
					Logger.error("Sono nel for e question null??? testId: " + test.getUuid());
					continue;
				}
				i++;
				if(question.getDeleted()) {
					Logger.warn("Test clone... skipping deleted question;");
				}
				try {
					Question clonedQuestion = question.clone(test, false);
					if (clonedQuestion != null) {
						Logger.warn("Test clone; " + i + "q " + question.getUuid() + " " + clonedQuestion.getUuid());
						ret.add(clonedQuestion);
					} else {
						throw new Exception("Clone question return null");
					}
				} catch (Exception ex) {
					Logger.error(ex, "Exception while cloning question %s for clone test %s", question.getUuid(),
							test.getUuid());
					continue;
				}
			}
		}
		return ret;
	}
}