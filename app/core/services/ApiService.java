/**
 *  
 */
package core.services;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import controllers.eSecurePlay1.Auth.Play_Session_Keys;
import play.Logger;
import play.mvc.Http.Request;
import play.mvc.Scope.RenderArgs;
import utils.JsonUtils;
import utils.StringUtil;
import utils.cfg.CfgUtil;

/**
 *
 * @author luca
 */
public class ApiService {

	private static enum ApiServiceSingleton {
		INSTANCE;

		ApiService singleton = new ApiService();

		public ApiService getSingleton() {
			return singleton;
		}
	}

	public static ApiService instance() {
		return ApiService.ApiServiceSingleton.INSTANCE.getSingleton();
	}

	public static class ErrorObject {
		public Integer errorCode;
		public String errorMessage;
	}

	public static enum ResponseStatus {
		OK, NOK
	}

	public static abstract class ResponseObject {
		public List<ErrorObject> errors = new LinkedList<ErrorObject>();

		public String toJson() {
			return JsonUtils.toJson(this);
		}
		public void addError(String errorMessage, Integer errorCode) {
			ErrorObject errObj = new ErrorObject();
			errObj.errorMessage = errorMessage;
			errObj.errorCode = errorCode;
			this.errors.add(errObj);
		}
	}

	public static class OKResponseObject extends ResponseObject {
		public String status = ResponseStatus.OK.name();
	}

	public static class NOKResponseObject extends ResponseObject {
		public String status = ResponseStatus.NOK.name();

		public Object response = new Object();

		
	}
	
	public String buildGenericPositiveResponse() {
		OKResponseObject res = new OKResponseObject();
		String json = res.toJson();
		return json;
	}

	public String getJsonFromRequest(Request request) {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(request.body, writer, request.encoding);
		} catch (IOException e) {
			Logger.error(e, "Error during body copy in a StringWriter.");
		}
		String theString = writer.toString();
		Logger.warn("request body string:" + theString);
		if (StringUtil.isNil(theString)) {
			return null;
		} else {
			return theString;
		}
	}

	public Object buildObjectFromJson(String jsonBody, Class outputObjectClassType) {
		Object req = JsonUtils.fromJson(jsonBody, outputObjectClassType, true);
		return req;
	}

	public static enum ApiErrors {
		GENERIC_ERROR("Generic Error", 1), HTTP_VERB_NOT_ALLOWED_ERROR("Http Verb not allowed", 2), FORBIDDEN_ERROR(
				"You can not perform this action",
				3), WRONG_CREDENTIALS_ERROR("Wrong Credentials", 4), NOT_WELL_FORMED_JSON_ERROR(
						"Not well formed input JSON",
						5), NOT_VALID_MAIL_ERROR("Specify a valid email json field", 6), MANDATORY_PARAMS_ERROR(
								"Some mandatory params omitted", 7), ENTITY_ALREADY_EXISTS_ERROR("Entity already exist",
										8), ENTITY_DOES_NOT_EXISTS_ERROR("Entity does not exist",
												9), ENTITY_ALREADY_DELETED_ERROR("Entity already deleted",
														9), WRONG_ENDPOINT_ERROR("Wrong endpoint called",
																10), MUST_SPECIFY_VALID_FILE_EXTENSION_ERROR(
																		"You must specify a valid file extension",
																		11), NOT_VALID_MEDIA_TYPE_ERROR(
																				"Not valid media type",
																				12), UNAUTHORIZED_ERROR(
																						"You are not authorized to perform this action",
																						13), NOT_VALID_ROLE_ERROR(
																								"Specify a valid role json field",
																								14), WRONG_TEST_STATUS_FIELD(
																										"Specify a valid field for test status",
																										15), WRONG_TRANSITION_TYPE_FIELD(
																												"Specify a valid field for transition type",
																												16), WRONG_SLIDE_TYPE_FIELD(
																														"Specify a valid field for slide type",
																														17), SLIDE_ALREADY_BELONGS_TO_QUESTION(
																																"Slide already belongs to the question",
																																18), WRONG_COMPONENT_TYPE_FIELD(
																																		"Specify a valid field for component type",
																																		19), NOT_VALID_USER_VALIDATION_CODE(
																																				"Some errors during code validation",
																																				20), PASSWORDS_DOES_NOT_MATCH(
																																						"Passwords does not matchs",
																																						21), NOT_VALID_TEST_TYPE_ERROR(
																																								"Test type not valid",
																																								22);

		private ApiErrors(String errorMessage, Integer errorCode) {
			this.errorMessage = errorMessage;
			this.errorCode = errorCode;
		}

		private String errorMessage;

		private Integer errorCode;

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public Integer getErrorCode() {
			return errorCode;
		}

		public void setErrorCode(Integer errorCode) {
			this.errorCode = errorCode;
		}
	}

	public static enum Play_Specific_Session_Keys {
		CURR_USER_LOGGED_HAS_ADMIN_ROLE("curr.esaccount.isAdmin"), CURR_USER_LOGGED_HAS_CONTENT_PROVIDER_ROLE(
				"curr.esaccount.isContentProvider"), CURR_USER_LOGGED_HAS_USER_ROLE("curr.esaccount.isUser"),
		CURR_USER_LOGGED_HAS_TOOL("curr.esaccount.tool");

		private Play_Specific_Session_Keys(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static enum AuthorizedHttpVerb {
		GET, POST, OPTIONS, DELETE, PUT
	}

	public String buildHttpVerbNotAllowedResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.HTTP_VERB_NOT_ALLOWED_ERROR.getErrorMessage(),
				ApiErrors.HTTP_VERB_NOT_ALLOWED_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildForbiddenResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.FORBIDDEN_ERROR.getErrorMessage(), ApiErrors.FORBIDDEN_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildGenericErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.GENERIC_ERROR.getErrorMessage(), ApiErrors.GENERIC_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildNotWellFormedJsonErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.NOT_WELL_FORMED_JSON_ERROR.getErrorMessage(),
				ApiErrors.NOT_WELL_FORMED_JSON_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildNotValidMailErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.NOT_VALID_MAIL_ERROR.getErrorMessage(), ApiErrors.NOT_VALID_MAIL_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildMandatoryParamsErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.MANDATORY_PARAMS_ERROR.getErrorMessage(),
				ApiErrors.MANDATORY_PARAMS_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildWrongEndpointErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.WRONG_ENDPOINT_ERROR.getErrorMessage(), ApiErrors.WRONG_ENDPOINT_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildEntityAlreadyExistsErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.ENTITY_ALREADY_EXISTS_ERROR.getErrorMessage(),
				ApiErrors.ENTITY_ALREADY_EXISTS_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildEntityDoesNotExistsErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.ENTITY_DOES_NOT_EXISTS_ERROR.getErrorMessage(),
				ApiErrors.ENTITY_DOES_NOT_EXISTS_ERROR.getErrorCode());
		String ret = res.toJson();
		return ret;
	}

	public String buildMustSpecifyValidFileExtensionErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.MUST_SPECIFY_VALID_FILE_EXTENSION_ERROR.getErrorMessage(),
				ApiErrors.MUST_SPECIFY_VALID_FILE_EXTENSION_ERROR.getErrorCode());
		String ret = res.toJson();
		return ret;
	}

	public String buildEntityAlreadyDeletedErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.ENTITY_ALREADY_DELETED_ERROR.getErrorMessage(),
				ApiErrors.ENTITY_DOES_NOT_EXISTS_ERROR.getErrorCode());
		String ret = res.toJson();
		return ret;
	}

	public String getCurrentJson() {
		String json = (String) RenderArgs.current().get(Play_Session_Keys.CURR_PARSED_JSON.getValue());
		return json;
	}

	public File retrieveMediaDirectory() {
		File destDir = new File(CfgUtil.getString("media.repository.path", "/usr/cini/media_repository"));
		return destDir;
	}

	public void createMediaDirectory() {
		File destDir = new File(CfgUtil.getString("media.repository.path", "/usr/cini/media_repository"));
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
	}

	public String buildNotValidMediaTypeErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.NOT_VALID_MEDIA_TYPE_ERROR.getErrorMessage(),
				ApiErrors.NOT_VALID_MEDIA_TYPE_ERROR.getErrorCode());
		String ret = res.toJson();
		return ret;
	}
	
	public String buildNotValidTestTypeErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.NOT_VALID_TEST_TYPE_ERROR.getErrorMessage(),
				ApiErrors.NOT_VALID_TEST_TYPE_ERROR.getErrorCode());
		String ret = res.toJson();
		return ret;
	}

	public String buildNotAuthorizedResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.UNAUTHORIZED_ERROR.getErrorMessage(), ApiErrors.UNAUTHORIZED_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildNotValidRoleErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.NOT_VALID_ROLE_ERROR.getErrorMessage(), ApiErrors.NOT_VALID_ROLE_ERROR.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildWrongTestStatusFieldErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.WRONG_TEST_STATUS_FIELD.getErrorMessage(),
				ApiErrors.WRONG_TEST_STATUS_FIELD.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildWrongTransitionTypeFieldErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.WRONG_TRANSITION_TYPE_FIELD.getErrorMessage(),
				ApiErrors.WRONG_TRANSITION_TYPE_FIELD.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public List<String> buildAuthorizedHttpVerbs(AuthorizedHttpVerb... httpVerbs) {
		List<String> ret = new LinkedList<String>();
		if (httpVerbs != null && httpVerbs.length != 0) {
			for (AuthorizedHttpVerb httpVerb : httpVerbs) {
				ret.add(httpVerb.name());
			}
		}
		return ret;
	}

	public String buildWrongSlideTypeFieldErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.WRONG_SLIDE_TYPE_FIELD.getErrorMessage(),
				ApiErrors.WRONG_SLIDE_TYPE_FIELD.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildSlideAlreadyBelongsToQuestionErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.SLIDE_ALREADY_BELONGS_TO_QUESTION.getErrorMessage(),
				ApiErrors.SLIDE_ALREADY_BELONGS_TO_QUESTION.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildWrongComponentTypeFieldErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.WRONG_COMPONENT_TYPE_FIELD.getErrorMessage(),
				ApiErrors.WRONG_COMPONENT_TYPE_FIELD.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildNotValidUserValidationCodeErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.NOT_VALID_USER_VALIDATION_CODE.getErrorMessage(),
				ApiErrors.NOT_VALID_USER_VALIDATION_CODE.getErrorCode());
		String json = res.toJson();
		return json;
	}

	public String buildPasswordsDoesNotMatchErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.PASSWORDS_DOES_NOT_MATCH.getErrorMessage(),
				ApiErrors.PASSWORDS_DOES_NOT_MATCH.getErrorCode());
		String json = res.toJson();
		return json;
	}
}