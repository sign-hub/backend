/**
 * 
 * Original file name: ConfigAdapter.java
 * Created by gannunziata 
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
