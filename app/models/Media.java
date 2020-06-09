package models;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.jcodec.common.logging.Logger;

import utils.StringUtil;

@Entity
public class Media extends ModelUuid implements Cloneable {
	
	public static enum MediasType {
		TESTINGTOOL, ATLAS, GRAMMAR;

		public static MediasType tryBuildMediasTypeFromName(String name) {
			if (StringUtil.isNil(name)) {
				return null;
			}

			for (MediasType testType : values()) {
				if (testType.name().equals(name)) {
					return testType;
				}
			}

			return null;
		}
	}
	
	public static enum MediaType {
		VIDEO, PHOTO, AUDIO, TEXT, ELAN, OTHER, FOLDER;

		public static MediaType tryBuildMediaTypeFromValue(String val) {
			if (StringUtil.isNil(val)) {
				return null;
			}

			for (MediaType mediaType : values()) {
				if (mediaType.name().equals(val)) {
					return mediaType;
				}
			}

			return null;
		}
	}

	@Basic
	@Enumerated(EnumType.STRING)
	private MediaType mediaType;
	
	@Basic
	@Enumerated(EnumType.STRING)
	private MediasType testType;

	@Basic
	private String mediaName;

	@Basic
	private String repositoryId;

	@Basic
	private String mediaPath;

	@Basic
	private String thumbRepositoryId;

	@Basic
	private String thumbPath;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private User mediaAuthor;

	@Basic
	private Date createdAt = new Date();

	@Transient
	public static final String CREATED_AT_DATE_FORMAT = "dd/MM/yyyy";

	@Basic
	private Boolean deleted = false;
	
	@Basic
	private Boolean isUser = false;

	@Basic
	private String report;

	private String question;
	
	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	private Media parent;
	
	@Basic
	private String publicCode;
	

	public Boolean getIsUser() {
		return isUser;
	}

	public void setIsUser(Boolean isUser) {
		this.isUser = isUser;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaName() {
		return mediaName;
	}

	public void setMediaName(String mediaName) {
		this.mediaName = mediaName;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}

	public User getMediaAuthor() {
		return mediaAuthor;
	}

	public void setMediaAuthor(User mediaAuthor) {
		this.mediaAuthor = mediaAuthor;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getThumbPath() {
		return thumbPath;
	}

	public void setThumbPath(String thumbPath) {
		this.thumbPath = thumbPath;
	}

	public String getThumbRepositoryId() {
		return thumbRepositoryId;
	}

	public void setThumbRepositoryId(String thumbRepositoryId) {
		this.thumbRepositoryId = thumbRepositoryId;
	}
	
	

	public Media getParent() {
		return parent;
	}

	public void setParent(Media parent) {
		this.parent = parent;
	}

	public static List<Media> findAllFilteredByMediaTypeAndMediaNameAndMediaAuthorAndMediaDateAndMediaTestId(
			boolean isAdmin, String mediaType, String mediaName, String mediaAuthor, String mediaDate,
			String mediaTestId, String testType, String orderBy, String orderType, String parentId, 
			Boolean showFolders, Boolean all, Integer page, Integer length) {
		List<Media> ret = new LinkedList<Media>();
		if (!StringUtil.isNil(mediaTestId)) {
			Test t = Test.findById(mediaTestId);
			List<SlideContentComponent> list = SlideContentComponent.findByTest(t);
			if (list != null && !list.isEmpty()) {
				for (SlideContentComponent c : list) {
					Media m = c.getMedia();
					if (m != null){
						ret.add(c.getMedia());
					}
				}
			}
			return ret;
		} else {
			boolean filtered = false;
			String query = "";
			String sep = "";
			Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
			if (!StringUtil.isNil(mediaType)) {
				filtered = true;
				String sep1 = "";
				String sep2 = "";
				if(showFolders) {
					sep1 = "(";
					sep2 = " OR mediaType LIKE 'FOLDER')";
				}
				
				query += sep + sep1 + "mediaType LIKE :mediaType" + sep2;
				sep = " AND ";
				queryParams.put("mediaType", MediaType.valueOf(mediaType));
			}
			if (!StringUtil.isNil(mediaName)) {
				filtered = true;
				query += sep + "mediaName LIKE :mediaName";
				sep = " AND ";
				queryParams.put("mediaName", "%" + mediaName + "%");
			}
			if (!StringUtil.isNil(mediaAuthor)) {
				filtered = true;
				query += sep + "mediaAuthor.name LIKE :mediaAuthor";
				sep = " AND ";
				queryParams.put("mediaAuthor", "%" + mediaAuthor + "%");
			}
			if (!StringUtil.isNil(mediaDate)) {
				Date d = StringUtil.tryParseDate(mediaDate, null, Media.CREATED_AT_DATE_FORMAT);
				filtered = true;
				query += sep + "createdAt >= :mediaDate";
				sep = " AND ";
				queryParams.put("mediaDate", d);
			}
			if (!StringUtil.isNil(parentId)) {
				filtered = true;
				query += sep + "parent.uuid = :parentId";
				sep = " AND ";
				queryParams.put("parentId",  parentId );
			} else {
				query += sep + "parent = null";
				sep = " AND ";
			}
			
			if (!StringUtil.isNil(testType)) {
				filtered = true;
				query += sep + "testType LIKE :testType";
				sep = " AND ";
				queryParams.put("testType", MediasType.valueOf(testType));
			}
			

			if (!filtered) {
				/*if (isAdmin)
					ret = Media.findAll();
				else*/
					ret = Media.findAllByDeleted(false);
			} else {
				if (!isAdmin) {
					query += sep + "deleted = :deleted";
					queryParams.put("deleted", false);
				} else {
					query += sep + "deleted = :deleted";
					queryParams.put("deleted", false);
				}
				
				if (!StringUtil.isNil(orderBy)) {
					query += " ORDER BY " + orderBy;
					if (!StringUtil.isNil(orderType))
						query += " " + orderType;
				}
				System.out.println(query);
				JPAQuery q = Media.find(query);
				for (String key : queryParams.keySet()) {
					q.setParameter(key, queryParams.get(key));
				}
				if(all == null || all) {
					ret = q.fetch();
				} else {
					if(page == null || page <= 0)
						page = 1;
					if(length == null || length <= 0)
						length = 10;
					ret = q.fetch(page, length);
				}
			}
		}
		return ret;
	}

	public static List<Media> findAllByDeleted(Boolean deleted) {
		List<Media> ret = new LinkedList<Media>();
		if (deleted == null) {
			ret = Media.findAll();
		} else {
			ret = Media.find("deleted=:deleted").setParameter("deleted", deleted).fetch();
		}
		return ret;
	}

	public static Media findByRepositoryId(String repositoryId) {
		if (StringUtil.isNil(repositoryId)) {
			return null;
		}
		Media ret = Media.find("repositoryId=:repositoryId").setParameter("repositoryId", repositoryId).first();

		return ret;
	}

	public static Media findByThumbRepositoryId(String thumbRepositoryId) {
		if (StringUtil.isNil(thumbRepositoryId)) {
			return null;
		}
		Media ret = Media.find("thumbRepositoryId=:thumbRepositoryId")
				.setParameter("thumbRepositoryId", thumbRepositoryId).first();

		return ret;
	}

	public void setTestType(MediasType tt) {
		this.testType = tt;
	}

	public MediasType getTestType() {
		return testType;
	}

	public void setReport(String reportUuid) {
		this.report = reportUuid;
	}

	public void setQuestion(String qid) {
		this.question = qid;
	}

	public String getReport() {
		return report;
	}

	public String getQuestion() {
		return question;
	}

	public static List<Media> findByReport(String reportUuid) {
		return find("report=:rep").setParameter("rep", reportUuid).fetch();
	}

	public void tryToSetParent(String parentId) {
		Logger.info("try to set parent " + parentId);
		if(!StringUtil.isNil(parentId)) {
			Media parent = Media.findById(parentId);
			if(parent!=null && parent.getMediaType().equals(MediaType.FOLDER)) {
				this.parent = parent;
				return;
			}
			Logger.error("Cannot set parent: " + parentId + " because type is " + MediaType.FOLDER.name());
		}
		
	}

	public boolean checkIsPublic() {
		// TODO Auto-generated method stub
		if(this.testType.equals(MediasType.GRAMMAR))
			return true;
		return false;
	}
	
	public String getPublicCode() {
		return this.publicCode;
	}

	public void setPublicCode(String code) {
		this.publicCode = code;
	}

	public static Media findByPublicCode(String code) {
		return find("publicCode=:rep").setParameter("rep", code).first();
	}
	
}