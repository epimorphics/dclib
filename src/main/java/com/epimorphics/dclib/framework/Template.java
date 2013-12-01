/******************************************************************
 * File:        Template.java
 * Created by:  Dave Reynolds
 * Created on:  30 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

/**
 * Defines a CSV to RDF data mapping, may be standalone or may be composed
 * into larger templates.
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Template {
    
    /**
     * Test if this template could be applied to a supplied CSV
     * based on checking column names.
     */
    public boolean isApplicableTo(String[] columnNames);

    /**
     * Execute the template on one row of data, in the context of a fully configured conversion process.
     * @return true if the conversion succeeded.
     */
    public boolean convertRow(ConverterProcess config, BindingEnv row, int rowNumber);
    
    /**
     * Return the name of this template, may be null
     */
    public String getName();
    
    /**
     * Return a description of this template, may be null
     */
    public String getDescription();

}
