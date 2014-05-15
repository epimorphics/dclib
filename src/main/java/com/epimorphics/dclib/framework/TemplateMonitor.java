/******************************************************************
 * File:        ConverterManager.java
 * Created by:  Dave Reynolds
 * Created on:  15 May 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.epimorphics.appbase.monitor.ConfigMonitor;
import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.util.EpiException;

/**
 * Manage a set of templates and sources to support conversion of data.
 * Alternative to ConfigService that allows for dynamically monitored
 * directory of templates.
 */
public class TemplateMonitor extends ConfigMonitor<Template> {
    protected DataContext dc;
    
    /**
     * Null constructor. Can be used to create a monitor from
     * a configuration file but setDataContext must be called before use.
     */
    public TemplateMonitor() {
    }
    
    public TemplateMonitor(DataContext dc) {
        this.dc = dc;
    }
    
    public void setDataContext(DataContext dc) {
        this.dc = dc;
    }

    @Override
    protected Collection<Template> configure(File file) {
        if (dc == null) {
            return Collections.emptyList();
        }
        try {
            return Collections.singletonList( TemplateFactory.templateFrom(file.getPath(), dc) );
        } catch (IOException e) {
            throw new EpiException("Problem loading tempate: " + file, e);
        }
    }
    
    protected void doAddEntry(Template entry) {
        super.doAddEntry(entry);
        dc.registerTemplate(entry);
    }

    protected void doRemoveEntry(Template entry) {
        super.doRemoveEntry(entry);
        dc.removeTemplate( entry.getName() );
    }

}
