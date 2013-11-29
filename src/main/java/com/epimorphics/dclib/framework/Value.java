/******************************************************************
 * File:        Value.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

/**
 * Represents a wrapped value during pattern processing. Some values may be represented
 * by their java native type and may not be wrapped. Subclasses can implement methods
 * usable in pattern expressions.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Value {
    /** Test for null values, an empty cell will be represented as a null instead of a missing value */
    public boolean isNull();

    /** Return an underlying Java native value */
    public Object getValue();
    
    /** Convert to a string */
    public String getString();
    
    /** Convert to a string */
    public String toString();
    
    /** Test if this is a multi valued value */
    public boolean isMulti();
    
    /** Return all the raw values of a multi-valued value */
    public Object[] getValues();
    
    /**
     * Concatenate two values. Typically just String concatenation
     * but in the case where either value is an array generates the combinations.
     */
    public Value append(Value val);
}
