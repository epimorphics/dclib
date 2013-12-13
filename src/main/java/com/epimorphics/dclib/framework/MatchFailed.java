/******************************************************************
 * File:        MatchFailed.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

/**
 * Signals a failure in pattern processing which should fail the conversion.
 * E.g. a missing lookup in a map.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
@SuppressWarnings("serial")
public class MatchFailed extends RuntimeException 
{
    public MatchFailed() {
        super();
    }
    
    public MatchFailed(String message) {
        super(message); 
    }
    
    public MatchFailed(Throwable cause) { 
        super(cause) ;
    }
    
    public MatchFailed(String message, Throwable cause) { 
        super(message, cause) ;
    }

}
