package models;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import core.services.OptionService;
import core.services.QuestionService;
import core.services.UserService;
import models.Test.TestType;
import models.User.Roles;
import play.Logger;
import utils.StringUtil;

@Entity
public class Test extends ModelUuid {

	public static enum TestStatus {
		NEW, DRAFT, PUBLISHED;

		public static TestStatus tryBuildTestStatusFromName(String name) {
			if (StringUtil.isNil(name)) {
				return null;
			}

			for (TestStatus testStatus : values()) {
				if (testStatus.name().equals(name)) {
					return testStatus;
				}
			}

			return null;
		}
	}
	
	public static enum TestType {
		TESTINGTOOL, ATLAS;

		public static TestType tryBuildTestTypeFromName(String name) {
			if (StringUtil.isNil(name)) {
				return null;
			}

			for (TestType testType : values()) {
				if (testType.name().equals(name)) {
					return testType;
				}
			}

			return null;
		}
	}

	@Basic
	private String testName;

	@Basic
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private User author;

	@Basic
	private Boolean deleted = false;

	@Basic
	@Enumerated(EnumType.STRING)
	private TestStatus state = TestStatus.NEW;
	
	@Basic
	@Enumerated(EnumType.STRING)
	private TestType testType;

	@Basic
	private Date revisionDate = new Date();
	
	@Basic
	private Date creationDate = new Date();

	@Transient
	public static final String REVISION_DATE_FORMAT = "dd_MM_yyyy_HH:mm:ss:SSS";
	
	@Transient
	public static final String CREATION_DATE_FORMAT = "yyyy MM dd HH:mm:ss:SSS";
	
	@Transient
	public static final String SMALL_DATE_FORMAT = "yyyy/MM/dd HH:mm";

	@OneToMany(mappedBy="test")
	private List<Question> questions = new LinkedList<Question>();

	@OneToMany(mappedBy="test")
	private Set<Option> options = new LinkedHashSet<Option>();
	
	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinTable(name="test_contentprovideruser")
	private List<User> contentProviderList;
	
	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinTable(name="test_editoruser")
	private List<User> editorList;

	public List<User> getEditorList() {
		return editorList;
	}

	public void setEditorList(List<User> editorList) {
		this.editorList = editorList;
	}

	public TestType getTestType() {
		return testType;
	}

	public void setTestType(TestType testType) {
		this.testType = testType;
	}

	public List<User> getContentProviderList() {
		return contentProviderList;
	}

	public void setContentProviderList(List<User> contentProviderList) {
		this.contentProviderList = contentProviderList;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public TestStatus getState() {
		return state;
	}

	public void setState(TestStatus state) {
		this.state = state;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

	public String buildRevisionDateFormatted() {
		return StringUtil.date(this.getRevisionDate(), Test.REVISION_DATE_FORMAT);
	}

	public void setRevisionDate(Date revisionDate) {
		this.revisionDate = revisionDate;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public String buildCreationDateFormatted() {
		return StringUtil.date(this.getCreationDate(), Test.REVISION_DATE_FORMAT);
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	

	public TestType getType() {
		return testType;
	}

	public void setType(TestType type) {
		this.testType = type;
	}

	public List<Question> getQuestions() {
		Logger.info("Questions: " + questions.size());
		return orderQuestion(questions);
	}
	
	private List<Question> orderQuestion(List<Question> questions) {
		/*LinkedList<Question> ret = new LinkedList<Question>();
		Question[] tmp = new Question[questions.size()];
		for(Question question : questions){
			if(question==null)
					continue;
			if(question.getDeleted())
					continue;
			if(question.getOrder()!=null && question.getOrder()>=tmp.length) {
				question.setOrder(tmp.length-1);
				question.save();
			}
			if(question.getOrder()!=null && question.getOrder()<tmp.length && tmp[question.getOrder()] != null ){
				return reorderQuestionComparator();
			}
			tmp[question.getOrder()] = question;
		}
		for(int i=0; i<tmp.length; i++){
			Question question = tmp[i];
			if(question!=null)
				ret.add(question);
		}
		return ret;*/
		Collections.sort(questions, new Comparator<Question>() {

			@Override
			public int compare(Question o1, Question o2) {
				if(o1 == null && o2 == null)
					return 0;
				if(o1 == null || o1.getOrder() == null)
					return -1;
				if(o2 == null || o2.getOrder() == null) {
					return 1;
				}
				return o1.getOrder().compareTo(o2.getOrder());
			}
			
		});
		int lastOrder = -1;
		boolean needRecalc = false;
		for(int i = 0; i < questions.size(); i++) {
			Question q = questions.get(i);
			if(q == null) {
				q.setOrder(i);
				q.save();
			}
			if(q.getOrder() == lastOrder) {
				needRecalc = true;
			}
			lastOrder = q.getOrder();
		}
		
		if(needRecalc) {
			return reorderQuestionComparator();
		}
		
		return questions;
	}
	
	private List<Question> reorderQuestionComparator(){
		LinkedList<Question> ordered = new LinkedList<Question>();
		for(Question q: questions){
			if(q == null || q.getDeleted())
				continue;
			ordered.add(q);
		}
		
		Collections.sort(ordered, new Comparator<Question>(){

			@Override
			public int compare(Question o1, Question o2) {
				if(o1==null && o2 == null)
					return -1;
				if(o2==null)
					return 1;
				if(o1.getOrder()==null)
					o1.setOrder(0);
				if(o2.getOrder()==null)
					o2.setOrder(0);
				if(o1.getOrder().compareTo(o2.getOrder())!=0)
					return o1.getOrder().compareTo(o2.getOrder());
				if(o1.getCreationDate()!=null && o2.getCreationDate()!=null &&
						o1.getCreationDate().compareTo(o2.getCreationDate()) != 0)
					return o1.getCreationDate().compareTo(o2.getCreationDate());
				return o1.getUuid().compareTo(o2.getUuid());
			}
			
		});
		
		for(int i = 0; i < ordered.size(); i++){
			Question q = ordered.get(i);
			q.setOrder(i);
			q.save();	
		}
		return ordered;
	}

	private List<Question> reorderQuestion() {
		Map<Integer, String> tmp1 = new HashMap<Integer, String>();
		for(int i = 0; i< questions.size(); i++){
			if(questions.get(i).getDeleted())
				continue;
			tmp1.put(i, "-");
		}
		for(Question question : questions){
			if(question==null)
					continue;
			if(question.getDeleted())
					continue;
			if(question.getOrder() >= tmp1.size()){
				question.setOrder(0);
				question.save();
			}
			if(tmp1.get(question.getOrder()).equals("-")){
				tmp1.put(question.getOrder(), question.uuid);
			} else {
				if(question.getOrder()==tmp1.size()-1){
					for(int i = 0; i < tmp1.size(); i++){
						if(tmp1.get(i).equals("-")){
							question.setOrder(i);
							question.save();
							tmp1.put(question.getOrder(), question.uuid);
						}
					}
				} else {
					//Logger.info("Posizione occupata: " + question.getOrder());
					for(int i = tmp1.size()-2; i>=question.getOrder(); i--){
						//Logger.info("moving " );
						if(tmp1.get(i).equals("-"))
							continue;
						tmp1.put(i+1, tmp1.get(i));
					}
					tmp1.put(question.getOrder(), question.uuid);
				}
			}
		}
		for(Integer pos : tmp1.keySet()){
			String uuid = tmp1.get(pos);
			if(StringUtil.isNil(uuid) || uuid.equals("-"))
				continue;
			Question q = Question.findById(uuid);
			if(q==null){
				Logger.warn("TEST reorder question: Q is null??? " + uuid);
				continue;
			}
			q.setOrder(pos);
			q.save();
		}
		return orderQuestion(questions);
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public Set<Option> getOptions() {
		return options;
	}

	public void setOptions(Set<Option> options) {
		this.options = options;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public boolean addQuestion(Question question) {
		if (question == null) {
			return false;
		}

		if (this.questions.contains(question)) {
			return false;
		}
		question.setOrder(this.questions.size());
		question.save();
		return this.questions.add(question);
	}

	public boolean addOption(Option option) {
		if (option == null) {
			return false;
		}

		if (this.options.contains(option)) {
			return false;
		}
		return options.add(option);
	}

	public static List<Test> findAllByDeleted(Boolean deleted, TestType type) {
		List<Test> ret = new LinkedList<Test>();
		if (deleted == null) {
			ret = Test.find("testType=:type").setParameter("testType", type).fetch();
		} else {
			ret = Test.find("deleted=:deleted and testType=:type").setParameter("deleted", deleted).setParameter("type", type).fetch();
		}
		return ret;
	}
	
	public static List<Test> findAllByDeletedOrdered(Boolean deleted, TestType type, String orderby, String ordertype) {
		if(StringUtil.isNil(orderby) || StringUtil.isNil(ordertype))
			return findAllByDeleted(deleted, type);
		List<Test> ret = new LinkedList<Test>();
		if (deleted == null) {
			ret = Test.find("testType=:type order by :orderby").setParameter("testType", type).setParameter("orderby", orderby).fetch();
		} else {
			ret = Test.find("deleted=:deleted and testType=:type order by "+ orderby +" " +ordertype).setParameter("deleted", deleted).setParameter("type", type).fetch();
		}
		return ret;
	}

	public List<Question> retrieveQuestionsByDeleted(Boolean deleted) {
		List<Question> ret = new LinkedList<Question>();
		if (deleted == null) {
			ret = getQuestions();
		} else {
			for (Question question : getQuestions()) {
				if (question.getDeleted() == deleted) {
					ret.add(question);
				}
			}
		}
		return ret;
	}

	public Test clone() throws CloneNotSupportedException {
		try {
			OptionService optionService = OptionService.instance();
			QuestionService questionService = QuestionService.instance();
			Test clonedTest = new Test();
			clonedTest.setTestName(this.getTestName() + "-clone");
			clonedTest.setState(this.getState());
			clonedTest.setType(this.getType());
			clonedTest.setAuthor(UserService.instance().getCurrentUserLogged());
			clonedTest.setDeleted(this.isDeleted());
			clonedTest.save();
			Logger.warn("Test cloned orig " + this.getUuid() + " cloned " + clonedTest.getUuid());
			
			Set<Option> clonedOptions = optionService.cloneOptionsList(clonedTest, null, null, null,
					this.getOptions());
			clonedTest.setOptions(clonedOptions);
			clonedTest.save();

			List<Question> clonedQuestions = questionService.cloneQuestionsList(clonedTest, this.getQuestions());
			clonedTest.setQuestions(clonedQuestions);

			clonedTest.save();

			return clonedTest;
		} catch (Exception ex) {
			Logger.error(ex, "Exception during Test clonation...return null");
			return null;
		}
	}
	
	public static List<Test> findAllOrdered(TestType type){
		return Test.find("testType=:type order by :revisiondate DESC").setParameter("type", type).setParameter("revisiondate", "revisionDate").fetch();
	}

	public static List<Test> findAll(TestType type) {
		return Test.find("testType=:type").setParameter("type", type).fetch();
	}
	
	public static List<Test> findAllOrdered(TestType type, String orderby, String ordertype) {
		return findAllOrdered(type, orderby, ordertype, false);
	}
	
	public static List<Test> findAllOrdered(TestType type, String orderby, String ordertype, Boolean deleted) {
		if(StringUtil.isNil(orderby) || StringUtil.isNil(ordertype))
			return findAll(type);
		if(deleted == null)
			deleted = false;
		return Test.find("testType=:type and deleted=:deleted  order by " +orderby + " " +ordertype).setParameter("type", type).setParameter("deleted", deleted).fetch();
	}

	public static List<Test> findAllByContentProviderOrdered(User user, TestType type, String orderby,
			String ordertype) {
		if(StringUtil.isNil(orderby) || StringUtil.isNil(ordertype))
			return findAll(type);
		Boolean deleted = false;
		String q = "testType=:type and deleted=:deleted and :user MEMBER OF contentProviderList";
		q += " and uuid IN (select test.uuid from Option where keyfield ='startSubmisssionInterval' and STR_TO_DATE(valuefield, '%Y%m%d') <= CURDATE()) ";
		q += " and uuid IN (select test.uuid from Option where keyfield ='endSubmisssionInterval' and STR_TO_DATE(valuefield, '%Y%m%d') > CURDATE()) ";
		q += " and state =:status";
		q += " order by " +orderby + " " +ordertype;
		System.out.println(q);
		
		return Test.find(q)
				.setParameter("type", type)
				.setParameter("deleted", deleted)
				.setParameter("user", user)
				.setParameter("status", TestStatus.PUBLISHED)
				.fetch();
	
	}
	

	public Map<String, Map<Integer, Question>> extractGroups(){
		Map<String, Map<Integer, Question>> ret = new LinkedHashMap<String, Map<Integer, Question>>();
	      for (Question q : this.questions) {
	        String rb = "_default_";
	        if (q.getOptions() != null) {
	          if (!StringUtil.isNil(q.getFromOption("randomizationBlock"))) {
	            rb = q.getFromOption("randomizationBlock");
	          }
	        }
	        if(!ret.containsKey(rb))
	        	ret.put(rb, new LinkedHashMap<Integer, Question>());
	      	ret.get(rb).put(ret.get(rb).size(), q);
	      }
		return ret;
	}

	public boolean getCanEdit(UserService userService) {
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(this))) {
			return true;
		} else {
			User user = userService.getCurrentUserLogged();
			if(this.getTestType().equals(TestType.TESTINGTOOL) &&user.retrieveUserRolesString().contains(Roles.TESTING_EDITOR) 
					&& (this.getAuthor().equals(user) || this.getContentProviderList().contains(user)))
					return true;
		}
		return false;
	}

	public boolean getCanView(UserService userService) {
		if (/*userService.hasCurrentUserLoggedAdminRole()*/ userService.checkAdminAndTool(QuestionService.getToolType(this))) {
			//Logger.info("TEST getCanView " + this.testName + " is admin");
			return true;
		} else {
			User user = userService.getCurrentUserLogged();
			if(this.getAuthor().equals(user)) {
				//Logger.info("TEST getCanView " + this.testName + " is author");
				return true;
			}
			if(this.getTestType().equals(TestType.TESTINGTOOL) && 
					(user.retrieveUserRolesString().contains(Roles.TESTING_EDITOR) || user.retrieveUserRolesString().contains(Roles.TESTING_USER))&& 
					this.getContentProviderList().contains(user)) {
					//Logger.info("TEST getCanView " + this.testName + " is in list!");
					return true;
			}
		}
		return false;
	}
	
}
