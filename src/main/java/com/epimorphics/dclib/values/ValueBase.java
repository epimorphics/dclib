/******************************************************************
 * File:        ValueBase.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.epimorphics.dclib.framework.ConverterProcess;

/**
 * A simple packaged value.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class ValueBase<T> implements Value {
    protected ConverterProcess proc;
    protected T value;
    
    public ValueBase(T value, ConverterProcess config) {
        this.value = value;
        this.proc = config;
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
    public Value getString() {
        return asString();
    }
    
    @Override
    public Value append(Value val) {
        String base = toString();
        if (val.isMulti()) {
            Object[] values = val.getValues();
            String[] results = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                results[i] = base + values[i];
            }
            return new ValueStringArray(results, proc);
        } else {
            return new ValueString(base + val.toString(), proc);
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
