/******************************************************************
 * File:        TemplateBase.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.values.ValueNull;
import com.epimorphics.dclib.values.ValueString;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

/**
 * Base implementation of a Template that can be instantiated from 
 * a JSON object.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TemplateBase implements Template {
    protected JsonObject spec;
    
    protected String[] requiredColumns;
    
    public TemplateBase(JsonObject spec) {
        this.spec = spec;
        init();
    }
    
    protected void init() {
        if (spec.hasKey(JSONConstants.REQUIRED)) {
            Object[] required = spec.get(JSONConstants.REQUIRED).getAsArray().toArray();
            requiredColumns = new String[ required.length ];
            for (int i = 0; i < required.length; i++) {
                requiredColumns[i] = NameUtils.safeVarName( required[i].toString() );
                
            }
        }
    }
    
    protected String getRequiredField(String name) {
        JsonValue field = spec.get(name);
        if (field == null) {
            throw new EpiException("Failed to find expected JSON field: " + name + " on " + spec);
        }
        return field.getAsString().value();
    }
    
    public void setRequired(String[] required) {
        requiredColumns = required;
    }
    
    @Override
    public boolean isApplicableTo(String[] columnNames) {
        if (requiredColumns != null) {
            for (String required : requiredColumns) {
                boolean ok = false; 
                for (String col : columnNames) {
                    if (col.equals(required)) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean isApplicableTo(BindingEnv row) {
        if (requiredColumns != null) {
            for (String required : requiredColumns) {
                Object binding = row.get(required);
                if (binding == null || binding instanceof ValueNull) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Node convertRow(ConverterProcess config, BindingEnv row, int rowNumber) {
        if (requiredColumns != null) {
            for (String required : requiredColumns) {
                if (!row.containsKey(required)) {
                    throw new EpiException("Missing parameter '" + required + "' required for template " + getName());
                }
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return getJsonString(JSONConstants.NAME);
    }

    @Override
    public String getDescription() {
        return getJsonString(JSONConstants.DESCRIPTION);
    }
    
    protected String getJsonString(String key) {
        JsonValue v = spec.get(key);
        if (v == null) {
            return null;
        } else if (v.isString()) {
            return v.getAsString().value();
        } else {
            throw new EpiException("Expected json property to be a string but found: " + v);
        }
    }

    // General JSON helper functions
    
    protected Template getTemplateRef(JsonValue ref, DataContext dc) {
        if (ref.isString()) {
            return new TemplateRef(ref.getAsString().value(), dc);
        } else if (ref.isObject()) {
            return TemplateFactory.templateFrom(ref.getAsObject(), dc);
        } else {
            throw new EpiException("Template must be specified as a name or an embedded object: " + ref);
        }
    }
    
    // General RDF helper functions

    protected Node asURINode(Object result) {
        if (result instanceof String || result instanceof ValueString) {
            return NodeFactory.createURI( result.toString() );
        } else if (result instanceof Node) {
            Node n = (Node)result;
            if (n.isBlank() || n.isURI()) {
                return n;
            }
        }
        throw new EpiException("Found " + result + " when expecting a URI");
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
            } else {
                return NodeFactory.createUncachedLiteral(((Number)result).intValue(), XSDDatatype.XSDinteger);
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
