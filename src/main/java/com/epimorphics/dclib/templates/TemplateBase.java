/******************************************************************
 * File:        TemplateBase.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.EvalFailed;
import com.epimorphics.dclib.framework.MapSource;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.sources.MapSourceFactory;
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
    protected String[] optionalColumns;
    protected String name;
    
    public TemplateBase(JsonObject spec) {
        this.spec = spec;
        init();
    }
    
    protected void init() {
        requiredColumns = getList( JSONConstants.REQUIRED );
        optionalColumns = getList( JSONConstants.OPTIONAL );
        name = getJsonString(JSONConstants.NAME);
        if (name == null) {
            name = "anon";
        }
    }
    
    private String[] getList(String key) {
        if (spec.hasKey(key)) {
            Object[] required = spec.get(key).getAsArray().toArray();
            String[] result = new String[ required.length ];
            for (int i = 0; i < required.length; i++) {
                result[i] = NameUtils.safeVarName( ((JsonValue)required[i]).getAsString().value() );
            }
            return result;
        }
        return null;
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
                Object value = row.get(required);
                if (value == null || value instanceof ValueNull) {
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
    public void preamble(ConverterProcess config, BindingEnv env) {
        if (spec.hasKey(JSONConstants.SOURCES)) {
            JsonValue sspec = spec.get(JSONConstants.SOURCES);
            if (sspec != null) {
                if (sspec.isObject()) {
                    processSourceSpec(sspec, config);
                } else if (sspec.isArray()) {
                    for (Iterator<JsonValue> i = sspec.getAsArray().iterator(); i.hasNext();) {
                        processSourceSpec(i.next(), config);
                    }
                }
            }
        }
    }
    
    private void processSourceSpec(JsonValue spec, ConverterProcess config) {
        try {
            MapSource source = null;
            if (spec.isObject()) {
                source = MapSourceFactory.sourceFrom(spec.getAsObject(), config);
            }
            if (source == null) {
                throw new EpiException("Failed to instantiate mapping source: " + spec);
            } else {
                config.getDataContext().registerSource(source);
            }
        } catch (IOException e) {
            throw new EpiException(e);
        }
    }


    @Override
    public String getName() {
        return name;
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
        validateNode(prop);
        validateNode(subject);
        Node so = propPattern.isInverse() ? valPattern.asURINode(v) : valPattern.asNode(v);
        validateNode(so);
        if (propPattern.isInverse()) {
            return new Triple( so, prop, subject);
        } else {
            return new Triple(subject, prop, so);
        }
    }

    @Override
    public String getSource() {
        return spec.toString();
    }

    @Override
    public List<String> required() {
        return asList(requiredColumns);
    }

    @Override
    public List<String> optional() {
        return asList(optionalColumns);
    }
    
    private List<String> asList(String[] array) {
        if (array != null) {
            List<String> result = new ArrayList<>( array.length );
            for (String s : array) {
                result.add(s);
            }
            return result;
        } else {
            return new ArrayList<>();
        }
    }
    
    static Set<String> ACCEPTED_URI_SCHEMES = new HashSet<>();
    static {
        ACCEPTED_URI_SCHEMES.add("http");
        ACCEPTED_URI_SCHEMES.add("https");
        ACCEPTED_URI_SCHEMES.add("ftp");
        ACCEPTED_URI_SCHEMES.add("ssh");
        ACCEPTED_URI_SCHEMES.add("sftp");
        ACCEPTED_URI_SCHEMES.add("urn");
        ACCEPTED_URI_SCHEMES.add("mailto");
    }
    
    /**
     * Check for things like unsubstituted URIs
     */
    public static void validateNode(Node n) {
        if (n.isLiteral()) {
            if (n.getLiteralValue() == null) {  // throws exception of the datatype is ill-formed
                throw new EvalFailed("Illegal or null literal node");
            }
        } else if (n.isURI()) {
            String uri = n.getURI();
            if (BAD_CHARS.matcher(uri).find()) {
                throw new EvalFailed("Generated URI contains suspect chars: " + uri);
            }
            Matcher m =  PREFIX.matcher(uri);
            if (m.matches()) {
                String prefix = m.group(1).toLowerCase();
                if (ACCEPTED_URI_SCHEMES.contains(prefix)) {
                    // OK
                } else {
                    throw new EvalFailed("Looks like unexpanded prefix in URI: " + uri);
                }
            }
        }
    }
    
    static final java.util.regex.Pattern BAD_CHARS = java.util.regex.Pattern.compile("[{}<>]");
    static final java.util.regex.Pattern PREFIX = java.util.regex.Pattern.compile("([^/]+):.*");
    
    @Override
    public String toString() {
        return "Template-" + name;
    }

    @Override
    public Template deref() {
        return this;
    }

}
