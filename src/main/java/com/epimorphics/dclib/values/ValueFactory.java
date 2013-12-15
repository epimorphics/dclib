/******************************************************************
 * File:        ValueFactory.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.epimorphics.dclib.framework.ConverterProcess;

/**
 * Construct Objects suitable for scripting from string sources.
 * Implements default parsing rules for numbers and dates.
 * Wraps some types as Value objects.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueFactory {
    protected static final Pattern INTEGER_PATTERN = Pattern.compile("[0-9]+");
    protected static final Pattern DECIMAL_PATTERN = Pattern.compile("[0-9]+\\.[0-9]+");
    protected static final Pattern FLOAT_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]+)?[eE][-+][0-9]+(\\.[0-9]+)?");
    
    public static Object asValue(String string, ConverterProcess proc) {
        if (string == null || string.isEmpty()) {
            return new ValueNull();
        } else if (INTEGER_PATTERN.matcher(string).matches()) {
            return Long.valueOf(string);
        } else if (FLOAT_PATTERN.matcher(string).matches()) {
            return Double.valueOf(string);
        } else if (DECIMAL_PATTERN.matcher(string).matches()) {
            return new BigDecimal(string);
        } else {
            // TODO handle dates
            return new ValueString(string, proc);
        }
    }
}
