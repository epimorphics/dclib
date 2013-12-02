/******************************************************************
 * File:        ResourceMapTemplate.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.util.ArrayList;
import java.util.List;
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
import com.epimorphics.dclib.framework.ValueStringArray;
import com.hp.hpl.jena.graph.Node;

/**
 * A template what generates triples based on pattners for the root resource and a set of property/value pairs.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResourceMapTemplate extends TemplateBase implements Template {
    protected Pattern root;
    protected List<Pattern> propPatterns = new ArrayList<>();
    protected List<Pattern> valPatterns = new ArrayList<>();
    
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
                Pattern val = new Pattern(entry.getValue().getAsString().value(), dc);
                propPatterns.add( prop );
                valPatterns.add( val );
            }
        }
    }

    @Override
    public Node convertRow(ConverterProcess config, BindingEnv row,
            int rowNumber) {
        super.convertRow(config, row, rowNumber);
        StreamRDF out = config.getOutputStream();
        Node subject = asURINode(root.evaluate(row));
        for (int i = 0; i < propPatterns.size(); i++) {
            Pattern propPattern = propPatterns.get(i);
            Pattern valPattern = valPatterns.get(i);
            try {
                Node prop = asURINode(propPattern.evaluate(row));
                Object value = valPattern.evaluate(row);
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
