/**
 * 
 * Original file name: ConfigManager.java
 * Created by gannunziata 
 */
package utils.cfg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import play.Logger;

/**
 * configuration manager. for Spring using @Configuration
 * 
 * @author gannunziata
 */
public class ConfigManager {

	private static enum ConfigManagerSingleton {
		INSTANCE;

		private static final ConfigManager singleton = new ConfigManager();

		public ConfigManager getSingleton() {
			return singleton;
		}
	}

	public static ConfigManager instance() {
		return ConfigManager.ConfigManagerSingleton.INSTANCE.getSingleton();
	}

	private ConfigAdapter adapter;

	/**
	 * per l'uso all'interno di Spring.
	 */
	public ConfigManager() {
		adapter = new NoConfigAdapter();
		// adapter = PlayConfigAdapter.instance();
	}

	public String getProperty(String key, String defValue) {
		return adapter.getProperty(key, defValue);
	}

	public boolean isDev() {
		return adapter.isDev();
	}

	/**
	 * @return the adapter
	 */
	public ConfigAdapter getAdapter() {
		return adapter;
	}

	/**
	 * @param adapter
	 *            the adapter to set
	 */
	public void setAdapter(ConfigAdapter adapter) {
		if (adapter == null)
			throw new ConfigException("adapter cannot be null");
		this.adapter = adapter;
	}

	@SuppressWarnings("rawtypes")
	private Map<String, ConfigRef> refs = new HashMap<String, ConfigRef>();

	@SuppressWarnings("unchecked")
	public synchronized <T> ConfigRef<T> refTo(Class<T> classElem) {
		ConfigRef<T> ref = null;
		ref = refs.get(classElem.getName());
		if (ref == null) {
			ref = new ConfigRef<T>(classElem);
			refs.put(classElem.getName(), ref);
			ref.saveNewRef(getConfiguration(classElem));
		} else {
			if (!ref.hasRef()) {
				ref.saveNewRef(getConfiguration(classElem));
			}
		}

		return ref;
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> void updateConfiguration(T config) {
		if (config == null)
			throw new ConfigException("config must be valid");

		String name = config.getClass().getName();
		ConfigRef<T> ref = null;

		ref = refs.get(name);
		if (ref == null) {
			ref = new ConfigRef<T>((Class<T>) config.getClass());
			refs.put(name, ref);
		}

		ref.saveNewRef(config);
		// String cfgName = config.getCodeName();
		// Cache.safeReplace(CONFIG_PREFIX + cfgName, config, EXPIRATION);

		// adapter..... signal update (reload config) to OTHER node in the
		// cluster!!

	}

	<T> T getConfiguration(Class<T> clazz) {
		T config = null;
		T cfg = null;
		try {
			cfg = clazz.newInstance();
			config = cfg;

		} catch (Exception e) {
			Logger.error(e, "getConfiguration internal error");
			throw new ConfigException("getConfiguration internal error");
		}

		return config;
	}

	/**
	 * @return
	 */
	public Iterable<String> getAllKeys() {
		return adapter.getAllKeys();
	}

	/**
	 * @return
	 */
	public File getAppPath() {
		return adapter.getAppPath();
	}

}
