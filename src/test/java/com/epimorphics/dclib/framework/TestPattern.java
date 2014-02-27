/******************************************************************
 * File:        TestPattern.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import org.junit.Test;

import com.epimorphics.dclib.values.ValueFactory;
import com.epimorphics.dclib.values.ValueStringArray;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import static org.junit.Assert.*;

public class TestPattern {
    BindingEnv env = new BindingEnv();
    DataContext dc = new DataContext();
    ConverterProcess proc = new ConverterProcess(dc, null);

    public TestPattern() {
        env.set("a", ValueFactory.asValue("a string", proc));
        env.set("b", ValueFactory.asValue("foo bar", proc));
        env.set("i", ValueFactory.asValue("42", proc));
        env.set("j", ValueFactory.asValue("042", proc));
        env.set("big", ValueFactory.asValue("05501000000000000000000000000000", proc));
        env.set("A", ValueFactory.asValue("A String", proc));
        
        dc.setPrefixes( FileManager.get().loadModel("prefixes.ttl") );
    }
    
    @Test
    public void testURIFixedPatterns() {
        Pattern p = new Pattern("<rdf:type>", dc);
        assertTrue(p.isURI());
        assertFalse(p.isInverse());
        
        p = new Pattern("^<rdf:type>", dc);
        assertTrue(p.isURI());
        assertTrue(p.isInverse());
        
        assertEquals(RDF.type.getURI(), eval("<rdf:type>"));
        assertEquals(RDF.type.getURI(), eval("^<rdf:type>"));
    }
    
    @Test
    public void testSimpleSubstitution() {
        assertEquals("this is a string", eval("this is {a}").toString());
        assertEquals("this is 42", eval("this is {i}").toString());
        assertEquals("this is 40", eval("this is {i.value - 2}").toString());
        assertEquals("bar = 42", eval("{b.value.split(' ').1} = {i}").toString());
        assertEquals("big", eval("{i.value > 10 ? 'big' : 'little'}"));
    }
    
    @Test
    public void testMultiValues() {
        Object result = eval("prefix:{b.split(' ')}");
        assertTrue(result instanceof ValueStringArray);
        Object[] ans = ((ValueStringArray)result).getValues();
        assertEquals("prefix:foo", ans[0]);
        assertEquals("prefix:bar", ans[1]);
    }
    
    @Test
    public void testScripts() {
        assertEquals("foo bar", eval("{={a;b}}").toString());
    }
    
    @Test
    public void testBigNum() {
        assertEquals("Number 05501000000000000000000000000000", eval("Number {big}").toString());
    }
    
    @Test
    public void testConversions() {
        assertEquals("42", eval("{j.value}").toString());
        assertEquals("43", eval("{j.value + 1}").toString());
        assertEquals("042", eval("{j}").toString());
        assertEquals("042", eval("{j.asString()}").toString());
        assertEquals("42", eval("{i.asString().toSegment()}").toString());
        assertEquals("42", eval("{i.toSegment()}").toString());
        assertEquals("43", eval("{j.asString().asNumber().value + 1}").toString());
        assertEquals("a_string", eval("{A.toSegment().toLowerCase()}").toString());
        assertEquals("A_String", eval("{A.toSegment().toSegment()}").toString());
        assertEquals("a_string", eval("{A.asString().toSegment().toLowerCase()}").toString());
        
//        assertEquals("", eval("{a}").toString());
    }
    
    private Object eval(String pattern) {
        return new Pattern(pattern, dc).evaluate(env, proc);
    }
}
