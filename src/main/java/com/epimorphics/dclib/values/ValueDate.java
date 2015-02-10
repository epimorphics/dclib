/******************************************************************
 * File:        ValueDate.java
 * Created by:  Dave Reynolds
 * Created on:  27 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.jena.riot.system.StreamRDF;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.govData.util.BritishCalendar;
import com.epimorphics.govData.util.CalendarDay;
import com.epimorphics.govData.util.CalendarInstant;
import com.epimorphics.govData.util.CalendarUtils;
import com.epimorphics.govData.util.CalendarWeek;
import com.epimorphics.govData.util.CalendarYear;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Represent a range of datetime types as a wrapped RDF node
 */
public class ValueDate extends ValueNode implements Value {
    
    protected DateTime jdt;
    protected RefTimeRepresentation reftime;
    
    public ValueDate(String value) {
        super(stringToDate(value));
    }
    
    public ValueDate(Node node) {
        super(node);
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
    
    protected static final DateTimeFormatter DATETIME_FMT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    protected static final DateTimeFormatter DATETIME_TZ_FMT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZZ").withOffsetParsed();
    protected static final DateTimeFormatter DATETIME_FMT_MS = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    protected static final DateTimeFormatter DATETIME_TZ_FMT_MS = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ").withOffsetParsed();
    protected static final DateTimeFormatter DATE_FMT = DateTimeFormat.forPattern("yyyy-MM-dd");
    protected static final DateTimeFormatter DATE_TZ_FMT = DateTimeFormat.forPattern("yyyy-MM-ddZZZ").withOffsetParsed();
    protected static final DateTimeFormatter TIME_FMT = DateTimeFormat.forPattern("HH:mm:ss");
    protected static final DateTimeFormatter TIME_TZ_FMT = DateTimeFormat.forPattern("HH:mm:ssZZZ").withOffsetParsed();
    protected static final DateTimeFormatter TIME_FMT_MS = DateTimeFormat.forPattern("HH:mm:ss.SSS");
    protected static final DateTimeFormatter TIME_TZ_FMT_MS = DateTimeFormat.forPattern("HH:mm:ss.SSSZZZ").withOffsetParsed();
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
     * @return A ValueDate containing a legal RDF literal of the given time, or a ValueNull if non of the parse options worked.
     */
    public static Value parse(String lex, String format, String typeURI) {
        for(String fmt : format.split("\\|")) {
            try {
                boolean withTZ = fmt.contains("Z");
                DateTimeFormatter p = DateTimeFormat.forPattern(fmt);
                if (withTZ) {
                    p = p.withOffsetParsed();
                }
                DateTime time = p.parseDateTime(lex);
                return fromDateTime(time, typeURI, withTZ);
            } catch(Exception e) {
                // Ignore and loop round to try the next pattern
            }
        }
        return new ValueNull();
    }
    
    protected static Value fromDateTime(DateTime time, String typeURI, boolean withTZ) {
        String formatted;
        if (typeURI.equals(XSD.dateTime.getURI())) {
            if (time.getMillisOfSecond() == 0) {
                formatted = (withTZ ? DATETIME_TZ_FMT : DATETIME_FMT).print(time);
            } else {
                formatted = (withTZ ? DATETIME_TZ_FMT_MS : DATETIME_FMT_MS).print(time);
            }
        } else if (typeURI.equals(XSD.date.getURI())) {
            formatted = (withTZ ? DATE_TZ_FMT : DATE_FMT).print(time);
        } else if (typeURI.equals(XSD.time.getURI())) {
            if (time.getMillisOfSecond() == 0) {
                formatted = (withTZ ? TIME_TZ_FMT : TIME_FMT).print(time);
            } else {
                formatted = (withTZ ? TIME_TZ_FMT_MS : TIME_FMT_MS).print(time);
            }
        } else if (typeURI.equals(XSD.gYearMonth.getURI())) {
            formatted = GYEARMONTH_FMT.print(time);
        } else if (typeURI.equals(XSD.gYear.getURI())) {
            formatted = GYEAR_FMT.print(time);
        } else {
            throw new EpiException("Unrecognized datetime type");
        }
        if (formatted.endsWith("UTC")) {
            formatted = formatted.replace("UTC", "Z");
        }
        Node n = NodeFactory.createLiteral(formatted, TypeMapper.getInstance().getSafeTypeByName(typeURI));
        return new ValueDate(n);        
    }
    
    protected static Value fromDateTime(LocalDateTime time, String typeURI) {
        String formatted;
        if (typeURI.equals(XSD.dateTime.getURI())) {
            if (time.getMillisOfSecond() == 0) {
                formatted = DATETIME_FMT.print(time);
            } else {
                formatted = DATETIME_FMT_MS.print(time);
            }
        } else if (typeURI.equals(XSD.date.getURI())) {
            formatted = DATE_FMT.print(time);
        } else if (typeURI.equals(XSD.time.getURI())) {
            if (time.getMillisOfSecond() == 0) {
                formatted = TIME_FMT.print(time);
            } else {
                formatted =  TIME_FMT_MS.print(time);
            }
        } else if (typeURI.equals(XSD.gYearMonth.getURI())) {
            formatted = GYEARMONTH_FMT.print(time);
        } else if (typeURI.equals(XSD.gYear.getURI())) {
            formatted = GYEAR_FMT.print(time);
        } else {
            throw new EpiException("Unrecognized datetime type");
        }

        Node n = NodeFactory.createLiteral(formatted, TypeMapper.getInstance().getSafeTypeByName(typeURI));
        return new ValueDate(n);        
    }
    
    /**
     * Wrap string as an xsd date time type
     * @param lex The string to be wrapped
     * @param typeURI The URI for the date time type - can be one of xsd:dateTime, xsd:date, xsd:time, xsd:gYearMonth, xsd:gYear.
     * @return A ValueDate containing an RDF literal
     */
    public static Value parse(String lex, String typeURI) {
        Node node = NodeFactory.createLiteral(lex, TypeMapper.getInstance().getSafeTypeByName(typeURI));
        node.getLiteral().getValue(); // Checks well formed, throws exception if not
        return new ValueDate( node);
    }
    
    /**
     * Wrap string as an xsd dateTime, date, time, or gYearMonth
     */
    public static Value parse(String lex) {
        return new ValueDate(lex);
    }
    
    /**
     * Access the years part (if any) of the date
     */
    public Value getYear() {
        return new ValueNumber( getXSDDateTime().getYears() );
    }
    
    /**
     * Access the month part (if any) of the date
     */
    public Value getMonth() {
        return new ValueNumber( getXSDDateTime().getMonths() );
    }
    
    /**
     * Access the day part (if any) of the date
     */
    public Value getDay() {
        return new ValueNumber( getXSDDateTime().getDays() );
    }
    
    /**
     * Access the hours part (if any) of the datetime
     */
    public Value getHour() {
        return new ValueNumber( getXSDDateTime().getHours() );
    }
    
    /**
     * Access the minue part (if any) of the datetime
     */
    public Value getMinute() {
        return new ValueNumber( getXSDDateTime().getMinutes() );
    }
    
    /**
     * Access the whole seconds part (if any) of the datetime
     */
    public Value getFullSecond() {
        return new ValueNumber( getXSDDateTime().getFullSeconds() );
    }
    
    /**
     * Access the seconds part (if any) of the datetime
     */
    public Value getSecond() {
        return new ValueNumber( getXSDDateTime().getSeconds() );
    }

    /**
     * Inject triples from the reference time service describing 
     * this date into the template output stream as a side effect.
     * Returns the date as a ref time URL
     */
    public Value referenceTime() {
        return new ValueNode( getRefTime().getRepresentation() );
    }
    
    protected RefTimeRepresentation getRefTime() {
        if (reftime == null) {
            reftime = new RefTimeRepresentation(getXSDDateTime(), value.getLiteralDatatype());
            injectRefTimeTriples();
        }
        return reftime;
    }
    
    protected void injectRefTimeTriples() {
        StreamRDF out = ConverterProcess.get().getOutputStream();
        ExtendedIterator<Triple> it = getRefTime().getModel().getGraph().find(null, null, null);
        while (it.hasNext()) {
            out.triple(it.next());
        }
    }
    
    public ValueNumber diffMilliSeconds(ValueDate other) {
    	long difference  = other.getJDateTime().getMillis()-this.getJDateTime().getMillis();
    	return new ValueNumber(difference) ;
    }

    public ValueNumber diffWholeDays(ValueDate other) {
    	long difference = Days.daysBetween(this.getJDateTime().toLocalDate(), other.getJDateTime().toLocalDate()).getDays();  
      	return new ValueNumber(difference) ;
    }
    
    /**
     * Inject triples from the reference time service describing 
     * this date into the template output stream as a side effect.
     * Returns the week for this date as a ref time URL
     */
    public Value referenceTimeWeek() {
        return new ValueNode( getRefTime().getWeek() );
    }
    
    protected XSDDateTime getXSDDateTime() {
        Object val = value.getLiteralValue();
        if (val instanceof XSDDateTime) {
            return (XSDDateTime)val;
        } else {
            throw new EpiException("Not a date/time node");
        }
    }
    
    // Warning: jDateTime doesn't support timezone less times so all times will appear as UTC
    protected DateTime getJDateTime() {
        XSDDateTime xdt = getXSDDateTime();
        if (getDatatype().equals(XSD.dateTime.getURI())) {
            int ms = (int)Math.round( 1000.0 * (xdt.getSeconds() - xdt.getFullSeconds()) );
            DateTime jdt =  
                    new DateTime(xdt.getYears(), xdt.getMonths(), xdt.getDays(), xdt.getHours(), xdt.getMinutes(), xdt.getFullSeconds(), ms, DateTimeZone.UTC);
            return jdt;
        } else if (getDatatype().equals(XSD.date.getURI())) {
            return new DateTime(xdt.getYears(), xdt.getMonths(), xdt.getDays(), 0, 0);
        } else if (getDatatype().equals(XSD.gYearMonth.getURI())) {
            return new DateTime(xdt.getYears(), xdt.getMonths(), 0, 0, 0);
        } else if (getDatatype().equals(XSD.gYear.getURI())) {
            return new DateTime(xdt.getYears(), 1, 0, 0, 0);
        } else {
            throw new EpiException("Unsupport date time format: " + getDatatype());
        }
    }
    
    
    protected boolean hasTimezone() {
        return getXSDDateTime().toString().endsWith("Z");
    }
    
    public Value plusYearDays(int years, int days) {
        DateTime dt = getJDateTime();
        dt = dt.plusDays(days).plusYears(years);
        return fromDateTime(dt, value.getLiteralDatatypeURI(), hasTimezone());
    }
    
    public Value minusYearDays(int years, int days) {
        DateTime dt = getJDateTime();
        dt = dt.minusDays(days).minusYears(years);
        return fromDateTime(dt, value.getLiteralDatatypeURI(), hasTimezone());
    }
    
    public Value plus(int hours, int minutes, int seconds) {
        DateTime dt = getJDateTime();
        dt = dt.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        return fromDateTime(dt, value.getLiteralDatatypeURI(), hasTimezone());
    }
    
    public Value minus(int hours, int minutes, int seconds) {
        DateTime dt = getJDateTime();
        dt = dt.minusHours(hours).minusMinutes(minutes).minusSeconds(seconds);
        return fromDateTime(dt, value.getLiteralDatatypeURI(), hasTimezone());
    }
    
    public Value toWholeSeconds() {
        DateTime dt = getJDateTime();
        dt = dt.minusMillis( dt.getMillisOfSecond() );
        return fromDateTime(dt, value.getLiteralDatatypeURI(), hasTimezone());
    }
    
    public Value toLocalTime() {
        if (hasTimezone()) {
            DateTime jdt = getJDateTime();
            jdt = jdt.withZone( DateTimeZone.getDefault() );
            return fromDateTime(jdt, value.getLiteralDatatypeURI(), false);
        } else {
            return this;
        }
    }
    
    @Override
    public Object format(String format) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
        String result = null;
        if (value.getLiteralDatatypeURI().equals(XSD.time.getURI())) {
            // Times are not datetimes and have to treated separately
            XSDDateTime xdt = getXSDDateTime();
            int ms = (int)Math.round( 1000.0 * (xdt.getSeconds() - xdt.getFullSeconds()) );
            LocalTime time = new LocalTime(xdt.getHours(), xdt.getMinutes(), xdt.getFullSeconds(), ms);
            result = formatter.print(time);
        } else {
            DateTime jdt = getJDateTime();
            result = hasTimezone() ? formatter.print(jdt) : formatter.print(jdt.toLocalDateTime());
        }
        return new ValueString(result);
    }
    
    public class RefTimeRepresentation {
        XSDDateTime time;
        BritishCalendar bcal = null;
        Model model = ModelFactory.createDefaultModel();
        
        Node week;
        Node ref = null;
        
        public RefTimeRepresentation(XSDDateTime time, RDFDatatype type) {
            this.time = time;
            int i_woy_year = 0;
            int i_woy_week = 0;
            
            if (type.equals(XSDDatatype.XSDdateTime)) {
                bcal = new BritishCalendar(
                        time.getYears(), time.getMonths()-1, time.getDays(), 
                        time.getHours(), time.getMinutes(), time.getFullSeconds() );
                ref = NodeFactory.createURI("http://reference.data.gov.uk/id/gregorian-instant/" + value.getLiteralLexicalForm());
                i_woy_year = CalendarUtils.getWeekOfYearYear(bcal);
                i_woy_week = bcal.get(Calendar.WEEK_OF_YEAR);
                new CalendarInstant(model, bcal, true);       
                new CalendarDay(model, bcal, true);
                new CalendarWeek(model, i_woy_year, i_woy_week, false, false);
                
            } else if (type.equals(XSDDatatype.XSDdate)) {
                bcal = new BritishCalendar(
                        time.getYears(), time.getMonths()-1, time.getDays()); 
                ref = NodeFactory.createURI("http://reference.data.gov.uk/id/day/" + value.getLiteralLexicalForm());
                i_woy_year = CalendarUtils.getWeekOfYearYear(bcal);
                i_woy_week = bcal.get(Calendar.WEEK_OF_YEAR);
                new CalendarDay(model, bcal, true);
                new CalendarWeek(model, i_woy_year, i_woy_week, false, false);
                
            } else {
                ref = NodeFactory.createURI("http://reference.data.gov.uk/id/year/" + time.getYears());
            }
            
            week = NodeFactory.createURI("http://reference.data.gov.uk/id/week/"
                    + String.format("%04d", i_woy_year) 
                    + "-W" + String.format("%02d", i_woy_week));
            
            new CalendarYear(model, time.getYears(), false, false);
        }
        
        public Model getModel() {
            return model;
        }
        
        public Node getRepresentation() {
            return ref;
        }
        
        public Node getWeek() {
            return week;
        }
        
    }
}
