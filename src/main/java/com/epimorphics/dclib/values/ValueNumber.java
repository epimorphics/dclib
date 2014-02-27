/******************************************************************
 * File:        ValueNumber.java
 * Created by:  Dave Reynolds
 * Created on:  27 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

import com.epimorphics.dclib.framework.ConverterProcess;

public class ValueNumber extends ValueBase<Number> implements Value {
    protected static final Pattern INTEGER_PATTERN = Pattern.compile("[0-9]+");
    protected static final Pattern DECIMAL_PATTERN = Pattern.compile("[0-9]+\\.[0-9]+");
    protected static final Pattern FLOAT_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]+)?[eE][-+]?[0-9]+(\\.[0-9]+)?");
    protected static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]+)?([eE][-+]?[0-9]+(\\.[0-9]+)?)?");

    protected String lexical;
    
    public ValueNumber(Number value, ConverterProcess proc) {
        super(value, proc);
    }

    public ValueNumber(String value, ConverterProcess proc) {
        super(stringToNumber(value), proc);
        lexical = value;
    }
    
    public static Number stringToNumber(String string) {
        if (string != null) {
            if (INTEGER_PATTERN.matcher(string).matches()) {
                try {
                    return Long.valueOf(string);
                } catch (NumberFormatException e) {
                    return new BigInteger(string);
                }
            } else if (FLOAT_PATTERN.matcher(string).matches()) {
                return Double.valueOf(string);
            } else if (DECIMAL_PATTERN.matcher(string).matches()) {
                return new BigDecimal(string);
            }
        }
        return null;
    }

    public static boolean isNumber(String value) {
        return NUMBER_PATTERN.matcher(value).matches();
    }
    
    public Value asNumber() {
        return this;
    }
    
    public Number toNumber() {
        return value;
    }
    
    @Override
    public Value asString() {
       return new ValueString(toString(), proc); 
    }
    
    @Override
    public String toString() {
        if (lexical != null) {
            return lexical;
        } else {
            return value.toString();
        }
    }
}
