/******************************************************************
 * File:        DataContext.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Packages access to the background context of reference data, templates and
 * prefix definitions need to process a data set. 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DataContext {
    // TODO templates
    // TODO sources
    
    protected PrefixMapping prefixes;
    
    public DataContext() {
    }
    
    public void setPrefixes(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }
    
    /**
     * Expand a prefixes in the given URI string using the reference prefix definitions.
     */
    public String expandURI(String uri) {
        if (prefixes != null) {
            return prefixes.expandPrefix(uri);
        } else {
            return uri;
        }
    }
}
