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
        assertEquals(XSD.xstring.getURI(), ValueFactory.asValue("foobar", null).datatype());
        assertEquals(XSD.integer.getURI(), ValueFactory.asValue("123", null).datatype());
        assertEquals(XSD.decimal.getURI(), ValueFactory.asValue("123.4", null).datatype());
        
        assertEquals(XSD.dateTime.getURI(), ValueFactory.asValue("2002-10-10T12:00:00-05:00", null).datatype());
        assertEquals(XSD.dateTime.getURI(), ValueFactory.asValue("2002-10-10T12:00:00", null).datatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00-05:00", null).datatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00Z", null).datatype());
        assertEquals(XSD.time.getURI(), ValueFactory.asValue("12:00:00", null).datatype());
        assertEquals(XSD.date.getURI(), ValueFactory.asValue("2002-10-10-05:00", null).datatype());
        assertEquals(XSD.date.getURI(), ValueFactory.asValue("2002-10-10", null).datatype());
        assertEquals(XSD.gYearMonth.getURI(), ValueFactory.asValue("2002-10", null).datatype());
    }
    
    private boolean matches(Pattern p, String s) {
        return p.matcher(s).matches();
    }
    
}
