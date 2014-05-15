/******************************************************************
 * File:        CSVMapSource.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import java.io.IOException;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.MapSource;
import com.epimorphics.dclib.templates.JSONConstants;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * A mapping source populated by two columns from a csv.
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class CSVMapSource extends MapSourceBase implements MapSource {
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        if (spec.hasKey(JSONConstants.SOURCE)) {
            if (spec.hasKey(JSONConstants.SOURCE_TYPE)) {
                return spec.get(JSONConstants.SOURCE_TYPE).getAsString().value().equals(JSONConstants.CSV);
            }
        }
        return false;
    }

    public CSVMapSource(JsonObject spec, ConverterProcess config) throws IOException {
        super(spec);
        String keyCol = getField(JSONConstants.KEY, "key");
        String valueCol = getField(JSONConstants.VALUE, "value");
        boolean makeURI = getFlag(JSONConstants.MAKE_URI, true);
        String sourceFile = getRequiredField(JSONConstants.SOURCE);
        
        CSVInput in = new CSVInput( findFile(sourceFile, config) );
        if (!in.hasHeader(keyCol) || !in.hasHeader(valueCol)) {
            if (in.getHeaders().length >= 2) {
                config.getMessageReporter().report("Defaulting to using first two columns as key and value");
                keyCol = in.getHeaders()[0];
                valueCol = in.getHeaders()[1];
            } else {
                throw new EpiException("Not enough columns in csv to use for lookups: " + sourceFile);
            }
        }
        
        while (true) {
            BindingEnv row = in.nextRow();
            if (row == null) {
                in.close();
                break;
            }
            
            put(row.get(keyCol).toString(), asNode(makeURI, row.get(valueCol).toString()));
        }
    }
    
    private Node asNode(boolean isURI, String value) {
        if (value.startsWith("<") && value.endsWith(">")) {
            isURI = true;
            value = value.substring(1, value.length() - 1);
        }
        if (isURI) {
            return NodeFactory.createURI(value);
        } else {
            return NodeFactory.createLiteral(value);
        }
    }
}
