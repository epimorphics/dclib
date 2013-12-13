/******************************************************************
 * File:        ValueBase.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.epimorphics.dclib.framework.DataContext;

/**
 * A simple packaged value.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueBase<T> implements Value {
    protected DataContext dc;
    protected T value;
    
    public ValueBase(T value, DataContext dc) {
        this.value = value;
        this.dc = dc;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getString() {
        return value == null ? "null" : value.toString();
    }

    @Override
    public String toString() {
        return getString();
    }
    
    @Override
    public Value append(Value val) {
        String base = getString();
        if (val.isMulti()) {
            Object[] values = val.getValues();
            String[] results = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                results[i] = base + values[i];
            }
            return new ValueStringArray(results, dc);
        } else {
            return new ValueString(base + val.toString(), dc);
        }
    }

    @Override
    public boolean isMulti() {
        return false;
    }

    @Override
    public Object[] getValues() {
        return new Object[]{value};
    }

}
