/******************************************************************
 * File:        TestValueDate.java
 * Created by:  Dave Reynolds
 * Created on:  27 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.TestBasicConverters;
import com.epimorphics.rdfutil.RDFUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.XSD;

public class TestValueDate {
    DataContext dc = new DataContext();
    ConverterProcess proc;
    BindingEnv env;

    @Before
    public void setUp() {
        proc = new ConverterProcess(dc, null);
        dc.setPrefixes( FileManager.get().loadModel("prefixes.ttl") );
        env = new BindingEnv();
        env.put("date", ValueDate.parse("2014-09-11") );
        env.put("datetime", ValueDate.parse("2014-09-11T12:42:21.23") );
    }

    @Test
    public void testPatterns() {
        assertTrue( matches(ValueDate.DATETIME_PATTERN, "2002-10-10T12:00:00-05:00") );
        assertTrue( matches(ValueDate.DATETIME_PATTERN, "2002-10-10T12:00:00Z") );
        assertTrue( matches(ValueDate.DATETIME_PATTERN, "2002-10-10T12:00:00") );
        assertTrue( matches(ValueDate.DATETIME_PATTERN, "2002-10-10T12:00:00.1234") );
        
        assertFalse( matches(ValueDate.DATETIME_PATTERN, "2002") );
        assertFalse( matches(ValueDate.DATETIME_PATTERN, "foobar") );
        
        assertFalse( matches(ValueDate.DATE_PATTERN, "2002-10-10T12:00:00-05:00") );
        assertFalse( matches(ValueDate.DATE_PATTERN, "2002-10-10T12:00:00") );
        assertTrue( matches(ValueDate.DATE_PATTERN, "2002-10-10") );
        assertTrue( matches(ValueDate.DATE_PATTERN, "2002-10-10Z") );
        assertTrue( matches(ValueDate.DATE_PATTERN, "2002-10-10-05:00") );
        
        assertFalse( matches(ValueDate.TIME_PATTERN, "2002-10-10T12:00:00-05:00") );
        assertFalse( matches(ValueDate.TIME_PATTERN, "2002-10-10T12:00:00") );
        assertFalse( matches(ValueDate.TIME_PATTERN, "2002-10-10") );
        assertTrue( matches(ValueDate.TIME_PATTERN, "12:00:00-05:00") );
        assertTrue( matches(ValueDate.TIME_PATTERN, "12:00:00Z") );
        assertTrue( matches(ValueDate.TIME_PATTERN, "12:00:00") );
        
        assertFalse( matches(ValueDate.GYEARMONTH_PATTERN, "2002-10-10T12:00:00-05:00") );
        assertFalse( matches(ValueDate.GYEARMONTH_PATTERN, "2002-10-10T12:00:00") );
        assertFalse( matches(ValueDate.GYEARMONTH_PATTERN, "2002-10-10") );
        assertFalse( matches(ValueDate.GYEARMONTH_PATTERN, "12:00:00-05:00") );
        assertFalse( matches(ValueDate.GYEARMONTH_PATTERN, "12:00:00Z") );
        assertFalse( matches(ValueDate.GYEARMONTH_PATTERN, "12:00:00") );
        assertTrue( matches(ValueDate.GYEARMONTH_PATTERN, "2002-10") );
        assertTrue( matches(ValueDate.GYEARMONTH_PATTERN, "2002-10Z") );
        assertTrue( matches(ValueDate.GYEARMONTH_PATTERN, "2002-10-05:00") );
        
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "2002-10-10T12:00:00-05:00") );
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "2002-10-10T12:00:00") );
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "2002-10-10") );
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "12:00:00-05:00") );
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "12:00:00Z") );
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "12:00:00") );
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "2002-10") );
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "2002-10Z") );
        assertTrue( matches(ValueDate.ANYDATE_PATTERN, "2002-10-05:00") );
        
        assertFalse( matches(ValueDate.ANYDATE_PATTERN, "2002") );
        assertFalse( matches(ValueDate.ANYDATE_PATTERN, "foobar") );
    }
    
    @Test
    public void testFactory() {
        assertEquals(XSD.xstring.getURI(), ValueFactory.asValue("foobar").getDatatype());
        assertEquals(XSD.integer.getURI(), ValueFactory.asValue("123").getDatatype());
        assertEquals(XSD.decimal.getURI(), ValueFactory.asValue("123.4").getDatatype());
        
        assertEquals(XSD.dateTime.getURI(), ValueFactory.asValue("2002-10-10T12:00:00-05:00").getDatatype());
        assertEquals(XSD.dateTime.getURI(), ValueFactory.asValue("2002-10-10T12:00:00").getDatatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00-05:00").getDatatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00Z").getDatatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00").getDatatype());
        assertEquals(XSD.date.getURI(), ValueFactory.asValue("2002-10-10-05:00").getDatatype());
        assertEquals(XSD.date.getURI(), ValueFactory.asValue("2002-10-10").getDatatype());
        assertEquals(XSD.gYearMonth.getURI(), ValueFactory.asValue("2002-10").getDatatype());
    }
    
    private boolean matches(java.util.regex.Pattern p, String s) {
        return p.matcher(s).matches();
    }
    
    @Test
    public void testCoercions() {
        doTestCoercion("2014", XSD.gYear.getURI(), "2014");
        
        try {
            doTestCoercion("2014 03", XSD.gYearMonth.getURI(), "2014-03");
            assertTrue("Should have raised exception", false);
        } catch (Exception e) { }
        doTestCoercion("2014 03", "yyyy MM", XSD.gYearMonth.getURI(), "2014-03");
        doTestCoercion("2014 03", "yyyy-MM|yyyy MM", XSD.gYearMonth.getURI(), "2014-03");
        
        doTestCoercion("2014 02 03", "yyyy dd MM", XSD.date.getURI(), "2014-03-02");
        doTestCoercion("2014 02 03 -05:00", "yyyy dd MM Z", XSD.date.getURI(), "2014-03-02-05:00");
        
        doTestCoercion("12 59 03", "HH mm ss", XSD.time.getURI(), "12:59:03");
        doTestCoercion("12 59", "HH mm", XSD.time.getURI(), "12:59:00");
        doTestCoercion("12 59 03 +0600", "HH mm ss Z", XSD.time.getURI(), "12:59:03+06:00");
        
        doTestCoercion("02 03 2014 15-34-03", "dd MM yyyy HH-mm-ss", XSD.dateTime.getURI(), "2014-03-02T15:34:03");
        doTestCoercion("02 03 2014 15-34-03 +0100", "dd MM yyyy HH-mm-ss Z", XSD.dateTime.getURI(), "2014-03-02T15:34:03+01:00");
        doTestCoercion("02 03 2014 15-34-03 Z", "dd MM yyyy HH-mm-ss Z", XSD.dateTime.getURI(), "2014-03-02T15:34:03Z");
    }
    
    private void doTestCoercion(String src, String typeURI, String expected) {
        Value v = ValueFactory.asValue(src);
        assertTrue(v instanceof ValueString || v instanceof ValueNumber);
        Value conv = (v instanceof ValueString) ? ((ValueString)v).asDate(typeURI) : ((ValueNumber)v).asDate(typeURI);
        assertTrue(conv instanceof ValueDate);
        assertEquals(typeURI, conv.getDatatype());
        assertEquals(expected, conv.toString());
    }
    
    private void doTestCoercion(String src, String format, String typeURI, String expected) {
        Value v = ValueFactory.asValue(src);
        assertTrue(v instanceof ValueString || v instanceof ValueNumber);
        Value conv = (v instanceof ValueString) ? ((ValueString)v).asDate(format, typeURI) : ((ValueNumber)v).asDate(format, typeURI);
        assertTrue(conv instanceof ValueDate);
        assertEquals(typeURI, conv.getDatatype());
        assertEquals(expected, conv.toString());
    }
    
    
    @Test
    public void testAccessors() {
        assertEquals( 2014, evaluateInt("{date.year}") );
        assertEquals( 2014, evaluateInt("{datetime.year}") );
        assertEquals( 9, evaluateInt("{date.month}") );
        assertEquals( 9, evaluateInt("{datetime.month}") );
        assertEquals( 11, evaluateInt("{date.day}") );
        assertEquals( 11, evaluateInt("{datetime.day}") );
        
        assertEquals( 12, evaluateInt("{datetime.hour}") );
        assertEquals( 42, evaluateInt("{datetime.minute}") );
        assertEquals( 21, evaluateInt("{datetime.fullSecond}") );
        
        assertEquals(21.23,  ((ValueNumber)evaluate("{datetime.second}")).toNumber().doubleValue(), 0.001);
    }
    
    @Test
    public void testExecTime() throws IOException {
        Model m = TestBasicConverters.convert("test/dates/exectime.yaml", "test/dates/date.csv");
        Resource r = m.getResource("http://example.org/01");
        assertTrue(r.hasProperty(DCTerms.modified));
        long time = RDFUtil.asTimestamp( r.getRequiredProperty(DCTerms.modified).getObject() );
        long now = System.currentTimeMillis();
        assertTrue (time <= now && time >= now-1000);
    }
    
    protected void printValue(Object val) {
        System.out.println( String.format("'%s' of type %s", val.toString(), val.getClass().getName()));
    }
    
    protected Object evaluate(String pattern) {
         return proc.evaluate(new Pattern(pattern, dc), env, 0);
    }
    
    protected int evaluateInt(String pattern) {
         return ((ValueNumber)proc.evaluate(new Pattern(pattern, dc), env, 0)).toNumber().intValue();
    }
    
    @Test
    public void testJodaDateTimeRoundTrip() {
        doTestJodaDateTimeRoundTrip("2014-09-24T10:20:30", "2014-09-24T10:20:30");
        doTestJodaDateTimeRoundTrip("2014-09-24T10:20:30.123", "2014-09-24T10:20:30.123");
        doTestJodaDateTimeRoundTrip("2014-09-24T10:20:30Z", "2014-09-24T10:20:30Z");
        doTestJodaDateTimeRoundTrip("2014-09-24T10:20:30+05:00", "2014-09-24T05:20:30Z");
    }
    
    protected void doTestJodaDateTimeRoundTrip(String lex, String expected) {
        ValueDate value = new ValueDate(lex);
        DateTime dt = value.getJDateTime();
        Value result = ValueDate.fromDateTime(dt, value.asNode().getLiteralDatatypeURI(), value.hasTimezone());
        assertEquals(expected, result.toString());
    }

    @Test
    public void testDateArithmetic() {
        assertEquals("2014-09-24T12:02:33", new ValueDate("2014-09-24T10:20:30").plus(1, 42, 3).toString()); 
        assertEquals("2014-09-24T12:02:33Z", new ValueDate("2014-09-24T10:20:30Z").plus(1, 42, 3).toString()); 
        assertEquals("2015-10-02T10:20:30", new ValueDate("2014-09-24T10:20:30").plusYearDays(1, 8).toString());
        ValueDate v = (ValueDate) new ValueDate("2014-09-24T10:20:30").plus(24,0,0);
        v = (ValueDate) v.minus(0,0,1);
        assertEquals("2014-09-25T10:20:29", v.toString()); 
        
        assertEquals("2014-09-24T10:20:30", new ValueDate("2014-09-24T10:20:30.123").toWholeSeconds().toString());
        assertEquals("2014-09-24T10:20:30Z", new ValueDate("2014-09-24T10:20:30.123Z").toWholeSeconds().toString());
        
        assertEquals("2014-09-24T10:20:30", new ValueDate("2014-09-24T10:20:30").toLocalTime().toString());
        
        assertFalse( new ValueDate("2014-09-24T14:20:30Z").toLocalTime().toString().endsWith("Z") );
        
        assertEquals("2014-09-24-10-20-30", new ValueDate("2014-09-24T10:20:30").format("yyyy-MM-dd-HH-mm-ss").toString());
        assertEquals("2014-09-24-10-20-30", new ValueDate("2014-09-24T10:20:30Z").format("yyyy-MM-dd-HH-mm-ss").toString());
    }
}
