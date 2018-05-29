/******************************************************************
 * File:        TestValueNode.java   
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  29 May 2018
 * 
 * (c) Copyright 2018, Epimorphics Limited
 *  *
 *****************************************************************/
package com.epimorphics.dclib.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Pattern;

/**
 * @author skw
 *
 */
public class TestValueNode {
	
	DataContext dc = new DataContext();
	BindingEnv env;
	ConverterProcess proc;
	
	public TestValueNode() {
	}
	
	@Before
	public void setUp() {
		// Need to set prefixes in DC *before* creating the converter process.
		dc.setPrefixes( FileManager.get().loadModel("prefixes.ttl") );
		proc = new ConverterProcess(dc, null);
		env = new BindingEnv();
		env.set("u", ValueFactory.asValue("http://example.com/foo"));
	}
	
	@Test
	public void testAddProperty() {
		// Make sure the result model is empty before we start.
		assertFalse(proc.getModel().listStatements().hasNext());
		Resource r = (Resource) eval(
				  "{"
				+ "u.asRDFNode()"
				+ ".addPropertyValue(       'rdfs:label',                         'This is a test@en')"									 // String,String
				+ ".addPropertyValue(       'dct:created',                        value('2018-05-29T12:24:00Z').asDate('xsd:dateTime'))" // String, ValueDate
				+ ".addPropertyValue(       'dct:hasVersion',                     value('10.5').asDecimal())"                          	 // String, ValueNumber
				+ ".addObjectPropertyValue( 'dct:isVersionOf',                   'http://example.com/bar')"                   		     // String, String
				+ ".addObjectPropertyValue( value('dct:isVersionOf').asRDFNode(), value('http://example.com/bar2'))"                     // ValueNode, ValueNode
				+ ".asResource()"
				+ "}"
			    );
	
		assertTrue("Missing or unexpected rdfs:label value",                 proc.getModel().contains(r, RDFS.label,proc.getModel().createLiteral("This is a test","en")));
		assertTrue("Missing or unexpected dct:created value",                proc.getModel().contains(r, DCTerms.created,proc.getModel().createTypedLiteral("2018-05-29T12:24:00Z",XSDDatatype.XSDdateTime)));
		assertTrue("Missing or unexpected dct:hasVersion (decimal) value",   proc.getModel().contains(r, DCTerms.hasVersion,proc.getModel().createTypedLiteral(BigDecimal.valueOf(10.5))));
		assertTrue("Missing or unexpected dct:isVersionOf (resource) value", proc.getModel().contains(r, DCTerms.isVersionOf, proc.getModel().getResource("http://example.com/bar")));
		assertTrue("Missing or unexpected dct:isVersionOf (resource) value", proc.getModel().contains(r, DCTerms.isVersionOf, proc.getModel().getResource("http://example.com/bar2")));		
	}
	
	private Object eval(String pattern) {
		return proc.evaluate(new Pattern(pattern, dc), env, 0);
	}
	
	private Node evalNode(String pattern) {
		return proc.evaluateAsNode(new Pattern(pattern, dc), env, 0);
	}
	
}
