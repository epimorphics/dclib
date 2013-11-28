/******************************************************************
 * File:        SimpleCSVConverter.java
 * Created by:  Dave Reynolds
 * Created on:  11 Oct 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import com.epimorphics.appbase.tasks.ProgressReporter;
import com.epimorphics.appbase.tasks.TaskState;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Simple (single threaded) driver for converting a CSV data source
 * using a RowBasedConverter. 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SimpleCSVConverter implements Converter { 
    static final int BATCH_SIZE = 100;
    
    protected Map<String, Object> parameters; 
    protected String[] headers;
    protected CSVReader reader;
    protected RowBasedConverter converter;
    protected int lineNumber = 0;
    
    public SimpleCSVConverter(Reader source, Map<String, Object> parameters, RowBasedConverter converter) {
        this.parameters = parameters;
        reader = new CSVReader(source);
        this.converter = converter;
        try {
            headers = reader.readNext();
            lineNumber = 1;
        } catch (IOException e) {
            headers = null;
        }
    }

    @Override
    public boolean validate() {
        return headers != null && converter.validate(headers);
    }

    @Override
    public void convert(Model target, ProgressReporter progress) {
        try {
            if (target.supportsTransactions()) {
                target.begin();
            }
            doConvert(target, progress);
        } finally {
            if (target.supportsTransactions()) {
                if (progress.succeeded()) {
                    target.commit();
                } else {
                    target.abort();
                }
            }
        }
    }

    @Override
    public void convert(Dataset target, String graphname,
            ProgressReporter progress) {
        try {
            if (target.supportsTransactions()) {
                target.begin( ReadWrite.WRITE );
            }
            doConvert(graphname == null ? target.getDefaultModel() : target.getNamedModel(graphname), progress);
        } finally {
            if (target.supportsTransactions()) {
                if (progress.succeeded()) {
                    target.commit();
                } else {
                    target.abort();
                }
            }
        }
    }
    
    protected void doConvert(Model target, ProgressReporter progress) {
        progress.setState(TaskState.Running);
        try {
            converter.initialize(parameters, target);
        } catch (Exception e) {
            progress.report("Error: " + e, lineNumber);
            progress.setSuccess(false);
            try {
                reader.close();
            } catch (IOException e1) { }
            return;
        }

        while(true) {
            if (lineNumber % BATCH_SIZE == 0) {
                progress.report("Processing row " + lineNumber);
            }
            try {
                String[] values = reader.readNext();
                if (values == null || values.length == 0) break;
                Row row = new Row(headers, values, lineNumber);
                try {
                    converter.convert(row, target);
                } catch (Exception e) {
                    progress.report("Error: " + e, lineNumber);
                    progress.setSuccess(false);
                }
                lineNumber++;
            } catch (IOException e) {
                progress.report("Failed to read data: " + e, lineNumber);
                progress.failed();
                try {
                    reader.close();
                } catch (IOException e1) { }
                return;
            }
        }
        progress.setState(TaskState.Terminated);
        try {
            reader.close();
        } catch (IOException e) {
            throw new EpiException(e);
        }
    }
    
}
