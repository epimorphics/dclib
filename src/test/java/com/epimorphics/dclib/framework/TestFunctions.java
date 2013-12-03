/******************************************************************
 * File:        TestFunctions.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.IOException;

import org.junit.Test;

import com.epimorphics.dclib.values.Row;
import com.epimorphics.dclib.values.ValueFactory;
import com.epimorphics.dclib.values.ValueStringArray;
import com.hp.hpl.jena.graph.Node;

import static org.junit.Assert.*;

public class TestFunctions {

    @Test
    public void testRow() {
        Row row = new Row(42);
        assertEquals(42, row.getNumber());
        assertTrue( row.getBnode().isBlank() );
        Node a1 = row.bnodeFor("a");
        Node a2 = row.bnodeFor("a");
        Node b1 = row.bnodeFor("b");
        assertEquals(a1, a2);
        assertNotSame(a1, b1);
        assertTrue( a1.isBlank() );
        assertNotNull( row.getUuid() );
    }
    
    @Test
    public void testRowVariable() throws IOException {
        TestBasicConverters.checkAgainstExpected("test/row-test.json", "test/test-ok.csv", "test/row-result.ttl");
    }
    
    @Test
    public void testStringOps() {
        assertEquals("lower", eval("LOWER", "{x.toLowerCase()}").toString());
        assertEquals("UPPER", eval("upper", "{x.toUpperCase()}").toString());
        assertEquals("This_is_a_foolish_-_pattern", eval("This    is a (foolish) - pattern", "{x.toSegment()}").toString());
        Object[] values = ((ValueStringArray)eval("a,b,c", "stub-{x.split(',')}")).getValues();
        assertEquals("stub-a", values[0]);
        assertEquals("stub-b", values[1]);
        assertEquals("stub-c", values[2]);
    }
    
    private Object eval(String value, String pattern) {
        DataContext dc = new DataContext();
        BindingEnv env = new BindingEnv();
        env.put("x", ValueFactory.asValue(value));
        return new Pattern(pattern, dc).evaluate(env);
    }
}
