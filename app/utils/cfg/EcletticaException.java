/**
 * 
 * Original file name: EWidgetException.java
 * Created by gannunziata at 19:24:25
 */
package utils.cfg;

/**
 *
 * @author gannunziata
 */
public class EcletticaException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * 
     */
    public EcletticaException() {
    }
    
    public EcletticaException(String message) {
        super(message);
    }
    
    public EcletticaException(Throwable ex, String message) {
        super(message, ex);
    }
    
    public EcletticaException(Throwable ex) {
        super(ex);
    }
    
    public String getErrorTitle() {
        return "Eclettica Exception";
    }
    
    public String getErrorDescription() {
        return getLocalizedMessage();
    }
}
