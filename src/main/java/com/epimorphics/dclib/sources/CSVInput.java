/******************************************************************
 * File:        CSVInput.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import au.com.bytecode.opencsv.CSVReader;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.util.NameUtils;

/**
 * Read a UTF-8 CSV file line by line
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class CSVInput {
    protected CSVReader in;
    protected String[] headers;
    protected int lineNumber = 0;
    protected boolean hasPreamble = false;
    protected String[] peekRow;
    
    public CSVInput(String filename) throws IOException {
        this( new FileInputStream(filename) );
    }
    
    public CSVInput(InputStream ins) throws IOException {
        in = new CSVReader( new InputStreamReader(ins, StandardCharsets.UTF_8) );
        
        String[] headerLine = in.readNext();
        headers = new String[headerLine.length];
        for(int i = 0; i < headerLine.length; i++) {
            headers[i] = NameUtils.safeVarName( headerLine[i].trim() );
        }
        lineNumber++;
        if (headerLine.length > 1 && headerLine[0].equals("#")) {
            hasPreamble = true;
        }
    }
    
    /**
     * Read the next row, not mapping to headers.
     * Sequences of peekRows read further rows.  
     */
    public String[] peekRow() throws IOException {
        peekRow = in.readNext();
        lineNumber++;
        return peekRow;
    }
    
    /**
     * Return the next row as a binding environment of strings
     * If there have been any peek rows then returns an env based
     * on the last peeked row.
     */
    public BindingEnv nextRow() throws IOException {
        if (in != null) {
            String[] rowValues = (peekRow != null) ? peekRow : in.readNext();
            if (peekRow == null) {
                lineNumber++;
            } else {
                peekRow = null;
            }
            if (rowValues == null || rowValues.length == 0) {
                return null;
            }
            BindingEnv row = new BindingEnv( );
            for (int i = 0; i < headers.length; i++) {
                row.put(headers[i], rowValues[i]);
            }
            return row;
        }
        return null;
    }
    
    public boolean hasHeader(String header) {
        for (String h : headers) {
            if (h.equals(header)) {
                return true;
            }
        }
        return false;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }

    public boolean hasPreamble() {
        return hasPreamble;
    }
    
    public String[] getHeaders() {
        return headers;
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            // swallow errors in closing, not useful here
        }
    }
    
    
     
}
