/******************************************************************
 * File:        MapSourceBase.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import java.io.File;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.MapSource;
import com.epimorphics.dclib.templates.JSONConstants;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;

/**
 * Generic implementation of MapSource based on an in-memory lookup table.
 * It is up to subclasses to populate the table from their actual data sources.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class MapSourceBase implements MapSource {
    JsonObject spec;
    protected LexIndex<Node> table = new LexIndex<>();

    public MapSourceBase(JsonObject spec) {
        this.spec = spec;
    }
    
    public void put(String key, Node value) {
        table.put(key, value);
    }
    
    @Override
    public Node lookup(String key) {
        return table.lookup(key);
    }

    @Override
    public String getName() {
        return getField(JSONConstants.NAME);
    }
    
    protected String getRequiredField(String name) {
        JsonValue field = spec.get(name);
        if (field == null) {
            throw new EpiException("Failed to find expected JSON field: " + name + " on " + spec);
        }
        return field.getAsString().value();
    }
    
    protected String getField(String name) {
        JsonValue field = spec.get(name);
        if (field != null) {
            JsonValue value = spec.get(name);
            if (value.isString()) {
                return value.getAsString().value();
            }
            throw new EpiException("Expected a string value for " + name + " on " + spec);
        }
        return null;
    }
    
    protected String getField(String name, String deflt) {
        String val = getField(name);
        return val == null ? deflt : val;
    }

    protected boolean getFlag(String name, boolean deflt) {
        JsonValue value = spec.get(name);
        if (value != null && value.isBoolean()) {
            return value.getAsBoolean().value();
        }
        return deflt;
    }
    
    protected String findFile(String source, ConverterProcess proc) {
        File f = proc.getDataContext().findFile(source);
        if (f != null) {
            return f.getPath();
        } else {
            throw new EpiException("Can't locate mapping source file: " + source);
        }
    }
}
