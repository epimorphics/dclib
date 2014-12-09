/******************************************************************
 * File:        ConverterService.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.core.PrefixService;
import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.tasks.LiveProgressMonitor;
import com.epimorphics.tasks.ProgressMonitorReporter;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;
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
public class ConverterService extends ComponentBase {
    static final Logger log = LoggerFactory.getLogger(ConverterService.class);
            
    public static final String DEFAULT_PREFIXES_RESOURCE = "defaultPrefixes.ttl";
    public static final String DEFAULT_BASE_URI = "";   // Default is relative URIs for registry use
    
    protected DataContext dc;
    protected boolean silent = false;
    protected TemplateMonitor monitor;
    
    public ConverterService() {
        dc = new DataContext();
        dc.getGlobalEnv().put(ConverterProcess.BASE_OBJECT_NAME, DEFAULT_BASE_URI);
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
     * Take global default prefixes from a configurable prefix service 
     */
    public void setPrefixService(PrefixService ps) {
        dc.setPrefixes( ps.getPrefixes() );
    }
    
    /**
     * Set a sequence of directories which be searched when loading
     * referenced files (specifically those used in mapping sources).
     * @param dirs a comma-separated list of directory names
     */
    public void setLoadDirectories(String dirs) {
        dc.setLoadDirectories(dirs);
    }
    
    /**
     * Attach a TemplateMonitor that will dynamically 
     * add configured templates to the data context.
     */
    public void setTemplateMonitor(TemplateMonitor monitor) {
        this.monitor = monitor;
        monitor.setDataContext(dc);
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

    // TODO finding template for a CSV
    
    public DataContext getDataContext() {
        return dc;
    }
    
    /**
     * Set to true to suppress process messages
     * @param silent
     */
    public void setSilent(boolean silent) {
        this.silent = silent;
    }
    
    /**
     * Simple invocation. Load template and data from a file, run process
     * and return memory model containing results or null if there was a problem.
     * Problems/progress reporting live to given reporter
     * @param templateFile the name of the template file to use
     * @param dataFile  the name of the data file to process
     * @param report the message reporter
     * @param debug set to true to enable voluminous debug message
     * @param allowNullRows set to true to allow output even if some rows don't match
     * @throws IOException 
     */
    public Model simpleConvert(String templateFile, String dataFile, ProgressMonitorReporter reporter, boolean debug, boolean allowNullRows) throws IOException {
        Template template = TemplateFactory.templateFrom(templateFile, dc);
        
        File dataFileF = new File(dataFile);
        String filename = dataFileF.getName();
        String filebasename = NameUtils.removeExtension(filename);
        put(ConverterProcess.FILE_NAME, filename);
        put(ConverterProcess.FILE_BASE_NAME, filebasename);
        InputStream is = new FileInputStream(dataFileF);
        
        ConverterProcess process = new ConverterProcess(dc, is);
        process.setDebug(debug);
        process.setTemplate( template );
        process.setMessageReporter( reporter );
        process.setAllowNullRows(allowNullRows);
        boolean ok = process.process();
        
        return ok ?  process.getModel() : null;
    }
    
    /**
     * Simple invocation. Load template and data from a file, run process
     * and return memory model containing results or null if there was a problem.
     * Problems/progress reporting live to given reporter
     * @throws IOException 
     */
    public Model simpleConvert(String templateFile, String dataFile, ProgressMonitorReporter reporter, boolean debug) throws IOException {
        return simpleConvert(templateFile, dataFile, reporter, debug, true);
    }
    
    /**
     * Simple invocation. Load template and data from a file, run process
     * and return memory model containing results or null if there was a problem.
     * Problems/progress reporting live to given reporter
     * @throws IOException 
     */
    public Model simpleConvert(String templateFile, String dataFile, ProgressMonitorReporter
            reporter) throws IOException {
        return simpleConvert(templateFile, dataFile, reporter, false);
    }
    
    /**
     * Simple invocation. Load template and data from a file, run process
     * and return memory model containing results or null if there was a problem.
     * Problems/progress reporting live to stdout unless silent is set.
     * @throws IOException 
     */
    public Model simpleConvert(String templateFile, String dataFile) throws IOException {
        return simpleConvert(templateFile, dataFile, silent ? new SimpleProgressMonitor() : new LiveProgressMonitor() );
    }
    
    /**
     * Set up a conversion process using the service's configured data context.
     * Caller can set the process to be streaming or not before triggering it.
     */
    public ConverterProcess startConvert(String templateName, InputStream is) {
        ConverterProcess process = new ConverterProcess(dc, is);
        Template template = dc.getTemplate(templateName);
        if (template == null) {
            throw new EpiException("Template not found: " + templateName);
        }
        process.setTemplate( template );
        return process;
    }
}
