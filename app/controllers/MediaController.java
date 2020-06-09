package controllers;

import java.io.File;
import java.util.List;

import com.google.gson.Gson;

import controllers.eSecurePlay1.Auth.Check;
import core.services.ApiService.AuthorizedHttpVerb;
import core.services.MediaService;
import models.Media.MediasType;
import models.Test.TestType;
import models.User.Authorizations;
import play.Logger;
import play.mvc.Util;
import utils.StringUtil;

@Check({ Authorizations.TEST_MANAGEMENT, Authorizations.MEDIA_MANAGEMENT })
public class MediaController extends ApiLoggedController {

	private static MediaService mediaService = MediaService.instance();

	public static void testingtoolMedia(String mediaId, File file, List<File> files, String mediaType, String mediaName, String mediaAuthor,
			String mediaDate, String testId, String orderBy, String orderType, String parentId, Boolean isFolder, String folderName, Integer page, Integer length) {
		if(StringUtil.isNil(orderBy)){
			orderBy = "mediaName";
			orderType = "ASC";
		}
		media(mediaId, file, files, mediaType, mediaName, mediaAuthor, mediaDate, testId, MediasType.TESTINGTOOL.name(), orderBy, orderType, parentId, isFolder, folderName, page, length);
	}
	
	public static void atlasMedia(String mediaId, File file, List<File> files, String mediaType, String mediaName, String mediaAuthor,
			String mediaDate, String testId, String orderBy, String orderType, String parentId, Boolean isFolder, String folderName, Integer page, Integer length) {
		media(mediaId, file, files, mediaType, mediaName, mediaAuthor, mediaDate, testId, MediasType.ATLAS.name(), orderBy, orderType, parentId, isFolder, folderName, page, length);
	}
	
	public static void grammarMedia(String mediaId, File file, List<File> files, String mediaType, String mediaName, String mediaAuthor,
			String mediaDate, String testId, String orderBy, String orderType, String parentId, Boolean isFolder, String folderName, Integer page, Integer length) {
		media(mediaId, file, files, mediaType, mediaName, mediaAuthor, mediaDate, testId, MediasType.GRAMMAR.name(), orderBy, orderType, parentId, isFolder, folderName, page, length);
	}
	
	@Util
	public static void media(String mediaId, File file, List<File> files, String mediaType, String mediaName, String mediaAuthor,
			String mediaDate, String mediaTestId, String tt, String orderBy, String orderType, String parentId, Boolean isFolder, String folderName, Integer page, Integer length) {
		String jsonResponse = "";
		try {
			addOptions(AuthorizedHttpVerb.POST, AuthorizedHttpVerb.OPTIONS, AuthorizedHttpVerb.GET,
					AuthorizedHttpVerb.DELETE);
			if (isGet()) {
				request.format = "json";
				if (StringUtil.isNil(mediaId)) {
					jsonResponse = mediaService.mediaList(mediaType, mediaName, mediaAuthor, mediaDate, mediaTestId, tt, orderBy, orderType, parentId, page, length);
				} else {
					jsonResponse = mediaService.getMedia(mediaId);
				}
			} else if (isPost()) {
				if (StringUtil.isNil(mediaId)) {
					System.out.println("Creating new media " + tt);
					System.out.println(file);
					System.out.println(files);
					for( String key: params.all().keySet()) {
						System.out.println(key + " " + params.get(key));
					}
					if(file != null)
						jsonResponse = mediaService.mediaNew(file, tt, mediaTestId, parentId, isFolder, folderName);
					else if(files != null) {
							jsonResponse = mediaService.mediaListNew(files, tt, mediaTestId, parentId, isFolder, folderName);
					} else if(isFolder != null && isFolder && !StringUtil.isNil(folderName)) { 
						jsonResponse = mediaService.mediaNew(file, tt, mediaTestId, parentId, isFolder, folderName);

					}else {
						jsonResponse = apiService.buildMandatoryParamsErrorResponse();
					}
				} else {
					jsonResponse = apiService.buildGenericErrorResponse();
				}
			} else if (isDelete()) {
				jsonResponse = mediaService.deleteMedia(mediaId);
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

	public static void retrieve(String repositoryId, boolean thumb, String thumbId, Boolean getPublic, Boolean base64, Boolean forceRender) {
		if (getPublic == null)
				getPublic = false;
		if (base64 == null)
			base64 = false;
		if(forceRender == null)
			forceRender = false;
		Logger.warn("retrieve called: " + repositoryId + " " + thumb + " " + getPublic + " " + base64);
		if (!StringUtil.isNil(repositoryId)) {
			mediaService.retrieveFromRepository(repositoryId, thumb, getPublic, base64, forceRender);
		} else {
			mediaService.retrieveThumb(thumbId);
		}
		renderJSON(apiService.buildGenericErrorResponse());
	}
	
	/*public static void retrieveById(String id) {
		if (!StringUtil.isNil(id)) {
			mediaService.retrieveFromid(id);
		}
		renderJSON(apiService.buildGenericErrorResponse());
	}*/
}