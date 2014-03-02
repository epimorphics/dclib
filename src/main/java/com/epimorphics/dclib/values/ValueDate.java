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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Represent a range of datetime types as a wrapped RDF node
 */
public class ValueDate extends ValueNode implements Value {
    
    protected String lexical;
    
    public ValueDate(String value, ConverterProcess proc) {
        super(stringToDate(value), proc);
        lexical = value;
    }
    
    public ValueDate(Node node, ConverterProcess proc) {
        super(node, proc);
        lexical = node.getLiteralLexicalForm();
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

    protected static final String DATE_BLOCK = "[0-9]{4}-[01][0-9]-[0-3][0-9]";
    protected static final String TIME_BLOCK = "[0-6][0-9]:[0-6][0-9]:[0-6][0-9](\\.[0-9]+)?";
    protected static final String TZONE_BLOCK = "([+-][0-6][0-9]:[0-6][0-9])|Z";
    protected static final String GYM_BLOCK = "[0-9]{4}-[01][0-9]";
    protected static final Pattern DATETIME_PATTERN = Pattern.compile( String.format("-?%sT%s(%s)?", DATE_BLOCK, TIME_BLOCK, TZONE_BLOCK) );
    protected static final Pattern DATE_PATTERN = Pattern.compile( String.format("-?%s(%s)?", DATE_BLOCK, TZONE_BLOCK) );
    protected static final Pattern TIME_PATTERN = Pattern.compile( String.format("%s(%s)?", TIME_BLOCK, TZONE_BLOCK) );
    protected static final Pattern GYEARMONTH_PATTERN = Pattern.compile( String.format("%s(%s)?", GYM_BLOCK, TZONE_BLOCK) );
    protected static final Pattern ANYDATE_PATTERN = Pattern.compile( String.format("-?(%sT%s|%s|%s|%s)(%s)?", DATE_BLOCK, TIME_BLOCK, DATE_BLOCK, TIME_BLOCK, GYM_BLOCK, TZONE_BLOCK) );
    
    protected static final DateTimeFormatter DATETIME_FMT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.S");
    protected static final DateTimeFormatter DATETIME_TZ_FMT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SZZZ").withOffsetParsed();
    protected static final DateTimeFormatter DATE_FMT = DateTimeFormat.forPattern("yyyy-MM-dd");
    protected static final DateTimeFormatter DATE_TZ_FMT = DateTimeFormat.forPattern("yyyy-MM-ddZZZ").withOffsetParsed();
    protected static final DateTimeFormatter TIME_FMT = DateTimeFormat.forPattern("HH:mm:ss.S");
    protected static final DateTimeFormatter TIME_TZ_FMT = DateTimeFormat.forPattern("HH:mm:ss.SZZZ").withOffsetParsed();
    protected static final DateTimeFormatter GYEARMONTH_FMT = DateTimeFormat.forPattern("yyyy-MM");
    protected static final DateTimeFormatter GYEAR_FMT = DateTimeFormat.forPattern("yyyy");
    
    /**
     * Attempt to parse a string as some form of date time.
     * @param lex The string to be parsed.
     * @param format A set of optional date formats. The format
     * should be a set of <a href="http://www.joda.org/joda-time/apidocs/index.html">Joda time</a> patterns separated by "|".
     * These patterns are nearly identical to Java SimpleDateFormat patterns. If the pattern contains a timezone parse (Z) then
     * the generated literal with have an explicit timezone, otherwise it will be a local, timezone-free, literal. 
     * @param typeURI The URI for the date time type - can be one of xsd:dateTime, xsd:date, xsd:time, xsd:gYearMonth, xsd:gYear.
     * @param proc the parent converter process for error reporting
     * @return A ValueDate containing a legal RDF literal of the given time, or a ValueNull if non of the parse options worked.
     */
    public static Value parse(String lex, String format, String typeURI, ConverterProcess proc) {
        for(String fmt : format.split("\\|")) {
            try {
                boolean withTZ = fmt.contains("Z");
                DateTimeFormatter p = DateTimeFormat.forPattern(fmt);
                if (withTZ) {
                    p = p.withOffsetParsed();
                }
                DateTime time = p.parseDateTime(lex);
                String formatted = lex;
                if (typeURI.equals(XSD.dateTime.getURI())) {
                    formatted = (withTZ ? DATETIME_TZ_FMT : DATETIME_FMT).print(time);
                } else if (typeURI.equals(XSD.date.getURI())) {
                    formatted = (withTZ ? DATE_TZ_FMT : DATE_FMT).print(time);
                } else if (typeURI.equals(XSD.time.getURI())) {
                    formatted = (withTZ ? TIME_TZ_FMT : TIME_FMT).print(time);
                } else if (typeURI.equals(XSD.gYearMonth.getURI())) {
                    formatted = GYEARMONTH_FMT.print(time);
                } else if (typeURI.equals(XSD.gYear.getURI())) {
                    formatted = GYEAR_FMT.print(time);
                }
                if (formatted.endsWith("UTC")) {
                    formatted = formatted.replace("UTC", "Z");
                }
                Node n = NodeFactory.createLiteral(formatted, TypeMapper.getInstance().getSafeTypeByName(typeURI));
                return new ValueDate(n , proc);
            } catch(Exception e) {
                // Ignore and loop round to try the next pattern
            }
        }
        return new ValueNull();
    }
    
    /**
     * Wrap string as an xsd date time type
     * @param lex The string to be wrapped
     * @param typeURI The URI for the date time type - can be one of xsd:dateTime, xsd:date, xsd:time, xsd:gYearMonth, xsd:gYear.
     * @return A ValueDate containing an RDF literal
     */
    public static Value parse(String lex, String typeURI, ConverterProcess proc) {
        Node node = NodeFactory.createLiteral(lex, TypeMapper.getInstance().getSafeTypeByName(typeURI));
        node.getLiteral().getValue(); // Checks well formed, throws exception if not
        return new ValueDate( node, proc);
    }
    
    /**
     * Wrap string as an xsd dateTime, date, time, or gYearMonth
     */
    public static Value parse(String lex, ConverterProcess proc) {
        return new ValueDate(lex, proc);
    }
    
    public static void main(String[] args) {
        DateTimeFormatter p = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withOffsetParsed();
        DateTime time = p.parseDateTime("2014-03-01T12:05:10.23+01:00");
        System.out.println("Time with +1 offset: " + time);
        time = p.parseDateTime("2014-03-01T12:05:10.23");
        System.out.println("Time with no offset: " + time);
    }

}
