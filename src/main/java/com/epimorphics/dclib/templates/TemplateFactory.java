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

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;

/**
 * Utilities to instantiate an appropriate template from a JSON specification.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TemplateFactory {

    public static Template templateFrom(JsonObject json, DataContext dc) {
        if (ResourceMapTemplate.isSpec(json)) {
            return new ResourceMapTemplate(json, dc);
        } else {
            return null;
        }
    }

    public static Template templateFrom(InputStream is, DataContext dc) {
        JsonObject json = JSON.parse(is);
        try {
            // just making sure
            is.close();
        } catch (IOException e) {
            // ignore
        }
        return templateFrom(json, dc);
    }

    public static Template templateFrom(String filename, DataContext dc) throws IOException {
        return templateFrom( new FileInputStream(filename), dc );
    }
}
