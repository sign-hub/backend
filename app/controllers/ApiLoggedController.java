package controllers;

import java.util.List;

import controllers.eSecurePlay1.Auth;
import core.services.ApiService.AuthorizedHttpVerb;
import core.services.ApiService.Play_Specific_Session_Keys;
import models.User;
import models.User.UserRoles;
import play.mvc.Before;
import play.mvc.Http.Header;
import utils.StringUtil;

public class ApiLoggedController extends BaseController {

	@Before(priority = 2)
	public static void checkLogged() {
		if(isOptions()){
			
			Header header = request.headers.get("access-control-request-method");
			if(header !=null){
				List<String> vv = header.values;
				String s = vv.get(0);
				Header h = new Header("Access-Control-Allow-Methods", s);
				response.headers.put("Access-Control-Allow-Methods", h);
				
			}
			
			header = request.headers.get("origin");
			
			if(header !=null){
				List<String> vv = header.values;
				String s = vv.get(0);
				Header h = new Header("Access-Control-Allow-Origin", s);
				response.headers.put("Access-Control-Allow-Origin", h);
			}
			renderJSON(new Object());
		}
		if (request.headers == null || request.headers.isEmpty()) {
			renderJSON(apiService.buildForbiddenResponse());
		}
		Header header = request.headers.get("authtoken");
		if (header == null) {
			renderJSON(apiService.buildForbiddenResponse());
		}
		List<String> values = header.values;
		if (values == null || values.isEmpty() || values.size() != 1) {
			renderJSON(apiService.buildForbiddenResponse());
		}

		String authToken = values.get(0);
		if (StringUtil.isNil(authToken)) {
			renderJSON(apiService.buildForbiddenResponse());
		}

		if (!userService.isLogged(authToken)) {
			renderJSON(apiService.buildForbiddenResponse());
		}

		Auth.setCurrentAuthToken(authToken);
		Auth.setCurrentEsAccountId(authToken);
	}

	// Non posso usare il @With(Auth.class) perchè altrimenti non viene
	// rispettata
	// la gerarchia dei before dei padri;
	// per questo chiamiamo il metodo Auth.checkAccess() in questo before di
	@Before(priority = 3)
	public static void checkAuthorizations() {
		boolean canDo = Auth.checkAccess();
		if (!canDo){
			renderJSON(apiService.buildNotAuthorizedResponse());
		}
	}

	@Before(priority = 4)
	public static void saveUserRoles() {
		User currentUserLogged = userService.getCurrentUserLogged();
		UserRoles roles = currentUserLogged.retrieveRoles();
		renderArgs.put(Play_Specific_Session_Keys.CURR_USER_LOGGED_HAS_ADMIN_ROLE.getValue(), roles.hasAdminRole);
		renderArgs.put(Play_Specific_Session_Keys.CURR_USER_LOGGED_HAS_CONTENT_PROVIDER_ROLE.getValue(),
				roles.hasContentProviderRole);
		renderArgs.put(Play_Specific_Session_Keys.CURR_USER_LOGGED_HAS_USER_ROLE.getValue(), roles.hasUserRole);
		renderArgs.put(Play_Specific_Session_Keys.CURR_USER_LOGGED_HAS_TOOL.getValue(), roles.hasUserTool);
	}
}