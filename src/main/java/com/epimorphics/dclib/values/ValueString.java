/******************************************************************
 * File:        ValueString.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.epimorphics.dclib.framework.NullResult;
import com.epimorphics.util.NameUtils;

public class ValueString extends ValueBase<String> implements Value {

    public ValueString(String value) {
        super(value);
    }

    public ValueStringArray split(String pattern) {
        return new ValueStringArray( value.split(pattern) );
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    public String toLowerCase() {
        return value.toLowerCase();
    }
    
    public String toUpperCase() {
        return value.toUpperCase();
    }
    
    public String toSegment() {
        return NameUtils.safeName(value);
    }
    
    public Object toNumber() {
        return ValueFactory.asValue(value);
    }
    
    public Object trim() {
        return new ValueString( value.trim() );
    }
    
    public Object substring(int offset) {
        return new ValueString( value.substring(offset) );
    }
    
    public Object substring(int start, int end) {
        return new ValueString( value.substring(start, end) );
    }
    
    public Object regex(String regex) {
        Matcher m = Pattern.compile(regex).matcher(value);
        if (m.matches()) {
            if (m.groupCount() > 0) {
                return new ValueString( m.group(1) );
            } else {
                return this;
            }
        } else {
            throw new NullResult("Regex " + regex + " did not match");
        }
    }
    
    public boolean matches(String regex) {
        return value.matches(regex);
    }
    
    // TODO implement string manipulation functions
}
