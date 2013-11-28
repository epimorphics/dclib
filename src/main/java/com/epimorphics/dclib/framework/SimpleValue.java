/******************************************************************
 * File:        SimpleValue.java
 * Created by:  Dave Reynolds
 * Created on:  19 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import com.epimorphics.util.EpiException;

/**
 * Wraps a value from a data source such as a CSV, which may be null.
 * Supports some value type conversions (currently integer only). 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class SimpleValue {

    String lex;
    Object value;
    
    public SimpleValue(String lexicalForm) {
        lex = lexicalForm;
        try {
            value = Long.parseLong(lex);
        } catch (NumberFormatException e) {
            value = lex;
        }
    }
    
    public SimpleValue(Object value) {
        this.value = value;
    }
        
    public String getLexical() {
        if (lex == null) {
            return value.toString();
        } else {
            return lex;
        }
    }
    
    public long asInt() {
        if (value != null && value instanceof Number) {
            return ((Number)value).longValue();
        }
        throw new EpiException("Value is not an integer: " + lex);
    }
    
    public boolean isInt() {
        if (value != null && value instanceof Number) {
            return true;
        } else {
            return false;
        }
    }
    
    public String toString() {
        return getLexical();
    }

    public Object getValue() {
        return value;
    }

    public boolean notEmpty() {
        if (lex != null) {
            return !lex.isEmpty();
        } else {
            return value != null;
        }
    }

    public boolean empty() {
        return !notEmpty();
    }

    public String str() {
        return getLexical();
    }
    
}

