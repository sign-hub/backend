/**
 *
 * Original file name: ConfigRef.java
 * Created by gannunziata at 18:49:52
 */
package utils.cfg;

import play.Logger;

/**
 * 
 * @author gannunziata
 */
public class ConfigRef<T> {
	private T elem;
	private Class<T> classElem;
	private String className;
	private final Object lock = new Object();

	/**
	 * non creabile dall'esterno del package config.
	 */
	ConfigRef(Class<T> classElem) {
		this.classElem = classElem;
		this.className = classElem.getName();
	}

	void saveNewRef(T elem) {
		synchronized (lock) {
			this.elem = elem;
		}
	}

	public boolean hasRef() {
		return this.elem != null;
	}

	public T get() {
		if (elem == null && ConfigManager.instance().isDev()) {
			// on reload classes can happen this...
			synchronized (this) {
				try {
					Logger.info("detect change - ref to config not found - try to reload for " + this.className);
					ConfigRef<T> cr = ConfigManager.instance().refTo(this.classElem);
					if (cr != null) {
						this.elem = cr.elem;
						if (this.elem == null) {
							Logger.warn("configuration found NULL from ConfigManager (%s)", this.className);
						}
					} else
						Logger.warn("configuration not found from ConfigManager (%s)", this.className);
				} catch (Exception e) {
					Logger.error(e, "config ref reload");
				}
			}
		}

		if (elem == null) {
			throw new ConfigException("configuration internal error (config still not referenced)");
		}

		return elem;
	}
}
