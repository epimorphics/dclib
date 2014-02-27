/******************************************************************
 * File:        ValueDate.java
 * Created by:  Dave Reynolds
 * Created on:  27 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.util.regex.Pattern;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * Represent a range of datetime types as a wrapped RDF node
 */
public class ValueDate extends ValueBase<Node> implements Value {
    
    protected String lexical;
    
    public ValueDate(String value, ConverterProcess proc) {
        super(stringToDate(value), proc);
        lexical = value;
    }
    
    private static Node stringToDate(String value) {
        if (DATETIME_PATTERN.matcher(value).matches()) {
            return NodeFactory.createLiteral(value, XSDDatatype.XSDdateTime);
        } else if (DATE_PATTERN.matcher(value).matches()) {
            return NodeFactory.createLiteral(value, XSDDatatype.XSDdate);
        } else if (TIME_PATTERN.matcher(value).matches()) {
            return NodeFactory.createLiteral(value, XSDDatatype.XSDtime);
        } else if (GYEARMONTH_PATTERN.matcher(value).matches()) {
            return NodeFactory.createLiteral(value, XSDDatatype.XSDgYearMonth);
        } else {
            return null;
        }
    }

    public static boolean isDate(String str) {
        return ANYDATE_PATTERN.matcher(str).matches();
    }
    
    @Override
    public boolean isDate() {
        return true;
    }

    @Override
    public Value asString() {
        return new ValueString(lexical, proc);
    }

    @Override
    public Node asNode() {
        return value;
    }

    protected static final String DATE_BLOCK = "[0-9]{4}-[01][0-9]-[0-3][0-9]";
    protected static final String TIME_BLOCK = "[0-6][0-9]:[0-6][0-9]:[0-6][0-9](\\.[0-9]+)?";
    protected static final String TZONE_BLOCK = "([+-][0-6][0-9]:[0-6][0-9])|Z";
    protected static final String GYM_BLOCK = "[0-9]{4}-[01][0-9]";
    protected static final Pattern DATETIME_PATTERN = Pattern.compile( String.format("-?%sT%s(%s)?", DATE_BLOCK, TIME_BLOCK, TZONE_BLOCK) );
    protected static final Pattern DATE_PATTERN = Pattern.compile( String.format("-?%s(%s)?", DATE_BLOCK, TZONE_BLOCK) );
    protected static final Pattern TIME_PATTERN = Pattern.compile( String.format("%s(%s)?", TIME_BLOCK, TZONE_BLOCK) );
    protected static final Pattern GYEARMONTH_PATTERN = Pattern.compile( String.format("%s(%s)?", GYM_BLOCK, TZONE_BLOCK) );
    protected static final Pattern ANYDATE_PATTERN = Pattern.compile( String.format("-?(%sT%s|%s|%s|%s)(%s)?", DATE_BLOCK, TIME_BLOCK, DATE_BLOCK, TIME_BLOCK, GYM_BLOCK, TZONE_BLOCK) );

    @Override
    public String datatype() {
        return value.getLiteralDatatypeURI();
    }
    

}
