package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Transient;

import net.eclettica.esecure.models.EsAccount;

import net.eclettica.esecure.models.EsRole;
import net.eclettica.esecure.services.AuthService;
import play.Logger;
import utils.StringUtil;

/**
 * A representation of the model object User
 * 
 */

@Entity(name = "EndUser")
public class User extends ModelUuid implements EsAccount {


	public static class Authorizations {
		public static final String TEST_MANAGEMENT = "TEST_MANAGEMENT";
		public static final String MEDIA_MANAGEMENT = "MEDIA_MANAGEMENT";
		public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
		public static final String REPORT_MANAGEMENT = "REPORT_MANAGEMENT";
		

		public static List<String> values() {
			List<String> ret = new LinkedList<String>();
			ret.add(MEDIA_MANAGEMENT);
			ret.add(REPORT_MANAGEMENT);
			ret.add(USER_MANAGEMENT);
			ret.add(TEST_MANAGEMENT);
			for (String auth : AuthService.instance().ES_AUTHS) {
				ret.add(auth);
			}
			return ret;
		}

		public static boolean checkAuthorization(String val) {
			if (StringUtil.isNil(val)) {
				return false;
			}

			for (String authorization : values()) {
				if (StringUtil.isNil(authorization)) {
					continue;
				}
				if (authorization.equals(val)) {
					return true;
				}
			}

			return false;
		}
	}
	
	public static class ToolsTypes {
		public static final String ATLAS = "ATLAS";
		public static final String TESTING = "TESTING";
		public static final String GRAMMAR = "GRAMMAR";
		public static final String STREAMING = "STREAMING";
		
		public static List<String> values() {
			List<String> ret = new LinkedList<String>();
			ret.add(ATLAS);
			ret.add(TESTING);
			ret.add(GRAMMAR);
			ret.add(STREAMING);
			return ret;
		}
		
		public static boolean checkTool(String val) {
			if (StringUtil.isNil(val)) {
				return false;
			}

			for (String role : values()) {
				if (StringUtil.isNil(role)) {
					continue;
				}
				if (role.equals(val)) {
					return true;
				}
			}

			return false;
		}
	}

	public static class Roles {
		public static final String ROLE_SEPARATOR = " ";
		public static final String ADMIN = "ADMIN";
		public static final String CONTENT_PROVIDER = "CON_PRO";
		public static final String USER = "USER";
		public static final String ATLAS_CONTENT_PROVIDER = "AT_CON_PRO";
		public static final String TESTING_USER = "TT_USER";
		public static final String TESTING_EDITOR = "TT_EDITOR";
		public static final String GRAMMAR_ADMIN = "GRAMMAR_ADMIN";
		public static final String GRAMMAR_CONTENT_PROVIDER = "GR_CON_PRO";
		public static final String ATLAS_ADMINISTRATOR = "AT_ADMIN";
		public static final String TESTING_ADMINISTRATOR = "TT_ADMIN";
		public static final String GRAMMAR_ADMINISTRATOR = "GR_ADMIN";
		public static final String STREAMING_ADMINISTRATOR = "ST_ADMIN";

		public static List<String> values() {
			List<String> ret = new LinkedList<String>();
			ret.add(ADMIN);
			ret.add(CONTENT_PROVIDER);
			ret.add(USER);
			ret.add(ATLAS_CONTENT_PROVIDER);
			ret.add(TESTING_EDITOR);
			ret.add(TESTING_USER);
			ret.add(GRAMMAR_ADMIN);
			ret.add(GRAMMAR_CONTENT_PROVIDER);
			ret.add(ATLAS_ADMINISTRATOR);
			ret.add(TESTING_ADMINISTRATOR);
			ret.add(GRAMMAR_ADMINISTRATOR);
			ret.add(STREAMING_ADMINISTRATOR);
			return ret;
		}

		public static boolean checkRole(String val) {
			if (StringUtil.isNil(val)) {
				return false;
			}

			for (String role : values()) {
				if (StringUtil.isNil(role)) {
					continue;
				}
				if (role.equals(val)) {
					return true;
				}
			}

			return false;
		}
	}

	@Basic(optional = false)
	private String esAccountId;

	@Transient
	public static final String REGISTRATION_DATE_FORMAT = "dd/MM/yyyy";
	private Date registrationDate;

	private String name;

	private String surname;

	private String email;

	private Boolean deleted = false;

	private String verificationCode;

	public User(String username) {
		this.email = username;
	}

	public User() {
		super();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public String getEsAccountId() {
		return esAccountId;
	}

	public void setEsAccountId(String esAccountId) {
		this.esAccountId = esAccountId;
	}

	public void setEsAccountId(models.eSecurePlay1.EsAccount esAccount) {
		this.esAccountId = esAccount.getUuid();
	}

	@Override
	public String getUserGroup() {
		models.eSecurePlay1.EsAccount acc = models.eSecurePlay1.EsAccount.findById(esAccountId);
		if (acc != null)
			return acc.getUserGroup();
		else
			return null;
	}

	@Override
	public String getUsername() {
		models.eSecurePlay1.EsAccount acc = models.eSecurePlay1.EsAccount.findById(esAccountId);
		if (acc != null)
			return acc.getUsername();
		else
			return null;
	}

	@Override
	public String getPassword() {
		models.eSecurePlay1.EsAccount acc = models.eSecurePlay1.EsAccount.findById(esAccountId);
		if (acc != null)
			return acc.getPassword();
		else
			return null;
	}

	public static User findByEsAccountId(String esAccountId) {
		if(StringUtil.isNil(esAccountId))
			return null;
		return User.find("esAccountId=:esAccountId").setParameter("esAccountId", esAccountId).first();
	}

	public String buildUserJsonFieldRole() {
		List<EsRole> roles = retrieveUserRoles();
		String ret = "";
		String sep = "";
		for (EsRole role : roles) {
			ret += sep + role.getName();
			sep = Roles.ROLE_SEPARATOR;
		}

		return ret;
	}

	public String[] buildUserStringArrayRole() {
		String[] ret = {};
		List<EsRole> roles = retrieveUserRoles();
		String temp = "";
		String sep = "";
		for (EsRole role : roles) {
			temp += sep + role.getName();
			sep = Roles.ROLE_SEPARATOR;
		}

		if (temp.length() > 0) {
			try {
				ret = temp.split(Roles.ROLE_SEPARATOR);
			} catch (Exception ex) {
				Logger.error(ex, "Exception while splitting roles");
			}
		}
		return ret;
	}

	public List<EsRole> retrieveUserRoles() {
		List<EsRole> roles = AuthService.instance().findRoleByUser(this.esAccountId);
		if (roles == null) {
			roles = new LinkedList<EsRole>();
		}
		return roles;
	}
	
	public List<String> retrieveUserRolesString() {
		List<EsRole> roles = AuthService.instance().findRoleByUser(this.esAccountId);
		List<String> ret = new LinkedList<String>();
		for(EsRole role : roles) {
			ret.add(role.getName());
		}
		return ret;
	}
	
	public String getRole() {
		models.eSecurePlay1.EsAccount acc = models.eSecurePlay1.EsAccount.findById(esAccountId);
		Set<models.eSecurePlay1.EsRole> roles = acc.getRoles();
		Iterator<models.eSecurePlay1.EsRole> it = roles.iterator();
		String ret = "";
		String sep = "";
		while(it.hasNext()){
			ret += sep + it.next().getName();
			sep = " ";
		}
		return ret;
	}
	
	public Set<String> getRoles() {
		models.eSecurePlay1.EsAccount acc = models.eSecurePlay1.EsAccount.findById(esAccountId);
		Set<models.eSecurePlay1.EsRole> roles = acc.getRoles();
		Iterator<models.eSecurePlay1.EsRole> it = roles.iterator();
		Set<String> ret = new HashSet<String>();
		while(it.hasNext()){
			ret.add(it.next().getName());
		}
		return ret;
	}

	public static class UserRoles {
		public boolean hasAdminRole = false;
		public boolean hasContentProviderRole = false;
		public boolean hasUserRole = false;
		public String hasUserTool = null;
	}

	public UserRoles retrieveRoles() {
		UserRoles userRoles = new UserRoles();
		List<EsRole> roles = retrieveUserRoles();
		if (roles != null && !roles.isEmpty()) {
			for (EsRole role : roles) {
				if (role.getName().equals(Roles.ADMIN)) {
					userRoles.hasAdminRole = true;
					userRoles.hasUserTool = null;
				} else if (role.getName().equals(Roles.ATLAS_ADMINISTRATOR)) {
					userRoles.hasAdminRole = true;
					userRoles.hasUserTool = ToolsTypes.ATLAS;
				} else if (role.getName().equals(Roles.TESTING_ADMINISTRATOR)) {
					userRoles.hasAdminRole = true;
					userRoles.hasUserTool = ToolsTypes.TESTING;
				} else if (role.getName().equals(Roles.GRAMMAR_ADMINISTRATOR)) {
					userRoles.hasAdminRole = true;
					userRoles.hasUserTool = ToolsTypes.GRAMMAR;
				} else if (role.getName().equals(Roles.STREAMING_ADMINISTRATOR)) {
					userRoles.hasAdminRole = true;
					userRoles.hasUserTool = ToolsTypes.STREAMING;
				} else if (role.getName().equals(Roles.CONTENT_PROVIDER)) {
					userRoles.hasContentProviderRole = true;
				} else if (role.getName().equals(Roles.USER)) {
					userRoles.hasUserRole = true;
				}
			}
		}
		return userRoles;
	}
	
	public static List<User> findAllFilteredByRoleAndEmailAndName(String role, String email, String name) {
		String[] r = new String[1];
		r[0] = role;
		return findAllFilteredByRoleAndEmailAndName(r, email, name);
	}
	
	public static List<User> findAllFilteredByRoleAndEmailAndName(String[] role, String email, String name) {
		return findAllFilteredByRoleAndEmailAndName(role, email, name, null, null);
	}

	public static List<User> findAllFilteredByRoleAndEmailAndName(String[] role, String email, String name, String sort, String order) {
		List<User> ret = new LinkedList<User>();

		boolean filtered = false;
		String query = "";
		String sep = "";
		Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
		if (!StringUtil.isNil(email)) {
			filtered = true;
			query += sep + "email LIKE :email";
			sep = " AND ";
			queryParams.put("email", "%" + email + "%");
		}
		if (!StringUtil.isNil(name)) {
			filtered = true;
			query += sep + "name LIKE :name";
			sep = " AND ";
			queryParams.put("name", "%" + name + "%");
		}
		
		if (!StringUtil.isNil(sort) && !StringUtil.isNil(order)) {
			filtered = true;
			query += " order by " + sort + " " + order;
		}
		
		if (filtered) {
			JPAQuery q = User.find(query);
			for (String key : queryParams.keySet()) {
				q.setParameter(key, queryParams.get(key));
			}
			ret = q.fetch();
		}
		
		if (role != null && role.length > 0) {
			List<User> filteredByRoles = new LinkedList<User>();
			List<User> list;

			if (filtered) {
				list = ret;
			} else {
				list = User.findAll();
			}

			filtered = true;

			for (User u : list) {
				String[] roles = u.buildUserStringArrayRole();
				if (roles == null || roles.length <= 0) {
					continue;
				}
				for (String esRole : roles) {
					for(String r : role) {
						if (esRole.equals(r)) {
							filteredByRoles.add(u);
							break;
						}
					}
				}
			}

			ret = filteredByRoles;
		}

//		if (!StringUtil.isNil(role)) {
//			String[] rr = null;
//			if(role.contains("||")) {
//				rr = role.split("||");
//			} else {
//				rr = new String[1];
//				rr[0] = role;
//			}
//			List<User> filteredByRoles = new LinkedList<User>();
//			List<User> list;
//
//			if (filtered) {
//				list = ret;
//			} else {
//				list = User.findAll();
//			}
//
//			filtered = true;
//
//			for (User u : list) {
//				String[] roles = u.buildUserStringArrayRole();
//				if (roles == null || roles.length <= 0) {
//					continue;
//				}
//				for (String esRole : roles) {
//					for(String r : rr) {
//						if (esRole.equals(r)) {
//							filteredByRoles.add(u);
//							break;
//						}
//					}
//				}
//			}
//
//			ret = filteredByRoles;
//		}

		if (!filtered) {
			ret = User.findAll();
			return ret;
		}

		return ret;
	}

	public static List<User> findAllByDeleted(Boolean deleted) {
		List<User> ret = new LinkedList<User>();
		if (deleted == null) {
			ret = User.findAll();
		} else {
			ret = User.find("deleted=:deleted").setParameter("deleted", deleted).fetch();
		}
		return ret;
	}

	public static User findByUsername(String username) {
		User u = find("email = :username").setParameter("username", username).first();
		return u;
	}

	public String buildFullName() {
		String fullName = "";
		if (!StringUtil.isNil(this.name)) {
			fullName += this.name + " ";
		}
		if (!StringUtil.isNil(this.surname)) {
			fullName += this.surname;
		}
		return fullName;
	}

	@Override
	public String generateToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isActive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isChecking() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isTokenExpired() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeToken() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChecking() {
		// TODO Auto-generated method stub
		
	}
}