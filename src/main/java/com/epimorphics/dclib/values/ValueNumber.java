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

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

public class ValueNumber extends ValueBase<Number> implements Value {
    protected static final Pattern INTEGER_PATTERN = Pattern.compile("[-+]?[0-9]+");
    protected static final Pattern DECIMAL_PATTERN = Pattern.compile("[-+]?[0-9]+\\.[0-9]+");
    protected static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?[0-9]+(\\.[0-9]+)?[eE][-+]?[0-9]+(\\.[0-9]+)?");
    protected static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?[0-9]+(\\.[0-9]+)?([eE][-+]?[0-9]+(\\.[0-9]+)?)?");

    protected String lexical;
    
    public ValueNumber(Number value) {
        super(value);
    }

    public ValueNumber(String value) {
        super(stringToNumber(value));
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
    
    public Value asDecimal() {
        BigDecimal decimal = null;
        // Added clause for BigInteger, not sure that clause for Long is relevant, but left for now.
        // typeFromNumber only covers Long by default
        // stringToNumber does makes BigInteger values, but not Long values, but Jexl evaluation might - skw 2015-11-10
        if (value instanceof Long) {
            decimal = new BigDecimal( (Long)value );
        } else if (value instanceof BigInteger) {
            decimal = new BigDecimal( ( BigInteger)value) ;	
        } else if (value instanceof Double) {
            decimal = lexical != null ? new BigDecimal(lexical) : new BigDecimal((Double)value);
        } else if (value instanceof BigDecimal) {
            decimal = (BigDecimal) value;
        }
        if (decimal == null) {
            return null;
        } else {
            return new ValueNumber( decimal );
        }
    }
    
    public Number toNumber() {
        return value;
    }
    
    @Override
    public String toString() {
        if (lexical != null) {
            return lexical;
        } else {
        	if (value instanceof BigDecimal) {
        		return ((BigDecimal) value).toPlainString() ;
            }    
            return value.toString();
        }
    }
    
    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public Node asNode() {
        return nodeFromNumber(value);
    }
    
    public static Node nodeFromNumber(Number result) {
    	if(result instanceof BigDecimal) {
    		return NodeFactory.createLiteral( ((BigDecimal)result).toPlainString(),  XSDDatatype.XSDdecimal) ;
    	}
        return NodeFactory.createUncachedLiteral(result, typeFromNumber(result));
    }
    
    public static RDFDatatype typeFromNumber(Number result) {
        if (result instanceof BigDecimal) {
            return XSDDatatype.XSDdecimal;
        } else if (result instanceof BigInteger) {
            return XSDDatatype.XSDinteger;
        } else if (result instanceof Double) {
            return XSDDatatype.XSDdouble;
        } else {
            return XSDDatatype.XSDinteger;
        }       
    }

    @Override
    public String getDatatype() {
        return typeFromNumber(value).getURI();
    }
    
}
