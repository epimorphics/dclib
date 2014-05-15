/******************************************************************
 * File:        Template.java
 * Created by:  Dave Reynolds
 * Created on:  30 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.util.List;

import com.epimorphics.appbase.monitor.ConfigInstance;
import com.hp.hpl.jena.graph.Node;

/**
 * Defines a CSV to RDF data mapping, may be standalone or may be composed
 * into larger templates.
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Template extends ConfigInstance {
    
    /**
     * Test if this template could be applied to a supplied CSV
     * based on checking column names.
     */
    public boolean isApplicableTo(String[] columnNames);
    
    /**
     * Test if this template can be applied to the given row
     */
    public boolean isApplicableTo(BindingEnv row);

    /**
     * Execute the template on one row of data, in the context of a fully configured conversion process.
     * Throw an exception if the conversion is not possible, this will be caught and logged higher up
     * @return a most salient created resource, if any
     */
    public Node convertRow(ConverterProcess config, BindingEnv row, int rowNumber);
    
    /**
     * Execute any one-off parts of the template
     */
    public void preamble(ConverterProcess config);
    
    /**
     * Return the name of this template, may be null
     */
    public String getName();
    
    /**
     * Return a description of this template, may be null
     */
    public String getDescription();

    /**
     * Return a textual rendering of the template specification.
     * May not be literally the same as the original source file
     */
    public String getSource();
    
    /**
     * Return a list of column names that should be present for the template to be applicable.
     * Provided for documentation and UI purposes, no operational.
     */
    public List<String> required();
    
    /**
     * Return a list of column names that will also be used if present.
     * Provided for documentation and UI purposes, no operational.
     */
    public List<String> optional();
    
}
