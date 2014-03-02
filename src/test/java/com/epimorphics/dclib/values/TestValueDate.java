/******************************************************************
 * File:        TestValueDate.java
 * Created by:  Dave Reynolds
 * Created on:  27 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.util.regex.Pattern;

import org.junit.Test;

import com.hp.hpl.jena.vocabulary.XSD;

import static org.junit.Assert.*;

public class TestValueDate {

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
        assertEquals(XSD.xstring.getURI(), ValueFactory.asValue("foobar", null).getDatatype());
        assertEquals(XSD.integer.getURI(), ValueFactory.asValue("123", null).getDatatype());
        assertEquals(XSD.decimal.getURI(), ValueFactory.asValue("123.4", null).getDatatype());
        
        assertEquals(XSD.dateTime.getURI(), ValueFactory.asValue("2002-10-10T12:00:00-05:00", null).getDatatype());
        assertEquals(XSD.dateTime.getURI(), ValueFactory.asValue("2002-10-10T12:00:00", null).getDatatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00-05:00", null).getDatatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00Z", null).getDatatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00", null).getDatatype());
        assertEquals(XSD.date.getURI(), ValueFactory.asValue("2002-10-10-05:00", null).getDatatype());
        assertEquals(XSD.date.getURI(), ValueFactory.asValue("2002-10-10", null).getDatatype());
        assertEquals(XSD.gYearMonth.getURI(), ValueFactory.asValue("2002-10", null).getDatatype());
    }
    
    private boolean matches(Pattern p, String s) {
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
        
        doTestCoercion("12 59 03", "HH mm ss", XSD.time.getURI(), "12:59:03.0");
        doTestCoercion("12 59", "HH mm", XSD.time.getURI(), "12:59:00.0");
        doTestCoercion("12 59 03 +0600", "HH mm ss Z", XSD.time.getURI(), "12:59:03.0+06:00");
        
        doTestCoercion("02 03 2014 15-34-03", "dd MM yyyy HH-mm-ss", XSD.dateTime.getURI(), "2014-03-02T15:34:03.0");
        doTestCoercion("02 03 2014 15-34-03 +0100", "dd MM yyyy HH-mm-ss Z", XSD.dateTime.getURI(), "2014-03-02T15:34:03.0+01:00");
        doTestCoercion("02 03 2014 15-34-03 Z", "dd MM yyyy HH-mm-ss Z", XSD.dateTime.getURI(), "2014-03-02T15:34:03.0Z");
    }
    
    private void doTestCoercion(String src, String typeURI, String expected) {
        Value v = ValueFactory.asValue(src, null);
        assertTrue(v instanceof ValueString || v instanceof ValueNumber);
        Value conv = (v instanceof ValueString) ? ((ValueString)v).asDate(typeURI) : ((ValueNumber)v).asDate(typeURI);
        assertTrue(conv instanceof ValueDate);
        assertEquals(typeURI, conv.getDatatype());
        assertEquals(expected, conv.toString());
    }
    
    private void doTestCoercion(String src, String format, String typeURI, String expected) {
        Value v = ValueFactory.asValue(src, null);
        assertTrue(v instanceof ValueString || v instanceof ValueNumber);
        Value conv = (v instanceof ValueString) ? ((ValueString)v).asDate(format, typeURI) : ((ValueNumber)v).asDate(format, typeURI);
        assertTrue(conv instanceof ValueDate);
        assertEquals(typeURI, conv.getDatatype());
        assertEquals(expected, conv.toString());
    }
    
}
