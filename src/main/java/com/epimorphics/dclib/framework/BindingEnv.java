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

import com.epimorphics.dclib.values.ValueNull;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

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
    
    /**
     * Return the most recent binding of a key or null if there is one (even 
     * if there is an inherited value further up the chain).
     */
    public Object getLocal(String name) {
        return super.get(name);
    }
    
    // Sometimes the java type system just seems to work against you
    private Object doGet(Object key) {
        Object v = super.get(key);
        if (v == null && parent != null) {
            return parent.get(key);
        } else if (v instanceof ValueNull) {
            return null;
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
    
    public void setParent(BindingEnv parent) {
        this.parent = parent;
    }
    
    /**
     * Iterate over all keys up the whole environment
     */
    public ExtendedIterator<String> allKeys() {
        ExtendedIterator<String> it = WrappedIterator.create( keySet().iterator() );
        if (parent != null){
            it = it.andThen( parent.allKeys() );
        }
        return it;
    }
    
    /**
     * Summary print of whole inherited environment
     */
    public String toStringDeep() {
        return toStringDeep("");
    }
    
    public String toStringDeep(String indent) {
        StringBuffer buff = new StringBuffer();
        for (Entry<String, Object> ent : entrySet()) {
            buff.append(String.format("%s %30s = %s\n", indent, ent.getKey(), ent.getValue().toString()));
        }
        if (parent != null) {
            buff.append( parent.toStringDeep(indent + "  ") );
        }
        return buff.toString();
    }
}
