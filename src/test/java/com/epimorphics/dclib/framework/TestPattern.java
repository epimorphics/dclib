/******************************************************************
 * File:        TestPattern.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.epimorphics.dclib.values.Value;
import com.epimorphics.dclib.values.ValueArray;
import com.epimorphics.dclib.values.ValueFactory;
import com.epimorphics.dclib.values.ValueNumber;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestPattern {
    DataContext dc = new DataContext();
    BindingEnv env;
    ConverterProcess proc;

    public TestPattern() {
    }
    
    @Before
    public void setUp() {
        proc = new ConverterProcess(dc, null);
        
        env = new BindingEnv();
        env.set("a", ValueFactory.asValue("a string"));
        env.set("a2", ValueFactory.asValue("a string"));
        env.set("b", ValueFactory.asValue("foo bar"));
        env.set("i", ValueFactory.asValue("42"));
        env.set("j", ValueFactory.asValue("042"));
        env.set("big", ValueFactory.asValue("05501000000000000000000000000000"));
        env.set("A", ValueFactory.asValue("A String"));
        env.set("u", ValueFactory.asValue("http://example.com/foo"));
        env.set("t", ValueFactory.asValue("true"));
        env.set("p", ValueFactory.asValue("G (A)"));
        env.set("q", ValueFactory.asValue("foo's - bar __ baz()"));
        env.set("m", ValueFactory.asValue("Fo o,Bar "));
        env.set("f", ValueFactory.asValue("12.7"));
        
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
        
        assertEquals(RDF.type.getURI(), eval("<rdf:type>").toString());
        assertEquals(RDF.type.getURI(), eval("^<rdf:type>").toString());
        
        p = new Pattern("\\<rdf:type>", dc);
        assertFalse(p.isURI());
        assertFalse(p.isInverse());
        assertEquals("<rdf:type>", p.evaluate(env, proc, 0));
        
        assertEquals("^<rdf:type>", eval("\\^<rdf:type>"));
    }
    
    @Test
    public void testSimpleSubstitution() {
        assertEquals("this is a string", eval("this is {a}").toString());
        assertEquals("this is 42", eval("this is {i}").toString());
        assertEquals("this is 40", eval("this is {i.value - 2}").toString());
        assertEquals("bar = 42", eval("{b.value.split(' ').1} = {i}").toString());
        assertEquals("big", eval("{i.value > 10 ? 'big' : 'little'}").toString());
        
        assertEquals("foo barbaz", eval("{b.toString() + 'baz'}").toString());
        
        assertEquals("foo \\ bar", eval("foo \\ bar"));
    }
        
    @Test
    public void testMultiValues() {
        Object result = eval("prefix:{b.split(' ')}");
        checkArray(result, "prefix:foo", "prefix:bar");
        assertEquals("foo", eval("{b.split(' ').0}").toString());
        checkArray( eval("{b.split(' ').substring(1)}"), "oo", "ar");
        checkArray( eval("{m.split(',').trim()}"), "Fo o", "Bar");
        checkArray( eval("{m.split(',')}"), "Fo o", "Bar ");
        checkArray( eval("{m.split(',').trim().toUpperCase()}"), "FO O", "BAR");
        checkArray( eval("{m.split(',').trim().toLowerCase()}"), "fo o", "bar");
        checkArray( eval("{m.split(',').toCleanSegment()}"), "fo-o", "bar");
        checkArray( eval("{m.split(',').replaceAll('[oa]','z')}"), "Fz z", "Bzr ");
        checkArray( eval("{b.split(' ').append(i)}"), "foo42", "bar42");
        checkArray( eval("{i.append(b.split(' '))}"), "42foo", "42bar");
    }
    
    protected void checkArray(Object result, String...expected) {
        assertTrue(result instanceof ValueArray);
        checkArray((ValueArray)result, expected);
    }
    
    protected void checkArray(ValueArray result, String...expected) {
        Value[] ans = result.getValues();
        assertEquals(expected.length, ans.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], ans[i].toString());
        }
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
    public void testFunctions() {
        assertEquals(13, ((ValueNumber)eval("{round(f)}")).toNumber().longValue());
        assertEquals(12, ((ValueNumber)eval("{round(f.value - 0.3)}")).toNumber().longValue());
        assertEquals("013", eval("{round(f).format('%03d')}").toString());
        
        assertEquals(5, ((ValueNumber)eval("{value(1+4)}")).toNumber().longValue());
        assertEquals("1.2", eval("{value(1.23).format('%2.1f')}").toString());
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
        assertEquals("A-String", eval("{A.toSegment('-')}").toString());
        assertEquals("a_string", eval("{A.asString().toSegment().toLowerCase()}").toString());
        assertEquals("a_string", eval("{A.asString().toSegment() \n\r .toLowerCase()}").toString());
        assertEquals("fzz bar", eval("{b.replaceAll('o','z')}").toString());
        assertEquals("zzz bar", eval("{b.replaceAll('[fo]','z')}").toString());
        assertEquals("yes", eval("{a == a2 ? 'yes' : 'no'}").toString());
        assertEquals("g-a", eval("{p.toCleanSegment()}").toString());
        assertEquals("foos-bar-baz", eval("{q.toCleanSegment()}").toString());
    }
    
    private Object eval(String pattern) {
        return proc.evaluate(new Pattern(pattern, dc), env, 0);
    }
    
    @Test
    public void testNodeValues() {
        assertEquals(NodeFactory.createURI("http://example.com/foo"), evalNode("<{u}>"));
        assertEquals(NodeFactory.createLiteral("42", XSDDatatype.XSDinteger), evalNode("{i}"));
        assertEquals(NodeFactory.createLiteral("true", XSDDatatype.XSDboolean), evalNode("{true}"));
        assertEquals(NodeFactory.createLiteral("false", XSDDatatype.XSDboolean), evalNode("{false}"));
        assertEquals(NodeFactory.createLiteral("true", XSDDatatype.XSDboolean), evalNode("{t.asBoolean()}"));
        
        assertEquals(NodeFactory.createLiteral("42", XSDDatatype.XSDshort), evalNode("{i.datatype('xsd:short')}"));
        assertEquals(NodeFactory.createLiteral("42", XSDDatatype.XSDstring), evalNode("{i.datatype('xsd:string')}"));
        assertEquals(NodeFactory.createLiteral("42"), evalNode("{i.asString()}"));
        
        assertEquals(NodeFactory.createLiteral("a string", "en", false), evalNode("{a.lang('en')}"));
        
        assertEquals(NodeFactory.createLiteral("foo bar", "en", false), evalNode("{b}@en") );
        assertEquals(NodeFactory.createLiteral("foo bar@en"), evalNode("{b}@@en") );
        assertEquals(NodeFactory.createLiteral("foo bar@en"), evalNode("{b}\\@en") );
        
        assertEquals(NodeFactory.createLiteral("foo bar", XSDDatatype.XSDstring), evalNode("{b}^^xsd:string"));
        assertEquals(NodeFactory.createLiteral("foo bar^^xsd:string"), evalNode("{b}\\^^xsd:string"));
        assertEquals(NodeFactory.createLiteral("foo bar", XSDDatatype.XSDstring), evalNode("{b}^^http://www.w3.org/2001/XMLSchema#string"));
        assertEquals(NodeFactory.createLiteral("foo bar", XSDDatatype.XSDstring), evalNode("{b}^^<http://www.w3.org/2001/XMLSchema#string>"));
    }
  
    @Test
    public void testFormatting() {
       assertEquals("00042", eval("{i.format('%05d')}").toString());
    }
    
    @Test
    public void testErrorInConversion() {
        eval("{a.asNumber()}");
        assertFalse( proc.getMessageReporter().succeeded() );
    }
    
    private Node evalNode(String pattern) {
        return proc.evaluateAsNode(new Pattern(pattern, dc), env, 0);
    }
}
