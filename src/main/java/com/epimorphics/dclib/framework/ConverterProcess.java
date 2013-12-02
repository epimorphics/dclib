/******************************************************************
 * File:        ConverterProcess.java
 * Created by:  Dave Reynolds
 * Created on:  30 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.epimorphics.tasks.ProgressReporter;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.tasks.TaskState;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * An instance of a converter running on a specific data configuration.
 * Provides access to all the context information, data, mapping sources
 * and output configuration required.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ConverterProcess {
    static final Logger log = LoggerFactory.getLogger( ConverterProcess.class );
    
    protected int BATCH_SIZE = 100;
    protected DataContext dataContext;
    protected ProgressReporter messageReporter = new SimpleProgressMonitor();
    
    protected String[] headers;
    protected CSVReader dataSource;
    
    protected Template template;
    protected BindingEnv env;
    protected Object state;  // Template - specific state information
    
    protected StreamRDF   outputStream;
    protected Model  result;   // May not be used if the stream is set directly  
    
    public ConverterProcess(DataContext context, InputStream data) {
        dataContext = context;
        
        // Set of stable binding values - merge rather than chain since we'll be doing a lot of lookups
        env = new BindingEnv( );
        env.putAll( dataContext.getGlobalEnv() );
        
        dataSource = new CSVReader( new InputStreamReader(data) );
        try {
            String[] headerLine = dataSource.readNext();
            headers = new String[headerLine.length];
            for(int i = 0; i < headerLine.length; i++) {
                headers[i] = NameUtils.safeVarName( headerLine[i] );
            }
            // Default is to converter into an in-memory model, can override by setting explicit StreamRDF dest
            setModel( ModelFactory.createDefaultModel() );
            
        } catch (IOException e) {
            messageReporter.report("Failed read headerline of data");
            messageReporter.failed();
            close();
        }
    }

    /**
     * Run the conversion process
     * @return true if the conversion succeeded
     */
    public boolean process() {
        // TODO locate a matching template it none is set

        int lineNumber = 1;
        if (messageReporter.getState() == TaskState.Terminated) {
            return messageReporter.succeeded();
        }
        messageReporter.setState( TaskState.Running );

        while(true) {
            if (lineNumber % BATCH_SIZE == 0) {
                messageReporter.report("Processing row " + lineNumber);
            }
            BindingEnv row = nextRow();
            if (row != null) {
                try {
                    template.convertRow(this, row, lineNumber);
                } catch (Exception e) {
                    messageReporter.report("Error: " + e, lineNumber);
                    messageReporter.failed();
                    if (!(e instanceof NullResult)) {
                        log.error("Error process line " + lineNumber, e);
                    }
                }
            } else {
                break;
            }
            lineNumber++;
        }
        messageReporter.setState(TaskState.Terminated);
        close();
        
        return messageReporter.succeeded();
    }
    
    private BindingEnv nextRow() {
        if (dataSource != null) {
            try {
                String[] rowValues = dataSource.readNext();
                if (rowValues == null || rowValues.length == 0) {
                    return null;
                }
                BindingEnv row = new BindingEnv( env );
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i], ValueFactory.asValue(rowValues[i]));
                }
                return row;
            } catch (IOException e) {
                messageReporter.report("Problem reading next line of source");
                messageReporter.failed();
                close();
            }
        }
        return null;
    }

    private void close() {
        try {
            dataSource.close();
        } catch (IOException e) {
            // Ignore
        }
        dataSource = null;
        messageReporter.setState(TaskState.Terminated);
    }
    
    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public DataContext getDataContext() {
        return dataContext;
    }

    public void setDataContext(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    public StreamRDF getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(StreamRDF outputStream) {
        this.outputStream = outputStream;
    }

    public ProgressReporter getMessageReporter() {
        return messageReporter;
    }

    public void setMessageReporter(ProgressReporter messageReporter) {
        this.messageReporter = messageReporter;
    }

    public String[] getHeaders() {
        return headers;
    }

    public CSVReader getDataSource() {
        return dataSource;
    }

    public BindingEnv getEnv() {
        return env;
    }

    public void setBatchSize(int bATCH_SIZE) {
        BATCH_SIZE = bATCH_SIZE;
    }
    
    
    public Object getState() {
        return state;
    }

    public void setState(Object state) {
        this.state = state;
    }

    /**
     * Return the model into which the conversion results were stored.
     * May be overridden by direct call to setOutputStream
     */
    public Model getModel() {
        return result;
    }
    
    /**
     * Set a model into which the conversion results will be stored.
     * @see #setOutputStream(StreamRDF)
     */
    public void setModel(Model model) {
        result = model;
        outputStream = StreamRDFLib.graph( result.getGraph() );
    }
}
