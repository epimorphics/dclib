/******************************************************************
 * File:        ValueNull.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

/**
 * Represent a missing value. Will typically get changed into a simple java null
 * before processing by a template.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueNull implements Value
{

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String getString() {
        return "null";
    }

    @Override
    public boolean isMulti() {
        return false;
    }

    @Override
    public Object[] getValues() {
        return null;
    }

    @Override
    public Value append(Value val) {
        return null;
    }

}