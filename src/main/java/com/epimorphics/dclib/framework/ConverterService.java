/******************************************************************
 * File:        ConverterService.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.tasks.LiveProgressMonitor;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;

/**
 * Provides management of a global data converter context from which 
 * individual data conversion runs can be fired off.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ConverterService {
    static final Logger log = LoggerFactory.getLogger(ConverterService.class);
            
    public static final String DEFAULT_PREFIXES_RESOURCE = "defaultPrefixes.ttl";
    public static final String DEFAULT_BASE_URI = "";   // Default is relative URIs for registry use
    
    protected DataContext dc;
    
    public ConverterService() {
        dc = new DataContext();
        dc.getGlobalEnv().put("$base", DEFAULT_BASE_URI);
        PrefixMapping prefixes = FileManager.get().loadModel(DEFAULT_PREFIXES_RESOURCE);
        if (prefixes != null) {
            dc.setPrefixes(prefixes);
        } else {
            log.warn("No default prefixes found");
            dc.setPrefixes( ModelFactory.createDefaultModel() );
        }
    }
    
    /**
     * Register a set of prefix definitions to be used for expanding
     * all qnames in templates.
     */
    public void setPrefixFile(String prefixes) {
        PrefixMapping pref = FileManager.get().loadModel(prefixes);
        dc.setPrefixes(pref);
    }
    
    /**
     * Add a variable value to the global environment 
     */
    public void put(String key, Object value) {
        dc.getGlobalEnv().put(key, value);
    }
    
    /**
     * Add variable values to the global environment using the syntax "key:value, key2:value2 ...", 
     * useful for putting bindings in configuration files. Values can contain embedded ":" characters
     * but not embedded ","
     */
    public void setBindings(String bindings) {
        for (String binding : bindings.split(",")) {
            int split = binding.indexOf(":");
            String key = binding.substring(0, split).trim();
            String value = binding.substring(split+1).trim();
            dc.getGlobalEnv().put(key, value);
        }
    }

    // TODO template management and template load from the environment
    
    // TODO finding template for a CSV
    
    public DataContext getDataContext() {
        return dc;
    }
    
    /**
     * Simple invocation. Load template and data from a file, run process
     * and return memory model containing results or null if there was a problem.
     * Problems/progress reporting live to stdout.
     * @throws IOException 
     */
    public Model simpleConvert(String templateFile, String dataFile) throws IOException {
        Template template = TemplateFactory.templateFrom(templateFile, dc);
        
        InputStream is = new FileInputStream(dataFile);
        ConverterProcess process = new ConverterProcess(dc, is);
        process.setTemplate( template );
        process.setMessageReporter(new LiveProgressMonitor() );
        boolean ok = process.process();
        
        return ok ?  process.getModel() : null;
    }
}