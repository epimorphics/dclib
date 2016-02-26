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

import org.apache.commons.io.input.BOMInputStream;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.util.EpiException;
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
        this( new BOMInputStream( new FileInputStream(filename) ) );
    }
    
    public CSVInput(InputStream ins) throws IOException {
        in = new CSVReader( new InputStreamReader(ins, StandardCharsets.UTF_8), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, '\0' );
        
        String[] headerLine = in.readNext();
        if (headerLine == null) {
            throw new EpiException("No data, cannot read header line");
        }
        headers = new String[headerLine.length];
        for(int i = 0; i < headerLine.length; i++) {
            headers[i] = safeColName( headerLine[i].trim() );
        }
        lineNumber++;
        if (headerLine.length > 1 && headerLine[0].equals("#")) {
            hasPreamble = true;
        }
    }
    
    private String safeColName(String col) {
        if (col.startsWith("<") && col.endsWith(">")) {
            // Let through URI wrapped column names raw
            return col;
        } else if (col.isEmpty()) {
            // Allow empty column names as special favour to organograms
            return "_";
        } else {
            return NameUtils.safeVarName(col);
        }
    }
    
    /**
     * Return a look ahead to the next row.
     * Repeat calls do not advance to further rows, 
     */
    public String[] getPeekRow() throws IOException {
        if (peekRow == null) {
            peekRow = in.readNext();
        }
        return peekRow;
    }
    
    /**
     * Advances to the next row after a prior peek.
     * Returns true if a new peek was available.
     */
    public boolean advancePeek() throws IOException {
        peekRow = in.readNext();
        lineNumber++;
        return peekRow != null;
    }
    
    
    
    /**
     * Return the next row as a binding environment of strings
     * If there have been any peek rows then returns an env based
     * on the last peeked row.
     */
    public BindingEnv nextRow() throws IOException {
        if (in != null) {
            String[] rowValues = (peekRow != null) ? peekRow : in.readNext();
            lineNumber++;
            peekRow = null;
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
