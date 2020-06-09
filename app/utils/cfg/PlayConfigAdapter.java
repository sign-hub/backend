/**
 * 
 * Original file name: PlayConfigAdapter.java
 * Created by Raffaele 
 */
package utils.cfg;

import java.io.File;

import play.Play;

/**
 * 
 * @author Raffaele
 */
public class PlayConfigAdapter implements ConfigAdapter {
	private static enum PlayConfigAdapterSingleton {
		INSTANCE;

		private static final PlayConfigAdapter singleton = new PlayConfigAdapter();

		public PlayConfigAdapter getSingleton() {
			return singleton;
		}
	}

	public static PlayConfigAdapter instance() {
		return PlayConfigAdapter.PlayConfigAdapterSingleton.INSTANCE.getSingleton();
	}

	private PlayConfigAdapter() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecommon.cfg.ConfigAdapter#getProperty(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		return Play.configuration.getProperty(key, defaultValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecommon.cfg.ConfigAdapter#isDev()
	 */
	@Override
	public boolean isDev() {
		return Play.mode.isDev();
	}

	@Override
	public Iterable<String> getAllKeys() {
		return Play.configuration.stringPropertyNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecommon.cfg.ConfigAdapter#getAppPath()
	 */
	@Override
	public File getAppPath() {
		return Play.applicationPath;
	}

}
