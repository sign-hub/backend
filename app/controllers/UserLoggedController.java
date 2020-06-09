package controllers;

import controllers.eSecurePlay1.Auth;
import controllers.eSecurePlay1.Auth.Check;
import core.services.ContactService;
import core.services.UserService;
import core.services.ApiService.AuthorizedHttpVerb;
import models.User.Authorizations;
import play.Logger;
import utils.StringUtil;

public class UserLoggedController extends ApiLoggedController {
	
	protected static ContactService contactService = ContactService.instance();

	public static void logout() {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS);
			if (isPost())
				jsonResponse = userService.logout();
			else if (isOptions()) {
				// continue
			}
		} catch (Throwable thr) {
			Logger.error(thr, "Error...");
			jsonResponse = apiService.buildGenericErrorResponse();
		}
		renderJSON(jsonResponse);
	}

	//@Check({ Authorizations.USER_MANAGEMENT })
	public static void user(String userId, String role, String email, String name) {
		
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			
			if(!isGet() || StringUtil.isNil(userId) || !userId.equals(userService.getCurrentUserLogged().getUuid())){
				if (!Auth.canIDo(Authorizations.USER_MANAGEMENT)){
					renderJSON(apiService.buildNotAuthorizedResponse());
				}
			}
			
			if (isGet()) {
				if (StringUtil.isNil(userId)) {
					jsonResponse = userService.usersList(role, email, name);
				} else {
					jsonResponse = userService.getUser(userId);
				}
			} else if (isPost()) {
				if (StringUtil.isNil(userId)) {
					jsonResponse = userService.userCreation();
				} else {
					jsonResponse = userService.updateUser(userId);
				}
			} else if (isDelete()) {
				jsonResponse = userService.deleteUser(userId);
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
	
	public static void createUsers(String path, String filename){
		Logger.info("createAtlasUsers..." + path + " " + filename);
		UserService.instance().createUsers(path, filename);
		
		Logger.info("Users created");
	}
	
	
	public static void contact(String param) {
		
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			
			if (isGet()) {
					jsonResponse = contactService.contactList(param);
			} else if (isPost()) {
				jsonResponse = contactService.addContact();
			} else if (isDelete()) {
				//jsonResponse = userService.deleteUser(userId);
				jsonResponse = contactService.deleteContact(param);
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