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
 * Original file name: ConfigAdapter.java
 * Created by gannunziata at 16:49:45 
 */
package utils.cfg;

import java.io.File;

/**
 *
 * @author gannunziata
 */
public interface ConfigAdapter {

    /**
     * @param key
     * @param defValue
     * @return
     */
    public String getProperty(String key, String defValue);

    /**
     * @return
     */
    public boolean isDev();

    /**
     * @return
     */
    public Iterable<String> getAllKeys();

    /**
     * @return
     */
    public File getAppPath();
    
    
}
