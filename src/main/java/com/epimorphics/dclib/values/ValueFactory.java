/******************************************************************
 * File:        ValueFactory.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.epimorphics.dclib.framework.ConverterProcess;

/**
 * Construct Objects suitable for scripting from string sources.
 * Implements default parsing rules for numbers and dates.
 * Wraps some types as Value objects.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueFactory {
    
    public static Value asValue(String string, ConverterProcess proc) {
        if (string == null || string.isEmpty()) {
            return new ValueNull();
        } else if (ValueNumber.isNumber(string)) {
            return new ValueNumber(string, proc);
        } else if (ValueDate.isDate(string)) {
            return new ValueDate(string, proc);
        } else {
            return new ValueString(string, proc);
        }
    }
}
