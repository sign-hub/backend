/**
 *
 * Original file name: CfgUtil.java
 * Created by gannunziata 
 */
package utils.cfg;

import java.io.File;

import play.Logger;
import utils.StringUtil;

/**
 * 
 * @author gannunziata
 */
public class CfgUtil {

	public static boolean isDev() {
		return ConfigManager.instance().isDev();
	}

	/**
	 * @param string
	 * @param i
	 * @return
	 */
	public static int getInt(String keyCfg, int defInt) {
		try {
			String cfg = (String) ConfigManager.instance().getProperty(keyCfg, String.valueOf(defInt));
			if (cfg == null) {
				Logger.trace("configurazione (int) per %s not trovata. Uso il valore di default: %d", keyCfg, defInt);
				return defInt;
			}

			return Integer.parseInt(cfg);
		} catch (Exception e) {
			Logger.error(e, "errore configurazione (int) per %s. Uso il valore di default: %d", keyCfg, defInt);
			return defInt;
		}
	}

	/**
	 * @param string
	 * @param l
	 * @return
	 */
	public static long getLong(String keyCfg, long defLong) {
		try {
			String cfg = (String) ConfigManager.instance().getProperty(keyCfg, null);
			if (cfg == null) {
				Logger.trace("configurazione (long) per %s not trovata. Uso il valore di default: %d", keyCfg, defLong);
				return defLong;
			}

			return Integer.parseInt(cfg);
		} catch (Throwable e) {
			Logger.error(e, "errore configurazione (long) per %s. Uso il valore di default: %d", keyCfg, defLong);
			return defLong;
		}
	}

	/**
	 * @param string
	 * @param i
	 * @return
	 */
	public static float getFloat(String keyCfg, float def) {
		try {
			String cfg = ConfigManager.instance().getProperty(keyCfg, null);
			if (cfg == null) {
				Logger.trace("configurazione (float) per %s not trovata. Uso il valore di default: %d", keyCfg, def);

				return def;
			}
			cfg = cfg.replace(',', '.');

			return Float.parseFloat(cfg);
		} catch (Exception e) {
			Logger.error(e, "errore configurazione (float) per %s. Uso il valore di default: %d", keyCfg, def);
			return def;
		}
	}

	/**
	 * @param keyName
	 * @param string2
	 * @return
	 */
	public static String getString(String keyName, String defaultValue) {
		try {
			String val = ConfigManager.instance().getProperty(keyName, null);
			if (val == null) {
				Logger.trace("configurazione (string) per %s not trovata. Uso il valore di default: %s", keyName,
						defaultValue);
				return defaultValue;
			}

			return val;

		} catch (Exception e) {
			Logger.error(e, "errore configurazione (string) per %s. Uso il valore di default: %s", keyName,
					defaultValue);
			return defaultValue;
		}
	}

	public static boolean getBool(String keyName, boolean defaultValue) {
		try {
			String val = ConfigManager.instance().getProperty(keyName, null);
			if (val == null) {
				Logger.trace("configurazione (boolean) per %s not trovata. Uso il valore di default: %s", keyName,
						String.valueOf(defaultValue));
				return defaultValue;
			}

			return StringUtil.isTrue(val);

		} catch (Exception e) {
			Logger.error(e, "errore configurazione (boolean) per %s. Uso il valore di default: %s", keyName,
					String.valueOf(defaultValue));
			return defaultValue;
		}
	}

	/**
	 * @param string
	 * @param tolleranceDistance
	 * @return
	 */
	public static double tryDouble(String keyName, double defValue) {
		try {
			String cfg = ConfigManager.instance().getProperty(keyName, null);
			if (cfg == null) {
				Logger.trace("configurazione (double) per %s not trovata. Uso il valore di default: %d", keyName,
						defValue);
				return defValue;
			}
			cfg = cfg.replace(',', '.');

			return Double.parseDouble(cfg);
		} catch (Exception e) {
			Logger.error(e, "errore configurazione (double) per %s. Uso il valore di default: %d", keyName, defValue);
			return defValue;
		}
	}

	/**
	 * @param string
	 * @param class1
	 * @return
	 */
	public static <T> Class<T> getClass(String key, Class<T> defaultClass) {
		try {
			String className = getString(key, null);
			if (className == null)
				return defaultClass;
			@SuppressWarnings("unchecked")
			Class<T> resClass = (Class<T>) Class.forName(className);
			return resClass;
		} catch (Throwable e) {
			return defaultClass;

		}
	}

	/**
	 * @param string
	 * @param class1
	 * @return
	 */
	public static <T> T getClassInstance(String key, Class<T> defaultClass) {
		return getClassInstance(key, defaultClass, null);
	}

	/**
	 * @param string
	 * @param class1
	 * @return
	 */
	public static <T> T getClassInstance(String key, T defaultInstance) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Class<T> defClass = (defaultInstance != null) ? (Class) defaultInstance.getClass() : null;
		return getClassInstance(key, defClass, defaultInstance);
	}

	/**
	 * @param string
	 * @param class1
	 * @return
	 */
	public static <T> T getClassInstance(String key, Class<T> defaultClass, T defaultInstance) {
		try {
			T instance = null;
			Class<T> cl = getClass(key, defaultClass);
			if (cl == null)
				return defaultInstance;
			instance = cl.newInstance();
			return instance;
		} catch (Exception e) {
			Logger.error(e, "getClassInstance error");
			return defaultInstance;
		}
	}

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	public static String getPath(String key, String defaultPath, String subfolder) {
		String path = CfgUtil.getString(key, defaultPath);
		if (path != null && !path.isEmpty()) {
			if (!path.endsWith("/"))
				path += '/';
		} else {
			path = "";
		}

		if (subfolder != null) {
			path += subfolder;
			if (!path.endsWith("/"))
				path += '/';
		}

		return path;
	}

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	public static String getPath(String key, String defaultPath) {
		return getPath(key, defaultPath, null);
	}

	/**
	 * @return
	 */
	public static Iterable<String> allKeys() {
		return ConfigManager.instance().getAllKeys();
	}

	/**
	 * @return
	 */
	public static File getAppPath() {
		return ConfigManager.instance().getAppPath();
	}

	/**
	 * @param string
	 * @param class1
	 * @param informational
	 * @return
	 */
	public static <E extends Enum<E>> E getEnum(String key, Class<E> enumClass, E defaultValue) {
		String name = getString(key, null);
		if (name == null)
			return defaultValue;
		E e = null;
		String uname = name.toUpperCase();
		try {
			e = Enum.valueOf(enumClass, uname);
			return e;
		} catch (Exception ignore) {
			try {
				e = Enum.valueOf(enumClass, name);
				return e;
			} catch (Exception ignore2) {
				return defaultValue;
			}
		}
	}

}
