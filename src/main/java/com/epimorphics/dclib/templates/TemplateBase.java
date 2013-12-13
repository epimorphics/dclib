/******************************************************************
 * File:        TemplateBase.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.values.ValueNull;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

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
                requiredColumns[i] = NameUtils.safeVarName( ((JsonValue)required[i]).getAsString().value() );
                
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
    public boolean isApplicableTo(BindingEnv row) {
        if (requiredColumns != null) {
            for (String required : requiredColumns) {
                Object binding = row.get(required);
                if (binding == null || binding instanceof ValueNull) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Node convertRow(ConverterProcess config, BindingEnv row, int rowNumber) {
        if (requiredColumns != null) {
            for (String required : requiredColumns) {
                Object binding = row.get(required);
                if (binding == null || binding instanceof ValueNull) {
                    throw new EpiException("Missing parameter '" + required + "' required for template " + getName());
                }
            }
        }
        return null;
    }
    
    
    /**
     * Execute any one-off parts of the template
     */
    public void preamble(ConverterProcess config) {
        // do nothing
    }


    @Override
    public String getName() {
        return getJsonString(JSONConstants.NAME);
    }

    @Override
    public String getDescription() {
        return getJsonString(JSONConstants.DESCRIPTION);
    }
    
    protected String getJsonString(String key) {
        JsonValue v = spec.get(key);
        if (v == null) {
            return null;
        } else if (v.isString()) {
            return v.getAsString().value();
        } else {
            throw new EpiException("Expected json property to be a string but found: " + v);
        }
    }

    // General JSON helper functions
    
    protected Template getTemplateRef(JsonValue ref, DataContext dc) {
        if (ref.isString()) {
            return new TemplateRef(ref.getAsString().value(), dc);
        } else if (ref.isObject()) {
            return TemplateFactory.templateFrom(ref.getAsObject(), dc);
        } else {
            throw new EpiException("Template must be specified as a name or an embedded object: " + ref);
        }
    }
    
    protected List<Template> getTemplates(JsonValue tspec, DataContext dc) {
        List<Template> templates = new ArrayList<>();
        if (tspec != null) {
            if (tspec.isObject()) {
                templates.add( TemplateFactory.templateFrom(tspec.getAsObject(), dc) );
            } else if (tspec.isArray()) {
                for (Iterator<JsonValue> i = tspec.getAsArray().iterator(); i.hasNext();) {
                    templates.add( TemplateFactory.templateFrom(i.next(), dc) );
                }
            } else if (tspec.isString()) {
                templates.add( new TemplateRef(tspec.getAsString().value(), dc) );
            }
        }
        return templates;
    }
    
    // General RDF helper functions
    
    protected Triple asTriple(Pattern propPattern, Pattern valPattern, Node subject, Node prop, Object v) {
        if (propPattern.isInverse()) {
            return new Triple( valPattern.asURINode(v), prop, subject);
        } else {
            return new Triple(subject, prop, valPattern.asNode(v));
        }
    }

}
