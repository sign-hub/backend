/**
 *  
 */
package core.services;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.jcodec.api.awt.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import com.ning.http.util.Base64;

import controllers.MediaController;
import core.services.ApiService.ApiErrors;
import core.services.ApiService.ErrorObject;
import core.services.ApiService.OKResponseObject;
import core.services.ApiService.ResponseStatus;
import models.Media;
import models.Media.MediaType;
import models.Media.MediasType;
import models.Question;
import models.Test.TestType;
import models.User.ToolsTypes;
import models.User;
import play.Logger;
import play.mvc.Router;
import play.mvc.results.*;
import utils.FileUploadManager;
import utils.FileUtil;
import utils.StringUtil;

/**
 *
 * @author luca
 */
public class MediaService {

	private static enum MediaServiceSingleton {
		INSTANCE;

		MediaService singleton = new MediaService();

		public MediaService getSingleton() {
			return singleton;
		}
	}
	
	private MediaService() {
		populateTypes();
	}

	public static MediaService instance() {
		return MediaService.MediaServiceSingleton.INSTANCE.getSingleton();
	}

	public static enum Thumb {
		AUDIO_THUMB, TEXT_THUMB, VIDEO_THUMB, PHOTO_THUMB;
	}

	private Map<String, MediaType> extensionMap;
	
	private static ApiService apiService = ApiService.instance();
	private static UserService userService = UserService.instance();
	private static FileUploadManager fileUploadManager = FileUploadManager.instance();
	//private static Map<String, Media> publicMap = new HashMap<String, Media>();
	private static Map<String, String> publicMap = new HashMap<String, String>();
	private static Map<String, String> publicMediaMap = new HashMap<String, String>();
	private static Map<String, Calendar> publicMediaDate = new HashMap<String, Calendar>();
	
	public static class MediaObject {
		public String mediaId;
		public String mediaType;
		public String mediaName;
		public String mediaPath;
		public String mediaThumbPath;
		public String mediaAuthorId;
		public String mediaAuthorName;
		public String mediaDate;
		public Boolean toEdit;
		public String publicUrl;
	}

	private void populateTypes(){
		if(this.extensionMap==null){
			extensionMap = new HashMap<String,MediaType>();
		}
		extensionMap.put("jpg", MediaType.PHOTO);
		extensionMap.put("jpeg", MediaType.PHOTO);
		extensionMap.put("png", MediaType.PHOTO);
		extensionMap.put("bmp", MediaType.PHOTO);
		extensionMap.put("gif", MediaType.PHOTO);
		extensionMap.put("flv", MediaType.VIDEO);
		extensionMap.put("avi", MediaType.VIDEO);
		extensionMap.put("mp4", MediaType.VIDEO);
		extensionMap.put("mov", MediaType.VIDEO);
		extensionMap.put("mpg", MediaType.VIDEO);
		extensionMap.put("wmv", MediaType.VIDEO);
		extensionMap.put("mp3", MediaType.AUDIO);
		extensionMap.put("waw", MediaType.AUDIO);
		extensionMap.put("wma", MediaType.AUDIO);
		extensionMap.put("txt", MediaType.TEXT);
		extensionMap.put("csv", MediaType.TEXT);
		extensionMap.put("elan", MediaType.ELAN);
	}
	
	public static class MediaListResponse extends OKResponseObject {
		public List<MediaObject> response;
	}
	
	public static class MediaResponse extends OKResponseObject {
		public MediaObject response;
	}

	public String mediaList(String mediaType, String mediaName, String mediaAuthor, String mediaDate,
			String mediaTestId, String tt, String orderBy, String orderType, String parentId, Integer page, Integer length) {
		if (!StringUtil.isNil(mediaType)) {
			MediaType check = MediaType.tryBuildMediaTypeFromValue(mediaType);
			if (check == null) {
				return apiService.buildNotValidMediaTypeErrorResponse();
			}
		}
		MediasType check = null;
		if (!StringUtil.isNil(tt)) {
			check = MediasType.tryBuildMediasTypeFromName(tt);
			if (check == null) {
				return apiService.buildNotValidMediaTypeErrorResponse();
			}
		}
		boolean isAdmin;
		String tooltype = null;
		if(check == MediasType.ATLAS)
			tooltype = ToolsTypes.ATLAS;
		else if(check == MediasType.GRAMMAR)
			tooltype = ToolsTypes.GRAMMAR;
		else if(check == MediasType.TESTINGTOOL)
			tooltype = ToolsTypes.TESTING;
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(tooltype)) {
			isAdmin = true;
		} else {
			isAdmin = false;
		}
		Boolean all = true;
		if(page != null)
			all = false;
		List<Media> mediaList = Media.findAllFilteredByMediaTypeAndMediaNameAndMediaAuthorAndMediaDateAndMediaTestId(
				isAdmin, mediaType, mediaName, mediaAuthor, mediaDate, mediaTestId, tt, 
				orderBy, orderType, parentId, true, all, page, length);
		MediaListResponse res = new MediaListResponse();
		res.response = buildMediaListObjectFromMediaList(mediaList);
		String ret = res.toJson();
		return ret;
	}
	
	public String getMedia(String mediaId) {
		Media m = Media.findById(mediaId);
		if(m==null)
			return apiService.buildGenericErrorResponse();;
		List<Media> mediaList = new ArrayList<Media>();
		mediaList.add(m);
		MediaResponse res = new MediaResponse();
		res.response = buildMediaObjectFromMedia(m);
		//res.response = buildMediaListObjectFromMediaList(mediaList);
		String ret = res.toJson();
		return ret;
	}

	public List<MediaObject> buildMediaListObjectFromMediaList(List<Media> mediaList) {
		List<MediaObject> ret = new LinkedList<MediaObject>();
		if (mediaList != null && !mediaList.isEmpty()) {
			for (Media media : mediaList) {
				MediaObject mediaObject = buildMediaObjectFromMedia(media);
				ret.add(mediaObject);
			}
		}
		return ret;
	}

	public MediaObject buildMediaObjectFromMedia(Media media) {
		MediaObject mediaObject = new MediaObject();
		if (media.getMediaAuthor() != null) {
			mediaObject.mediaAuthorId = media.getMediaAuthor().getUuid();
			mediaObject.mediaAuthorName = media.getMediaAuthor().getName();
		}
		mediaObject.mediaDate = StringUtil.date(media.getCreatedAt(), Media.CREATED_AT_DATE_FORMAT);
		mediaObject.mediaId = media.getUuid();
		mediaObject.mediaName = media.getMediaName();
		Map<String, Object> args = new LinkedHashMap<>();
		args.put("repositoryId", media.getRepositoryId());
		args.put("thumb", false);
		mediaObject.mediaPath = Router.reverse("MediaController.retrieve", args).url;
		
		if (media.getMediaType().equals(MediaType.VIDEO) || media.getMediaType().equals(MediaType.PHOTO)
				|| (!media.getMediaType().equals(MediaType.FOLDER) && !StringUtil.isNil(media.getThumbRepositoryId()))) {
			if (!StringUtil.isNil(media.getThumbRepositoryId())) {
				if (!media.getMediaType().equals(MediaType.TEXT) && !media.getMediaType().equals(MediaType.AUDIO)
						&& !media.getMediaType().equals(MediaType.OTHER)
						&& !media.getMediaType().equals(MediaType.ELAN)) {
					args.put("repositoryId", media.getThumbRepositoryId());
					args.put("thumb", true);
				} else {
					args.clear();
					if (media.getMediaType().equals(MediaType.AUDIO)) {
						args.put("thumbId", Thumb.AUDIO_THUMB.name());
					} else {
						args.put("thumbId", Thumb.TEXT_THUMB.name());
					}
				}
			} else if (media.getMediaType().equals(MediaType.VIDEO)) {
				args.clear();
				args.put("thumbId", Thumb.VIDEO_THUMB.name());
			} else if (media.getMediaType().equals(MediaType.PHOTO)) {
				args.clear();
				args.put("thumbId", Thumb.PHOTO_THUMB.name());
			}
			mediaObject.mediaThumbPath = Router.reverse("MediaController.retrieve", args).url;
		}
		mediaObject.mediaType = media.getMediaType().name();
		MediasType check = media.getTestType();
		String tooltype = null;
		if(check == MediasType.ATLAS)
			tooltype = ToolsTypes.ATLAS;
		else if(check == MediasType.GRAMMAR)
			tooltype = ToolsTypes.GRAMMAR;
		else if(check == MediasType.TESTINGTOOL)
			tooltype = ToolsTypes.TESTING;
		
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(tooltype)) {
			mediaObject.toEdit = true;
		} else {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (currentUserLogged != null && currentUserLogged.equals(media.getMediaAuthor())) {
				mediaObject.toEdit = true;
			} else {
				mediaObject.toEdit = false;
			}
		}
		if(media.getMediaType().equals(MediaType.VIDEO) || 
				media.getMediaType().equals(MediaType.ELAN) ||
				media.getMediaType().equals(MediaType.OTHER)){
			String code = null;
			if(StringUtil.isNil(media.getPublicCode())) {
				code = UUID.randomUUID().toString();
				media.setPublicCode(code);
				media.save();
			}
			else
				code = media.getPublicCode();
			instance().publicMap.put(code, media.getUuid());
			instance().publicMediaMap.put(media.getRepositoryId(), code);
			Calendar c = new GregorianCalendar();
			c.add(Calendar.MINUTE, 20);
			instance().publicMediaDate.put(media.getRepositoryId(), c);
			mediaObject.publicUrl = code;
		}
		
		return mediaObject;
	}

	public static class MediaDeleteRequest {
		public String mediaId;
	}

	public static class MediaDeleteResponse extends OKResponseObject {
		public Object response = new Object();
	}

	public String deleteMedia(String mediaId) {

		if (StringUtil.isNil(mediaId)) {
			return apiService.buildWrongEndpointErrorResponse();
		}

		Media media = Media.findById(mediaId);
		if (media == null) {
			return apiService.buildEntityDoesNotExistsErrorResponse();
		}

		MediasType check = media.getTestType();
		String tooltype = null;
		if(check == MediasType.ATLAS)
			tooltype = ToolsTypes.ATLAS;
		else if(check == MediasType.GRAMMAR)
			tooltype = ToolsTypes.GRAMMAR;
		else if(check == MediasType.TESTINGTOOL)
			tooltype = ToolsTypes.TESTING;
		
		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(tooltype)) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(media.getMediaAuthor())) {
				return apiService.buildNotAuthorizedResponse();
			}
		}

		if (media.getDeleted()) {
			return apiService.buildEntityAlreadyDeletedErrorResponse();
		}

		deleteMedia(media);

		/* move temporary media file in a deleted directory (in order to deny
		 media file and thumb as long as media is another time deleted = false
		 [there is no API to do this {updateMedia...}])*/

		MediaDeleteResponse res = new MediaDeleteResponse();
		String ret = res.toJson();
		return ret;
	}

	private boolean deleteMedia(Media media) {
		if(media == null)
			return false;
		
		media.setDeleted(true);
		media.save();
		if(media.getMediaType().equals(MediaType.FOLDER)) {
			deleteAllChilds(media);
		}
		return true;
	}

	private void deleteAllChilds(Media media) {
		List<Media> childs = Media.findAllFilteredByMediaTypeAndMediaNameAndMediaAuthorAndMediaDateAndMediaTestId(false, null, null, null, null, null, null, null, null, media.getUuid(), true, true, null, null);
		for(Media m: childs) {
			if(m==null)
				continue;
			deleteMedia(m);
		}
	}

	public static class MediaNewResponse extends OKResponseObject {
		public MediaObject response = new MediaObject();
	}
	
	public static class ListMediaNewResponse extends OKResponseObject {
		public List<MediaObject> response = new ArrayList<MediaObject>();
	}
	
	public String mediaNew(File file, String testType, String mediaTestId, String parentId, Boolean isFolder, String folderName) {
		MediaNewResponse res = mediaSingleNew(file, testType, mediaTestId, parentId, isFolder, folderName);
		String ret = res.toJson();
		return ret;
	}
	
	public String mediaListNew(List<File> files, String testType, String mediaTestId, String parentId, Boolean isFolder, String folderName) {
		ListMediaNewResponse res = new ListMediaNewResponse();
		res.response = new ArrayList<MediaObject>();
		for(File file: files) {
			MediaNewResponse r = mediaSingleNew(file, testType, mediaTestId, parentId, isFolder, folderName);
			if(r.status.equals(ResponseStatus.NOK.name())) {
				res.status = ResponseStatus.NOK.name();
				for(ErrorObject error : r.errors) {
					res.addError(error.errorMessage, error.errorCode);
				}
				break;
			} else {
				res.response.add(r.response);
			}
		}
		String ret = res.toJson();
		return ret;
	}
	
	

	private MediaNewResponse mediaSingleNew(File file, String testType, String mediaTestId, String parentId,
			Boolean isFolder, String folderName) {
		MediaNewResponse res = new MediaNewResponse();
		
		if(isFolder == null)
			isFolder = false;
		
		if(file == null && !StringUtil.isNil(folderName))
			isFolder = true;
		
		Boolean isUser = false;
		
		String subfolder = null;
		if(mediaTestId!=null && !mediaTestId.trim().equals("")){
			subfolder = mediaTestId;
			isUser = true;
		}
		
		
		MediasType check = null;
		if (!StringUtil.isNil(testType)) {
			check = MediasType.tryBuildMediasTypeFromName(testType);
			if (check == null) {
				res.status = ResponseStatus.NOK.name();
				res.addError(ApiErrors.NOT_VALID_MEDIA_TYPE_ERROR.getErrorMessage(),
						ApiErrors.NOT_VALID_MEDIA_TYPE_ERROR.getErrorCode());
				return res;
			}
		}
		if(check.equals(MediasType.ATLAS)) {
			String mediaName = folderName;
			if(isFolder == null || !isFolder) {
				mediaName = file.getName();
			}
			
			Media fm = null;
			if(parentId!=null) {
			fm = Media.find("mediaName=:mediaName and isUser=:isUser and parent.uuid=:parentId and deleted=false")
					.setParameter("mediaName", mediaName)
					.setParameter("isUser", isUser)
					.setParameter("parentId", parentId)
					.first();
			} else {
				fm = Media.find("mediaName=:mediaName and isUser=:isUser and deleted=false")
						.setParameter("mediaName", mediaName)
						.setParameter("isUser", isUser)
						.first();
			}
			if(fm!=null) {
				Logger.info("mediaName="+mediaName+" and isUser="+isUser+" and parent.uuid="+parentId);
				Logger.info("Media already found " + fm.getUuid());
				res.status = ResponseStatus.NOK.name();
				res.addError(ApiErrors.ENTITY_ALREADY_EXISTS_ERROR.getErrorMessage(),
						ApiErrors.ENTITY_ALREADY_EXISTS_ERROR.getErrorCode());
				return res;
			}
		}
		
		String repositoryId = null;
		MediaType mediaType = null;
		String mediaPath = null;
		File repositoryThumbnailFile = null;
		String mediaName = folderName;
		if(isFolder == null || !isFolder) {
			String tempId = fileUploadManager.handleUploadTemporary(file);
			if (tempId == null) {
				res.status = ResponseStatus.NOK.name();
				res.addError(ApiErrors.MANDATORY_PARAMS_ERROR.getErrorMessage(),
						ApiErrors.MANDATORY_PARAMS_ERROR.getErrorCode());
				return res;
			}
			repositoryId = fileUploadManager.tempToRepositoryFile(subfolder, tempId);
			if (repositoryId == null) {
				res.status = ResponseStatus.NOK.name();
				res.addError(ApiErrors.GENERIC_ERROR.getErrorMessage(),
						ApiErrors.GENERIC_ERROR.getErrorCode());
				return res;
			}
			File repositoryFile = fileUploadManager.getRepositoryFile(null, repositoryId);
			if (repositoryFile != null) {
				mediaPath = repositoryFile.getAbsolutePath();
			}
			mediaType = buildMediaTypeFromExtensionFile(repositoryId);
			if (mediaType == null) {
				res.status = ResponseStatus.NOK.name();
				res.addError(ApiErrors.MUST_SPECIFY_VALID_FILE_EXTENSION_ERROR.getErrorMessage(),
						ApiErrors.MUST_SPECIFY_VALID_FILE_EXTENSION_ERROR.getErrorCode());
				return res;
			}
			repositoryThumbnailFile = buildMediaThumbnailFile(repositoryFile, mediaType);
			mediaName = file.getName();
		} else {
			mediaType = MediaType.FOLDER;
		}

		Media media = new Media();
		media.setCreatedAt(new Date());
		media.setMediaAuthor(userService.getCurrentUserLogged());
		media.setIsUser(isUser);
		media.setMediaName(mediaName);
		media.setRepositoryId(repositoryId);
		media.setTestType(check);
		media.tryToSetParent(parentId);
		media.setMediaType(mediaType);
		media.setMediaPath(mediaPath);
		
		
		if (repositoryThumbnailFile != null) {
			media.setThumbRepositoryId(repositoryThumbnailFile.getName());
			media.setThumbPath(repositoryThumbnailFile.getAbsolutePath());
		}
		media.save();
		MediaObject mediaObject = buildMediaObjectFromMedia(media);
		
		res.response = mediaObject;
		return res;
	}
	

	private MediaType buildMediaTypeFromExtensionFile(String fileName) {
		if (StringUtil.isNil(fileName)) {
			return null;
		}
		String ext = FileUtil.extension(fileName);
		if (StringUtil.isNil(ext)) {
			return null;
		}
		ext = ext.toLowerCase();
		MediaType ret = extensionMap.get(ext);
		if(ret==null)
			ret = MediaType.OTHER;
		return ret;
		
		/*if (ext.equals("jpg") || ext.equals("png") || ext.equals("bmp")) {
			return MediaType.PHOTO;
		} else if (ext.equals("flv") || ext.equals("avi") || ext.equals("mp4") || ext.equals("mpg") || ext.equals("wmv")
				|| ext.equals("mpeg")) {
			return MediaType.VIDEO;
		} else if (ext.equals("mp3") || ext.equals("waw") || ext.equals("wma")) {
			return MediaType.AUDIO;
		} else if (ext.equals("txt") || ext.equals("csv")) {
			return MediaType.TEXT;
		} else {
			return null;
		}*/
	}

	private File buildMediaThumbnailFile(File repositoryFile, MediaType mediaType) {
		File mediaThumb = null;
		if (repositoryFile != null && mediaType != null) {
			if (mediaType.equals(MediaType.PHOTO)) {
				mediaThumb = createJpgFrom(repositoryFile);
				if (mediaThumb == null)
					mediaThumb = buildBasicThumb(MediaType.PHOTO);
			} else if (mediaType.equals(MediaType.VIDEO)) {
				mediaThumb = extractFirstVideoFrame(repositoryFile);
				if (mediaThumb == null)
					mediaThumb = buildBasicThumb(MediaType.VIDEO);
			} else if (mediaType.equals(MediaType.AUDIO) || mediaType.equals(MediaType.TEXT)
					|| mediaType.equals(MediaType.OTHER)
					|| mediaType.equals(MediaType.ELAN)) {
				mediaThumb = buildBasicThumb(mediaType);
			}
		}

		return mediaThumb;
	}

	private File buildBasicThumb(MediaType mediaType) {
		if (mediaType.equals(MediaType.AUDIO))
			return getThumb(Thumb.AUDIO_THUMB.name());
		else if (mediaType.equals(MediaType.TEXT) 
				|| mediaType.equals(MediaType.OTHER) 
				|| mediaType.equals(MediaType.ELAN))
			return getThumb(Thumb.TEXT_THUMB.name());
		else if (mediaType.equals(MediaType.VIDEO))
			return getThumb(Thumb.VIDEO_THUMB.name());
		else if (mediaType.equals(MediaType.PHOTO))
			return getThumb(Thumb.PHOTO_THUMB.name());
		else
			return null;
	}

	public File extractFirstVideoFrame(File repositoryFile) {
		File outputFile = null;
		String newExtension = "jpg";
		Picture picture = null;
		BufferedImage frame = null;
		try {
			picture = FrameGrab.getNativeFrame(repositoryFile, 0);
			frame = AWTUtil.toBufferedImage(picture);
			ImageIO.write(frame, "png", new File("frame_150.png"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(frame, newExtension, baos);
			baos.flush();
			String tempId = fileUploadManager.createTempFile(baos.toByteArray(), FileUtil.extension(repositoryFile));
			String repositoryId = fileUploadManager.tempToRepositoryFile(null, tempId, newExtension);
			outputFile = fileUploadManager.getRepositoryFile(null, repositoryId);
			return outputFile;
		} catch (Exception e) {
			Logger.error(e, "Exception during first video frame extraction... adding default thumb");
			return null;
		}
	}

	/* il repository contiene anche le thumb di PHOTO e VIDEO*/
	public static void retrieveFromRepository(String repositoryId, boolean thumb, Boolean getPublic, Boolean base64, Boolean forceRender) {
		String partialLog;
		if(getPublic == null)
			getPublic = false;
		Media media = null;
		if (!thumb) {
			partialLog = "repositoryId";
			media = Media.findByRepositoryId(repositoryId);
		} else {
			partialLog = "thumbRepositoryId";
			media = Media.findByThumbRepositoryId(repositoryId);
		}
		if(media == null) {
			Logger.warn("try to find by id...");
			media = Media.findById(repositoryId);
			if(media!=null) {
				System.out.println("media is null");
				repositoryId = media.getRepositoryId();
			}
		}
		if (media == null) {
			Logger.error("No media exists for " + partialLog + " %s", repositoryId);
			return;
		}
		/*if (!userService.hasCurrentUserLoggedAdminRole()) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (!currentUserLogged.equals(media.getMediaAuthor())) {
				Logger.error("User %s try to retrieveMediaFromRepository (" + partialLog + " %s) whose author is %s",
						currentUserLogged.getUuid(), repositoryId, media.getMediaAuthor().getUuid());
				return;
			}
		}*/
		MediasType check = media.getTestType();
		String tooltype = null;
		if(check == MediasType.ATLAS)
			tooltype = ToolsTypes.ATLAS;
		else if(check == MediasType.GRAMMAR)
			tooltype = ToolsTypes.GRAMMAR;
		else if(check == MediasType.TESTINGTOOL)
			tooltype = ToolsTypes.TESTING;
		
		if (/*!userService.hasCurrentUserLoggedAdminRole()*/ !userService.checkAdminAndTool(tooltype)) {
			User currentUserLogged = userService.getCurrentUserLogged();
			if (media.getDeleted()) {
				Logger.warn("User %s try to retrieveMediaFromRepository deleted (" + partialLog + " %s)",
						currentUserLogged.getUuid(), repositoryId);
				return;
			}
		}
		if(((media.getMediaType().equals(MediaType.VIDEO) 
				|| media.getMediaType().equals(MediaType.ELAN)
				|| media.getMediaType().equals(MediaType.OTHER)
				|| media.getMediaType().equals(MediaType.TEXT)
				|| (media.getMediaType().equals(MediaType.PHOTO) && getPublic == true)) 
				&& thumb==false) && !forceRender){
			String code = "";
			if(instance().publicMediaMap.containsKey(media.getRepositoryId())){
				code = instance().publicMediaMap.get(media.getRepositoryId());
			} else {
				if(StringUtil.isNil(media.getPublicCode())) {
					code = UUID.randomUUID().toString();
					media.setPublicCode(code);
					media.save();
				}
				else {
					code = media.getPublicCode();
				}
				
				instance().publicMap.put(code, media.getUuid());
				instance().publicMediaMap.put(media.getRepositoryId(), code);
				Calendar c = new GregorianCalendar();
				c.add(Calendar.MINUTE, 20);
				instance().publicMediaDate.put(media.getRepositoryId(),c);
			}
			String json = "{\"code\":\"" + code +"\"}";
			throw new RenderJson(json);
		}
		try {
			String subfolder = null;
			if(media.getIsUser()) {
				Question q = Question.findById(media.getQuestion());
				subfolder = q.getTest().getUuid();
				System.out.println("subfolder: " + subfolder);
			}
			if(thumb && repositoryId.equalsIgnoreCase("video-default-thumbnail.png")) {
					File f = new File(media.getThumbPath());
					if(!f.exists())
						return;
					if(!base64)
						throw new RenderBinary(f);
					byte[] bytes = FileUploadManager.loadFile(f);
					
					String encodedFile = Base64.encode(bytes);
					throw new RenderText(encodedFile);
			} else 
				fileUploadManager.renderRepositoryFile(subfolder, repositoryId, base64);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void retrievePublic(String code, Boolean base64) {
		String mediaUuid = instance().publicMap.get(code);
		
		Media m = null;
		if(mediaUuid != null)
			m = Media.findById(mediaUuid);
		else {
			m = Media.findByPublicCode(code);
		}
		if(m != null) {
			try {
				String subfolder = null;
				if(m.getIsUser()) {
					String questionId = m.getQuestion();
					if(StringUtil.isNil(questionId)) {
						Logger.error("ATTENZIONE - MEDIA DOWNLOAD: download del media ma la question id è null? " + questionId + " media: " + m.getUuid());
						throw new RenderJson("{\"error\":\"error\"}");
					}
					Question q = Question.findById(m.getQuestion());
					if(StringUtil.isNil(questionId)) {
						Logger.error("ATTENZIONE - MEDIA DOWNLOAD: download del media ma la question è null? " + questionId + " media: " + m.getUuid());
						throw new RenderJson("{\"error\":\"error\"}");
					}
					subfolder = q.getTest().getUuid();
					System.out.println("subfolder: " + subfolder);
				}
				fileUploadManager.renderRepositoryFile(subfolder, m.getRepositoryId(), base64);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new RenderJson("{\"error\":\"error\"}");
		}
	}
	
	public void retrievePublicMedia(String mediaUuid, Boolean base64) {
		Media m = Media.findById(mediaUuid);
		if(m != null) {
			if(!m.checkIsPublic()) {
				Logger.error("ATTENZIONE tentativo di recuperare un media non pubblico! " + mediaUuid);
				throw new RenderJson("{\"error\":\"error\"}");
			}
			try {
				String subfolder = null;
				if(m.getIsUser()) {
					String questionId = m.getQuestion();
					if(StringUtil.isNil(questionId)) {
						Logger.error("ATTENZIONE - MEDIA DOWNLOAD: download del media ma la question id è null? " + questionId + " media: " + m.getUuid());
						throw new RenderJson("{\"error\":\"error\"}");
					}
					Question q = Question.findById(m.getQuestion());
					if(StringUtil.isNil(questionId)) {
						Logger.error("ATTENZIONE - MEDIA DOWNLOAD: download del media ma la question è null? " + questionId + " media: " + m.getUuid());
						throw new RenderJson("{\"error\":\"error\"}");
					}
					subfolder = q.getTest().getUuid();
					System.out.println("subfolder: " + subfolder);
				}
				fileUploadManager.renderRepositoryFile(subfolder, m.getRepositoryId(), base64);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RenderJson("{\"error\":\"error\"}");
			}
		} else {
			throw new RenderJson("{\"error\":\"error\"}");
		}
	}

	public void retrieveThumb(String thumbId) {
		if (thumbId == null)
			return;
		if (!thumbId.equals(Thumb.AUDIO_THUMB.name()) && !thumbId.equals(Thumb.TEXT_THUMB.name())
				&& !thumbId.equals(Thumb.VIDEO_THUMB.name()) && !thumbId.equals(Thumb.PHOTO_THUMB.name())) {
			return;
		}
		File f = getThumb(thumbId);
		throw new RenderBinary(f);
	}

	private File getThumb(String thumbId) {
		if (StringUtil.isNil(thumbId))
			return null;
		String fileName = null;
		if (thumbId.equals(Thumb.AUDIO_THUMB.name())) {
			fileName = "audio-thumbnail.jpg";
		} else if (thumbId.equals(Thumb.TEXT_THUMB.name())) {
			fileName = "text-thumbnail.jpg";
		} else if (thumbId.equals(Thumb.VIDEO_THUMB.name())) {
			fileName = "video-default-thumbnail.png";
		} else if (thumbId.equals(Thumb.PHOTO_THUMB.name())) {
			fileName = "image-default-thumbnail.png";
		}
		File f = fileUploadManager.getRepositoryFile("Static_Thumbs", fileName);
		return f;
	}

	public File createJpgFrom(File input) {
		try {
			InputStream is = new FileInputStream(input);
			BufferedImage image = ImageIO.read(is);
			image = imageFillAlphaWithColor(image);
			Iterator<ImageWriter> jpgWriters = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter jpgWriter = jpgWriters.next();
			ImageWriteParam iwp = jpgWriter.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality(0.50f);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			jpgWriter.setOutput(ImageIO.createImageOutputStream(baos));
			jpgWriter.write(null, new IIOImage(image, null, null), iwp);
			String tempId = fileUploadManager.createTempFile(baos.toByteArray(), FileUtil.extension(input));
			String repositoryId = fileUploadManager.tempToRepositoryFile(null, tempId, "jpg");
			File outputFile = fileUploadManager.getRepositoryFile(null, repositoryId);
			return outputFile;
		} catch (Exception ex) {
			Logger.error(ex, "Error during jpg thumb creation... adding default thumb");
			return null;
		}
	}
	
	public static BufferedImage imageFillAlphaWithColor(BufferedImage image) {
		Color fillColor = new Color(255,255,255);
		
	    if (image.getColorModel().getTransparency() == Transparency.OPAQUE) return image;

	    int w = image.getWidth();
	    int h = image.getHeight();
	    BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = newImage.createGraphics();
	    g.drawImage(image, 0, 0, fillColor, null);
	    g.dispose();
	    return newImage;
	}

	public void removeOldPublic() {
		Calendar c = new GregorianCalendar();
		List<String> toremove = new ArrayList<String>();
		for(String mid : publicMediaDate.keySet()){
			Calendar cc = publicMediaDate.get(mid);
			if(c.after(cc)){
				toremove.add(mid);
			}
		}
		
		for(String mid : toremove){
			String code = publicMediaMap.get(mid);
			if(code != null)
				publicMap.remove(code);
			publicMediaMap.remove(mid);
			publicMediaDate.remove(mid);
		}
		
	}
}