package models;

import com.google.gson.Gson;

public class LogEntry {

	
	
	private String type;
	private String status;
	private String message;
	
	

	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public LogEntry(String type, String status, Object obj) {
		this.type = type;
		this.status = status;
		
		if(obj instanceof String){
			this.message = "" +obj;
		} else {
			try{
				Gson gson = new Gson();
				this.message = gson.toJson(obj);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}



	public void save() {
		// TODO Auto-generated method stub
	}

}
