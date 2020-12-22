/**
 * Copyright 2015 by Eclettica s.r.l. All Rights Reserved.
 *
 * This file is part of ewidget project.
 *
 * The contents of this file are subject to the Eclettica Private License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclettica.net/code/license
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
