/******************************************************************
 * File:        BindingEnv.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;

/**
 * Represents a set of name to value mappings. Can be chained so it's 
 * possible to efficiently override some values in a local environment
 * that's later discarded.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class BindingEnv extends HashMap<String, Object> implements Map<String, Object>, JexlContext {
    private static final long serialVersionUID = 1L;
    
    protected BindingEnv parent;
    
    /**
     * Create an empty, standalone environment
     */
    public BindingEnv() {
        super();
    }
    
    /**
     * Create an environment which inherits from a parent
     * environment. Locally asserted bindings will override
     * bindings from the parent. 
     */
    public BindingEnv(BindingEnv parent) {
        this.parent = parent;
    }
    
    @Override
    public Object get(Object key) {
        return doGet(key);
    }

    @Override
    public Object get(String name) {
        return doGet(name);   
    }
    
    // Sometimes the java type system just seems to work against you
    private Object doGet(Object key) {
        Object v = super.get(key);
        if (v == null) {
            return parent.get(key);
        } 
        return v;
    }

    @Override
    public void set(String name, Object value) {
        put(name, value);
    }

    @Override
    public boolean has(String name) {
        return doGet(name) != null;
    }
}
