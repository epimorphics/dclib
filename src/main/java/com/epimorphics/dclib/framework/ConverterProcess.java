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

import com.epimorphics.dclib.values.Row;
import com.epimorphics.dclib.values.ValueFactory;
import com.epimorphics.tasks.ProgressReporter;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.tasks.TaskState;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * An instance of a converter running on a specific data configuration.
 * Provides access to all the context information, data, mapping sources
 * and output configuration required. Not thread safe.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ConverterProcess {
    static final Logger log = LoggerFactory.getLogger( ConverterProcess.class );
    
    public static final String ROW_OBJECT_NAME = "$row";
    public static final String BASE_OBJECT_NAME = "$base";
    public static final String DATASET_OBJECT_NAME = "$dataset";
    
    static final String META = "meta";

    protected int BATCH_SIZE = 100;
    protected DataContext dataContext;
    protected ProgressReporter messageReporter = new SimpleProgressMonitor();
    
    protected String[] headers;
    protected CSVReader dataSource;
    protected boolean hasPreamble = false;
    protected String[] peekRow;
    protected int lineNumber = 0;
    protected boolean debug = false;
    
    protected Template template;
    protected BindingEnv env;
    protected Object state;  // Template - specific state information
    
    protected StreamRDF   outputStream;
    protected Model  result;   // May not be used if the stream is set directly  
    
    public ConverterProcess(DataContext context, InputStream data) {
        dataContext = new DataContext( context );
        
        // Could flatten env here to avoid chaining lookup
        env = dataContext.getGlobalEnv();

        dataSource = new CSVReader( new InputStreamReader(data) );
        try {
            String[] headerLine = dataSource.readNext();
            headers = new String[headerLine.length];
            for(int i = 0; i < headerLine.length; i++) {
                headers[i] = NameUtils.safeVarName( headerLine[i].trim() );
            }
            lineNumber++;
            if (headerLine.length > 1 && headerLine[0].equals("#")) {
                hasPreamble = true;
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
     * Set to true to provide verbose output for all template matching
     * to aid with debugging
     */
    public void setDebug(boolean debugOn) {
        this.debug = debugOn;
    }
    
    public boolean isDebugging() {
        return debug;
    }

    /**
     * Run the conversion process
     * @return true if the conversion succeeded
     */
    public boolean process() {
        try {
            preprocess();
            
            // TODO locate a matching template it none is set
    
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
                    row.put(ROW_OBJECT_NAME, new Row(lineNumber));
                    try {
                        Node result = template.convertRow(this, row, lineNumber);
                        if (result == null) {
                            messageReporter.report("Warning: no templates matched line " + lineNumber, lineNumber);
                        }
                    } catch (Exception e) {
                        if (!(e instanceof NullResult)) {
                            messageReporter.report("Error: " + e, lineNumber);
                            log.error("Error processing line " + lineNumber, e);
                        } else {
                            messageReporter.report("Warning: no templates matched line " + lineNumber + ", " + e, lineNumber);
                        }
                        messageReporter.failed();
                    }
                } else {
                    break;
                }
                lineNumber++;
            }
        } catch (IOException e) {
            messageReporter.report("Problem reading next line of source");
            messageReporter.failed();
        }
        messageReporter.setState(TaskState.Terminated);
        close();
        
        return messageReporter.succeeded();
    }
    
    private void preprocess() throws IOException {
        getTemplate().preamble(this);
        
        Node dataset = NodeFactory.createAnon();
        
        Object baseURI = env.get(BASE_OBJECT_NAME);
        if (baseURI != null) {
            dataset = NodeFactory.createURI(baseURI.toString());
            env.put(DATASET_OBJECT_NAME, dataset);
        }

        // Check for linkedcsv-style preamble
        if (hasPreamble) {
            while (true) {
                peekRow = dataSource.readNext();
                lineNumber++;
                if (peekRow == null || peekRow.length == 0 || peekRow[0].isEmpty()) {
                    break;
                }
                if (peekRow[0].equals(META)) {
                    if (peekRow.length >= 3) {
                        if (peekRow[1].equals(BASE_OBJECT_NAME)) {
                            String base = peekRow[2];
                            env.put(BASE_OBJECT_NAME, base);
                            dataset = NodeFactory.createURI(base);
                            env.put(DATASET_OBJECT_NAME, dataset);
                        } else {
                            try {
                                Node prop = new Pattern(peekRow[1], dataContext).evaluateAsURINode(env);
                                Node value = new Pattern(peekRow[2], dataContext).evaluateAsNode(env);
                                getOutputStream().triple( new Triple(dataset, prop, value) );
                            } catch (Exception e) {
                                messageReporter.report("Failed to process metadata row: " + e, lineNumber);
                            }
                        }
                    } else {
                        messageReporter.report("Badly formed metadata row", lineNumber);
                    }
                } else {
                    messageReporter.report("Unrecognized preamble row: " + peekRow[0], lineNumber);
                }
            }
        }
    }
    
    private BindingEnv nextRow() throws IOException {
        if (dataSource != null) {
            String[] rowValues = (peekRow != null) ? peekRow : dataSource.readNext(); 
            peekRow = null;
            if (rowValues == null || rowValues.length == 0) {
                return null;
            }
            BindingEnv row = new BindingEnv( env );
            for (int i = 0; i < headers.length; i++) {
                row.put(headers[i], ValueFactory.asValue(rowValues[i]));
            }
            return row;
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
    

    /**
     * Called by template implementations to test for pattern matching errors during debugging
     */
    public void debugCheck(BindingEnv row, int rowNumber, Pattern p) {
        if (isDebugging()) {
            try {
                p.evaluate(row);
            } catch (Exception e) {
                getMessageReporter().report("Debug: pattern " + p + " failed to match environment " + row, rowNumber);
            }
        }
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
