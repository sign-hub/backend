/**
 *  
 */
package core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.text.html.CSS;

import com.google.gson.Gson;

import play.Play;
import play.mvc.Router;
import play.mvc.Router.Route;
import schedulers.ReportJob;
import schedulers.WorkerJob;
import core.services.ApiService.ApiErrors;
import core.services.ApiService.NOKResponseObject;
import core.services.ApiService.OKResponseObject;
import core.services.MediaService.MediaObject;
import core.services.QuestionService.QuestionObjectComplete;
import core.services.TestService.CreateTestRequest;
import core.services.TestService.Language;
import models.Media;
import models.Option;
import models.Question;
import models.Report;
import models.Slide;
import models.SlideContentComponent;
import models.SlideContentComponent.ComponentType;
import models.Test;
import models.Test.TestStatus;
import models.Test.TestType;
import models.User;
import models.User.ToolsTypes;
import models.Slide.SlideType;
import play.Logger;
import utils.CsvToExcel;
import utils.StringUtil;
import utils.cfg.CfgUtil;

/**
 *
 * @author luca
 */
public class ReportService {

	private static enum ReportServiceSingleton {
		INSTANCE;

		ReportService singleton = new ReportService();

		public ReportService getSingleton() {
			return singleton;
		}
	}

	public static ReportService instance() {
		return ReportService.ReportServiceSingleton.INSTANCE.getSingleton();
	}

	private static ApiService apiService = ApiService.instance();
	private static OptionService optionService = OptionService.instance();
	private static QuestionService questionService = QuestionService.instance();
	private static UserService userService = UserService.instance();
	
	public static final String ATLAS_REPORT_LIST_DATE_FORMAT = "dd_MM_yyyy_HH:mm";


	public static class ReportObjectSmall {
		public String reportId;
		public String reportDate;
		public String reportCsvPath;
		public String reportTestName;
		public String reportTestId;
		public String authorId;
		public Boolean isSaved;
		public Boolean isUploaded;
		public Boolean toEdit;
		public String language;
		public String reportMnemonicId;

		public ReportObjectSmall(String reportId, String reportDate, String reportCsvPath, String reportTestName,
				String reportTestId, String authorId, String language) {
			this.reportId = reportId;
			this.reportDate = reportDate;
			this.reportCsvPath = reportCsvPath;
			this.reportMnemonicId = this.extractFileName(reportCsvPath, reportId);
			this.reportTestName = reportTestName;
			this.reportTestId = reportTestId;
			this.authorId = authorId;
			this.isSaved = true;
			this.isUploaded = true;
			this.toEdit = false;
			this.language = language;
		}

		private String extractFileName(String reportCsvPath, String reportId) {
			if(StringUtil.isNil(reportCsvPath))
				return reportId;
			File f = new File(reportCsvPath);
			String ret = f.getName();
			return ret.substring(0, ret.lastIndexOf('.'));
		}

	}

	/*
	 * public static class TestObjectComplete {
	 * 
	 * public String TestId;
	 * 
	 * public String TestName;
	 * 
	 * public String authorId;
	 * 
	 * public Boolean deleted;
	 * 
	 * public String state;
	 * 
	 * public String revId;
	 * 
	 * public Boolean toEdit;
	 * 
	 * public List<QuestionObjectComplete> questions;
	 * 
	 * public Map<String, String> options;
	 * 
	 * }
	 */

	/*
	 * public static class CreateReportRequest { public TestObjectComplete test; }
	 */

	public static class CreateReportResponse extends OKResponseObject {
		public ReportObjectSmall response;
	}

	public String createReport() {
		// TODO
		Report report = new Report();
		report.setAuthor(userService.getCurrentUserLogged());
		report.setReportDate(new Date());
		report.save();
		String json = apiService.getCurrentJson();
		report.setJsonContent(json);
		TestResult req = null;
		Test t = null;
		try {
			req = (TestResult) apiService.buildObjectFromJson(json, TestResult.class);
			if (req.testEndTime != null) {
				report.setEndedAt(new Date(req.testEndTime));
			} else {
				report.setEndedAt(new Date());
			}
			if (!StringUtil.isNil(req.standaloneId)) {
				report.setStandaloneId(req.standaloneId);
				report.setReportDate(report.getEndedAt());
			}
			t = Test.findById(req.testId);
			if (t != null) {
				report.setReportTest(t);
				/*
				 * if(t!=null && t.getType().equals(TestType.ATLAS)) { List<Report> reportlist =
				 * }
				 */
				report.save();
			} else {
				System.err.println("Test non trovato: " + req.testId);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			report.setJsonFilePath(
					instance().saveJson(json, "newjson_" + StringUtil.date(new Date(), Test.REVISION_DATE_FORMAT)));
			report.save();
		}
		if (req == null) {
			report.setJsonFilePath(
					instance().saveJson(json, "newjson_" + StringUtil.date(new Date(), Test.REVISION_DATE_FORMAT)));
			report.save();
			return null;
		}
		report.setJsonFilePath(instance().saveJson(json, report.getUuid()));
		report.save();
		String csvPath = null;
		if (t != null && t.getType().equals(TestType.ATLAS)) {
			csvPath = instance().generateAtlasCsvFromJson(req, report);
		} else {
			csvPath = instance().generateCsvFromJson(req, report.getUuid());
		}
		if (csvPath != null) {
			report.setReportCsvPath(csvPath);
			report.save();
		}
		ReportObjectSmall ros = buildReportObjectSmallFromReport(report, t.getType().equals(TestType.ATLAS));
		CreateReportResponse res = new CreateReportResponse();
		res.response = ros;
		String ret = res.toJson();
		return ret;
	}

	private String generateCsvFromJson(TestResult req, String reportUuid) {

		Map<String, List<String>> uploaded = new HashMap<String, List<String>>();

		String csv = "";
		String sep = "|";

		csv += "Test Name"; // A
		csv += sep;
		csv += "Test ID"; // B
		csv += sep;
		csv += "Test Administrator"; // C
		csv += sep;
		csv += "Administrator ID"; // D
		csv += sep;
		/*
		 * csv += "Participant First Name"; //E csv += sep; csv +=
		 * "Participant Last Name"; //F csv += sep;
		 */
		csv += "ID Partecipant"; // E
		csv += sep;
		csv += "Participant Age"; // G
		csv += sep;
		csv += "Participant Gender"; // H
		csv += sep;
		/*
		 * csv += "Participant Custom1"; csv += sep; csv += "Participant Custom2"; csv
		 * += sep; csv += "Participant Custom3"; csv += sep;
		 */
		csv += "Age of acquisition"; // I
		csv += sep;
		/* csv += "Signing group";
		csv += sep; */
		csv += "Partcipant Custom1"; // J
		csv += sep;
		csv += "Participant Custom2"; // K
		csv += sep;
		csv += "Randomization Number"; // L
		csv += sep;
		csv += "Question presentation order"; // M
		csv += sep;
		csv += "Question Absolute Presentation Order"; // M1
		csv += sep;
		csv += "Question ID"; // N
		csv += sep;
		csv += "Question Name"; // O
		csv += sep;
		csv += "Slide Name"; // O1
		/*
		 * csv += sep; csv += "Item order in test";
		 */ // P
		/*
		 * csv += sep; csv += "Item order in block";
		 */ // Q
		csv += sep;
		csv += "Question Procedure Order"; // P1
		/*
		 * csv += sep; csv += "Answer";
		 */ // R
		/*csv += sep;
		csv += "Expected Answer"; // S */
		/*csv += sep;
		csv += "Match"; // T */
		csv += sep;
		csv += "Stimulus slide start"; // U
		csv += sep;
		csv += "Stimulus slide end"; // V
		csv += sep;
		csv += "Stimulus duration"; // W
		csv += sep;
		csv += "Answer slide start"; // X
		csv += sep;
		csv += "Answer slide end"; // Y
		csv += sep;
		csv += "Question duration"; // Z
		csv += sep;
		/*
		 * csv += "Y-W"; //AA csv += sep;
		 */
		csv += "Answer duration"; // AB
		csv += sep;
		csv += "Name";
		csv += sep;
		csv += "Type";
		csv += sep;
		csv += "Value";
		csv += "\n";
		Test t = null;
		Map<String, Map<Integer, Question>> groups = null;
		t = Test.findById(req.testId);
		if (t == null)
			return null;
		groups = t.extractGroups();
		
		String idPartecipant = generateRandomID(req.userInfo);
		
		//CICLO SU CIASCUNA QUESTION DEL TEST
		for (Question quest : t.getQuestions()) {
			String csvQuestion = "";
			String csvQuestionBaseInformations = "";
			String questionName = "NA";
			String slideName = "";
			// if(q.questionId!=null){
			questionName = quest.getName();
			
			//CERCO LA SLIDE ANSWER PER DETERMINARNE IL NOME
			Slide ansSlide = null;
			for (Slide ss : quest.getSlides()) {
				if (ss == null || ss.getDeleted() == null || ss.getDeleted())
					continue;
				if (ss != null && ss.getType() != null && ss.getType().equals(SlideType.ANSWER)) {
					Map<String, String> smo = ss.getMapOptions();
					slideName = smo.get("name");
					ansSlide = ss;
				}
			}
			if (StringUtil.isNil(slideName))
				slideName = "N/A";
			// }

			Integer questionProcedureOrder = getQuestionProcedureOrder(groups, quest);

			QuestionResult questionResult = findInList(quest, req.questions);
			if (questionResult == null)
				continue;
			
			//INSERISCO LE INFORMAZIONI DI BASE DEL TEST E DELL'UTENTE
			csvQuestionBaseInformations += req.testName + sep;
			csvQuestionBaseInformations += req.testId + sep;
			csvQuestionBaseInformations += userService.getCurrentUserLogged().buildFullName() + sep;
			csvQuestionBaseInformations += userService.getCurrentUserLogged().getUuid() + sep;
			
			//INSERISCO LE INFORMAZIONI RELATIVE ALL'UTENTE
			if (req.userInfo != null) {
				// csv1 += req.userInfo.name + sep;
				// csv1 += req.userInfo.surname + sep;
				csvQuestionBaseInformations += generateRandomID(req.userInfo) + sep;
				csvQuestionBaseInformations += req.userInfo.age + sep;
				csvQuestionBaseInformations += req.userInfo.gender + sep;
				csvQuestionBaseInformations += req.userInfo.age_acq + sep;
				String sgs = "null";
				if (req.userInfo.age_acq != null) {
					try {
						int sg = Integer.parseInt(req.userInfo.age_acq);
						if (sg >= 0 && sg <= 3)
							sgs = "Native";
						else if (sg <= 8)
							sgs = "Early";
						else if (sg <= 13)
							sgs = "Late";
						else
							sgs = "Adult";
					} catch (Exception e) {
						System.out.println("exception parsing " + req.userInfo.age_acq);
					}
				}
				csvQuestionBaseInformations += sgs + sep;
				csvQuestionBaseInformations += req.userInfo.custom1 + sep;
				csvQuestionBaseInformations += req.userInfo.custom2 + sep;

				/*
				 * csv1 += req.userInfo.custom2 + sep; csv1 += req.userInfo.custom3 + sep;
				 */
			} else {
				csvQuestionBaseInformations += "NA" + sep;
				csvQuestionBaseInformations += "NA" + sep;
				csvQuestionBaseInformations += "NA" + sep;
				csvQuestionBaseInformations += "NA" + sep;
				csvQuestionBaseInformations += "NA" + sep;
				csvQuestionBaseInformations += "NA" + sep;
				csvQuestionBaseInformations += "NA" + sep;
			}
			
			//INSERISCO LE INFORMAZIONI RELATIVE ALLA RANDOMIZZAZIONE E ALLA PRESENTAZIONE
			csvQuestionBaseInformations += questionResult.randomizationBlock + sep;
			csvQuestionBaseInformations += questionResult.randomizationPresentationBlock + sep;
			csvQuestionBaseInformations += questionResult.randomizationPresentationBlock + sep; // M1 ???

			if (questionResult.questionId != null)
				csvQuestionBaseInformations +=questionResult.questionId + sep;
			else
				csvQuestionBaseInformations += "NA" + sep;

			csvQuestionBaseInformations += questionName + sep;
			csvQuestionBaseInformations += slideName + sep; // O1
			// csv1 += q.stimulusOrderInTest + sep;
			// csv1 += q.stimulusOrderInBlock + sep;
			csvQuestionBaseInformations += questionProcedureOrder + sep; // P1
			String sep1 = "";
			/*
			 * String dr = ""; for(String dkk : q.directAnswer.keySet()){ Map<String,
			 * Object> dvv = q.directAnswer.get(dkk); for(String ddk : dvv.keySet()){ Object
			 * ddv = dvv.get(ddk); dr += sep1 + ddv ; sep1 = ","; } } csv1 += dr + sep;
			 */ // R
			
			//QUESTO E' IL BLOCCO NEL QUALE VENGONO INSERITE LE EXPECTED ANSWER
			/*String er = "";
			sep1 = "";
			for (String ek : questionResult.expectedAnswer.keySet()) {
				Object ev = questionResult.expectedAnswer.get(ek);
				er += sep1 + ev;
				sep1 = ",";
			}
			if (StringUtil.isNil(er))
				er = "N/A";
			csv1 += er + sep; // S */
			
			// csv1 += "false" + sep; // T
			if (questionResult.stimulusStartTime != null)
				csvQuestionBaseInformations += questionResult.stimulusStartTime + sep; // U
			else
				csvQuestionBaseInformations += "NA" + sep;

			if (questionResult.stimulusEndTime != null)
				csvQuestionBaseInformations += questionResult.stimulusEndTime + sep; // V
			else
				csvQuestionBaseInformations += "NA" + sep;

			if (questionResult.stimulusStartTime != null && questionResult.stimulusEndTime != null) {
				csvQuestionBaseInformations += questionResult.stimulusEndTime - questionResult.stimulusStartTime + sep; // q.delay + sep; //W
			} else {
				csvQuestionBaseInformations += "NA" + sep;
			}

			if (questionResult.answerStartTime != null)
				csvQuestionBaseInformations += questionResult.answerStartTime + sep; // X
			else
				csvQuestionBaseInformations += "NA" + sep;

			if (questionResult.answerEndTime != null)
				csvQuestionBaseInformations += questionResult.answerEndTime + sep; // Y
			else
				csvQuestionBaseInformations += "NA" + sep;
			/*
			 * Tempo che intercorre dal momento in cui parte la slide di stimolo a quando
			 * viene completata la slide di answer
			 */
			if (questionResult.answerEndTime != null && questionResult.stimulusStartTime != null) {
				csvQuestionBaseInformations += (questionResult.answerEndTime - questionResult.stimulusStartTime) + sep; // Z
			} else {
				csvQuestionBaseInformations += "NA" + sep;
			}

			/*
			 * if(q.answerEndTime != null && q.stimulusEndTime != null) { csv1 +=
			 * (q.answerEndTime - q.stimulusEndTime) + sep; //AA } else { csv1 +="NA"+sep; }
			 */
			if (questionResult.answerEndTime != null && questionResult.answerStartTime != null) {
				csvQuestionBaseInformations += (questionResult.answerEndTime - questionResult.answerStartTime) + sep; // AB
			} else {
				csvQuestionBaseInformations += "NA" + sep;
			}

			/*
			 * for(String ek : q.expectedAnswer.keySet()){ Object ev =
			 * q.expectedAnswer.get(ek); csv1 +=ev+sep; Map<String, Object> dv =
			 * q.directAnswer.get(ek); dr = ""; sep1 = ""; if(dv!=null &&
			 * dv.keySet()!=null){ for(String ddk : dv.keySet()){ Object ddv = dv.get(ddk);
			 * dr += ddv + sep1; sep1 = ","; } } csv1 +=dr+sep; }
			 */

			// csv1 += name + "="+val +sep;
			//SE NON CI SONO RISPOSTE TERMINO LA RIGA
			if (ansSlide == null) {
				csvQuestionBaseInformations += "\n";
				continue;
			}
			// CICLO SUI COMPONENTI DELLA SLIDE
			for (SlideContentComponent ascc : ansSlide.getSlideContent()) {
				String csv1 = "";
				String sccname = ascc.getName(null);
				String label = ascc.getLabel();
				// Map<String, Object> slideDirectAnswers =
				// q.directAnswer.get(ansSlide.getUuid());
				// Map<String, Object> del1 = (Map<String,
				// Object>)slideDirectAnswers.get(sccname);
				Map<String, Object> del1 = questionResult.directAnswer.get(sccname);
				ComponentType scct = ascc.getComponentType();
				Map<String, String> opts = ascc.getMapOptions();

				if (del1 != null) {
					for (String dell11 : del1.keySet()) {
						Map<String, Object> dell = (Map<String, Object>) del1.get(dell11);
						/*
						 * dell è un oggetto di questo tipo: { "isChecked": true, "id":
						 * "input_1781559141015873", "name":
						 * "checkbox675.2399748402447,100.05882352941177", "type": "CHECKBOX" }
						 */
						switch (scct) {
						case TEXT:
							csv1 += sccname + sep + scct.name() + sep + "\"" + dell.get("value") + "\"" + sep;
							break;
						case TEXTAREA:
							csv1 += sccname + sep + scct.name() + sep + "\"" + dell.get("value") + "\"" + sep;
							break;
						case BUTTON:
							// System.out.println("not to save??? " + opts.get("notToSave"));
							if (opts.containsKey("notToSave") && opts.get("notToSave").equals("true")) {
								continue;
							} else {
								csv1 += sccname + sep + scct.name() + sep + "" + dell.get("value") + "" + sep;
							}
							break;
						case VIDEO_RECORD:
						case UPLOADS: {
							String filename = (String) dell.get("filename");
							if (dell.containsKey("id") && dell.get("id") != null) {
								if (!uploaded.containsKey(questionResult.questionId))
									uploaded.put(questionResult.questionId, new LinkedList<String>());
								List<String> ll = uploaded.get(questionResult.questionId);
								ll.add((String) dell.get("id"));
								uploaded.put(questionResult.questionId, ll);
							}
							if (!dell.containsKey("id"))
								continue;
							String mediaId = (String) dell.get("id");
							if (mediaId == null || mediaId.trim().equals(""))
								continue;
							Media med = Media.findById(mediaId);
							if (med == null)
								continue;
							String subfolder = t.getUuid();
							System.out.println("subfolder: " + subfolder);

							String repo = CfgUtil.getString("media.repository.path", "/usr/cini/media_repository");
							File f = new File(repo + "/" + subfolder, med.getRepositoryId());
							// csv1 += name + "=\""+f.getAbsolutePath() +"\"" +sep;
							Map<String, Object> m = new HashMap<String, Object>();
							m.put("repositoryId", med.getRepositoryId());
							m.put("thumb", false);
							m.put("forceRender", true);
							String val = Play.configuration.getProperty("fronthost", "https://testing02.eclettica.net");
							val += "/#/home/testingtool/reportdownload/" + med.getRepositoryId();

							csv1 += sccname + sep + scct.name() + sep + val + sep;
						}
							break;
						case RADIO: {
							String ll = "";

							String numRadioString = opts.get("numRadio");

							if (StringUtil.isNil(numRadioString))
								continue;
							try {
								Integer numRadio = Integer.parseInt(numRadioString);

								Integer valNum = Math.round(Float.parseFloat("" + dell.get("value")));
								String radioVal = "" + valNum;
								for (int i = 0; i < numRadio; i++) {
									Integer parsed = Integer.parseInt(opts.get("radioComponentValue_" + i));
									if (parsed == valNum)
										radioVal = opts.get("radioComponentLabel_" + i);
								}
								csv1 += sccname + sep + scct.name() + sep + radioVal + sep;
							} catch (Exception e) {
								System.out.println(e.getLocalizedMessage());
								Logger.error(e, e.getLocalizedMessage());
								continue;
							}
						}
							break;
						case CHECKBOX:
							csv1 += sccname + sep + scct.name() + sep + opts.get("value") + sep;
							break;
						case CLICK_AREA:
							csv1 += sccname + sep + scct.name() + sep + dell.get("value") + sep;
							break;
						case CUSTOM_CLICK_AREA:
							csv1 += sccname + sep + scct.name() + sep + dell.get("value") + sep;
							break;
						case RANGE:
							csv1 += sccname + sep + scct.name() + sep + dell.get("value") + sep;
							break;
						case CHECKABLETABLE:
							break;
						case IMAGE:
							csv1 += sccname + sep + scct.name() + sep + dell.get("value") + sep;
						default:
							continue;
						}
					}
				} /*else {
					if (!((scct.equals(ComponentType.BUTTON) || (scct.equals(ComponentType.IMAGE)
							&& opts.containsKey("checkable") && opts.get("checkable").equals("true")))
							&& opts.containsKey("notToSave") && opts.get("notToSave").equals("true"))) {
						// if((!scct.equals(ComponentType.BUTTON) && !scct.equals(ComponentType.IMAGE))
						// ||
						// !opts.containsKey("notToSave") || !opts.get("notToSave").equals("true")) {
						System.out.println("into the else: " + scct.name() + " " + opts.get("notToSave"));
						csv1 += sccname + sep + scct.name() + sep + "N/A" + sep;
					}
				} */
				if(!StringUtil.isNil(csv1)) {
					csvQuestion += csvQuestionBaseInformations + csv1 + "\n";
				}
			}
			
			//TERMINE CICLO SUI COMPONENTI DELLA SLIDE

			/*
			 * Object ev = ""; if(q.expectedAnswer.containsKey(dk)) ev =
			 * q.expectedAnswer.get(dk); csv +=ev+sep;
			 */
			if(!StringUtil.isNil(csvQuestion)) {				
				csv += csvQuestion;
			}
			// }
			// }
		} // TERMINE CICLO DELLE QUESTIONS

		addUploadedToMedia(reportUuid, uploaded);
		Date endDate = new Date(req.testEndTime);
		String filename = t.getTestName() + "-" + idPartecipant + "-" + StringUtil.date(endDate, "yyyyMMdd_HHmmss");
		return instance().saveCsv(csv, filename);
	}

	private QuestionResult findInList(Question s, List<QuestionResult> questions) {
		for (QuestionResult q : questions) {
			if (q.questionId.equals(s.getUuid()))
				return q;
		}
		return null;
	}

	private Integer getQuestionProcedureOrder(Map<String, Map<Integer, Question>> groups, Question q) {
		for (String groupName : groups.keySet()) {
			for (Integer i : groups.get(groupName).keySet()) {
				if (groups.get(groupName).get(i).equals(q))
					return i + 1;
			}
		}
		return null;
	}

	private String generateRandomID(UserInfo userInfo) {
		if(userInfo == null)
			return UUID.randomUUID().toString();
		if (!StringUtil.isNil(userInfo.idPartecipant))
			return userInfo.idPartecipant;
		String ret = "ID-";
		if(!StringUtil.isNil(userInfo.name))
			ret += userInfo.name.hashCode() + "";
		if(!StringUtil.isNil(userInfo.surname))
			ret += userInfo.surname.hashCode() + "";
		if(!StringUtil.isNil(userInfo.gender))
			ret += userInfo.gender.hashCode() + "";
		if(!StringUtil.isNil(userInfo.age_acq))
			ret += userInfo.age_acq.hashCode();
		return ret;
	}

	// private String generateCsvFromJson(TestResult req, String reportUuid) {
	//
	// Map<String, List<String>> uploaded = new HashMap<String, List<String>>();
	//
	// String csv = "";
	// String sep = "|";
	//
	// csv += "Test Name";
	// csv += sep;
	// csv += "Test ID";
	// csv += sep;
	// csv += "Test Administrator";
	// csv += sep;
	// csv += "Administrator ID";
	// csv += sep;
	// /*csv += "Participant First Name";
	// csv += sep;
	// csv += "Participant Last Name";
	// csv += sep;*/
	// csv += "Participant Age";
	// csv += sep;
	// csv += "Participant Gender";
	// csv += sep;
	// /*csv += "Participant Custom1";
	// csv += sep;
	// csv += "Participant Custom2";
	// csv += sep;
	// csv += "Participant Custom3";
	// csv += sep;*/
	// csv += "Age of acquisition";
	// csv += sep;
	// csv += "Signing group";
	// csv += sep;
	// csv += "Participant Custom1";
	// csv += sep;
	// csv += "Participant Custom2";
	// csv += sep;
	// csv += "Block Number";
	// csv += sep;
	// csv += "Block presentation order";
	// csv += sep;
	// csv += "Question ID";
	// csv += sep;
	// csv += "Question Name";
	// csv += sep;
	// csv += "Item order in test";
	// csv += sep;
	// csv += "Item order in block";
	// csv += sep;
	// csv += "Answer";
	// csv += sep;
	// csv += "Expected Answer";
	// csv += sep;
	// csv += "Match";
	// csv += sep;
	// csv += "Stimulus slide start";
	// csv += sep;
	// csv += "Stimulus slide end";
	// csv += sep;
	// csv += "Delay";
	// csv += sep;
	// csv += "Answer slide start";
	// csv += sep;
	// csv += "Answer slide end";
	// csv += sep;
	// csv += "Question duration";
	// csv += sep;
	// csv += "Y-W";
	// csv += sep;
	// csv += "Answer duration";
	// csv += "\n";
	//
	// for(QuestionResult q : req.questions){
	//
	// for(String del : q.directAnswer.keySet()){
	// Map<String, Object> dell = q.directAnswer.get(del);
	// for(String dk: dell.keySet()) {
	// Map<String, Object> dv = null;
	// try {
	// dv = (Map<String, Object>)dell.get(dk);
	// }catch(Exception e) {
	// System.out.println("Key in error: " + dk);
	// throw e;
	// }
	// String name = null;
	//
	// if(dv.containsKey("name"))
	// name = ""+dv.get("name");
	// else if(dv.containsKey("groupName"))
	// name = ""+dv.get("groupName");
	// else
	// name = "undefined";
	//
	// Object val = dv.get("value");
	// if(val == null)
	// val = dv.get("filename");
	// System.out.println("get id " + dv.get("id") + " " + q.questionId);
	// if(dv.containsKey("id") && dv.get("id") != null) {
	// if(!uploaded.containsKey(q.questionId))
	// uploaded.put(q.questionId, new LinkedList<String>());
	// List<String> ll = uploaded.get(q.questionId);
	// ll.add((String) dv.get("id"));
	// uploaded.put(q.questionId, ll);
	// }
	//
	// csv += req.testName + sep;
	// csv += req.testId + sep;
	// csv += userService.getCurrentUserLogged().buildFullName() + sep;
	// csv += userService.getCurrentUserLogged().getUuid() + sep;
	// if(req.userInfo != null){
	// //csv += req.userInfo.name + sep;
	// //csv += req.userInfo.surname + sep;
	// csv += req.userInfo.age + sep;
	// csv += req.userInfo.gender + sep;
	// csv += req.userInfo.age_acq + sep;
	// String sgs = "null";
	// if(req.userInfo.age_acq != null) {
	// try {
	// int sg = Integer.parseInt(req.userInfo.age_acq);
	// if(sg >= 0 && sg <=3)
	// sgs = "Native";
	// else if(sg <=8)
	// sgs = "Early";
	// else if(sg <=13)
	// sgs = "Late";
	// else
	// sgs = "Adult";
	// } catch(Exception e) {
	// System.out.println("exception parsing " + req.userInfo.age_acq);
	// }
	// }
	// csv += req.userInfo.custom1 + sep;
	// csv += req.userInfo.custom2 + sep;
	// csv += sgs + sep;
	// /*csv += req.userInfo.custom2 + sep;
	// csv += req.userInfo.custom3 + sep;*/
	// } else {
	// csv += "NA"+sep;
	// csv += "NA"+sep;
	// csv += "NA"+sep;
	// csv += "NA"+sep;
	// csv += "NA"+sep;
	// csv += "NA"+sep;
	// csv += "NA"+sep;
	// }
	// csv += q.randomizationBlock + sep;
	// csv += q.randomizationPresentationBlock + sep;
	// if(q.questionId!=null)
	// csv += q.questionId + sep;
	// else
	// csv += "NA"+sep;
	// String questionName = "NA";
	// if(q.questionId!=null){
	// Question s = Question.findById(q.questionId);
	// questionName = s.getName();
	// }
	// csv += questionName+ sep;
	// csv += q.stimulusOrderInTest + sep;
	// csv += q.stimulusOrderInBlock + sep;
	// String dr = "";
	// String sep1 = "";
	// for(String dkk : q.directAnswer.keySet()){
	// Map<String, Object> dvv = q.directAnswer.get(dkk);
	// for(String ddk : dvv.keySet()){
	// Object ddv = dvv.get(ddk);
	// dr += sep1 + ddv ;
	// sep1 = ",";
	// }
	// }
	// csv += dr + sep;
	// String er = "";
	// sep1 = "";
	// for(String ek : q.expectedAnswer.keySet()){
	// Object ev = q.expectedAnswer.get(ek);
	// er += sep1 + ev;
	// sep1 = ",";
	// }
	// csv += er + sep;
	// csv += "false" + sep;
	// if(q.stimulusStartTime!=null)
	// csv += q.stimulusStartTime + sep;
	// else
	// csv += "NA"+sep;
	// if(q.stimulusEndTime!=null)
	// csv += q.stimulusEndTime + sep;
	// else
	// csv += "NA"+sep;
	// if(q.answerStartTime!=null && q.stimulusEndTime !=null){
	// csv += q.answerStartTime - q.stimulusEndTime + sep; //q.delay + sep;
	// } else {
	// csv +="NA"+sep;
	// }
	// if(q.answerStartTime!=null)
	// csv += q.answerStartTime + sep;
	// else
	// csv += "NA"+sep;
	// if(q.answerEndTime!=null)
	// csv += q.answerEndTime + sep;
	// else
	// csv += "NA"+sep;
	// if(q.answerEndTime != null && q.stimulusStartTime != null) {
	// csv += (q.answerEndTime - q.stimulusStartTime) + sep;
	// } else {
	// csv +="NA"+sep;
	// }
	// if(q.answerEndTime != null && q.stimulusEndTime != null) {
	// csv += (q.answerEndTime - q.stimulusEndTime) + sep;
	// } else {
	// csv +="NA"+sep;
	// }
	// if(q.answerEndTime != null && q.answerStartTime != null) {
	// csv += (q.answerEndTime - q.answerStartTime) + sep;
	// } else {
	// csv +="NA"+sep;
	// }
	//
	// /*for(String ek : q.expectedAnswer.keySet()){
	// Object ev = q.expectedAnswer.get(ek);
	// csv +=ev+sep;
	// Map<String, Object> dv = q.directAnswer.get(ek);
	// dr = "";
	// sep1 = "";
	// if(dv!=null && dv.keySet()!=null){
	// for(String ddk : dv.keySet()){
	// Object ddv = dv.get(ddk);
	// dr += ddv + sep1;
	// sep1 = ",";
	// }
	// }
	// csv +=dr+sep;
	// }*/
	//
	//
	// csv += name + "="+val +sep;
	//
	// /*Object ev = "";
	// if(q.expectedAnswer.containsKey(dk))
	// ev = q.expectedAnswer.get(dk);
	// csv +=ev+sep;*/
	//
	// csv += "\n";
	// }
	// }
	// }
	//
	// addUploadedToMedia(reportUuid, uploaded);
	//
	// return instance().saveCsv(csv, reportUuid);
	// }

	private void addUploadedToMedia(String reportUuid, Map<String, List<String>> uploaded) {
		System.out.println("addUploadedToMedia " + reportUuid);
		Report r = Report.findById(reportUuid);
		if (r == null) {
			System.out.println("Report is null???");
			return;
		}
		System.out.println("uploaded is empty?" + uploaded.size());
		for (String qid : uploaded.keySet()) {
			if (qid == null) {
				continue;
			}
			System.out.println("addUploadedToMedia qid" + qid);
			Question q = Question.findById(qid);
			if (q == null)
				continue;
			System.out.println("addUploadedToMedia q" + q.getUuid());
			List<String> medias = uploaded.get(qid);
			if (medias == null) {
				System.out.println("addUploadedToMedia medias is null");
				continue;
			}
			for (String mid : medias) {
				if (mid == null)
					continue;
				System.out.println("addUploadedToMedia mid" + mid);
				Media m = Media.findById(mid);
				if (m == null)
					continue;
				m.setReport(reportUuid);
				m.setQuestion(qid);
				m.save();
			}
		}
	}

	private String generateCsvFromJson_old(TestResult req, String reportUuid) {

		String csv = "";
		String sep = "|";

		csv += "Test Name";
		csv += sep;
		csv += "Test ID";
		csv += sep;
		csv += "Test Administrator";
		csv += sep;
		csv += "Administrator ID";
		csv += sep;
		csv += "Participant First Name";
		csv += sep;
		csv += "Participant Last Name";
		csv += sep;
		csv += "Participant Age";
		csv += sep;
		csv += "Participant Gender";
		csv += sep;
		csv += "Participant Custom1";
		csv += sep;
		csv += "Participant Custom2";
		csv += sep;
		csv += "Participant Custom3";
		csv += sep;
		csv += "Block Number";
		csv += sep;
		csv += "Block presentation order";
		csv += sep;
		csv += "Question ID";
		csv += sep;
		csv += "Question Name";
		csv += sep;
		csv += "Item order in test";
		csv += sep;
		csv += "Item order in block";
		csv += sep;
		csv += "Answer";
		csv += sep;
		csv += "Expected Answer";
		csv += sep;
		csv += "Match";
		csv += sep;
		csv += "Stimulus slide start";
		csv += sep;
		csv += "Stimulus slide end";
		csv += sep;
		csv += "Delay";
		csv += sep;
		csv += "Answer slide start";
		csv += sep;
		csv += "Answer slide end";
		csv += sep;
		csv += "Question duration";
		csv += sep;
		csv += "Y-W";
		csv += sep;
		csv += "Answer duration";
		csv += "\n";
		for (QuestionResult q : req.questions) {
			csv += req.testName + sep;
			csv += req.testId + sep;
			csv += userService.getCurrentUserLogged().buildFullName() + sep;
			csv += userService.getCurrentUserLogged().getUuid() + sep;
			if (req.userInfo != null) {
				csv += req.userInfo.name + sep;
				csv += req.userInfo.surname + sep;
				csv += req.userInfo.age + sep;
				csv += req.userInfo.gender + sep;
				csv += req.userInfo.custom1 + sep;
				csv += req.userInfo.custom2 + sep;
				csv += req.userInfo.custom3 + sep;
			} else {
				csv += "NA" + sep;
				csv += "NA" + sep;
				csv += "NA" + sep;
				csv += "NA" + sep;
				csv += "NA" + sep;
				csv += "NA" + sep;
				csv += "NA" + sep;
			}
			csv += q.randomizationBlock + sep;
			csv += q.randomizationPresentationBlock + sep;
			if (q.questionId != null)
				csv += q.questionId + sep;
			else
				csv += "NA" + sep;
			String questionName = "NA";
			if (q.questionId != null) {
				Question s = Question.findById(q.questionId);
				questionName = s.getName();
			}
			csv += questionName + sep;
			csv += q.stimulusOrderInTest + sep;
			csv += q.stimulusOrderInBlock + sep;
			String dr = "";
			String sep1 = "";
			for (String dk : q.directAnswer.keySet()) {
				Map<String, Object> dv = q.directAnswer.get(dk);
				for (String ddk : dv.keySet()) {
					Object ddv = dv.get(ddk);
					dr += sep1 + ddv;
					sep1 = ",";
				}
			}
			csv += dr + sep;
			String er = "";
			sep1 = "";
			for (String ek : q.expectedAnswer.keySet()) {
				Object ev = q.expectedAnswer.get(ek);
				er += sep1 + ev;
				sep1 = ",";
			}
			csv += er + sep;
			csv += "false" + sep;
			if (q.stimulusStartTime != null)
				csv += q.stimulusStartTime + sep;
			else
				csv += "NA" + sep;
			if (q.stimulusEndTime != null)
				csv += q.stimulusEndTime + sep;
			else
				csv += "NA" + sep;
			if (q.answerStartTime != null && q.stimulusEndTime != null) {
				csv += q.answerStartTime - q.stimulusEndTime + sep; // q.delay + sep;
			} else {
				csv += "NA" + sep;
			}
			if (q.answerStartTime != null)
				csv += q.answerStartTime + sep;
			else
				csv += "NA" + sep;
			if (q.answerEndTime != null)
				csv += q.answerEndTime + sep;
			else
				csv += "NA" + sep;
			if (q.answerEndTime != null && q.stimulusStartTime != null) {
				csv += (q.answerEndTime - q.stimulusStartTime) + sep;
			} else {
				csv += "NA" + sep;
			}
			if (q.answerEndTime != null && q.stimulusEndTime != null) {
				csv += (q.answerEndTime - q.stimulusEndTime) + sep;
			} else {
				csv += "NA" + sep;
			}
			if (q.answerEndTime != null && q.answerStartTime != null) {
				csv += (q.answerEndTime - q.answerStartTime) + sep;
			} else {
				csv += "NA" + sep;
			}

			/*
			 * for(String ek : q.expectedAnswer.keySet()){ Object ev =
			 * q.expectedAnswer.get(ek); csv +=ev+sep; Map<String, Object> dv =
			 * q.directAnswer.get(ek); dr = ""; sep1 = ""; if(dv!=null &&
			 * dv.keySet()!=null){ for(String ddk : dv.keySet()){ Object ddv = dv.get(ddk);
			 * dr += ddv + sep1; sep1 = ","; } } csv +=dr+sep; }
			 */

			for (String del : q.directAnswer.keySet()) {
				Map<String, Object> dell = q.directAnswer.get(del);
				for (String dk : dell.keySet()) {
					Map<String, Object> dv = (Map<String, Object>) dell.get(dk);
					String name = null;

					if (dv.containsKey("name"))
						name = "" + dv.get("name");
					else if (dv.containsKey("groupName"))
						name = "" + dv.get("groupName");
					else
						name = "undefined";

					Object val = dv.get("value");
					csv += name + "=" + val + sep;

					/*
					 * Object ev = ""; if(q.expectedAnswer.containsKey(dk)) ev =
					 * q.expectedAnswer.get(dk); csv +=ev+sep;
					 */
				}
			}

			csv += "\n";
		}
		return instance().saveCsv(csv, reportUuid);
	}

	private String saveJson(String json, String testId) {
		return saveFile(json, testId, "jsonDir", "json");
	}

	private String saveCsv(String stringa, String filename) {
		return saveFile(stringa, filename, "csvDir", "csv");
	}

	private String saveFile(String stringa, String fileName, String dirName, String extension) {
		File outDir = new File(Play.tmpDir, dirName);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		File outFile = new File(outDir, fileName + "." + extension);
		try {
			PrintWriter printer = new PrintWriter(outFile);
			printer.print(stringa);
			printer.flush();
			printer.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return outFile.getAbsolutePath();
	}

	public static class ReportListResponse extends OKResponseObject {
		public List<ReportObjectSmall> response;
	}

	public String reportList(String type) {
		List<Report> reportObjs = new LinkedList<Report>();
		boolean isAtlas = false;
		if (type.equals(TestType.ATLAS.name())) {
			isAtlas = true;
			if (userService.userCan(Auths.AT_MANAGE_TESTS) || /*userService.hasCurrentUserLoggedRole("ADMIN")*/ userService.checkAdminAndTool(ToolsTypes.ATLAS))
				reportObjs = Report.findAllByType(type);
			else if (userService.userCan(Auths.AT_PLAY_TESTS))
				reportObjs = Report.findAllByTypeAndContentProvide(type, userService.getCurrentUserLogged());
		} else {
			// reportObjs = Report.findAllByType(type);
			if (/*userService.hasCurrentUserLoggedRole("ADMIN")*/ userService.checkAdminAndTool(ToolsTypes.TESTING))
				reportObjs = Report.findAllByType(type);
			else {
//				reportObjs = new LinkedList<Report>();
				User u = userService.getCurrentUserLogged();
				reportObjs = Report.findAllByTypeAndContentProvide(type, u);
//				List<Report> reportObjsL = Report.findAllByTypeAndContentProvide(type, u);
//				for (Report r : reportObjsL) {
//					if(r.getAuthor().getUuid().equals(u.getUuid()))
//						reportObjs.add(r);
//					if(r.getReportTest().getAuthor().getUuid().equals(u.getUuid()))
//						reportObjs.add(r);
//					else if (r.getReportTest().getCanViewForReport(userService))
//						reportObjs.add(r);
//				}
				// reportObjs = Report.findAllByTypeAndContentProviderList(type,
				// userService.getCurrentUserLogged());
			}
		}
		List<ReportObjectSmall> reportObjsSmall = buildReportObjectSmallListFromReportList(reportObjs, isAtlas);
		ReportListResponse res = new ReportListResponse();
		res.response = reportObjsSmall;
		String ret = res.toJson();
		return ret;
	}

	public List<ReportObjectSmall> buildReportObjectSmallListFromReportList(List<Report> reports, boolean isAtlas) {
		List<ReportObjectSmall> reportObjsSmall = new LinkedList<ReportObjectSmall>();
		if (reports != null && !reports.isEmpty()) {
			for (Report report : reports) {
				ReportObjectSmall reportObjSmall = buildReportObjectSmallFromReport(report, isAtlas);
				if (reportObjSmall != null)
					reportObjsSmall.add(reportObjSmall);
			}
		}
		return reportObjsSmall;
	}

	/*
	 * public static class GetTestObjectSmallResponse extends OKResponseObject {
	 * public TestObjectSmall response; }
	 * 
	 * public static class GetTestObjectCompleteResponse extends OKResponseObject {
	 * public TestObjectComplete response; }
	 * 
	 * public String getTest(String testId, Boolean complete) { if
	 * (StringUtil.isNil(testId)) { return
	 * apiService.buildMandatoryParamsErrorResponse(); }
	 * 
	 * Test test = Test.findById(testId); if (test == null) { return
	 * apiService.buildEntityDoesNotExistsErrorResponse(); }
	 * 
	 * if (!userService.hasCurrentUserLoggedAdminRole()) { if (test.isDeleted()) {
	 * return apiService.buildEntityDoesNotExistsErrorResponse(); } }
	 * 
	 * if (complete == null) { return
	 * apiService.buildMandatoryParamsErrorResponse(); }
	 * 
	 * String ret; if (complete) { GetTestObjectCompleteResponse res = new
	 * GetTestObjectCompleteResponse(); res.response =
	 * buildTestObjectCompleteFromTest(test); ret = res.toJson(); } else {
	 * GetTestObjectSmallResponse res = new GetTestObjectSmallResponse();
	 * res.response = buildTestObjectSmallFromTest(test); ret = res.toJson(); }
	 * return ret; }
	 */

	public ReportObjectSmall buildReportObjectSmallFromReport(Report report, boolean isAtlas) {
		String id = report.getUuid();
		if (!StringUtil.isNil(report.getStandaloneId()))
			id = report.getStandaloneId();
		//
		
		String ddd = null;
		if(isAtlas) {
			ddd = StringUtil.date(report.getReportDate(), ATLAS_REPORT_LIST_DATE_FORMAT);
		} else {
			ddd = StringUtil.date(report.getReportDate(), Test.REVISION_DATE_FORMAT);
		}
		
		ReportObjectSmall ret = new ReportObjectSmall(id,
				ddd, report.getReportCsvPath(),
				report.getReportTest().getTestName(), report.getReportTest().getUuid(), report.getAuthor().getUuid(),
				report.getLanguageName());
		if (StringUtil.isNil(report.getReportCsvPath()))
			return null;
		File f = new File(report.getReportCsvPath());
		if (!f.exists())
			return null;

		return ret;
	}

	/*
	 * public TestObjectComplete buildTestObjectCompleteFromTest(Test test) {
	 * TestObjectComplete ret = new TestObjectComplete(); if (test != null) {
	 * ret.authorId = test.getAuthor().getUuid(); ret.deleted = test.isDeleted();
	 * ret.options =
	 * optionService.buildOptionsMapFromOptionsList(test.getOptions());
	 * ret.questions =
	 * questionService.buildQuestionObjectCompleteListFromQuestionList(test.
	 * getQuestions()); ret.revId = test.buildRevisionDateFormatted(); ret.state =
	 * test.getState().name(); ret.TestId = test.getUuid(); ret.TestName =
	 * test.getTestName(); if (userService.hasCurrentUserLoggedAdminRole()) {
	 * ret.toEdit = true; } else { User currentUserLogged =
	 * userService.getCurrentUserLogged(); if
	 * (currentUserLogged.equals(test.getAuthor())) { ret.toEdit = true; } else {
	 * ret.toEdit = false; } } } return ret; }
	 */
	public static class TestResult {
		String date;
		String testName;
		String testId;
		String piId; // identificativo del presentatore;
		String partecipantId; // identificativo del partecipante;
		String partecipantAge; // età del partecipante;
		List<QuestionResult> questions;
		UserInfo userInfo;
		Long testStartTime;
		Long testEndTime;
		String standaloneId;
		String oldReportId;
	}

	public static class QuestionResult {
		public String questionId;
		public String answerSlideId;

		public String stimulusOrderInBlock;
		public String stimulusOrderInTest;
		String randomizationBlock;
		String randomizationPresentationBlock;
		String stimulusSlideId;
		Integer stimulusSlideNumber;
		Map<String, Map<String, Object>> directAnswer;
		Map<String, Object> expectedAnswer;
		Long stimulusStartTime;
		Long stimulusEndTime;
		Long delay;
		Long answerStartTime;
		Long answerEndTime;
	}

	public static class UserInfo {
		public String age_acq;
		String name;
		String surname;
		String idPartecipant;
		Integer age;
		String gender;
		String custom1;
		String custom2;
		String custom3;
		UserInfoLanguage language;
	}

	public static class UserInfoLanguage {
		String code;
		String name;
	}

	public static File getCsvFile(String reportId) {
		if (StringUtil.isEmpty(reportId))
			return null;
		Report r = Report.findById(reportId);
		if (r == null)
			return null;
		if (StringUtil.isEmpty(r.getReportCsvPath()))
			return null;
		File f = new File(r.getReportCsvPath());
		return f;
	}

	public static String getCsvPublic(String reportId) {
		if (StringUtil.isEmpty(reportId))
			return null;
		Report r = Report.findById(reportId);
		if (r == null) {
			Logger.info("getCsvPublic findByStandalone" + reportId);
			r = Report.findByStandalone(reportId);
		}
		if (r == null)
			return null;
		if (StringUtil.isEmpty(r.getReportCsvPath()))
			return null;
		String code = UUID.randomUUID().toString();
		instance().publicLinks.put(code, r.getUuid());
		return code;
	}

	public static File getCsvFromCode(String code) {
		if (StringUtil.isEmpty(code))
			return null;
		if (!instance().publicLinks.containsKey(code))
			return null;
		String reportId = instance().publicLinks.get(code);
		instance().publicLinks.remove(code);
		Report r = Report.findById(reportId);
		if (r == null)
			return null;
		if (StringUtil.isEmpty(r.getReportCsvPath()))
			return null;
		File f = new File(r.getReportCsvPath());
		return f;
	}

	Map<String, String> publicLinks = new HashMap<String, String>();

	public static class UploadedListResponse extends OKResponseObject {
		public UploadedListObject response;
	}

	public static class UploadedListObject {
		Map<String, UploadedListQ> elements;
	}

	public static class UploadedListQ {
		String questionName;
		Integer questionOrder;
		List<MediaObject> medias = new LinkedList<MediaObject>();
	}

	public String getUploadedMediaList(String reportUuid) {
		Report r = Report.findById(reportUuid);
		if (r == null)
			return null;
		Test t = r.getReportTest();
		if (t == null)
			return null;
		List<Media> ml = Media.findByReport(reportUuid);
		if (ml == null)
			return null;
		UploadedListResponse ulr = new UploadedListResponse();
		ulr.response = new UploadedListObject();
		ulr.response.elements = new HashMap<String, UploadedListQ>();
		for (Media m : ml) {
			if (!ulr.response.elements.containsKey(m.getQuestion()))
				ulr.response.elements.put(m.getQuestion(), new UploadedListQ());
			Question q = Question.findById(m.getQuestion());
			if (q == null)
				continue;
			UploadedListQ ulrq = ulr.response.elements.get(m.getQuestion());
			ulrq.questionName = q.getName();
			ulrq.questionOrder = q.getOrder();
			ulrq.medias.add(MediaService.instance().buildMediaObjectFromMedia(m));
		}
		return ulr.toJson();
	}

	// INIZIO GENERAZIONE REPORT ATLAS
	private String generateAtlasCsvFromJson(TestResult req, Report report) {
		String reportUuid = report.getUuid();
		Map<String, List<String>> uploaded = new HashMap<String, List<String>>();

		if (req.userInfo == null) {
			req.userInfo = new UserInfo();
			req.userInfo.name = "TEST";
			req.userInfo.surname = "TEST";
			req.userInfo.language = new UserInfoLanguage();
			req.userInfo.language.code = "TEST";
			req.userInfo.language.name = "TEST";

		}

		String csv = "";
		String sep = "|";
		String lineSep = "\n";

		if (req.userInfo.language.name.equals("Other")) {
			String app = req.userInfo.language.name;
			req.userInfo.language.name = req.userInfo.language.code;
			req.userInfo.language.code = app;
		}

		csv += "Name" + sep + sep + sep + sep + req.userInfo.name + lineSep;
		csv += "Surname" + sep + sep + sep + sep + req.userInfo.surname + lineSep;
		csv += "Language" + sep + sep + sep + sep + req.userInfo.language.name + lineSep + lineSep;
		csv += "Section" + sep + "Slide" + sep + "Group Name" + sep + "Label" + sep + "Value" + lineSep;
		if (StringUtil.isNil(req.userInfo.language.name))
			req.userInfo.language.name = "TEST";

		report.setLanguageName(req.userInfo.language.name);
		report.save();

		Test test = Test.findById(req.testId);
		if (test == null) {
			return null;
		}

		if (!StringUtil.isNil(req.oldReportId)) {
			Report oldReport = Report.findById(req.oldReportId);
			if (oldReport != null) {
				report.setAuthor(oldReport.getAuthor());
				report.save();
			}
		}

		cleanReports(report, test, req.userInfo.language.name);
		Logger.info("generateAtlasCsvFromJson - report id: " + report.getUuid());
		for (Question qq : test.getQuestions()) {
			Logger.info("generateAtlasCsvFromJson - QUESTION: " + qq.getUuid());
			// for(QuestionResult q : req.questions){
			/*
			 * if(qq == null || q== null || !qq.getUuid().equals(q.questionId)) continue;
			 */
			boolean logElements = false;
			if(qq.getUuid().equals("UUID-QSTN-a83c30b2-0c24-4290-87b7-7262f80da1ad"))
				logElements = true;

			for (Slide slide : qq.getSlides()) {
				if (slide == null || slide.getDeleted())
					continue;
				Logger.info("generateAtlasCsvFromJson - SLIDE: " + slide.getUuid());
				String slideName = slide.getUuid();
				if (slide.getOptions() != null) {
					for (Option o : slide.getOptions()) {
						if (o.getKey() != null && o.getKey().equals("name"))
							slideName = o.getValue();
					}
				}
				// take all responses for each slide

				/*
				 * if(slideDirectAnswers == null) continue;
				 */
				// it's needed to cyclate over all slide content components
				List<SlideContentComponent> sccl = new LinkedList<SlideContentComponent>();
				sccl.addAll(slide.getSlideContent());
				Collections.sort(sccl, new Comparator<SlideContentComponent>() {

					@Override
					public int compare(SlideContentComponent o1, SlideContentComponent o2) {
						if(o1 == null)
							return 1;
						if(o2 == null)
							return -1;
						return o1.getName(o1.getComponentType().equals(ComponentType.UPLOADS)?"name":null).compareTo(o2.getName(o2.getComponentType().equals(ComponentType.UPLOADS)?"name":null));
					}
					
				});
				for (SlideContentComponent scc : sccl) {
					Logger.info("generateAtlasCsvFromJson - SLIDECONTENTCOMPONENT: " + scc.getComponentType() + " " + scc.getUuid());
					if(logElements) {
						Logger.warn("generateAtlasCsvFromJson - SLIDECONTENTCOMPONENT: " + scc.getComponentType() + " " + scc.getUuid());
					}
					ComponentType scct = scc.getComponentType();
					if (scct.equals(ComponentType.IMAGE) || scct.equals(ComponentType.CLICK_AREA)
							|| scct.equals(ComponentType.TEXTBLOCK) || scct.equals(ComponentType.VIDEO))
						continue;
					String name = scc.getName(null);
					if(scct.equals(ComponentType.UPLOADS) || scct.equals(ComponentType.BUTTON)) {
						name = scc.getName("name");
					}
					String label = scc.getLabel();
					Map<String, String> opts = scc.getMapOptions();

					Map<String, Object> slideDirectAnswers = null;
					Map<String, Object> del1 = null;
					QuestionResult q = null;
					for (QuestionResult q1 : req.questions) {
						if (q1 == null)
							continue;
						if (q1.questionId != null && qq.getUuid().equals(q1.questionId)) {
							q = q1;
							break;
						}
					}
					if(logElements) {
						Logger.warn("generateAtlasCsvFromJson - question null? " + (q == null));
						if(q!=null)
							Logger.warn("generateAtlasCsvFromJson - direct null? " + (q.directAnswer == null));

					}
					if (q != null && q.directAnswer != null) {
						slideDirectAnswers = q.directAnswer.get(slide.getUuid());
						/*if(scct.equals(ComponentType.UPLOADS)) {
							for(String sid : q.directAnswer.keySet()) {
								Logger.info("generateAtlasCsvFromJson - uptest11: " + sid + " " + slide.getUuid());
								Logger.info("generateAtlasCsvFromJson - uptest112: " + (sid.equals(slide.getUuid())));
								
							}
							Logger.info("generateAtlasCsvFromJson - uptest1: " + q.questionId + " " + slide.getUuid() + " " + scc.getUuid() + " " + name);
							Logger.info("generateAtlasCsvFromJson - uptest113: " + (slideDirectAnswers == null));
						}*/
						if(logElements) {
							Logger.warn("generateAtlasCsvFromJson - slide null? " + (slideDirectAnswers == null));
						}
						if (slideDirectAnswers != null)
							del1 = (Map<String, Object>) slideDirectAnswers.get(name);
					}
					boolean scctSet = false;
					if(logElements) {
						Logger.warn("generateAtlasCsvFromJson - del1 null? " + (del1 == null));
					}
					if (del1 != null) {
						boolean containsKey = false;
						if(del1.containsKey(scc.getUuid()))
							containsKey = true;
						for (String dell11 : del1.keySet()) {
							if(containsKey) {
								if (!dell11.equals(scc.getUuid()))
									continue;
							}
							
							String csv1 = qq.getName() + sep + slideName + sep;
							Map<String, Object> dell = null;
							ArrayList<Object> upList = null;
							ArrayList<Map<String, Object>> upList1 = null;
							if(scct.equals(ComponentType.UPLOADS)) {
								Gson gson = new Gson();
								Logger.info("generateAtlasCsvFromJson - UPLOADCOMPONENT: " + gson.toJson(del1.get(dell11)));
								upList = (ArrayList<Object>) del1.get(dell11);
								upList1 = new ArrayList<Map<String, Object>>();
								for(Object o : upList) {
									upList1.add((Map<String, Object>) o);
								}
							} else {
								dell = (Map<String, Object>) del1.get(dell11);
							}
							/*
							 * dell è un oggetto di questo tipo: { "isChecked": true, "id":
							 * "input_1781559141015873", "name":
							 * "checkbox675.2399748402447,100.05882352941177", "type": "CHECKBOX" }
							 */
							
							switch (scct) {
							case TEXT:{
								 String v;
								 v = (String) dell.get("value");
								 if(StringUtil.isNil(v))
									 v = "N/A";
								csv1 += name + sep + label + sep + "\"" + v + "\"";
								break;
							}
							case TEXTAREA:{
								 String v;
								 v = (String) dell.get("value");
								 if(StringUtil.isNil(v))
									 v = "N/A";
								csv1 += name + sep + label + sep + "\"" + v + "\"";
								break;
							}
							case BUTTON:
								{
									// System.out.println("not to save??? " + opts.get("notToSave"));
									String v;
										 v = (String) dell.get("value");
									if (opts.containsKey("notToSave") && opts.get("notToSave").equals("true")) {
										//v ="N/A";
										continue;
									} 									
										csv1 += name + sep + name + sep + v ;
										//csv1 += sccname + sep + scct.name() + sep + "" + dell.get("value") + "" + sep;
								}
								break;
							case UPLOADS: {
								String upsep = "";
								String uptype = "";
								String upvals = "";
								for(Map<String, Object> mm : upList1) {
									dell = mm;
									Logger.info("generateAtlasCsvFromJson - UPLOADCOMPONENT: " + dell);
									String filename = (String) dell.get("filename");
									if (dell.containsKey("id") && dell.get("id") != null) {
										if (!uploaded.containsKey(q.questionId))
											uploaded.put(q.questionId, new LinkedList<String>());
										List<String> ll = uploaded.get(q.questionId);
										ll.add((String) dell.get("id"));
										uploaded.put(q.questionId, ll);
									}
									if (!dell.containsKey("id"))
										continue;
									String mediaId = (String) dell.get("id");
									if (mediaId == null || mediaId.trim().equals(""))
										continue;
									Media med = Media.findById(mediaId);
									if (med == null)
										continue;
									String subfolder = qq.getTest().getUuid();
									System.out.println("subfolder: " + subfolder);

									String repo = CfgUtil.getString("media.repository.path", "/usr/cini/media_repository");
									File f = new File(repo + "/" + subfolder, med.getRepositoryId());
									// csv += name + "=\""+f.getAbsolutePath() +"\"" +sep;
									Map<String, Object> m = new HashMap<String, Object>();
									m.put("repositoryId", med.getRepositoryId());
									m.put("thumb", false);
									m.put("forceRender", true);
									String val = Router.getFullUrl("MediaController.retrieve", m);
									val = Play.configuration.getProperty("fronthost", "https://testing02.eclettica.net");
									val += "/#/home/atlas/reportdownload/" + med.getRepositoryId();
									uptype += upsep + med.getMediaType().name();
									upvals += upsep + val;
									upsep = ",";
								}
								csv1 += name + sep + uptype + sep + upvals;
								//csv1 += name + sep + med.getMediaType().name() + sep + val;
							}
								break;
							case RADIO: {
								String ll = "";

								String numRadioString = opts.get("numRadio");

								if (StringUtil.isNil(numRadioString))
									continue;
								try {
									Integer numRadio = Integer.parseInt(numRadioString);

									String s = "";
									Integer valNum = Math.round(Float.parseFloat("" + dell.get("value")));
									String val = "" + valNum;
									for (int i = 0; i < numRadio; i++) {
										ll += s + opts.get("radioComponentLabel_" + i);
										s = ",";
										Integer parsed = null;
										try {
										parsed = Integer.parseInt(opts.get("radioComponentValue_" + i));
										if ( parsed == valNum )
											val = opts.get("radioComponentLabel_" + i);
										} catch(Exception e) {
											if(opts.get("radioComponentValue_" + i).equals(""+valNum))
												val = opts.get("radioComponentLabel_" + i);
										}
									}
									csv1 += name + sep + ll + sep + val;
								} catch (Exception e) {
									System.out.println(e.getLocalizedMessage());
									Logger.error(e, e.getLocalizedMessage());
									continue;
								}
							}
								break;
							case CHECKBOX:
								{
									Object obj = dell.get("isChecked");
									Boolean val = null;
									if(obj instanceof String) {
										if(obj.equals("true"))
											val = true;
										else
											val = false;
									} else if(obj instanceof Boolean) {
										val = (Boolean) obj;
									}
									if(val)
										csv1 += name + sep + label + sep + opts.get("value");
									else
										csv1 += name + sep + label + sep + "N/A";
								}
								break;
							case RANGE:
								csv1 += name + sep + label + sep + dell.get("value");
								break;
							case CHECKABLETABLE: {
								String ll = "";

								String numTableColLabelString = opts.get("numTableColLabel");
								String numTableRowLabelString = opts.get("numTableRowLabel");

								if (StringUtil.isNil(numTableColLabelString)
										|| StringUtil.isNil(numTableRowLabelString))
									continue;
								try {
									Integer numTableColLabel = Integer.parseInt(numTableColLabelString);
									Integer numTableRowLabel = Integer.parseInt(numTableRowLabelString);

									String s = "";
									String val = (String) dell.get("value");
									String[] values = val.split("#");
									Map<Integer, Set<Integer>> resultMap = new HashMap<Integer, Set<Integer>>();
									for (String ss : values) {
										if (StringUtil.isNil(ss.trim()))
											continue;
										String[] rt = ss.split("-");
										if (rt.length != 2)
											continue;
										Integer x = Integer.parseInt(rt[0]);
										Integer y = Integer.parseInt(rt[1]);
										if (!resultMap.containsKey(x)) {
											resultMap.put(x, new HashSet<Integer>());
										}
										resultMap.get(x).add(y);
									}

									for (int i = 0; i < numTableRowLabel; i++) {
										for (int y = 0; y < numTableColLabel; y++) {
											ll = opts.get("tableComponentRowLabel_" + i);
											ll += "," + opts.get("tableComponentColLabel_" + y);
											Object v;
											if (resultMap.containsKey(i) && resultMap.get(i).contains(y))
												v = 1;
											else
												v = "N/A";
											// per la prima riga già ho i campi relativi all'intestazione
											if (i > 0 || y > 0)
												csv1 += qq.getName() + sep + slideName + sep;
											csv1 += name + sep + ll + sep + v;
											// per l'ultima riga il lineSep verrà inserito dopo
											if (i != (numTableRowLabel - 1) || y != (numTableColLabel - 1)) {
												csv1 += lineSep;
											}
										}
									}

								} catch (Exception e) {
									System.out.println(e.getLocalizedMessage());
									Logger.error(e, e.getLocalizedMessage());
									continue;
								}
							}
								break;

							default:
								break;
							}
							csv1 += lineSep;
							csv += csv1;
							scctSet = true;
							break;
						}
					} 
					if(!scctSet){
						switch (scct) {
						case TEXT:
							csv += qq.getName() + sep + slideName + sep;
							csv += name + sep + label + sep + "N/A" + lineSep;
							break;
						case TEXTAREA:
							csv += qq.getName() + sep + slideName + sep;
							csv += name + sep + label + sep + "N/A" + lineSep;
							break;
						case BUTTON:
							if (opts.containsKey("notToSave") && opts.get("notToSave").equals("true")) {
								//v ="N/A";
								continue;
							}
							csv += qq.getName() + sep + slideName + sep;
							csv += name + sep + name + sep + "N/A" + lineSep;
							break;
						case UPLOADS:
							csv += qq.getName() + sep + slideName + sep;
							csv += name + sep + opts.get("value") + sep + "N/A" + lineSep;
							break;
						case RADIO:
							String ll = "";
							String numRadioString = opts.get("numRadio");
							if (StringUtil.isNil(numRadioString))
								continue;
							try {
								Integer numRadio = Integer.parseInt(numRadioString);
								String s = "";
								for (int i = 0; i < numRadio; i++) {
									ll += s + opts.get("radioComponentLabel_" + i);
									s = ",";
								}
								csv += qq.getName() + sep + slideName + sep;
								csv += name + sep + ll + sep + "N/A" + lineSep;
							} catch (Exception e) {
								continue;
							}
							break;
						case CHECKBOX:
							csv += qq.getName() + sep + slideName + sep;
							csv += name + sep + label + sep + "N/A" + lineSep;
							break;
						case RANGE:
							csv += qq.getName() + sep + slideName + sep;
							csv += name + sep + label + sep + "N/A" + lineSep;
							break;
						case CHECKABLETABLE: {
							String numTableColLabelString = opts.get("numTableColLabel");
							String numTableRowLabelString = opts.get("numTableRowLabel");

							if (StringUtil.isNil(numTableColLabelString) || StringUtil.isNil(numTableRowLabelString))
								continue;
							try {
								Integer numTableColLabel = Integer.parseInt(numTableColLabelString);
								Integer numTableRowLabel = Integer.parseInt(numTableRowLabelString);

								String s = "";

								for (int i = 0; i < numTableRowLabel; i++) {
									for (int y = 0; y < numTableColLabel; y++) {
										ll = opts.get("tableComponentRowLabel_" + i);
										ll += "," + opts.get("tableComponentColLabel_" + y);
										// per la prima riga già ho i campi relativi all'intestazione
										csv += qq.getName() + sep + slideName + sep;
										csv += name + sep + ll + sep + "N/A" + lineSep;
										// per l'ultima riga il lineSep verrà inserito dopo
									}
								}

							} catch (Exception e) {
								System.out.println(e.getLocalizedMessage());
								Logger.error(e, e.getLocalizedMessage());
								continue;
							}
						}
							break;

						default:
							break;
						}
					}
				} // FINE SLIDE CONTENT COMPONENTE

			} // FINE SLIDE

			// break;
			// }

		} // FINE QUESTION

		addUploadedToMedia(reportUuid, uploaded);
		String filename = "" + req.userInfo.language.name + "-" + test.getTestName()+"-"+req.userInfo.name+"_"+req.userInfo.surname;
		String csvPath = instance().saveCsv(csv, filename);
		System.out.println("Now creating csv");
		try {
			File csvFile = new File(csvPath);
			File xlsFile = new File(csvFile.getParentFile(), filename + ".xlsx");
			return CsvToExcel.convertCsvToXls(xlsFile, csvPath, '|');
		} catch (Exception e) {
			e.printStackTrace();
			return csvPath;
		}
	} // FINE GENERAZIONE CSV ATLAS

	private void cleanReports(Report report, Test test, String name) {
		List<Report> replist = Report.findListByTestAndAuthor(report.getAuthor(), test);
		if (replist == null)
			return;
		replist.stream().filter(r -> r != null && !r.equals(report)).forEach(Report::delete);
	}

	private String generateAtlasCsvFromJson_old(TestResult req, String reportUuid) {

		Map<String, List<String>> uploaded = new HashMap<String, List<String>>();

		if (req.userInfo == null) {
			req.userInfo = new UserInfo();
			req.userInfo.name = "TEST";
			req.userInfo.surname = "TEST";
			req.userInfo.language = new UserInfoLanguage();
			req.userInfo.language.code = "TEST";
			req.userInfo.language.name = "TEST";

		}

		String csv = "";
		String sep = "|";
		String lineSep = "\n";

		if (req.userInfo.language.name.equals("Other")) {
			String app = req.userInfo.language.name;
			req.userInfo.language.name = req.userInfo.language.code;
			req.userInfo.language.code = app;
		}

		/*
		 * csv += "Sign Language"; csv += sep; csv += "Questions"; csv += sep; csv +=
		 * req.userInfo.language.name; csv += lineSep;
		 */

		csv += "Name" + sep + sep + sep + sep + req.userInfo.name + lineSep;
		csv += "Surname" + sep + sep + sep + sep + req.userInfo.surname + lineSep;
		csv += "Language" + sep + sep + sep + sep + req.userInfo.language.name + lineSep + lineSep;
		csv += "Section" + sep + "Slide" + sep + "Group Name" + sep + "Value" + lineSep;

		Test test = Test.findById(req.testId);
		if (test == null) {
			return null;
		}

		for (Question qq : test.getQuestions()) {
			for (QuestionResult q : req.questions) {
				// Question qq = Question.findById(q.questionId);
				if (!qq.getUuid().equals(q.questionId))
					continue;

				/*
				 * csv += qq.getName()+sep+sep; csv += "\n";
				 */

				for (Slide slide : qq.getSlides()) {
					if (slide.getDeleted())
						continue;
					for (String del : q.directAnswer.keySet()) {
						if (!del.equals(slide.getUuid()))
							continue;
						String slideName = slide.getUuid();
						if (slide.getOptions() != null) {
							for (Option o : slide.getOptions()) {
								if (o.getKey() != null && o.getKey().equals("name"))
									slideName = o.getValue();
							}
						}
						Map<String, Object> dell1 = q.directAnswer.get(del);
						System.out.println(dell1);

						for (String dell11 : dell1.keySet()) {
							Map<String, Object> dell = (Map<String, Object>) q.directAnswer.get(del).get(dell11);
							for (String dk : dell.keySet()) {
								Map<String, Object> dv = null;
								try {
									dv = (Map<String, Object>) dell.get(dk);
								} catch (Exception e) {
									System.out.println("Key in error: " + dk);
									// throw e;
									continue;
								}
								String name = null;

								if (dv.containsKey("name"))
									name = "" + dv.get("name");
								else if (dv.containsKey("groupName"))
									name = "" + dv.get("groupName");
								else if (dv.containsKey("type"))
									name = "" + dv.get("type");
								else
									name = "undefined";

								Object val = dv.get("value");
								if (val == null)
									val = dv.get("filename");
								System.out.println("get id " + dv.get("id") + " " + q.questionId);
								if (dv.containsKey("id") && dv.get("id") != null) {
									if (!uploaded.containsKey(q.questionId))
										uploaded.put(q.questionId, new LinkedList<String>());
									List<String> ll = uploaded.get(q.questionId);
									ll.add((String) dv.get("id"));
									uploaded.put(q.questionId, ll);
								}

								String type = null;

								if (dv.containsKey("type"))
									type = "" + dv.get("type");
								else
									type = "undefined";

								csv += qq.getName() + sep + slideName + sep;

								if (type.equals("RADIO")) {
									csv += "Value:" + val + sep;
									csv += "\n";
									csv += qq.getName() + sep + slideName + sep;
									csv += "Label:" + name;
								} else if (type.equals("CHECKABLETABLE")) {

									for (SlideContentComponent sc : slide.getSlideContent()) {
										if (!sc.getComponentType().equals(ComponentType.CHECKABLETABLE))
											continue;
										Option sco = Option.findBySlideContentComponentAndKey(sc, "groupName");
										if (sco == null || !sco.getValue().equals(name))
											continue;
										String res = "";
										String[] splitted = ((String) val).split("#");
										System.out.println(splitted.length);
										for (String s : splitted) {
											if (s.trim().equals(""))
												continue;
											System.out.println(s);
											String[] el = s.split("-");
											Option row = Option.findBySlideContentComponentAndKey(sc,
													"tableComponentRowLabel_" + el[0]);
											Option col = Option.findBySlideContentComponentAndKey(sc,
													"tableComponentColLabel_" + el[1]);
											if (row == null || col == null)
												continue;
											res += " " + row.getValue() + "-" + col.getValue();
										}
										csv += name + "=\"" + res + "\"" + sep;
										break;
									}

								} else if (type.equals("UPLOADS")) {

									if (!dv.containsKey("id"))
										continue;
									String mediaId = (String) dv.get("id");
									if (mediaId == null || mediaId.trim().equals(""))
										continue;
									Media med = Media.findById(mediaId);
									if (med == null)
										continue;
									String subfolder = qq.getTest().getUuid();
									System.out.println("subfolder: " + subfolder);

									String repo = CfgUtil.getString("media.repository.path",
											"/usr/cini/media_repository");
									File f = new File(repo + "/" + subfolder, med.getRepositoryId());

									csv += name + "=\"" + f.getAbsolutePath() + "\"" + sep;
								} else {
									if (val == null)
										val = "";
									val = ((String) val).replaceAll("(\r\n|\n)", "</ br>");
									csv += name + "=\"" + val + "\"" + sep;
								}

								csv += lineSep;
							}
						}
						break;
					}
				}

				break;
			}

		}

		addUploadedToMedia(reportUuid, uploaded);

		String csvPath = instance().saveCsv(csv, reportUuid);
		System.out.println("Now creating csv");
		try {
			File csvFile = new File(csvPath);
			File xlsFile = new File(csvFile.getParentFile(), reportUuid + ".xlsx");
			return CsvToExcel.convertCsvToXls(xlsFile, csvPath, '|');
		} catch (Exception e) {
			e.printStackTrace();
			return csvPath;
		}
	}

	public static String getJson(String reportId) {

		Report r = Report.findById(reportId);
		if (r != null && r.getJsonContent() != null)
			return r.getJsonContent();

		File outDir = new File(Play.tmpDir, "jsonDir");
		if (!outDir.exists()) {
			return null;
		}
		File outFile = new File(outDir, reportId + ".json");
		if (!outFile.exists()) {
			return null;
		}
		FileReader fr = null;
		BufferedReader br = null;
		try {
			String ret = "";
			fr = new FileReader(outFile);
			br = new BufferedReader(fr);
			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				ret += sCurrentLine;
			}
			br.close();
			fr.close();
			return ret;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
	}

	public static class ReportWorkerFacade extends OKResponseObject {
		WorkerFacade response;
	}

	public static class WorkerFacade {
		String workerId;
	}

	public static class WorkerStatusFacade extends OKResponseObject {
		WorkerStatusInfoFacade response;
	}

	public static class WorkerStatusInfoFacade {
		public String id;
		public String status;
	}

	public static String generateReport(String testId) {
		if (StringUtil.isNil(testId))
			return apiService.buildEntityDoesNotExistsErrorResponse();
		Test t = Test.findById(testId);
		ReportJob reportJob = WorkerService.getInstance().generateReportJob(t);
		if (reportJob.getIsWorking()) {
			NOKResponseObject res = new NOKResponseObject();
			res.addError("Already working", 701);
			String json = res.toJson();
			return json;
		}
		ReportWorkerFacade rwf = new ReportWorkerFacade();
		rwf.response = new WorkerFacade();
		rwf.response.workerId = reportJob.getId();
		reportJob.now();
		return rwf.toJson();
	}

	public static String checkReport(String workerId) {
		if (StringUtil.isNil(workerId))
			return apiService.buildEntityDoesNotExistsErrorResponse();
		WorkerJob wj = WorkerService.getInstance().getReportJob(workerId);
		if (wj == null)
			return apiService.buildEntityDoesNotExistsErrorResponse();
		ReportJob reportJob = (ReportJob) wj;
		WorkerStatusFacade wsf = new WorkerStatusFacade();
		wsf.response = new WorkerStatusInfoFacade();
		wsf.response.id = workerId;
		wsf.response.status = reportJob.getCurrentStatus();
		return wsf.toJson();
	}

	public static String generateFeatures(String testId) {
		if (StringUtil.isNil(testId))
			return apiService.buildEntityDoesNotExistsErrorResponse();
		Test t = Test.findById(testId);
		ReportJob reportJob = WorkerService.getInstance().generateReportJob(t);
		if (reportJob.getIsWorking()) {
			NOKResponseObject res = new NOKResponseObject();
			res.addError("Already working", 701);
			String json = res.toJson();
			return json;
		}
		ReportWorkerFacade rwf = new ReportWorkerFacade();
		rwf.response = new WorkerFacade();
		rwf.response.workerId = reportJob.getId();
		reportJob.now();
		return rwf.toJson();
	}

	public static String checkFeatures(String workerId) {
		if (StringUtil.isNil(workerId))
			return apiService.buildEntityDoesNotExistsErrorResponse();
		WorkerJob wj = WorkerService.getInstance().getReportJob(workerId);
		if (wj == null)
			return apiService.buildEntityDoesNotExistsErrorResponse();
		ReportJob reportJob = (ReportJob) wj;
		WorkerStatusFacade wsf = new WorkerStatusFacade();
		wsf.response = new WorkerStatusInfoFacade();
		wsf.response.id = workerId;
		wsf.response.status = reportJob.getCurrentStatus();
		return wsf.toJson();
	}

	public static String downloadPublicReportPublic(String reportId) {
		Logger.info("downloadPublicReportPublic " + reportId);
		if (StringUtil.isEmpty(reportId))
			return null;
		Report r = Report.findById(reportId);
		if (r == null) {
			Logger.info("downloadPublicReportPublic findByWorker" + reportId);
			r = Report.findByWorker(reportId);
		}
		Logger.info("downloadPublicReportPublic report" + r);

		if (r == null) {
			return null;
		}
		Logger.info("downloadPublicReportPublic getReportCsvPath" + r.getReportCsvPath());

		if (StringUtil.isEmpty(r.getReportCsvPath()))
			return null;
		String code = UUID.randomUUID().toString();
		instance().publicLinks.put(code, r.getUuid());
		Logger.info("downloadPublicReportPublic code" + code);

		return code;
	}

	public void generateCompleteReport(Test test, String workerId) {
		generateCompleteAtlasCsv(test, workerId);
	}

	private void cleanComplexiveReports(Report report, Test test) {
		List<Report> replist = Report.findGlobalListByTest(test);
		if (replist == null)
			return;
		replist.stream().filter(r -> r != null && !r.equals(report)).forEach(Report::delete);
	}

	private String generateCompleteAtlasCsv(Test test, String workerId) {

		Report report = new Report();
		report.setReportDate(new Date());
		report.setLanguageName("ALL");
		report.setWorkerId(workerId);
		report.setIsComplessive(true);
		report.save();
		report.setReportTest(test);
		report.setAuthor(null);
		report.save();

		Map<String, List<String>> uploaded = new HashMap<String, List<String>>();

		String csv = "";
		String sep = "|";
		String lineSep = "\n";

		csv += "Section" + sep + "Slide" + sep + "Group Name" + sep + "Value";

		String reportUuid = report.getUuid();

		cleanComplexiveReports(report, test);
		List<Report> reportList = Report.findAllByTest(test);
		Logger.info("generateCompleteAtlasCsv - reportList dimension " + reportList.size());
		// List<Language> langList = TestService.instance().getLanguages();
		Collections.sort(reportList, new Comparator<Report>() {
			@Override
			public int compare(Report o1, Report o2) {
				if (o1 == null || StringUtil.isNil(o1.getLanguageName()))
					return 1;
				if (o2 == null || StringUtil.isNil(o2.getLanguageName()))
					return -1;
				return o1.getLanguageName().compareTo(o2.getLanguageName());
			}

		});
		TestResult trr = null;
		LinkedList<TestResult> testResultList = new LinkedList<TestResult>();
		for (Report r : reportList) {
			if (r == null || StringUtil.isNil(r.getJsonContent()))
				continue;
			String json = r.getJsonContent();
			trr = (TestResult) apiService.buildObjectFromJson(json, TestResult.class);
			testResultList.add(trr);
			csv += sep + r.getLanguageName();
		}
		csv += lineSep;

		List<String> csv2 = null;

		for (Question qq : test.getQuestions()) {
			Logger.info("generateCompleteAtlasCsv - QUESTION " + qq.getUuid());
			// for(QuestionResult q : req.questions){
			/*
			 * if(qq == null || q== null || !qq.getUuid().equals(q.questionId)) continue;
			 */

			for (Slide slide : qq.getSlides()) {
				if (slide == null || slide.getDeleted())
					continue;
				Logger.info("generateCompleteAtlasCsv - SLIDE " + slide.getUuid());
				String slideName = slide.getUuid();
				if (slide.getOptions() != null) {
					for (Option o : slide.getOptions()) {
						if (o.getKey() != null && o.getKey().equals("name"))
							slideName = o.getValue();
					}
				}
				// take all responses for each slide

				/*
				 * if(slideDirectAnswers == null) continue;
				 */
				// it's needed to cyclate over all slide content components
				for (SlideContentComponent scc : slide.getSlideContent()) {
					Logger.info("generateCompleteAtlasCsv - SLIDECONTENTCOMPONENT " + scc.getUuid());
					ComponentType scct = scc.getComponentType();
					if (scct.equals(ComponentType.IMAGE) || scct.equals(ComponentType.CLICK_AREA)
							|| scct.equals(ComponentType.TEXTBLOCK) || scct.equals(ComponentType.VIDEO))
						continue;
					String name = scc.getName(null);
					if(scct.equals(ComponentType.UPLOADS))
						name = scc.getName("name");
					String label = scc.getLabel();
					Map<String, String> opts = scc.getMapOptions();

					// INIZIAMO A POPOLARE LA PARTE COMUNE
					switch (scct) {
					case TEXT:
						csv += qq.getName() + sep + slideName + sep;
						csv += name + sep + label + sep;
						break;
					case TEXTAREA:
						csv += qq.getName() + sep + slideName + sep;
						csv += name + sep + label + sep;
						break;
					case UPLOADS:
						csv += qq.getName() + sep + slideName + sep;
						csv += name + sep + opts.get("value") + sep;
						break;
					case RADIO:
						String ll = "";
						String numRadioString = opts.get("numRadio");
						if (StringUtil.isNil(numRadioString))
							continue;
						try {
							Integer numRadio = Integer.parseInt(numRadioString);
							String s = "";
							for (int i = 0; i < numRadio; i++) {
								ll += s + opts.get("radioComponentLabel_" + i);
								s = ",";
							}
							csv += qq.getName() + sep + slideName + sep;
							csv += name + sep + ll + sep;
						} catch (Exception e) {
							continue;
						}
						break;
					case CHECKBOX:
						csv += qq.getName() + sep + slideName + sep;
						csv += name + sep + label + sep;
						break;
					case RANGE:
						csv += qq.getName() + sep + slideName + sep;
						csv += name + sep + label + sep;
						break;
					case CHECKABLETABLE: {
						String numTableColLabelString = opts.get("numTableColLabel");
						String numTableRowLabelString = opts.get("numTableRowLabel");

						if (StringUtil.isNil(numTableColLabelString) || StringUtil.isNil(numTableRowLabelString))
							continue;
						try {
							Integer numTableColLabel = Integer.parseInt(numTableColLabelString);
							Integer numTableRowLabel = Integer.parseInt(numTableRowLabelString);

							String s = "";

							csv2 = new ArrayList<String>();
							for (int i = 0; i < numTableRowLabel; i++) {
								for (int y = 0; y < numTableColLabel; y++) {
									ll = opts.get("tableComponentRowLabel_" + i);
									ll += "," + opts.get("tableComponentColLabel_" + y);
									// per la prima riga già ho i campi relativi all'intestazione
									String part = "";
									part += qq.getName() + sep + slideName + sep;
									part += name + sep + ll;
									csv2.add(part);
									// per l'ultima riga il lineSep verrà inserito dopo
								}
							}

						} catch (Exception e) {
							System.out.println(e.getLocalizedMessage());
							Logger.error(e, e.getLocalizedMessage());
							continue;
						}
					}
						break;
					case BUTTON:
						continue;
					// System.out.println("not to save??? " + opts.get("notToSave"));
					/*
					 * if(opts.containsKey("notToSave") && opts.get("notToSave").equals("true")) {
					 * continue; } else { csv += sccname + sep + scct.name() + sep + "" +
					 * dell.get("value")+ "" + sep; }
					 */
					default:
						break;
					}

					for (TestResult req : testResultList) {
						Map<String, Object> slideDirectAnswers = null;
						Map<String, Object> del1 = null;
						QuestionResult q = null;
						for (QuestionResult q1 : req.questions) {
							if (q1 == null)
								continue;
							if (q1.questionId != null && qq.getUuid().equals(q1.questionId)) {
								q = q1;
								break;
							}
						}
						if (q != null && q.directAnswer != null) {
							slideDirectAnswers = q.directAnswer.get(slide.getUuid());
							if (slideDirectAnswers != null)
								del1 = (Map<String, Object>) slideDirectAnswers.get(name);
						}

						if (del1 != null && del1.get(scc.getUuid()) != null) {
							// for(String dell11 : del1.keySet()) {
							/*
							 * if(!dell11.equals(scc.getUuid())) continue;
							 */
							String csv1 = "";
							Map<String, Object> dell = (Map<String, Object>) del1.get(scc.getUuid());
							/*
							 * dell è un oggetto di questo tipo: { "isChecked": true, "id":
							 * "input_1781559141015873", "name":
							 * "checkbox675.2399748402447,100.05882352941177", "type": "CHECKBOX" }
							 */
							switch (scct) {
							case TEXT:
								csv1 += "\"" + dell.get("value") + "\"";
								break;
							case TEXTAREA:
								csv1 += "\"" + dell.get("value") + "\"";
								break;
							case UPLOADS: {
								String filename = (String) dell.get("filename");
								if (dell.containsKey("id") && dell.get("id") != null) {
									if (!uploaded.containsKey(q.questionId))
										uploaded.put(q.questionId, new LinkedList<String>());
									List<String> ll = uploaded.get(q.questionId);
									ll.add((String) dell.get("id"));
									uploaded.put(q.questionId, ll);
								}
								if (!dell.containsKey("id"))
									continue;
								String mediaId = (String) dell.get("id");
								if (mediaId == null || mediaId.trim().equals(""))
									continue;
								Media med = Media.findById(mediaId);
								if (med == null)
									continue;
								String subfolder = qq.getTest().getUuid();
								System.out.println("subfolder: " + subfolder);

								String repo = CfgUtil.getString("media.repository.path", "/usr/cini/media_repository");
								File f = new File(repo + "/" + subfolder, med.getRepositoryId());
								// csv += name + "=\""+f.getAbsolutePath() +"\"" +sep;
								Map<String, Object> m = new HashMap<String, Object>();
								m.put("repositoryId", med.getRepositoryId());
								m.put("thumb", false);
								m.put("forceRender", true);
								String val = Router.getFullUrl("MediaController.retrieve", m);
								val = Play.configuration.getProperty("fronthost", "https://testing02.eclettica.net");
								val += "/#/home/atlas/reportdownload/" + med.getRepositoryId();
								csv1 += val;
							}
								break;
							case RADIO: {
								String ll = "";

								String numRadioString = opts.get("numRadio");

								if (StringUtil.isNil(numRadioString))
									continue;
								try {
									Integer numRadio = Integer.parseInt(numRadioString);

									String s = "";
									Integer valNum = Math.round(Float.parseFloat("" + dell.get("value")));
									String val = "" + valNum;
									for (int i = 0; i < numRadio; i++) {
										ll += s + opts.get("radioComponentLabel_" + i);
										s = ",";
										Integer parsed = Integer.parseInt(opts.get("radioComponentValue_" + i));
										if (parsed == valNum)
											val = opts.get("radioComponentLabel_" + i);
									}
									csv1 += val;
								} catch (Exception e) {
									System.out.println(e.getLocalizedMessage());
									Logger.error(e, e.getLocalizedMessage());
									continue;
								}
							}
								break;
							case CHECKBOX:
								csv1 += opts.get("value");
								break;
							case RANGE:
								csv1 += dell.get("value");
								break;
							case CHECKABLETABLE: {
								String ll = "";

								String numTableColLabelString = opts.get("numTableColLabel");
								String numTableRowLabelString = opts.get("numTableRowLabel");

								if (StringUtil.isNil(numTableColLabelString)
										|| StringUtil.isNil(numTableRowLabelString))
									continue;
								try {
									Integer numTableColLabel = Integer.parseInt(numTableColLabelString);
									Integer numTableRowLabel = Integer.parseInt(numTableRowLabelString);

									String s = "";
									String val = (String) dell.get("value");
									String[] values = val.split("#");
									Map<Integer, Set<Integer>> resultMap = new HashMap<Integer, Set<Integer>>();
									for (String ss : values) {
										if (StringUtil.isNil(ss.trim()))
											continue;
										String[] rt = ss.split("-");
										if (rt.length != 2)
											continue;
										Integer x = Integer.parseInt(rt[0]);
										Integer y = Integer.parseInt(rt[1]);
										if (!resultMap.containsKey(x)) {
											resultMap.put(x, new HashSet<Integer>());
										}
										resultMap.get(x).add(y);
									}
									for (int i = 0; i < numTableRowLabel; i++) {
										for (int y = 0; y < numTableColLabel; y++) {
											ll = opts.get("tableComponentRowLabel_" + i);
											ll += "," + opts.get("tableComponentColLabel_" + y);
											Object v;
											if (resultMap.containsKey(i) && resultMap.get(i).contains(y))
												v = 1;
											else
												v = "N/A";
											String part = csv2.get(i + y);
											part += sep + v;
											csv2.set(i + y, part);
										}
									}
									for (int i = 0; i < csv2.size(); i++) {
										String part = csv2.get(i);
										csv1 += part;
										if (i < (csv2.size() - 1))
											csv1 += lineSep;
									}

								} catch (Exception e) {
									System.out.println(e.getLocalizedMessage());
									Logger.error(e, e.getLocalizedMessage());
									continue;
								}
							}
								break;

							default:
								break;
							}
							csv1 += sep;
							csv += csv1;
							// }
						} else {
							if (csv2 != null && !csv2.isEmpty()) {
								for (int i = 0; i < csv2.size(); i++) {
									String part = csv2.get(i);
									part += sep + "N/A";
									csv2.set(i, part);
								}
							} else {
								csv += "N/A" + sep;
							}
						}
					} // FINE CICLO SUI JSON
					csv += lineSep;
				} // FINE SLIDE
			} // FINE QUESTION
		}
		// }

		String csvPath = instance().saveCsv(csv, reportUuid);
		System.out.println("Now creating csv");
		try {
			File csvFile = new File(csvPath);
			File xlsFile = new File(csvFile.getParentFile(), reportUuid + ".xlsx");
			csvPath = CsvToExcel.convertCsvToXls(xlsFile, csvPath, '|');
			if (csvPath != null) {
				report.setReportCsvPath(csvPath);
				report.save();
			}
			return csvPath;
		} catch (Exception e) {
			e.printStackTrace();
			return csvPath;
		}

	}

}
