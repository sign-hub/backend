package models;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class Topic extends ModelUuid {

	private String word;
	
	@Lob
	private String definition;
	
	public Topic() {
		// TODO Auto-generated constructor stub
	}
	

	public Topic(String word, String definition) {
		super();
		this.word = word;
		this.definition = definition;
	}



	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}


	public static Topic findByName(String name) {
		return Topic.find("word=?", name).first();
	}
	
	
	
}
