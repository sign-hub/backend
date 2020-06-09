/**
 * 
 * Original file name: CacheService.java
 * Created by gannunziata
 */
package core.services;

import java.util.HashSet;
import java.util.Set;

import com.mongodb.util.JSON;

import play.cache.Cache;
import play.mvc.Scope.Session;
import utils.ClassUtil;
import utils.ToJson;

/**
 * 
 * @author gannunziata
 */
public class CacheService {

	public static String getSessionId() {
		// return Session.current().getId();
		String id = Session.current().getId();
		return id;
	}

	public static Object get(String name) {
		return cacheGet(name);
	}

	public synchronized static void save(String name, Object obj) {
		if (cacheContains(name))
			cacheReplace(obj, name);
		else
			cacheAdd(obj, name);
	}

	public static void remove(String name) {
		cacheDelete(name);
	}

	/**
	 * @param name
	 * @return
	 */
	public static Object getAsSessionCache(String name) {
		if (name == null)
			return null;

		Object res = null;

		String id = name + "___" + getSessionId();

		res = cacheGet(id);

		return res;
	}

	private static Set<String> nameUsed = new HashSet<String>();

	public static void removeAllSessionCache() {
		String sid = getSessionId();
		for (String name : nameUsed) {
			String id = name + "___" + sid;
			cacheDelete(id);
		}
	}

	/**
	 * @param id
	 * @return
	 */
	protected static Object cacheGet(String id) {
		Object res;
		res = Cache.get(id);
		res = deserialize(res);
		return res;
	}

	/**
	 * @param name
	 * @param obj
	 */
	public synchronized static void saveAsSessionCache(String name, Object obj) {
		String id = name + "___" + getSessionId();
		nameUsed.add(name);
		if (cacheContains(id))
			cacheReplace(obj, id);
		else
			cacheAdd(obj, id);
	}

	public static void removeAsSessionCache(String name) {
		String id = name + "___" + getSessionId();
		cacheDelete(id);
	}

	/**
	 * @param id
	 * @return
	 */
	protected static boolean cacheContains(String id) {
		return Cache.get(id) != null;
	}

	/**
	 * @param obj
	 * @param id
	 */
	protected static void cacheAdd(Object obj, String id) {
		obj = serialize(obj);
		Cache.safeAdd(id, obj, "20mn");
	}

	/**
	 * @param obj
	 * @param id
	 */
	protected static void cacheReplace(Object obj, String id) {
		obj = serialize(obj);
		Cache.safeReplace(id, obj, "20mn");
	}

	public static class JsonWrap {
		public String objJson = null;
	}

	private static Object serialize(Object obj) {
		if (obj == null)
			return null;

		if (// ClassUtil.hasInterface(obj.getClass(), Jsonable.class) ||
		ClassUtil.hasAnnotation(obj.getClass(), ToJson.class)) {
			JsonWrap jw = new JsonWrap();
			jw.objJson = JSON.serialize(obj);
			obj = jw;
		}

		return obj;
	}

	private static Object deserialize(Object obj) {
		if (obj instanceof JsonWrap) {
			JsonWrap jw = (JsonWrap) obj;
			obj = JSON.parse(jw.objJson);
		}
		return obj;
	}

	/**
	 * @param id
	 */
	protected static void cacheDelete(String id) {
		Cache.safeDelete(id);
	}

	/**
	 * 
	 */
	public static void clearAll() {
		Cache.clear();
	}

}
