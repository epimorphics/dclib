/******************************************************************
 * File:        GlobalFunctions.java
 * Created by:  Dave Reynolds
 * Created on:  16 May 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.EvalFailed;
import com.epimorphics.geo.GeoPoint;
import com.epimorphics.util.EpiException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.XSD;

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
        return NodeFactory.createLiteralDT(value.toString(), typeR);
    }
    
    public static Value nullValue() {
        return new ValueNull();
    }
    
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String) {
            return true;
        } else if (value instanceof ValueString) {
            return ((ValueString)value).toString().isEmpty();
        } else if (value instanceof Value) {
            return ((Value)value).isNull();
        } else if (value instanceof Collection<?>) {
            return ((Collection<?>) value).isEmpty();
        } else {
            return false;
        }
    }
    
    public static void abort() {
        throw new EvalFailed("Aborted at user request");
    }
    
    public static Object round(Object value) {
        Number result = null;
        if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                result = ((BigDecimal)value).round( MathContext.DECIMAL64 );
            } else {
                result = Math.round( ((Number)value).doubleValue() );
            }
        } else if (value instanceof ValueNumber) {
            result = Math.round( ((ValueNumber)value).toNumber().doubleValue() );
        } else {
            throw new EpiException("Round could not process value " + value + " (" + value.getClass() + ")");
        }
        return new ValueNumber(result);
    }
    
    /** Debug aid - log the value  */
    public static Object print(Object value) {
        log.info("Value = '" + value + "' [" + value.getClass() + "]");
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
    
    /** Wrap a plain object as a Value */
    public static Object value(Object value) {
        
    	if(value.getClass().isArray()) {
	        Object[] oa = (value instanceof int[] )    ? (Object[] ) (Arrays.stream((int[])    value).boxed().toArray()) :
    	                  (value instanceof long[] )   ? (Object[] ) (Arrays.stream((long[])   value).boxed().toArray()) :
          	              (value instanceof double[] ) ? (Object[] ) (Arrays.stream((double[]) value).boxed().toArray()) :
		    		      (Object[] )value;
    		int len = oa.length;
        	Value[] v = new Value[len] ;
        	for(int i=0; i<len; i++) {
        		Object wrapped = value(oa[i]);
        		if ( !(wrapped instanceof Value) ) {
        			throw new EpiException("Can't wrap array/collection member:" + oa[i] +" as a Value()");
        		}
        		v[i] = (Value) value(oa[i]);
        	}
        	return new ValueArray(v);
    	}

    	if (value instanceof Value) {
            return value;
        } else if (value instanceof String) {
            return new ValueString((String)value);
        } else if (value instanceof Number) {
            return new ValueNumber((Number)value);
        } else {
            return value;
        }
    }
    
    public static Object value(Collection<Object> values) {
    	return value(values.toArray(new Object[values.size()]));
    }
    
    /** Convert a URI string to a resource, expanding any prefixes */
    public static ValueNode asResource(Object value) {
        if (value instanceof ValueNode) {
            return (ValueNode) value;
        } else {
            String uri = ConverterProcess.get().getDataContext().expandURI( value.toString() );
            return new ValueNode( NodeFactory.createURI(uri) );
        }
    }
    
    /**
     * Wrap a lat/lon pair as a geographic point
     */
    public static ValueGeoPoint fromLatLonRaw(Number lat, Number lon) {
        return new ValueGeoPoint( GeoPoint.fromLatLon(lat.doubleValue(), lon.doubleValue()) );
    }
    
    /**
     * Wrap a lat/lon pair as a geographic point
     */
    public static ValueGeoPoint fromLatLon(ValueNumber lat, ValueNumber lon) {
        return fromLatLonRaw( lat.toNumber(), lon.toNumber() );
    }
    
    /**
     * Wrap easting/northing pair as a geographic point
     */
    public static ValueGeoPoint fromEastingNorthing(ValueNumber e, ValueNumber n) {
        return fromEastingNorthingRaw(e.toNumber(), n.toNumber());
    }
    
    /**
     * Wrap easting/northing pair as a geographic point
     */
    public static ValueGeoPoint fromEastingNorthingRaw(Number e, Number n) {
        return new ValueGeoPoint( GeoPoint.fromEastingNorthing(e.longValue(), n.longValue()) );
    }
    
    /**
     * Wrap an OS grid reference as a geographic point
     */
    public static ValueGeoPoint fromGridRefRaw(String gridref) {
        return new ValueGeoPoint( GeoPoint.fromGridRef(gridref) );
    }
    
    /**
     * Wrap an OS grid reference as a geographic point
     */
    public static ValueGeoPoint fromGridRef(Value gridref) {
        return fromGridRefRaw( gridref.toString() );
    }
    
    static protected Map<Object, Node> bNodes;
    
    public static  ValueNode bnodeFor(Object key) {
        if (bNodes == null) {
            bNodes = new HashMap<Object, Node>();
        }
        Node bnode = bNodes.get(key);
        if (bnode == null) {
            bnode = NodeFactory.createBlankNode();
            bNodes.put(key, bnode);
        }
        return new ValueNode(bnode);
    }
}
