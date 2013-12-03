/******************************************************************
 * File:        ValueString.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

public class ValueString extends ValueBase<String> implements Value {

    public ValueString(String value) {
        super(value);
    }

    public ValueStringArray split(String pattern) {
        return new ValueStringArray( value.split(pattern) );
    }
    
    // TODO implement string manipulation functions
}
