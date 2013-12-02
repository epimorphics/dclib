/******************************************************************
 * File:        NullResult.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

/**
 * Exception used to signal that a null result was generated during
 * expression evaluation, this can be caught in cases where the 
 * expression can be regard as optional.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
@SuppressWarnings("serial")
public class NullResult extends RuntimeException 
{
    public NullResult() {
        super();
    }
    
    public NullResult(String message) {
        super(message); 
    }
    
    public NullResult(Throwable cause) { 
        super(cause) ;
    }
    
    public NullResult(String message, Throwable cause) { 
        super(message, cause) ;
    }

}
