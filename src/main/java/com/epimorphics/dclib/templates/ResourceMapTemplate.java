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
import com.epimorphics.dclib.values.ValueStringArray;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.util.OneToManyMap;

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
    public Node convertRow(ConverterProcess config, BindingEnv row,
            int rowNumber) {
        super.convertRow(config, row, rowNumber);
        DataContext dc = config.getDataContext();
        StreamRDF out = config.getOutputStream();
        config.debugCheck(row, rowNumber, root);
        Node subject = root.evaluateAsURINode(row, dc);
        if (subject == null) return subject;
        for (Map.Entry<Pattern, Pattern> entry : patterns.entrySet()) {
            Pattern propPattern = entry.getKey();
            config.debugCheck(row, rowNumber, propPattern);
            Pattern valPattern = entry.getValue();
            config.debugCheck(row, rowNumber, valPattern);
            try {
                Node prop = propPattern.evaluateAsNode(row, dc);
                Object value = valPattern.evaluate(row, dc);
                if (value instanceof ValueStringArray) {
                    for (Object v : ((ValueStringArray) value).getValues()) {
                        out.triple(asTriple(propPattern, valPattern, subject,
                                prop, v));
                    }
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
