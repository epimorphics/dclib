/******************************************************************
 * File:        ValueStingArray.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

/**
 * Wraps an array of strings, e.g. from a split operation. This allows
 * a pattern to return multiple results.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueStringArray extends ValueBase<String[]> implements Value {
    
    public ValueStringArray(String[] values) {
        super(values);
    }

    @Override
    public boolean isNull() {
        return value == null || value.length == 0;
    }
    
    @Override
    public boolean isMulti() {
        return true;
    }

    @Override
    public Object[] getValues() {
        return value;
    }
    
    @Override
    public Value append(Value app) {
        if (app.isMulti()) {
            Object[] apps = app.getValues();
            int len = apps.length;
            String[] results = new String[value.length * len];
            for (int i = 0; i < value.length; i++) {
                for (int j = 0; j < len; j++) {
                    results[i*len + j] = value[i] + apps[j];
                }
            }
            return new ValueStringArray(results);
        } else {
            String[] results = new String[value.length];
            for (int i = 0; i < value.length; i++) {
                results[i] = value[i] + app.toString();
            }
            return new ValueStringArray(results);
        }
    }

}
