/******************************************************************
 * File:        GlobalFunctions.java
 * Created by:  Dave Reynolds
 * Created on:  16 May 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.dclib.framework.EvalFailed;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * A set of globabl functions that will be available in expressions.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class GlobalFunctions {
    static Logger log = LoggerFactory.getLogger( GlobalFunctions.class );

    public static Node lang(Object value, Object lang) {
        return NodeFactory.createLiteral(value.toString(), lang.toString(), null);
    }
    
    public static Node datatype(Object value, Object type) {
        String typeURI = type.toString();
        if (typeURI.startsWith("xsd:")) {
            typeURI = typeURI.replace("xsd:", XSD.getURI());
        }
        RDFDatatype typeR = TypeMapper.getInstance().getSafeTypeByName( typeURI );
        return NodeFactory.createLiteral(value.toString(), typeR);
    }
    
    public static Value nullValue() {
        return new ValueNull();
    }
    
    public static void abort() {
        throw new EvalFailed("Aborted at user request");
    }
    
    /** Debug aid - log the value  */
    public static Object print(Object value) {
        log.info("Value = " + value + " [" + value.getClass() + "]");
        return value;
    }
    
    /** Debug aid - log the value  */
    public static Object print(String label, Object value) {
        log.info(label + ": value = " + value + " [" + value.getClass() + "]");
        return value;
    }
    
    public static Map<String, Object> getFunctions() {
        Map<String, Object> fns = new HashMap<String, Object>();
        fns.put(null, GlobalFunctions.class);
        return fns;
    }
    
    
}
