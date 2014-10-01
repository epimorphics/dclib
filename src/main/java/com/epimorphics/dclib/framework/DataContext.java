/******************************************************************
 * File:        DataContext.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/**
 * Packages access to the background context of reference data, templates and
 * prefix definitions need to process a data set. 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DataContext {
    protected Map<String, Template> templates = new HashMap<String, Template>();
    protected PrefixMapping prefixes = new PrefixMappingImpl();
    protected BindingEnv env = new BindingEnv();
    protected DataContext parent;
    protected Map<String, MapSource> sources = new HashMap<>();
    protected String[] loadDirectories = null;
    
    public DataContext() {
    }
    
    public DataContext(DataContext parent) {
        this.parent = parent;
        env = new BindingEnv( parent.getGlobalEnv() );
        prefixes = parent.prefixes;
        loadDirectories = parent.loadDirectories;
    }
    
    public void setPrefixes(PrefixMapping prefixes) {
         this.prefixes.setNsPrefixes(prefixes);
    }
    
    public void setPrefix(String prefix, String uri) {
         this.prefixes.setNsPrefix(prefix, uri);
    }
    
    public PrefixMapping getPrefixes() {
        return prefixes;
    }
    
    /**
     * Set a sequence of directories which be searched when loading
     * referenced files (specifically those used in mapping sources).
     * @param dirs a comma-separated list of directory names
     */
    public void setLoadDirectories(String dirs) {
        loadDirectories = dirs.split(",");
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
    public synchronized void registerTemplate(Template template) {
        String name = template.getName();
        if (name != null) {
            templates.put(name, template);
        } else {
            throw new EpiException("Can't register a nameless template: " + template);
        }
    }
    
    /**
     * Register a new template
     */
    public synchronized void registerTemplate(String name, Template template) {
        templates.put(name, template);

    }

    /**
     * Register a template from a file
     */
    public synchronized Template registerTemplate(String src) throws IOException {
        Template template = TemplateFactory.templateFrom(src, this);
        if (template.getName() == null) {
            templates.put(src, template);
        } else {
            templates.put(template.getName(), template);
        }
        return template;
    }
    
    /**
     * Find a named template
     */
    public synchronized Template getTemplate(String name) {
        Template template = templates.get(name);
        if (template == null && parent != null) {
            template = parent.getTemplate(name);
        }
        return template;
    }
    
    /**
     * Remove a template from the register
     */
    public synchronized void removeTemplate(String name) {
        templates.remove(name);
    }
    
    /**
     * Return a list of all known templates, ordered by name
     */
    public synchronized List<Template> listTemplates() {
        List<Template> results = new ArrayList<>();
        results.addAll( templates.values() );
        if (parent != null) {
            results.addAll( parent.listTemplates() );
        }
        Collections.sort(results, new Comparator<Template>() {
            @Override
            public int compare(Template o1, Template o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return results;
    }
    
    /**
     * Register a new data source
     */
    public synchronized void registerSource(MapSource source) {
        String name = source.getName();
        if (name != null) {
            sources.put(name, source);
        } else {
            throw new EpiException("Can't register a nameless source: " + source);
        }
    }
    
    /**
     * Find a named source
     */
    public synchronized MapSource getSource(String name) {
        MapSource source = sources.get(name);
        if (source == null && parent != null) {
            source = parent.getSource(name);
        }
        return source;
    }
    
    /**
     * Locate a file, source as a source data file, using any configured load directories
     */
    public File findFile(String source) {
        if (loadDirectories != null) {
            for (String dirname : loadDirectories) {
                File dir = new File(dirname.trim());
                File src = new File(dir, source);
                if (src.exists()) {
                    return src;
                }
            }
        }
        File src = new File(source);
        if (src.exists()) {
            return src;
        }
        return null;
    }
}
