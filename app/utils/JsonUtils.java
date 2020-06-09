/**
 * 
 * Original file name: JsonUtils.java
 * Created by gannunziata 
 */
package utils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * 
 * @author gannunziata
 */
public class JsonUtils {
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

	private JsonUtils() {

	}

	public static Gson gson() {
		return gson(false, false);
	}

	public static Gson gson(boolean pretty, boolean serializeNulls) {
		return gson(pretty, serializeNulls, DEFAULT_DATE_FORMAT);
	}

	public static Gson gson(String dateFormat) {
		return gson(false, false, dateFormat);
	}

	public static Gson gson(boolean pretty, boolean serializeNulls, String dateFormat) {
		GsonBuilder gb = new GsonBuilder().registerTypeAdapter(Date.class, new DateDeserializer())
				.setDateFormat(dateFormat);
		if (pretty)
			gb.setPrettyPrinting();
		if (serializeNulls)
			gb.serializeNulls();
		return gb.create();
	}

	private static final String[] DATE_FORMATS = new String[] { DEFAULT_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd", "yyyy-MM-dd", "dd-MM-yyyy HH:mm:ss", "d MMM yyyy HH:mm:ss", "dd MM yyyy HH:mm:ss",
			"MMM dd, yyyy HH:mm:ss", "MMM dd, yyyy" };

	private static class DateDeserializer implements JsonDeserializer<Date> {

		@Override
		public Date deserialize(JsonElement jsonElement, Type typeOF, JsonDeserializationContext context)
				throws JsonParseException {
			String dt = jsonElement.getAsString();
			if (StringUtil.isNil(dt))
				return null;

			dt = dt.replace('/', '-');
			for (String format : DATE_FORMATS) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
					sdf.setLenient(false);

					return sdf.parse(dt);
				} catch (ParseException e) {
					// ignore
				}
			}

			throw new JsonParseException("Unparseable date: \"" + jsonElement.getAsString() + "\". Supported formats: "
					+ Arrays.toString(DATE_FORMATS));
		}

	}

	/**
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String json) {
		return gson().fromJson(json, LinkedHashMap.class);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> get(Map<String, Object> obj, String path) {
		if (obj == null || path == null || path.isEmpty())
			return null;
		if (!path.contains("."))
			return (Map<String, Object>) obj.get(path);

		String[] pp = path.split("\\.");
		Map<String, Object> o = obj;
		for (String p : pp) {
			o = (Map<String, Object>) o.get(p);
			if (o == null)
				return null;
		}

		return o;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getList(Map<String, Object> obj, String path) {
		if (obj == null || path == null || path.isEmpty())
			return null;
		if (!path.contains("."))
			return (List<Map<String, Object>>) obj.get(path);

		String[] pp = path.split("\\.");
		Map<String, Object> o = obj;
		for (int i = 0; i < pp.length - 1; i++) {
			String p = pp[i];
			o = (Map<String, Object>) o.get(p);
			if (o == null)
				return null;
		}

		List<Map<String, Object>> res = (List<Map<String, Object>>) o.get(pp[pp.length - 1]);
		return res;
	}

	@SuppressWarnings("unchecked")
	public static Object getValue(Map<String, Object> obj, String path) {
		if (obj == null || path == null || path.isEmpty())
			return null;
		if (!path.contains("."))
			return obj.get(path);

		String[] pp = path.split("\\.");
		Map<String, Object> o = obj;
		for (int i = 0; i < pp.length - 1; i++) {
			String p = pp[i];
			o = (Map<String, Object>) o.get(p);
			if (o == null)
				return null;
		}

		Object res = o.get(pp[pp.length - 1]);
		return res;
	}

	public static String toJson(Object elem) {
		if (elem == null)
			return "null";

		return gson().toJson(elem);
	}

	/**
	 * @param object
	 * @param class1
	 * @return
	 */
	public static <T> T reconvert(Object map, Class<T> classElem) {
		return reconvert(map, null, classElem);
	}

	/**
	 * @param object
	 * @param class1
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T reconvert(Object map, String dateFormat, Class<T> classElem) {
		if (classElem.isInstance(map))
			return (T) map;

		String json = (dateFormat != null) ? gson(dateFormat).toJson(map) : gson().toJson(map);
		return gson().fromJson(json, classElem);
	}

	/**
	 * @param targetList
	 * @param object
	 * @param class1
	 */
	public static <T> void reconvertList(List<T> targetList, List<?> list, Class<T> classElem) {
		for (Object el : list) {
			String json = gson().toJson(el);
			T convEl = gson().fromJson(json, classElem);
			targetList.add(convEl);
		}
	}

	/**
	 * @param json
	 * @param class1
	 * @return
	 */
	public static <T> T fromJson(String json, Class<T> class1) {
		return gson().fromJson(json, class1);
	}

	public static <T> T fromJson(String json, Class<T> class1, boolean serializeNulls) {
		if (serializeNulls) {
			return gson(false, true).fromJson(json, class1);
		} else
			return gson().fromJson(json, class1);
	}

}
