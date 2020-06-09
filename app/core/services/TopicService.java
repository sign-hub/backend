package core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Topic;
import play.Logger;
import utils.StringUtil;

public class TopicService {
	private static enum TopicServiceSingleton {
		INSTANCE;

		TopicService singleton = new TopicService();

		public TopicService getSingleton() {
			return singleton;
		}
	}

	public static TopicService instance() {
		return TopicService.TopicServiceSingleton.INSTANCE.getSingleton();
	}
	
	private TopicService() {
		// TODO Auto-generated constructor stub
	}
	
	
	public List<Topic> getTopicList(){
		return Topic.all().fetch();
	}
	
	public Map<String, Topic> getTopicMap(){
		List<Topic> all = Topic.all().fetch();
		Map<String, Topic> ret = new HashMap<String, Topic>();
		
		for(Topic t : all){
			ret.put(t.getWord(), t);
		}
		
		return ret;
	}
	
	
	
	public void parseCsv(String path, String filename){
		 String csvFile = buildFilePath(path, filename);
		 parseCsv(new File(csvFile));
	}
	
	private void parseCsv(File f) {
	        BufferedReader br = null;
	        String line = "";
	        String cvsSplitBy = "\\|";

	        try {

	            br = new BufferedReader(new FileReader(f));
	            while ((line = br.readLine()) != null) {
	            	
	                String[] fields = line.split(cvsSplitBy);
	       
	                if(fields.length>0){
	                	
	                	String definition = fields[0];
	                	definition = definition.trim();
	                	if(StringUtil.isEmpty(definition)){
	                		Logger.warn("Skip definition ");
	                		continue;
	                	}
	                	for(int i=1; i<fields.length; i++){
	                		String name = fields[i];
	                		name = name.trim();
		                	if(StringUtil.isEmpty(name)){
		                		Logger.warn("Skip name ");
		                		continue;
		                	}
	                		Topic t = Topic.findByName(name);
	                		if(t==null){
		                		t = new Topic(name, definition);
		                		t.save();
	                		} else {
	                			Logger.warn("Skip: " + name);
	                		}
	                	}
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

	/*
	 * This function build the absolute path;
	 */
	private String buildFilePath(String path, String filename) {
		String ret = "";
		if(!StringUtil.isNil(path)){
			ret += path;
			if(!path.endsWith("/"))
				ret += "/";
		}
		if(!StringUtil.isNil(filename)){
			if(filename.startsWith("/"))
				filename = filename.substring(1);
			ret += filename;
		}
		return ret;
	}

	public void createTopics(String path, String filename) {
		if(StringUtil.isNil(path) || StringUtil.isNil(filename))
			return;
		File f = new File(path, filename);
		if(!f.exists()){
			Logger.info("file don't exists " + f.getAbsolutePath());
			return;
		}
		if(!f.isFile() || f.isDirectory()){
			Logger.info("file is not a file??? " + f.getAbsolutePath());
			return;
		}
			
		Topic.deleteAll();
		this.parseCsv(f);
		
	}

	
	
}
