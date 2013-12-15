/******************************************************************
 * File:        MapSourceFactory.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import java.io.IOException;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.MapSource;

public class MapSourceFactory {

    public static MapSource sourceFrom(JsonObject spec, ConverterProcess proc) throws IOException {
        if (CSVMapSource.isSpec(spec)) {
            return new CSVMapSource(spec, proc);
        } else if (RDFMapSource.isSpec(spec)) {
            return new RDFMapSource(spec, proc);
        }
        return null;
    }
}
