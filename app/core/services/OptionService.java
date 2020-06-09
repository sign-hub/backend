/**
 *  
 */
package core.services;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Option;
import models.Question;
import models.Slide;
import models.SlideContentComponent;
import models.Test;

/**
 *
 * @author luca
 */
public class OptionService {

	private static enum OptionServiceSingleton {
		INSTANCE;

		OptionService singleton = new OptionService();

		public OptionService getSingleton() {
			return singleton;
		}
	}

	public static OptionService instance() {
		return OptionService.OptionServiceSingleton.INSTANCE.getSingleton();
	}
	
	public Map<String, String> buildOptionsMapFromOptionsList(Set<Option> options) {
		Map<String, String> ret = new LinkedHashMap<String, String>();
		if (options != null && !options.isEmpty()) {
			for (Option option : options) {
				ret.put(option.getKey(), option.getValue());
			}
		}
		return ret;
	}

	public Map<String, String> buildOptionsMapFromOptionsList(List<Option> options) {
		Map<String, String> ret = new LinkedHashMap<String, String>();
		if (options != null && !options.isEmpty()) {
			for (Option option : options) {
				ret.put(option.getKey(), option.getValue());
			}
		}
		return ret;
	}

	// public List<Option> buildOptionsListFromOptionsMap(Question question,
	// Map<String, Object> options) {
	// List<Option> ret = new LinkedList<Option>();
	// if (options != null && !options.isEmpty()) {
	// for (String key : options.keySet()) {
	// Option opt = new Option();
	// opt.setKey(key);
	// opt.setValue(options.get(key));
	// opt.setQuestion(question);
	// opt.save();
	// question.getOptions().add(opt);
	// question.save();
	// }
	// }
	// return ret;
	// }

	public void updateOptions(Test test, Map<String, String> options) {
		if (options != null && !options.isEmpty()) {
			for (String key : options.keySet()) {
				Option currOpt = Option.findByTestAndKey(test, key);
				if (currOpt == null) {
					currOpt = new Option();
				}
				String value = options.get(key);
				if (value == null) {
					currOpt.delete();
					test.getOptions().remove(currOpt);
				} else {
					currOpt.setKey(key);
					currOpt.setValue(value);
					currOpt.setTest(test);
					currOpt.save();
					test.addOption(currOpt);
				}
				test.save();
			}
		}
	}

	public void createOptions(Test test, Map<String, String> options) {
		if (options != null && !options.isEmpty()) {
			for (String key : options.keySet()) {
				Option currOpt = new Option();
				String value = options.get(key);
				if (value == null) {
					continue;
				}
				currOpt.setKey(key);
				currOpt.setValue(value);
				currOpt.setTest(test);
				currOpt.save();
				test.addOption(currOpt);
				test.save();
			}
		}
	}

	public void updateOptions(Question question, Map<String, String> options) {
		if (options != null && !options.isEmpty()) {
			for (String key : options.keySet()) {
				Option currOpt = Option.findByQuestionAndKey(question, key);
				if (currOpt == null) {
					currOpt = new Option();
				}
				String value = options.get(key);
				if (value == null) {
					currOpt.delete();
					question.getOptions().remove(currOpt);
				} else {
					currOpt.setKey(key);
					currOpt.setValue(value);
					currOpt.setQuestion(question);
					currOpt.save();
					question.addOption(currOpt);
				}
				question.save();
			}
		}
	}

	public void createOptions(Question question, Map<String, String> options) {
		if (options != null && !options.isEmpty()) {
			for (String key : options.keySet()) {
				Option currOpt = new Option();
				String value = options.get(key);
				if (value == null) {
					continue;
				}
				currOpt.setKey(key);
				currOpt.setValue(value);
				currOpt.setQuestion(question);
				currOpt.save();
				question.addOption(currOpt);
				question.save();
			}
		}
	}

	public void updateOptions(Slide slide, Map<String, String> options) {
		if (options != null && !options.isEmpty()) {
			for (String key : options.keySet()) {
				Option currOpt = Option.findBySlideAndKey(slide, key);
				if (currOpt == null) {
					currOpt = new Option();
				}
				String value = options.get(key);
				if (value == null) {
					currOpt.delete();
					slide.getOptions().remove(currOpt);
				} else {
					currOpt.setKey(key);
					currOpt.setValue(value);
					currOpt.setSlide(slide);
					currOpt.save();
					slide.addOption(currOpt);
				}
				slide.save();
			}
		}
	}

	public void createOptions(Slide slide, Map<String, String> options) {
		if (options != null && !options.isEmpty()) {
			for (String key : options.keySet()) {
				Option currOpt = new Option();
				String value = options.get(key);
				if (value == null) {
					continue;
				}
				currOpt.setKey(key);
				currOpt.setValue(value);
				currOpt.setSlide(slide);
				currOpt.save();
				slide.addOption(currOpt);
				slide.save();
			}
		}
	}

	public void updateOptions(SlideContentComponent slideContentComponent, Map<String, String> options) {
		if (options != null && !options.isEmpty()) {
			for (String key : options.keySet()) {
				Option currOpt = Option.findBySlideContentComponentAndKey(slideContentComponent, key);
				if (currOpt == null) {
					currOpt = new Option();
				}
				String value = options.get(key);
				if (value == null) {
					currOpt.delete();
					slideContentComponent.getOptions().remove(currOpt);
				} else {
					currOpt.setKey(key);
					currOpt.setValue(value);
					currOpt.setSlideContentComponent(slideContentComponent);
					currOpt.save();
					slideContentComponent.addOption(currOpt);
				}
				slideContentComponent.save();
			}
		}
	}

	public void createOptions(SlideContentComponent slideContentComponent, Map<String, String> options) {
		if (options != null && !options.isEmpty()) {
			for (String key : options.keySet()) {
				Option currOpt = new Option();
				String value = options.get(key);
				if (value == null) {
					continue;
				}
				currOpt.setKey(key);
				currOpt.setValue(value);
				currOpt.setSlideContentComponent(slideContentComponent);
				currOpt.save();
				slideContentComponent.addOption(currOpt);
				slideContentComponent.save();
			}
		}
	}
	
	public Set<Option> cloneOptionsList(Test clonedTest, Question clonedQuestion, Slide clonedSlide, SlideContentComponent clonedSlideContentComponent, Collection<Option> options){
		Set<Option> ret = new LinkedHashSet<Option>();
		if(options != null){
			for (Option originalOption : options) {
				Option clonedOption = originalOption.clone(clonedTest, clonedQuestion, clonedSlide, clonedSlideContentComponent);
				ret.add(clonedOption);
			}
		}
		return ret;
	}
}