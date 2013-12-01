/******************************************************************
 * File:        ResourceMapTemplate.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.riot.system.StreamRDF;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.framework.ValueString;
import com.epimorphics.dclib.framework.ValueStringArray;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

/**
 * A template what generates triples based on pattners for the root resource and a set of property/value pairs.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResourceMapTemplate extends TemplateBase implements Template {
    protected Pattern root;
    protected Map<Pattern, Pattern> map = new HashMap<Pattern, Pattern>();
    
    public ResourceMapTemplate(JsonObject spec, DataContext dc) {
        this.spec = spec;
        init();
        root = new Pattern( getRequiredField(JSONConstants.ID), dc );
        for (Entry<String, JsonValue> entry : spec.entrySet()) {
            Pattern prop = new Pattern(entry.getKey(), dc);
            if (prop.isURI()) {
                Pattern val = new Pattern(entry.getValue().toString(), dc);
                map.put(prop, val);
            }
        }
    }

    @Override
    public boolean convertRow(ConverterProcess config, BindingEnv row, int rowNumber) {
        StreamRDF out = config.getOutputStream();
        Node subject = asURINode( root.evaluate(row) );
        for (Entry<Pattern, Pattern> mapent : map.entrySet()) {
            Pattern propPattern = mapent.getKey();
            Pattern valPattern = mapent.getValue();
            Node prop = asURINode( propPattern.evaluate(row) );
            Object value = valPattern.evaluate(row);
            if (value instanceof ValueStringArray) {
                for (Object v : ((ValueStringArray)value).getValues()) {
                    out.triple( asTriple(propPattern, valPattern, subject, prop, v) );
                }
            } else {
                out.triple( asTriple(propPattern, valPattern, subject, prop, value) );
            }
        }
        return true;
    }
    
    // TODO move this up to TemplateBase?
    
    protected Node asURINode(Object result) {
        if (result instanceof String || result instanceof ValueString) {
            return NodeFactory.createURI( result.toString() );
        } else {
            throw new EpiException("Found " + result + " when expecting a URI");
        }
    }

    protected Node asNode(Pattern pattern, Object result) {
        // Assumes we have already taken care of multiple valued objects
        if (pattern.isURI()) {
            return asURINode(result);
        } else if (result instanceof Node) {
            return (Node) result;
        } else if (result instanceof String) {
            return NodeFactory.createLiteral( (String)result );
        } else if (result instanceof ValueString) {
            return NodeFactory.createLiteral( ((ValueString)result).getString() );
        } else if (result instanceof Number) {
            if (result instanceof BigDecimal) {
                return NodeFactory.createUncachedLiteral(result, XSDDatatype.XSDdecimal);
            } else if (result instanceof BigInteger) {
                return NodeFactory.createUncachedLiteral(result, XSDDatatype.XSDinteger);
            } else if (result instanceof Double) {
                return NodeFactory.createUncachedLiteral(result, XSDDatatype.XSDdouble);
            } else if (result instanceof Long) {
                return NodeFactory.createUncachedLiteral(result, XSDDatatype.XSDlong);
            } else {
                return NodeFactory.createUncachedLiteral(((Number)result).intValue(), XSDDatatype.XSDint);
            }
        } else {
            // TODO handle dates
        }                
        return null;
    }
    
    protected Triple asTriple(Pattern propPattern, Pattern valPattern, Node subject, Node prop, Object v) {
        if (propPattern.isInverse()) {
            return new Triple( asURINode(v), prop, subject);
        } else {
            return new Triple(subject, prop, asNode(valPattern, v));
        }
    }
}
