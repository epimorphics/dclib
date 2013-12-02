/******************************************************************
 * File:        DataContext.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Packages access to the background context of reference data, templates and
 * prefix definitions need to process a data set. 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DataContext {
    // TODO sources
    
    protected Map<String, Template> templates = new HashMap<String, Template>();
    protected PrefixMapping prefixes;
    protected BindingEnv env = new BindingEnv();
    
    public DataContext() {
    }
    
    public void setPrefixes(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }
    
    /**
     * Expand a prefixes in the given URI string using the reference prefix definitions.
     */
    public String expandURI(String uri) {
        if (prefixes != null) {
            return prefixes.expandPrefix(uri);
        } else {
            return uri;
        }
    }
    
    /**
     * Get the global binding environment with default parameter settings
     */
    public BindingEnv getGlobalEnv() {
        return env;
    }
    
    /**
     * Register a new template
     */
    public void registerTemplate(Template template) {
        String name = template.getName();
        if (name != null) {
            templates.put(name, template);
        } else {
            throw new EpiException("Can't register a nameless template: " + template);
        }
    }

    /**
     * Register a template from a file
     */
    public void registerTemplate(String src) throws IOException {
        Template template = TemplateFactory.templateFrom(src, this);
        if (template.getName() == null) {
            templates.put(src, template);
        } else {
            templates.put(template.getName(), template);
        }
    }
    
    /**
     * Find a named template
     */
    public Template getTemplate(String name) {
        return templates.get(name);
    }
}
