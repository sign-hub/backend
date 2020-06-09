/**
 * 
 * Original file name: NoConfigAdapter.java
 * Created by gannunziata at 16:50:49 
 */
package utils.cfg;

import java.io.File;
import java.util.Collections;

/**
 *
 * @author gannunziata
 */
public class NoConfigAdapter implements ConfigAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecommon.cfg.ConfigAdapter#getProperty(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getProperty(String key, String defValue) {
		return defValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecommon.cfg.ConfigAdapter#isDev()
	 */
	@Override
	public boolean isDev() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecommon.cfg.ConfigAdapter#getAllKeys()
	 */
	@Override
	public Iterable<String> getAllKeys() {
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecommon.cfg.ConfigAdapter#getAppPath()
	 */
	@Override
	public File getAppPath() {
		return new File(".");
	}
}
