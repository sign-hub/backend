/**
 * Copyright 2015 by Eclettica s.r.l. All Rights Reserved.
 *
 * This file is part of edrain project.
 *
 * The contents of this file are subject to the Eclettica Private License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclettica.net/code/license
 * 
 * Original file name: FileUploadManager.java
 * Created by gannunziata at 14:18:58
 */
package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.ning.http.util.Base64;

import play.Logger;
import play.Play;
import play.mvc.results.RenderBinary;
import play.mvc.results.RenderText;
import utils.cfg.CfgUtil;

/**
 * 
 * @author gannunziata
 */
public class FileUploadManager {
	private static enum FileUploadManagerSingleton {
		INSTANCE;

		private static final FileUploadManager singleton = new FileUploadManager();

		public FileUploadManager getSingleton() {
			return singleton;
		}
	}

	public static FileUploadManager instance() {
		return FileUploadManager.FileUploadManagerSingleton.INSTANCE.getSingleton();
	}

	private FileUploadManager() {

	}

	Map<String, File> tempFiles = Collections.synchronizedMap(new HashMap<String, File>());
	File tempFolder = null;

	public File getTmpFolder() {
		if (tempFolder != null)
			return tempFolder;

		tempFolder = new File(Play.tmpDir, "mediaFiles");
		tempFolder.mkdirs();
		return tempFolder;
	}

	public File getTmpFolder(String subfolder) {
		if (tempFolder != null)
			return tempFolder;

		tempFolder = new File(getRepositoryFolder(subfolder), "mediaFiles");
		tempFolder.mkdirs();
		return tempFolder;
	}

	public String createTempFile(byte[] data, String ext) {

		String tempId = UuidGenerator.generate("TMP_");
		if (ext != null && !ext.isEmpty())
			tempId += "." + ext;

		File destFile = new File(getTmpFolder(), tempId);
		tempFiles.put(tempId, destFile);

		try {
			FileUtils.writeByteArrayToFile(destFile, data);
		} catch (IOException e) {
			return null;
		}

		return tempId;
	}

	public String createTempFile(String ext) {

		String tempId = UuidGenerator.generate("TMP_");
		if (ext != null && !ext.isEmpty())
			tempId += "." + ext;

		File destFile = new File(getTmpFolder(), tempId);
		tempFiles.put(tempId, destFile);
		return tempId;
	}

	/**
	 * @param prefix
	 * @param ext
	 * @return
	 */
	public File createTempFileWith(String prefix, String ext) {
		String tempId = UuidGenerator.generate20(prefix);
		if (ext != null && !ext.isEmpty())
			tempId += "." + ext;

		return new File(getTmpFolder(), tempId);
	}

	/**
	 * @param prefix
	 * @param ext
	 * @return
	 */
	public File createTempFileWith(String subfolder, String prefix, String ext) {
		String tempId = UuidGenerator.generate20(prefix);
		if (ext != null && !ext.isEmpty())
			tempId += "." + ext;

		return new File(getTmpFolder(subfolder), tempId);
	}

	public File createDirectTempFile(String ext) {
		String id = createTempFile(ext);
		return getTempFile(id);
	}

	public String handleUploadTemporary(File file) {
		if (file == null || !file.exists()) {
			System.out.println("FILE IS NULL???");
			return null;
		}

		String tempId = UuidGenerator.generate("TMP_");
		String ext = FileUtil.extension(file);
		if (!ext.isEmpty())
			tempId += "." + ext;
		System.out.println(tempId);
		try {
			File destFile = new File(getTmpFolder(), tempId);
			FileUtils.moveFile(file, destFile);
			tempFiles.put(tempId, destFile);
			return tempId;
		} catch (IOException e) {
			Logger.error(e, "move tmp file error");
			return null;
		}

	}

	/**
	 * 
	 * @param subfolder
	 * @param currId
	 * @param base64 
	 * @throws IOException 
	 */
	public void renderRepositoryFile(String subfolder, String currId) throws IOException {
		renderRepositoryFile(subfolder, currId, false);
	}
	public void renderRepositoryFile(String subfolder, String currId, Boolean base64) throws IOException {
		File curr = getRepositoryFile(subfolder, currId);
		if (curr == null || !curr.exists() || curr.length() <= 0)
			return;
		Logger.warn("to render file " + curr.getName() + " " + (curr.length()/(1024*1024)));
		if(!base64)
			throw new RenderBinary(curr);
		byte[] bytes = loadFile(curr);
		
		String encodedFile = Base64.encode(bytes);
		throw new RenderText(encodedFile);
	}
	
	public static byte[] loadFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }
	    byte[] bytes = new byte[(int)length];
	    
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    is.close();
	    return bytes;
	}

	public File getRepositoryFile(String subfolder, String currId) {
		File folder = getRepositoryFolder(subfolder);
		if (folder == null)
			return null;

		if (!folder.exists())
			return null;

		File curr = new File(folder, currId);
		if (!curr.exists())
			return null;
		return curr;
	}

	public void renderTempFile(String tempId) {
		File file = tempFiles.get(tempId);
		if (file == null)
			return;

		throw new RenderBinary(file);
	}

	public void renderDownloadTempFile(String tempId) {
		renderDownloadTempFile(tempId, null);
	}

	public void renderDownloadTempFile(String tempId, String name) {
		File file = tempFiles.get(tempId);
		if (file == null)
			return;
		if (name == null)
			name = file.getName();

		throw new RenderBinary(file, name, false);
	}

	/**
	 * 
	 * @param companyCodeName
	 * @param currId
	 */
	public void renderDownloadRepositoryFile(String companyCodeName, String currId) {
		File curr = getRepositoryFile(companyCodeName, currId);
		if (!curr.exists())
			return;

		throw new RenderBinary(curr, curr.getName(), false);
	}

	public boolean existsRepositoryFile(String companyCodeName, String fileId) {
		File folder = getRepositoryFolder(companyCodeName);
		if (folder == null)
			return false;

		File curr = new File(folder, fileId);
		return curr.exists();
	}

	/**
	 * 
	 * @param companyCodeName
	 * @return
	 */
	public File getRepositoryFolder(String subfolder) {
		File currRepository = null;

		String repo = CfgUtil.getString("media.repository.path", "/usr/cini/media_repository");
		currRepository = new File(repo);
		currRepository.mkdirs();
		if (!StringUtil.isNil(subfolder)) {
			currRepository = new File(repo + "/" + subfolder);
		} else {
			currRepository = new File(repo);
		}

		currRepository.mkdirs();
		return currRepository;
	}

	/**
	 * 
	 * @param companyCodeName
	 * @param tmpFile
	 * @return
	 */
	public String tempToRepositoryFile(String subFolder, String tmpFile) {
		File tmp = tempFiles.get(tmpFile);
		if (tmp == null)
			return null;
		String ext = FileUtil.extension(tmp);
		String currId = UuidGenerator.generate("REP_") + "." + ext;
		File curr = new File(getRepositoryFolder(subFolder), currId);
		try {
			FileUtils.moveFile(tmp, curr);
			tempFiles.remove(tmpFile);
			return currId;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 * @param companyCodeName
	 * @param tmpFile
	 * @return
	 */
	public String tempToRepositoryFile(String subFolder, String tmpFile, String newExtension) {
		File tmp = tempFiles.get(tmpFile);
		if (tmp == null)
			return null;
		String ext = null;
		if (StringUtil.isNil(newExtension))
			ext = FileUtil.extension(tmp);
		else
			ext = newExtension;
		String currId = UuidGenerator.generate("REP_") + "." + ext;
		File curr = new File(getRepositoryFolder(null), currId);
		try {
			FileUtils.moveFile(tmp, curr);
			tempFiles.remove(tmpFile);
			return currId;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param tempId
	 */
	public void deleteTemp(String tempId) {
		if (tempId == null)
			return;

		File tmp = tempFiles.get(tempId);
		if (tmp != null) {
			tmp.delete();
			tempFiles.remove(tempId);
		}
	}

	/**
	 * @param tempFileId
	 * @return
	 */
	public File getTempFile(String tempFileId) {
		if (tempFileId == null)
			return null;
		File tmp = tempFiles.get(tempFileId);
		return tmp;
	}

	/**
	 * 
	 * @param companyCodeName
	 * @param file
	 * @return
	 */
	public File createEmptyRepositoryFile(String companyCodeName, String pre, String ext) {
		String currId = pre + UuidGenerator.generate20("") + "." + ext;
		File curr = new File(getRepositoryFolder(companyCodeName), currId);
		try {
			if (curr.exists() || curr.isDirectory())
				return null;

			curr.createNewFile();
			return curr;
		} catch (Exception e) {

			return null;
		}
	}

	/**
	 * 
	 * @param companyCodeName
	 * @param file
	 * @return
	 */
	public String createRepositoryFile(String companyCodeName, File file) {
		String ext = FileUtil.extension(file);
		String currId = UuidGenerator.generate("REP_") + "." + ext;
		File curr = new File(getRepositoryFolder(companyCodeName), currId);
		try {
			FileUtils.moveFile(file, curr);
			return currId;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 * @param companyCodeName
	 * @param repoId
	 * @return
	 */
	public boolean removeRepositoryFile(String companyCodeName, String repoId) {
		File curr = new File(getRepositoryFolder(companyCodeName), repoId);
		try {
			if (curr.exists())
				curr.delete();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * @param companyCodeName
	 * @param oldRepoId
	 * @param file
	 * @return
	 */
	public String replaceRepositoryFile(String companyCodeName, String oldRepoId, File file) {
		if (oldRepoId != null && !oldRepoId.startsWith("/"))
			removeRepositoryFile(companyCodeName, oldRepoId);

		return createRepositoryFile(companyCodeName, file);
	}

	/**
	 * 
	 * @param tempFile
	 * @return
	 */
	public String createTempFile(File tempFile) {
		if (tempFile == null)
			return null;
		String tempId = UuidGenerator.generate("TMP_");
		String ext = FileUtil.extension(tempFile);
		if (ext != null && !ext.isEmpty())
			tempId += "." + ext;

		File destFile = new File(getTmpFolder(), tempId);
		tempFiles.put(tempId, destFile);

		try {
			FileUtils.moveFile(tempFile, destFile);
		} catch (IOException e) {
			return null;
		}

		return tempId;

	}

}
