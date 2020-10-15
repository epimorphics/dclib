/******************************************************************
 * File:        TestValueArray.java   
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  15 Oct 2020
 * 
 * (c) Copyright 2020, Epimorphics Limited
 *  *
 *****************************************************************/
package com.epimorphics.dclib.values;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.jena.util.FileManager;
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
public class TestValueArray {
	
	DataContext dc = new DataContext();
	BindingEnv env;
	ConverterProcess proc;
	
	public TestValueArray() {
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
	public void testCreateFromArrayAndCollection() {
		// Make sure the result model is empty before we start.
		assertFalse(proc.getModel().listStatements().hasNext());
		//Use split to create a ValueArray and then use .value to reach the underlying array
		Value  t = (Value) eval( "{ { var res = [ 'a', 'b', 'c', 'd', 'e' ]; return (value(res)) } } ");
		Value  u = (Value) eval( "{ { var res = [ 6, 7, 8, 9, 10]; return (value(res)) } } ");
		Value  v = (Value) eval( "{value( value('one|two|three|4|rdfs:label').split('\\|').value ) }" );
		Value  w = (Value) eval( "{ { var res=({1:1}) ;\n"   // make a map
				                + "    res.clear()     ;\n"  // empty it
				                + "    res.put(1,1)    ;\n"  // add some entires
				                + "    res.put(2,2)    ;\n"
				                + "    res.put(3,3)    ;\n"
				                + "    res.put(4,4)    ;\n"
				                + "    res.put(5,5)    ;\n"
				                + "    return value(res.keySet()) } }" ) ;  // return it's key set.
		
		String[] t_s = {"a", "b", "c", "d", "e"} ;
	    ValueArray t_= new ValueArray(  t_s ) ;
	    
	    assertTrue("Failed to match array of strings",     Arrays.equals( (Value[])t.getValue(), (Value [])t_.getValue() ) ) ;
	    assertTrue("Failed to match integers", 
 		                ((ValueArray)u).get(0).equals(value(6)) &&
	                    ((ValueArray)u).get(1).equals(value(7)) &&
                        ((ValueArray)u).get(2).equals(value(8)) &&
                        ((ValueArray)u).get(3).equals(value(9)) &&
                        ((ValueArray)u).get(4).equals(value(10))
                  ) ;
	    assertTrue("Failed to match values from split", 		                
	    		        ((ValueArray)v).get(0).equals(value("one")) &&
                        ((ValueArray)v).get(1).equals(value("two")) &&
                        ((ValueArray)v).get(2).equals(value("three")) &&
                        ((ValueArray)v).get(3).equals(value("4")) &&
                        ((ValueArray)v).get(4).equals(value("rdfs:label"))
                );
	    assertTrue("Failed to match values from collection" , 
	                    ((ValueArray)w).get(0).equals(value(1)) &&
                        ((ValueArray)w).get(1).equals(value(2)) &&
                        ((ValueArray)w).get(2).equals(value(3)) &&
                        ((ValueArray)w).get(3).equals(value(4)) &&
                        ((ValueArray)w).get(4).equals(value(5))
          ) ;
	    
	}
	
	private Object eval(String pattern) {
		return proc.evaluate(new Pattern(pattern, dc), env, 0);
	}
	
	private Value value(Object o) {
		return (Value) GlobalFunctions.value(o);
	}
		
}
