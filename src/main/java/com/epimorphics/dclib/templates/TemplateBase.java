/******************************************************************
 * File:        TemplateBase.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;

/**
 * Base implementation of a Template that can be instantiated from 
 * a JSON object.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TemplateBase implements Template {
    protected JsonObject spec;
    
    protected String[] requiredColumns;
    
    public TemplateBase(JsonObject spec) {
        this.spec = spec;
        init();
    }
    
    protected void init() {
        if (spec.hasKey(JSONConstants.REQUIRED)) {
            Object[] required = spec.get(JSONConstants.REQUIRED).getAsArray().toArray();
            requiredColumns = new String[ required.length ];
            for (int i = 0; i < required.length; i++) {
                requiredColumns[i] = NameUtils.safeVarName( required[i].toString() );
                
            }
        }
    }
    
    protected String getRequiredField(String name) {
        JsonValue field = spec.get(name);
        if (field == null) {
            throw new EpiException("Failed to find expected JSON field: " + name + " on " + spec);
        }
        return field.getAsString().value();
    }
    
    public void setRequired(String[] required) {
        requiredColumns = required;
    }
    
    @Override
    public boolean isApplicableTo(String[] columnNames) {
        if (requiredColumns != null) {
            for (String required : requiredColumns) {
                boolean ok = false; 
                for (String col : columnNames) {
                    if (col.equals(required)) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) return false;
            }
        }
        return true;
    }

    @Override
    public boolean convertRow(ConverterProcess config, BindingEnv row, int rowNumber) {
        if (requiredColumns != null) {
            for (String required : requiredColumns) {
                if (!row.containsKey(required)) {
                    throw new EpiException("Missing parameter '" + required + "' required for template " + getName());
                }
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return spec.get(JSONConstants.NAME).getAsString().value();
    }

    @Override
    public String getDescription() {
        return spec.get(JSONConstants.DESCRIPTION).getAsString().value();
    }


}
