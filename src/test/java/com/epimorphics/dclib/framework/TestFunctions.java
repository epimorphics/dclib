/******************************************************************
 * File:        TestFunctions.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.epimorphics.dclib.values.Row;
import com.epimorphics.dclib.values.Value;
import com.epimorphics.dclib.values.ValueArray;
import com.epimorphics.dclib.values.ValueFactory;
import com.epimorphics.dclib.values.ValueNode;

import org.apache.jena.graph.Node;

public class TestFunctions {

    @Test
    public void testRow() {
        Row row = new Row(42);
        assertEquals(42, row.getNumber());
        assertTrue( row.getBnode().asNode().isBlank() );
        Node a1 = row.bnodeFor("a").asNode();
        Node a2 = row.bnodeFor("a").asNode();
        Node b1 = row.bnodeFor("b").asNode();
        assertEquals(a1, a2);
        assertNotSame(a1, b1);
        assertTrue( a1.isBlank() );
        assertNotNull( row.getUuid() );
    }
    
    @Test public void testGlobalNode() {
    	Object a1 = eval("void","{bnodeFor('abc')}");
    	Object b1 = eval("void","{bnodeFor('bcd')}");
    	Object a2 = eval("void","{bnodeFor('abc')}");
    	
    	assertEquals(ValueNode.class, a1.getClass());
    	
    	assert(((ValueNode) a1).isAnon());
    	assert(((ValueNode) a2).isAnon());
    	assert(((ValueNode) b1).isAnon());
 
 /* a1 and a2 happen to be different ValueNode objects that wrap a common blank Node object. 
  * They don't have to be. They could be the same object. So testing for equality rather
  * than identity.
  */
    	assertEquals(a1, a2);
    	assertNotEquals(a1, b1);
    	assertNotEquals(a2, b1);
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
        Value[] values = ((ValueArray)eval("a,b,c", "stub-{x.split(',')}")).getValues();
        assertEquals("stub-a", values[0].toString());
        assertEquals("stub-b", values[1].toString());
        assertEquals("stub-c", values[2].toString());
        assertEquals("12", eval("UK012", "{x.regex('UK0?([0-9]*)')}").toString() );
        assertTrue( (Boolean)eval("UK012", "{x.matches('UK0?([0-9]*)')}") );
        assertFalse( (Boolean)eval("UK0a", "{x.matches('UK0?([0-9]*)')}") );
        assertEquals("12", eval("UK12", "{x.substring(2)}").toString() );
        assertEquals("baz", eval("http://foo/bar/baz", "{x.lastSegment()}").toString() );
        assertEquals("baz", eval("http://foo/bar#baz", "{x.lastSegment()}").toString() );
    }
    
    private Object eval(String value, String pattern) {
        DataContext dc = new DataContext();
        ConverterProcess proc = new ConverterProcess(dc, null);
        BindingEnv env = new BindingEnv();
        env.put("x", ValueFactory.asValue(value));
        Object result = new Pattern(pattern, dc).evaluate(env, proc, 0);
        return result;
    }
}
