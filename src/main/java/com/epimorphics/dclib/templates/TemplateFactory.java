/******************************************************************
 * File:        TemplateFactory.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonParseException;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.util.EpiException;

/**
 * Utilities to instantiate an appropriate template from a JSON specification.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TemplateFactory {

    public static Template templateFrom(JsonValue json, DataContext dc) {
        if (json.isArray()) {
            JsonArray templates = json.getAsArray();
            Template firstTemplate = null;
            for (Iterator<JsonValue> i = templates.iterator(); i.hasNext();) {
                JsonValue j = i.next();
                if (j.isObject()) {
                    Template t = templateFrom(j.getAsObject(), dc);
                    if (firstTemplate == null) {
                        firstTemplate = t;
                    }
                    if (t.getName() != null) {
                        dc.registerTemplate(t);
                    }
                }
            }
            return firstTemplate;
        } else if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            if (ResourceMapTemplate.isSpec(jo)) {
                return new ResourceMapTemplate(jo, dc);
            } else if (ParameterizedTemplate.isSpec(jo)) {
                return new ParameterizedTemplate(jo, dc);
            } else if (HierarchyTemplate.isSpec(jo)) {
                return new HierarchyTemplate(jo, dc);
            } else if (CompositeTemplate.isSpec(jo)) {
                return new CompositeTemplate(jo, dc);
            } else {
                return null;
            }
        } else if (json.isString()) {
            return new TemplateRef( json.getAsString().value(), dc);
        } else {
            
            throw new EpiException("Templates must be specified as a JSON object or an array of objects");
        }
    }

    public static Template templateFrom(InputStream is, DataContext dc) {
        try {
            JsonValue json = JSON.parseAny(is);
            try {
                // just making sure
                is.close();
            } catch (IOException e) {
                // ignore
            }
            return templateFrom(json, dc);
        } catch (JsonParseException e) {
            if (e.getLine() >= 0) {
                throw new EpiException( String.format("Illegal json: %s at line %d column %d", e.toString(), e.getLine(), e.getColumn()));
            } else {
                throw e;
            }
        }
    }

    public static Template templateFrom(String filename, DataContext dc) throws IOException {
        return templateFrom( new FileInputStream(filename), dc );
    }
}
