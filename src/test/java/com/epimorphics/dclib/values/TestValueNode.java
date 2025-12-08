/******************************************************************
 * File:        TestValueNode.java   
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  29 May 2018
 * 
 * (c) Copyright 2018, Epimorphics Limited
 *  *
 *****************************************************************/
package com.epimorphics.dclib.values;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	
	@BeforeEach
	public void setUp() {
		// Need to set prefixes in DC *before* creating the converter process.
		dc.setPrefixes( RDFDataMgr.loadModel("prefixes.ttl") );
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
				+ ".addPropertyValue(        'rdfs:label',                        value('This is also a test').lang('en'))"              // String, Node 
				+ ".asResource()"
				+ "}"
			    );
	
		assertTrue(proc.getModel().contains(r, RDFS.label,proc.getModel().createLiteral("This is a test","en")), "Missing or unexpected rdfs:label value");
		assertTrue(proc.getModel().contains(r, DCTerms.created,proc.getModel().createTypedLiteral("2018-05-29T12:24:00Z",XSDDatatype.XSDdateTime)), "Missing or unexpected dct:created value");
		assertTrue(proc.getModel().contains(r, DCTerms.hasVersion,proc.getModel().createTypedLiteral(BigDecimal.valueOf(10.5))), "Missing or unexpected dct:hasVersion (decimal) value");
		assertTrue(proc.getModel().contains(r, DCTerms.isVersionOf, proc.getModel().getResource("http://example.com/bar")), "Missing or unexpected dct:isVersionOf (resource) value");
		assertTrue(proc.getModel().contains(r, DCTerms.isVersionOf, proc.getModel().getResource("http://example.com/bar2")), "Missing or unexpected dct:isVersionOf (resource) value");
		assertTrue(proc.getModel().contains(r, RDFS.label, proc.getModel().createLiteral("This is also a test","en")), "Missing or unexpected rdfs:label value");
		
	}
	
	private Object eval(String pattern) {
		return proc.evaluate(new Pattern(pattern, dc), env, 0);
	}
	
	private Node evalNode(String pattern) {
		return proc.evaluateAsNode(new Pattern(pattern, dc), env, 0);
	}
	
}
