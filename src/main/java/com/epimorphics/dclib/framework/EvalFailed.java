/******************************************************************
 * File:        EvalFailed.java
 * Created by:  Dave Reynolds
 * Created on:  2 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

/**
 * Used to signal errors in pattern evaluation or result checking
 */
@SuppressWarnings("serial")
public class EvalFailed extends RuntimeException { 
        public EvalFailed() {
            super();
        }
        
        public EvalFailed(String message) {
            super(message); 
        }
        
        public EvalFailed(Throwable cause) { 
            super(cause) ;
        }
        
        public EvalFailed(String message, Throwable cause) { 
            super(message, cause) ;
        }

}
