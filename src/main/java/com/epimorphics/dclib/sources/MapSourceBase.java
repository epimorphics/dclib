/******************************************************************
 * File:        MapSourceBase.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import java.util.Collection;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.riot.system.StreamRDF;

import com.epimorphics.dclib.framework.MapSource;
import org.apache.jena.graph.Node;

/**
 * Generic implementation of MapSource based on an in-memory lookup table.
 * It is up to subclasses to populate the table from their actual data sources.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class MapSourceBase extends MapSourceBaseBase implements MapSource {
    protected LexIndex<Node> table = new LexIndex<>();

    public MapSourceBase(JsonObject spec) {
        super(spec);
    }
    
    public void put(String key, Node value) {
        table.put(key, value);
    }
    
    @Override
    public Node lookup(String key) {
        return table.lookup(key);
    }

    @Override
    public Node lookup(String key, String valueToReturn) {
        return lookup(key);
    }
    
    @Override
    public Collection<Node> lookupAll(String key) {
        return table.lookupAll(key);
    } 
    
    @Override
    public void enrich(StreamRDF stream, Node match) {
        // Default is no enrichment
    }
    
}
