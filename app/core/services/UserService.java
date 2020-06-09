package core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import controllers.eSecurePlay1.Auth;
import core.services.ApiService.ApiErrors;
import core.services.ApiService.NOKResponseObject;
import core.services.ApiService.OKResponseObject;
import core.services.ApiService.Play_Specific_Session_Keys;
import models.Topic;
import models.User;
import models.User.Roles;
import models.User.UserRoles;
import models.eSecurePlay1.EsAccount;
import net.eclettica.esecure.dao.EsDao;
import net.eclettica.esecure.models.EsRole;
import net.eclettica.esecure.services.AuthService;
import net.eclettica.esecure.services.EsExceptionAuth;
import net.eclettica.esecure.services.EsExceptionSession;
import notifiers.Mails;
import play.Logger;
import play.data.validation.EmailCheck;
import play.mvc.Scope.RenderArgs;
import utils.JsonUtils;
import utils.StringUtil;

/**
 *
 * @author luca
 */
public class UserService {

	private static enum UserServiceSingleton {
		INSTANCE;

		UserService singleton = new UserService();

		public UserService getSingleton() {
			return singleton;
		}
	}

	public static UserService instance() {
		return UserService.UserServiceSingleton.INSTANCE.getSingleton();
	}

	private UserService() {
		// TODO Auto-generated constructor stub
	}

	private ApiService apiService = ApiService.instance();

	public static class LoginRequest {
		public String login;
		public String password;
	}

	public static class LoginResponse extends OKResponseObject {
		public LoginSubResponse response;
	}

	public static class LoginSubResponse {
		public String userId;
		public String authToken;
	}

	public String login() {

		String json = apiService.getCurrentJson();
		LoginRequest req;
		try {
			req = (LoginRequest) apiService.buildObjectFromJson(json, LoginRequest.class);
			if (req == null) {
				LoggerService.instance().log("login", "fail", "Request not well formed");
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			LoggerService.instance().log("login", "fail", "Request not well formed");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (StringUtil.isNil(req.login) || StringUtil.isNil(req.password)) {
			LoggerService.instance().log("login", "fail", "Mandatory params not found");
			return apiService.buildMandatoryParamsErrorResponse();
		}

		String ret;
		LoginResponse res = new LoginResponse();
		boolean loginOk = Auth.authenticate(req.login, req.password);
		if (loginOk) {

			User u = User.findByEsAccountId(Auth.getCurrentEsAccountId());
			if (u != null && !u.isDeleted()) {
				LoggerService.instance().log("login", "OK", req.login);
				LoginSubResponse subRes = new LoginSubResponse();
				subRes.userId = u.getUuid();
				subRes.authToken = Auth.getCurrentUserSessionId();
				res.response = subRes;
				ret = res.toJson();
			} else {
				LoggerService.instance().log("login", "fail", "Login fails " + req.login);
				Auth.logout(Auth.getCurrentUserSessionId());
				ret = buildWrongCredentialsErrorResponse();
			}
		} else {
			LoggerService.instance().log("login", "fail", "Login fails " + req.login);
			Logger.error("login error with %s", req.login);
			ret = buildWrongCredentialsErrorResponse();
		}
		return ret;
	}

	public boolean isLogged(String authToken) {
		if (StringUtil.isNil(authToken)) {
			return false;
		}
		String esAccountId = Auth.getCurrentEsAccountId(authToken);
		if(StringUtil.isNil(esAccountId))
			return false;
		User u = User.findByEsAccountId(esAccountId);
		if (u == null || u.isDeleted()) {
			Auth.logout(authToken);
			return false;
		}
		return Auth.isLogged(authToken);
	}

	public boolean hasCurrentUserLoggedAdminRole() {
		try {
			boolean ret = (boolean) RenderArgs.current()
					.get(Play_Specific_Session_Keys.CURR_USER_LOGGED_HAS_ADMIN_ROLE.getValue());
			return ret;
		} catch (Exception e) {
			return false;
		}
	}
	
	public String hasCurrentUserLoggedTool() {
		try {
			String ret = (String) RenderArgs.current()
					.get(Play_Specific_Session_Keys.CURR_USER_LOGGED_HAS_TOOL.getValue());
			return ret;
		} catch (Exception e) {
			return null;
		}
	}
	
	public boolean checkAdminAndTool(String tool) {
		Logger.debug("checkAdminAndTool " + tool);
		try {
			if(hasCurrentUserLoggedAdminRole()) {
				Logger.debug("checkAdminAndTool hasCurrentUserLoggedAdminRole");
				String t = hasCurrentUserLoggedTool();
				Logger.debug("checkAdminAndTool hasCurrentUserLoggedTool " + t);
				if(tool != null) {
					if(t == null || t.equals(tool)) {
						Logger.debug("checkAdminAndTool return true");
						return true;
					}
				} else {
					if(t == null) {
						Logger.debug("checkAdminAndTool return true");
						return true;
					}
				}
			}
		} catch (Exception e) {
			
		}
		Logger.debug("checkAdminAndTool return false");
		return false;
	}
	

	public boolean hasCurrentUserLoggedContentProviderRole() {
		try {
			boolean ret = (boolean) RenderArgs.current()
					.get(Play_Specific_Session_Keys.CURR_USER_LOGGED_HAS_CONTENT_PROVIDER_ROLE.getValue());
			return ret;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean hasCurrentUserLoggedUserRole() {
		try {
			boolean ret = (boolean) RenderArgs.current()
					.get(Play_Specific_Session_Keys.CURR_USER_LOGGED_HAS_USER_ROLE.getValue());
			return ret;
		} catch (Exception e) {
			return false;
		}
	}

	public User getCurrentUserLogged() {
		try {
			String authToken = Auth.getCurrentUserSessionId();
			if (!isLogged(authToken)) {
				return null;
			}
			User u = User.findByEsAccountId(Auth.getCurrentEsAccountId());
			return u;
		} catch(Exception e) {
			return null;
		}
	}

	public static class LogoutResponse extends OKResponseObject {
		public Object response = new Object();
	}

	public String logout() {
		try {
			LogoutResponse res = new LogoutResponse();
			Auth.logout(Auth.getCurrentUserSessionId());
			String ret = res.toJson();
			return ret;
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
	}

	public static class UserObject {
		public String userId;
		public String registrationDate;
		public String name;
		public String surname;
		public String role;
		public String email;
		public String password;
		public Boolean deleted;
	}

	public static class UserCreationRequest {
		public UserObject user;
	}

	public static class UserCreationResponse extends OKResponseObject {
		public UserObject response;
	}

	public String userCreation() {

		String json = apiService.getCurrentJson();
		UserCreationRequest req;
		UserObject userToCreate;
		try {
			req = (UserCreationRequest) apiService.buildObjectFromJson(json, UserCreationRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			userToCreate = req.user;
			if (userToCreate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (StringUtil.isNil(userToCreate.email) || StringUtil.isNil(userToCreate.name)
				|| StringUtil.isNil(userToCreate.password) || StringUtil.isNil(userToCreate.surname)
				|| StringUtil.isNil(userToCreate.role)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		EmailCheck emailCheck = new EmailCheck();
		boolean isValidEmail = emailCheck.isSatisfied(null, userToCreate.email, null, null);
		if (!isValidEmail) {
			return apiService.buildNotValidMailErrorResponse();
		}

		String[] roles = buildRolesStringArray(userToCreate.role);
		if (roles == null || roles.length <= 0) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		boolean isValidRole = validateRoles(roles);
		if (!isValidRole) {
			return apiService.buildNotValidRoleErrorResponse();
		}

		User u = User.findByUsername(userToCreate.email);
		if (u != null) {
			return apiService.buildEntityAlreadyExistsErrorResponse();
		}

		u = new User();
		u.setEmail(userToCreate.email);
		u.setName(userToCreate.name);
		u.setRegistrationDate(new Date());
		u.setSurname(userToCreate.surname);
		u.setVerificationCode(null);
		String uuidAccount = Auth.createAccount(userToCreate.email, userToCreate.password, "users");
		u.setEsAccountId(uuidAccount);
		u.save();
		try {
			AuthService.instance().editUserRoles(Auth.getCurrentUserSessionId(), u.getEsAccountId(), roles);
		} catch (EsExceptionSession e) {
			e.printStackTrace();
			u.delete();
			EsAccount esa = EsAccount.findByUsername(userToCreate.email);
			if(esa!=null)
				esa.delete();
			return apiService.buildForbiddenResponse();
		} catch (EsExceptionAuth e) {
			e.printStackTrace();
			u.delete();
			EsAccount esa = EsAccount.findByUsername(userToCreate.email);
			if(esa!=null)
				esa.delete();
			return apiService.buildNotAuthorizedResponse();
		}
		UserCreationResponse res = new UserCreationResponse();
		res.response = buildUserObjectFromUser(u);
		String ret = JsonUtils.toJson(res);
		return ret;
	}

	private boolean validateRoles(String[] roles) {
		if (roles == null || roles.length <= 0) {
			return false;
		}

		for (int i = 0; i < roles.length; i++) {
			String role = roles[i];
			if (StringUtil.isNil(role) || !Roles.checkRole(role))
				return false;
		}

		return true;
	}

	private String[] buildRolesStringArray(String roles) {
		String[] ret = {};
		if (!StringUtil.isNil(roles)) {
			ret = roles.split(Roles.ROLE_SEPARATOR);
			if (ret == null || ret.length <= 0) {
				ret[0] = roles;
			}
		}
		return ret;
	}

	public UserObject buildUserObjectFromUser(User u) {
		UserObject ret = new UserObject();
		ret.deleted = u.isDeleted();
		ret.email = u.getEmail();
		ret.name = u.getName();
		ret.registrationDate = StringUtil.date(u.getRegistrationDate(), User.REGISTRATION_DATE_FORMAT);
		ret.role = u.buildUserJsonFieldRole();
		ret.surname = u.getSurname();
		ret.userId = u.getUuid();
		return ret;
	}

	public String updateUser(String userId) {
		if (StringUtil.isNil(userId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		User u = User.findById(userId);
		if (u == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		String json = apiService.getCurrentJson();
		UserCreationRequest req;
		UserObject userToUpdate;
		try {
			req = (UserCreationRequest) apiService.buildObjectFromJson(json, UserCreationRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			userToUpdate = req.user;
			if (userToUpdate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (!StringUtil.isNil(userToUpdate.name)) {
			u.setName(userToUpdate.name);

		}
		if (!StringUtil.isNil(userToUpdate.surname)) {
			u.setSurname(userToUpdate.surname);
		}

		if (!StringUtil.isNil(userToUpdate.password)) {
			Auth.saveNewPassword(u.getEmail(), userToUpdate.password);
		}

		if (!StringUtil.isNil(userToUpdate.role)) {
			String[] roles = buildRolesStringArray(userToUpdate.role);
			if (roles == null || roles.length <= 0) {
				return apiService.buildNotValidRoleErrorResponse();
			}

			boolean isValidRole = validateRoles(roles);
			if (!isValidRole) {
				return apiService.buildNotValidRoleErrorResponse();
			}

			try {
				AuthService.instance().editUserRoles(Auth.getCurrentUserSessionId(), u.getEsAccountId(), roles);
			} catch (EsExceptionSession e) {
				e.printStackTrace();
				u.delete();
				EsAccount esa = EsAccount.findByUsername(u.getEmail());
				if(esa!=null)
					esa.delete();
				return apiService.buildForbiddenResponse();
			} catch (EsExceptionAuth e) {
				e.printStackTrace();
				u.delete();
				EsAccount esa = EsAccount.findByUsername(u.getEmail());
				if(esa!=null)
					esa.delete();
				return apiService.buildNotAuthorizedResponse();
			}
		}

		u.save();

		UserCreationResponse res = new UserCreationResponse();
		res.response = buildUserObjectFromUser(u);
		String ret = JsonUtils.toJson(res);
		return ret;
	}

	public static class UserListResponse extends OKResponseObject {
		public List<UserObject> response;
	}

	public String usersList(String role, String email, String name) {

		String[] roles = buildRolesStringArray(role);
		if(roles != null && roles.length>0) {
			boolean isValidRole = validateRoles(roles);
			if (!isValidRole) {
					return apiService.buildNotValidRoleErrorResponse();
				}
		}
		/*if (!StringUtil.isNil(role)) {
			boolean check = Roles.checkRole(role);
			if (!check) {
				return apiService.buildNotValidRoleErrorResponse();
			}
		}*/

		if (!StringUtil.isNil(email)) {
			EmailCheck emailCheck = new EmailCheck();
			boolean isValidEmail = emailCheck.isSatisfied(null, email, null, null);
			if (!isValidEmail) {
				return apiService.buildNotValidMailErrorResponse();
			}
		}

		List<User> users = User.findAllFilteredByRoleAndEmailAndName(roles, email, name, "surname", "ASC");

		UserListResponse res = new UserListResponse();
		List<UserObject> retList = buildUserObjectListFromUserList(users);
		res.response = retList;
		String ret = res.toJson();
		return ret;
	}

	public List<UserObject> buildUserObjectListFromUserList(List<User> users) {
		List<UserObject> retList = new LinkedList<UserObject>();
		if (users != null && !users.isEmpty()) {
			for (User user : users) {
				UserObject userObject = buildUserObjectFromUser(user);
				retList.add(userObject);
			}
		}
		return retList;
	}

	public static class GetUserResponse extends OKResponseObject {
		public UserObject response;
	}

	public String getUser(String userId) {
		if (StringUtil.isNil(userId)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		GetUserResponse res = new GetUserResponse();
		User user = User.findById(userId);
		if (user == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}
		UserObject userObject = buildUserObjectFromUser(user);
		res.response = userObject;
		String ret = res.toJson();
		return ret;
	}

	public static class UserDeleteResponse extends OKResponseObject {
		public Object response = new Object();
	}

	public String deleteUser(String userId) {
		if (StringUtil.isNil(userId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		User user = User.findById(userId);
		if (user == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (user.isDeleted()) {
			return apiService.buildEntityAlreadyDeletedErrorResponse();
		}

		user.setDeleted(true);
		user.save();

		UserDeleteResponse res = new UserDeleteResponse();
		String ret = res.toJson();
		return ret;
	}

	public String buildWrongCredentialsErrorResponse() {
		NOKResponseObject res = new NOKResponseObject();
		res.addError(ApiErrors.WRONG_CREDENTIALS_ERROR.getErrorMessage(),
				ApiErrors.WRONG_CREDENTIALS_ERROR.getErrorCode());
		String ret = res.toJson();
		return ret;
	}

	public static class LoginRecoveryRequest {
		public String login;
	}

	public static class LoginRecoveryResponse extends OKResponseObject {
		public Object response = new Object();
	}

	public String loginRecovery() {
		String json = apiService.getCurrentJson();
		LoginRecoveryRequest req;
		String login = null;

		try {
			req = (LoginRecoveryRequest) apiService.buildObjectFromJson(json, LoginRecoveryRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}

		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
		login = req.login;

		if (StringUtil.isNil(login)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		User u = User.findByUsername(login);
		if (u == null || u.getDeleted()) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		String verificationCode = null;
		try {
			verificationCode = UUID.randomUUID().toString();
			u.setVerificationCode(verificationCode);
			u.save();
		} catch (Exception ex) {
			Logger.error(ex, "Error during user saving on db (uuid: %s)", u.getUuid());
			return apiService.buildMandatoryParamsErrorResponse();
		}

		try {
			Mails.sendVerificationCode(u.buildFullName(), u.getUsername(), verificationCode);
		} catch (Exception ex) {
			Logger.error(ex, "Error during loginRecovery (user uuid: %s)", u.getUuid());
			return apiService.buildGenericErrorResponse();
		}

		LoginRecoveryResponse res = new LoginRecoveryResponse();
		String ret = res.toJson();
		return ret;
	}

	public static class PasswordResetRequest {
		public String login;
		public String validationCode;
		public String newPassword;
		public String rePassword;
	}

	public static class PasswordResetResponse extends OKResponseObject {
		public Object response = new Object();
	}

	public String passwordReset() {
		String json = apiService.getCurrentJson();
		PasswordResetRequest req;

		try {
			req = (PasswordResetRequest) apiService.buildObjectFromJson(json, PasswordResetRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}

		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}

		if (StringUtil.isNil(req.login) || StringUtil.isNil(req.validationCode) || StringUtil.isNil(req.newPassword)
				|| StringUtil.isNil(req.rePassword)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}

		User u = User.findByUsername(req.login);
		if (u == null || u.getDeleted()) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		if (StringUtil.isNil(u.getVerificationCode()) || !u.getVerificationCode().equals(req.validationCode)) {
			return apiService.buildNotValidUserValidationCodeErrorResponse();
		}

		if (!req.newPassword.equals(req.rePassword)) {
			return apiService.buildPasswordsDoesNotMatchErrorResponse();
		}

		try {
			EsDao dao = new EsDao();
			dao.changePassword(u.getEsAccountId(), req.newPassword);
		} catch (Exception ex) {
			Logger.error(ex, "Error during loginRecovery (user uuid: %s)", u.getUuid());
			return apiService.buildGenericErrorResponse();
		}

		u.setVerificationCode(null);
		u.save();

		PasswordResetResponse res = new PasswordResetResponse();
		String ret = res.toJson();
		return ret;
	}

	public boolean hasCurrentUserLoggedRole(String role) {
		User u = this.getCurrentUserLogged();
		if (u == null)
			return false;
		List<EsRole> roles = u.retrieveUserRoles();
		for (EsRole r : roles) {
			if (r == null)
				continue;
			if (r.getName().equals(role))
				return true;
		}
		return false;
	}

	public boolean userCan(Auths auth) {
		return Auth.canIDo(auth.name());
	}

	private void parseCsv(File f) {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";

		try {

			br = new BufferedReader(new FileReader(f));
			int i = 0;
			while ((line = br.readLine()) != null) {
				i++;
				String[] fields = line.split(cvsSplitBy);
				if (fields.length < 5) {
					Logger.warn("parseCsv row:" + i + " not valid fields: " + line);
					continue;
				}
				UserObject userToCreate = new UserObject();
				userToCreate.deleted = false;
				userToCreate.name = fields[0].trim();
				userToCreate.surname = fields[1].trim();
				userToCreate.email = fields[2].trim();
				userToCreate.password = fields[3].trim();
				userToCreate.role = fields[4].trim();

				EmailCheck emailCheck = new EmailCheck();
				boolean isValidEmail = emailCheck.isSatisfied(null, userToCreate.email, null, null);
				if (!isValidEmail) {
					Logger.warn("parseCsv row:" + i + " not valid mail: " + userToCreate.email);
					continue;
				}

				String[] roles = buildRolesStringArray(userToCreate.role);
				if (roles == null || roles.length <= 0) {
					Logger.warn("parseCsv row:" + i + " empty roles: " + userToCreate.role);
					continue;
				}

				boolean isValidRole = validateRoles(roles);
				if (!isValidRole) {
					Logger.warn("parseCsv row:" + i + " invalid roles: " + userToCreate.role);
					continue;
				}

				User u = User.findByUsername(userToCreate.email);
				if (u != null) {
					Logger.warn("parseCsv row:" + i + " user already exist: " + userToCreate.email);
					//continue;
					Set<String> rolesList = u.getRoles();
					for(String r : roles) {
						rolesList.add(r);
					}
					roles = rolesList.toArray(roles);
				} else {
					u = new User();
					u.setEmail(userToCreate.email);
					u.setName(userToCreate.name);
					u.setRegistrationDate(new Date());
					u.setSurname(userToCreate.surname);
					u.setVerificationCode(null);
					String uuidAccount = Auth.createAccount(userToCreate.email, userToCreate.password, "users");
					u.setEsAccountId(uuidAccount);
					u.save();
				}

				
				try {
					AuthService.instance().editUserRoles(Auth.getCurrentUserSessionId(), u.getEsAccountId(), roles);
					Logger.warn("parseCsv row:" + i + " created: " + userToCreate.email);
				} catch (EsExceptionSession e) {
					e.printStackTrace();
					u.delete();
					EsAccount esa = EsAccount.findByUsername(userToCreate.email);
					if(esa!=null)
						esa.delete();
					Logger.warn("parseCsv row:" + i + " unauthorized: " + userToCreate.email);
				} catch (EsExceptionAuth e) {
					e.printStackTrace();
					u.delete();
					EsAccount esa = EsAccount.findByUsername(userToCreate.email);
					if(esa!=null)
						esa.delete();
					Logger.warn("parseCsv row:" + i + " unauthorized: " + userToCreate.email);
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

	public void createUsers(String path, String filename) {
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
		this.parseCsv(f);

	}

}