/******************************************************************
 * File:        Row.java
 * Created by:  Dave Reynolds
 * Created on:  19 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.epimorphics.util.EpiException;

/**
 * Represents single row of a spread sheet.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Row {

    protected Map<String, SimpleValue> row = new HashMap<>();
    protected int rowNum;
    
    public Row(String[] headers, String[] values, int rowNum) {
        if (headers.length != values.length) {
            throw new EpiException("Header/value row lengths don't match");
        }
        for (int i = 0; i < headers.length; i++) {
            row.put(headers[i], new SimpleValue(values[i]));
        }
        this.rowNum = rowNum;
    }
    
    public SimpleValue get(String key) {
        return row.get(key);
    }
    
    public String getString(String key) {
        SimpleValue value = row.get(key);
        if (value != null) {
            return value.str();
        } else {
            return null;
        }
    }
    
    public Set<String> getHeaders() {
        return row.keySet();
    }
    
    public int getRowNum() {
        return rowNum;
    }
}
