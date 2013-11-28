/******************************************************************
 * File:        RowConverter.java
 * Created by:  Dave Reynolds
 * Created on:  19 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;


/**
 * Signature for classes that convert row-based data sources.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface RowBasedConverter {

    /**
     * Validate if the headers are acceptable for this converter
     * @return true if the conversion can proceed 
     */
    public boolean validate(String[] headers);
    
    /**
     * Start the conversion, passing in any global parameters
     */
    public void initialize(Map<String, Object> parameters, Model model);
    
    /**
     * Transform the given spreadsheet data row, inserting the resulting data
     * in the given accumulator. Throws an exception if there is an error during conversion.
     */
    public void convert(Row row, Model accumulator);
}
