/**
 * Copyright 2015 by Eclettica s.r.l. All Rights Reserved.
 *
 * This file is part of ecommon project.
 *
 * The contents of this file are subject to the Eclettica Private License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclettica.net/code/license
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
