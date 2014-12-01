/******************************************************************
 * File:        ValueGeoPoint.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.epimorphics.geo.GeoPoint;
import com.hp.hpl.jena.graph.Node;

/**
 * A value which represents a geographic point. 
 * Should normally only be used within expressions not returned as a final binding.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueGeoPoint extends ValueBase<GeoPoint> {

    public ValueGeoPoint(GeoPoint value) {
        super(value);
    }

    @Override
    public Node asNode() {
        // No default RDF mapping
        return null;
    }

    @Override
    public String getDatatype() {
        // No default RDF mapping
        return null;
    }
    
    public Value getEasting() {
        return new ValueNumber( value.getEasting() );
    }
    
    public Value getNorthing() {
        return new ValueNumber( value.getNorthing() );
    }
    
    public Value getLat() {
        return new ValueNumber( value.getLat() );
    }
    
    public Value getLon() {
        return new ValueNumber( value.getLon() );
    }
    
    public Value getGridRef() {
        return new ValueString( value.getGridRefString() );
    }

}
