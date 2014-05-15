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
import java.util.Collection;

import com.epimorphics.appbase.monitor.ConfigMonitor;

/**
 * Manage a set of templates and sources to support conversion of data.
 * Alternative to ConfigService that allows for dynamically monitored
 * directory of templates.
 */
public class TemplateMonitor extends ConfigMonitor<Template> {

    @Override
    protected Collection<Template> configure(File file) {
        // TODO Auto-generated method stub
        return null;
    }

}
