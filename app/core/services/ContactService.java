package core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import controllers.eSecurePlay1.Auth;
import core.services.ApiService.ApiErrors;
import core.services.ApiService.NOKResponseObject;
import core.services.ApiService.OKResponseObject;
import core.services.ApiService.Play_Specific_Session_Keys;
import core.services.TestService.CreateTestRequest;
import core.services.UserService.UserCreationRequest;
import core.services.UserService.UserObject;
import models.Contacts;
import models.Topic;
import models.User;
import models.Contacts.ContactTool;
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
import play.db.jpa.GenericModel.JPAQuery;
import play.mvc.Scope.RenderArgs;
import utils.JsonUtils;
import utils.StringUtil;

public class ContactService {

	private static enum ContactServiceSingleton {
		INSTANCE;

		ContactService singleton = new ContactService();

		public ContactService getSingleton() {
			return singleton;
		}
	}

	public static ContactService instance() {
		return ContactService.ContactServiceSingleton.INSTANCE.getSingleton();
	}

	private ContactService() {
		// TODO Auto-generated constructor stub
	}

	private ApiService apiService = ApiService.instance();
	
	public static class ContactListResponse extends OKResponseObject {
		public List<Contacts> response;
	}

	public String contactList(String type) {
		ContactListResponse ret = null;
		
		List<Contacts> contacts = null;
		if(StringUtil.isNil(type))
			contacts = Contacts.all().fetch();
		else {
			contacts = Contacts.findByType(ContactTool.tryBuildContactToolFromName(type));
		}
		ret = buildContactsFromList(contacts);
		return ret.toJson();
	}

	private ContactListResponse buildContactsFromList(List<Contacts> contacts) {
		ContactListResponse ret = new ContactListResponse();
		if(contacts == null)
			return ret;
		ret.response = contacts;
		return ret;
	}
	
	public static class ContactCreationRequest {
		ContactObject contact;
	}
	
	public static class ContactObject {
		String email;
		String tool;
		String uuid;
	}
	

	public String addContact() {
		String json = apiService.getCurrentJson();
		ContactCreationRequest req;
		ContactObject contactToCreate;
		try {
			req = (ContactCreationRequest) apiService.buildObjectFromJson(json, ContactCreationRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			contactToCreate = req.contact;
			if (contactToCreate == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
		
		if (StringUtil.isNil(contactToCreate.email) || StringUtil.isNil(contactToCreate.tool)) {
			return apiService.buildMandatoryParamsErrorResponse();
		}
		//Contacts c = Contacts.findByMailAndTool(contactToCreate.email, ContactTool.tryBuildContactToolFromName(contactToCreate.tool));
		Contacts c = null;
		if(!StringUtil.isNil(contactToCreate.uuid)) {
			c = Contacts.findById(contactToCreate.uuid);
		} else {
			c = new Contacts();
		}
		c.setEmail(contactToCreate.email);
		c.setTool(ContactTool.tryBuildContactToolFromName(contactToCreate.tool));
		c.save();
		/*if(c == null) {
			c = new Contacts(contactToCreate.email, ContactTool.tryBuildContactToolFromName(contactToCreate.tool));
			c.save();
		}*/
		return (new OKResponseObject()).toJson();
	}

	public String deleteContact(String uuid) {

		if (StringUtil.isNil(uuid)) {
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
		Contacts c = Contacts.findById(uuid);
		if(c != null) {
			c.delete();
		}
		return (new OKResponseObject()).toJson();
	}

	public static class CreateRegistrationRequest {
		String name;
		String lastName;
		String email;
		String organization;
		String description;
		String tool;
	}
	
	public String registerRegistrationRequest() {
		String json = apiService.getCurrentJson();
		CreateRegistrationRequest req = null;
		try {
			req = (CreateRegistrationRequest) apiService.buildObjectFromJson(json, CreateRegistrationRequest.class);
			if (req == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			ContactTool ct = ContactTool.tryBuildContactToolFromName(req.tool);
			if(ct == null) {
				return apiService.buildNotWellFormedJsonErrorResponse();
			}
			
			List<String> toList = new ArrayList<String>();
			List<Contacts> contacts = Contacts.findByType(ct);
			for(Contacts c : contacts) {
				toList.add(c.getEmail());
			}
			
			String subject = "Contact request " + ct.name();
			String body = "<h1>"+ req.name + " " + req.lastName + "</h1>";
			body += "</br> organization: " + req.organization;
			body += "</br> email: " + req.email;
			body += "</br> description: " + req.description;
			
			Mails.sendEmail(toList, null, subject, body, null);
		} catch (Throwable ex) {
			Logger.error(ex, "Error...");
			return apiService.buildNotWellFormedJsonErrorResponse();
		}
		return apiService.buildGenericPositiveResponse();
	}

}