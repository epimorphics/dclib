/******************************************************************
 * File:        ResourceMapTemplate.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jexl2.JexlException;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.riot.system.StreamRDF;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.NullResult;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.values.Value;
import com.epimorphics.dclib.values.ValueArray;
import com.epimorphics.dclib.values.ValueNode;
import com.epimorphics.dclib.values.ValueNull;
import com.epimorphics.util.EpiException;
import org.apache.jena.graph.Node;
import org.apache.jena.util.OneToManyMap;

/**
 * A template what generates triples based on pattners for the root resource and a set of property/value pairs.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResourceMapTemplate extends TemplateBase implements Template {
    protected Pattern root;
    protected OneToManyMap<Pattern, Pattern> patterns = new OneToManyMap<Pattern, Pattern>();
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        return spec.hasKey( JSONConstants.ID );
    }
    
    public ResourceMapTemplate(JsonObject spec, DataContext dc) {
        super(spec);
        root = new Pattern( getRequiredField(JSONConstants.ID), dc );
        for (Entry<String, JsonValue> entry : spec.entrySet()) {
            Pattern prop = new Pattern(entry.getKey(), dc);
            if (prop.isURI()) {
                JsonValue vals = entry.getValue();
                if (vals.isString()) {
                    patterns.put( prop, new Pattern(vals.getAsString().value(), dc) );
                } else if (vals.isArray()) {
                    for (Iterator<JsonValue> i =vals.getAsArray().iterator(); i.hasNext();) {
                        JsonValue val = i.next();
                        if (val.isString()) {
                            patterns.put( prop, new Pattern(val.getAsString().value(), dc) );
                        } else {
                            throw new EpiException("Resource map found non-string value pattern: " + val);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Node convertRow(ConverterProcess proc, BindingEnv row,
            int rowNumber) {
        super.convertRow(proc, row, rowNumber);
        StreamRDF out = proc.getOutputStream();
        proc.debugCheck(row, rowNumber, root);
        Node subject = root.evaluateAsURINode(row, proc, rowNumber);
        if (subject == null) return subject;
        BindingEnv env = new BindingEnv(row);
        env.put(ConverterProcess.ROOT_NAME, new ValueNode(subject));
        for (Map.Entry<Pattern, Pattern> entry : patterns.entrySet()) {
            Pattern propPattern = entry.getKey();
            proc.debugCheck(env, rowNumber, propPattern);
            Pattern valPattern = entry.getValue();
            proc.debugCheck(env, rowNumber, valPattern);
            try {
                Node prop = propPattern.evaluateAsNode(env, proc, rowNumber);
                validateNode(prop);
                Object value = valPattern.evaluate(env, proc, rowNumber);
                if (value instanceof Node) {
                    validateNode((Node)value);
                }
                if (value instanceof ValueArray) {
                    for (Value v : ((ValueArray) value).getValues()) {
                        out.triple(asTriple(propPattern, valPattern, subject,
                                prop, v));
                    }                   
                } else if (value instanceof ValueNull) {
                        // E.g. failed to parse a date, treat like missing data?
                        proc.getMessageReporter().report("Skipping null result for property " + propPattern, rowNumber);

                } else {
                    out.triple(asTriple(propPattern, valPattern, subject, prop,
                            value));
                }
            } catch (JexlException.Variable e) {
                // Missing data at this stage is silently ignored so can have optional properties in the map
            } catch (NullResult e) {
                // Missing data at this stage is silently ignored so can have optional properties in the map
            }
        }
        return subject;
    }

}
