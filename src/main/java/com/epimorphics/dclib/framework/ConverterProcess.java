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
import java.util.Map.Entry;

import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.collections.map.LRUMap;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.stream.Locator;
import org.apache.jena.riot.system.stream.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.dclib.sources.CSVInput;
import com.epimorphics.dclib.templates.TemplateBase;
import com.epimorphics.dclib.values.Row;
import com.epimorphics.dclib.values.ValueDate;
import com.epimorphics.dclib.values.ValueFactory;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.tasks.ProgressMonitorReporter;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.tasks.TaskState;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * An instance of a converter running on a specific data configuration.
 * Provides access to all the context information, data, mapping sources
 * and output configuration required. Not thread safe.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ConverterProcess {
    static final Logger log = LoggerFactory.getLogger( ConverterProcess.class );
    
    public static final int MAX_FETCH_CACHE = 500;
    
    public static final String ROW_OBJECT_NAME = "$row";
    public static final String BASE_OBJECT_NAME = "$base";
    public static final String DATASET_OBJECT_NAME = "$dataset";
    public static final String FILE_NAME = "$filename";
    public static final String FILE_BASE_NAME = "$filebasename";
    public static final String ROOT_NAME = "$root";
    public static final String EXECUTION_TIME_NAME = "$exectime";
    
    static final String META = "meta";
    
    private static final ThreadLocal<ConverterProcess> current = new ThreadLocal<>();
    private static final DataContext defaultDC = new DataContext();

    protected int BATCH_SIZE = 1000;
    protected DataContext dataContext;
    protected ProgressMonitorReporter messageReporter = new SimpleProgressMonitor();
    
    protected CSVInput dataSource;

    protected boolean debug = false;
    protected boolean allowNullRows = true;
    
    protected Template template;
    protected BindingEnv env;
    protected Object state;  // Template - specific state information
    
    protected StreamRDF   outputStream;
    protected Model  result;   // May not be used if the stream is set directly  
    
    protected LRUMap fetchCache = new LRUMap(MAX_FETCH_CACHE);
    
	// During static class initialisation replaced the builtin Locator HTTP with one that doesn't accept "*/*" in
	// the mix.
	static {
		StreamManager stm = StreamManager.get();
		for (Locator l : stm.locators()) {
			if (l.getName().equals("LocatorHTTP")) {
				stm.remove(l);
				break;
			}
		}
		stm.addLocator(new LocatorHTTP());
	}
    
    public ConverterProcess(DataContext context, InputStream data) {
        dataContext = new DataContext( context );
        
        // Could flatten env here to avoid chaining lookup
        env = dataContext.getGlobalEnv();

        try {
            if (data != null) {
                // This is the normal path, null input is normally only used in testing
                dataSource = new CSVInput( data );
            }

            // Default is to converter into an in-memory model, can override by setting explicit StreamRDF dest
            setModel( ModelFactory.createDefaultModel() );
            result.setNsPrefixes( dataContext.getPrefixes() );
            
        } catch (Exception e) {
            messageReporter.reportError("Failed to read headerline of data");
            close();
            throw new EpiException("Failed to open data or read header line");
        }
    }
    
    /**
     * Return the current, thread specific, instance of the converter process
     */
    public static ConverterProcess get() {
        return current.get();
    }
    
    /**
     * Return the current, thread specific, data context. If there is no converter process
     * then returns the default data context.
     */
    public static DataContext getGlobalDataContext() {
        ConverterProcess proc = get();
        return proc == null ? defaultDC : proc.getDataContext();
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
     * Set to true to allow some rows to generate no results without error (just a warning)
     */
    public void setAllowNullRows(boolean allowed) {
        this.allowNullRows = allowed;
    }

    /**
     * Run the conversion process
     * @return true if the conversion succeeded
     */
    public boolean process() {
        try {
            current.set(this);
            Node now = RDFUtil.fromDateTime( System.currentTimeMillis() ).asNode();
            ValueDate exectime = new ValueDate( now );
            getEnv().put(EXECUTION_TIME_NAME, exectime);
            preprocess();
            
            // TODO locate a matching template it none is set
    
            if (messageReporter.getState() == TaskState.Terminated) {
                return messageReporter.succeeded();
            }
            messageReporter.setState( TaskState.Running );
            
            if ( ! template.isApplicableTo(getHeaders()) ) {
                messageReporter.reportError("Data shape does not match template, missing columns: " + ((TemplateBase)template).listMissingColumns(getHeaders()));
                return false;
            }
    
            boolean started = false;
            while(true) {
                int lineNumber = dataSource.getLineNumber();
//                log.debug("Line " + lineNumber);
                if (lineNumber % BATCH_SIZE == 0) {
                    messageReporter.report("Processing row " + lineNumber);
                }
                BindingEnv row = nextRow();
                if (row != null) {
                    started = true;
                    row.put(ROW_OBJECT_NAME, new Row(lineNumber));
                    try {
                        Node result = template.convertRow(this, row, lineNumber);
                        if (result == null) {
                            if (allowNullRows) {
                                messageReporter.report("Warning: no templates matched line " + lineNumber, lineNumber);
                            } else {
                                messageReporter.reportError("Error: no templates matched line " + lineNumber, lineNumber);
                            }
                        }
                    } catch (Exception e) {
                        if (!(e instanceof NullResult)) {
                            messageReporter.reportError("Error: " + e, lineNumber);
//                            log.error("Error processing line " + lineNumber, e);
                        } else {
                            if (allowNullRows) {
                                messageReporter.report("Warning: no templates matched line " + lineNumber + ", " + e, lineNumber);
                            } else {
                                messageReporter.reportError("Error: no templates matched line " + lineNumber + ", " + e, lineNumber);
                            }
                        }
                    }
                } else {
                    break;
                }
            }
            
            if (!started) {
                // No data rows, which means header shape hasn't been tested, disallow empty data 
                messageReporter.reportError("No data content to convert");
            }
            
        } catch (IOException e) {
            messageReporter.reportError("Problem reading next line of source");
        } catch (CsvValidationException csvve) {
            messageReporter.reportError("The CSV content was invalid: " + csvve.getMessage());
        } finally {
            current.set(null);
        }
        messageReporter.report("Processed " + (dataSource.getLineNumber() - 1) + " lines");
        messageReporter.setState(TaskState.Terminated);
        close();
        
        return messageReporter.succeeded();
    }
    
    /**
     * Evaluate a pattern in the context of this process.
     * Mostly used for testing
     */
    public Object evaluate(Pattern pattern, BindingEnv env, int rowNumber){
        try {
            current.set(this);
            return pattern.evaluate(env, this, rowNumber);
        } finally {
            current.set(null);
        }
    }
    
    /**
     * Evaluate a pattern in the context of this process.
     * Mostly used for testing
     */
    public Node evaluateAsNode(Pattern pattern, BindingEnv env, int rowNumber) {
        try {
            current.set(this);
            return pattern.evaluateAsNode(env, this, rowNumber);
        } finally {
            current.set(null);
        }
    }
    
    public BindingEnv nextRow() throws IOException {
        try {
            BindingEnv row = dataSource.nextRow();
            if (row == null) return null;
            BindingEnv wrapped = new BindingEnv(env);
            for (Entry<String, Object> entry : row.entrySet()) {
                wrapped.put(entry.getKey(), ValueFactory.asValue(entry.getValue().toString().trim()));
            }
            return wrapped;
        } catch (Exception e) {
            // Most likely problem here is bad data such as an unterminated line
            messageReporter.reportError("Error during CSV reading, unterminated final line? " + e, dataSource.getLineNumber());
            return null;
        }
    }
    
    public BindingEnv peekRow() {
        try {
            String[] row = dataSource.getPeekRow();
            String[] headers = dataSource.getHeaders();
            if (row == null) return null;
            BindingEnv wrapped = new BindingEnv(env);
            int safeLength = Math.min(row.length,headers.length);
            for (int i = 0; i < safeLength; i++) {
                wrapped.put(headers[i], ValueFactory.asValue(row[i].trim()));
            }
            return wrapped;
        } catch (Exception e) {
            // Most likely problem here is bad data such as an unterminated line
            messageReporter.reportError("Error during CSV reading, unterminated final line? " + e, dataSource.getLineNumber());
            return null;
        }
    }
    
    protected void preprocess() throws IOException, CsvValidationException {
        Node dataset = NodeFactory.createBlankNode();
        
        Object baseURI = env.get(BASE_OBJECT_NAME);
        if (baseURI != null) {
            dataset = NodeFactory.createURI(baseURI.toString());
            env.put(DATASET_OBJECT_NAME, dataset);
        }
        
        try {
            BindingEnv initialEnv = peekRow();
            if (initialEnv == null) {
                // now rows to peek to
                initialEnv = getEnv();
            }
            initialEnv.put(ROW_OBJECT_NAME, new Row(0));
            getTemplate().preamble(this, initialEnv);
        } catch (Exception e) {
            messageReporter.reportError("Problem with one-off preprocessing of template: " + e);
            log.error("Problem with one-off preprocessing of template", e);
        }

        // Check for linkedcsv-style preamble
        if (dataSource.hasPreamble()) {
            while (true) {
                String[] peekRow = dataSource.getPeekRow();
                if (peekRow == null || peekRow.length == 0 || peekRow[0].isEmpty()) {
                    break;
                }
                dataSource.advancePeek();
                if (peekRow[0].equals(META)) {
                    if (peekRow.length >= 3) {
                        if (peekRow[1].equals(BASE_OBJECT_NAME)) {
                            String base = peekRow[2];
                            env.put(BASE_OBJECT_NAME, base);
                            dataset = NodeFactory.createURI(base);
                            env.put(DATASET_OBJECT_NAME, dataset);
                        } else {
                            try {
                                Node prop = new Pattern(peekRow[1], dataContext).evaluateAsURINode(env, this, -1);
                                Node value = new Pattern(peekRow[2], dataContext).evaluateAsNode(env, this, -1);
                                getOutputStream().triple(Triple.create(dataset, prop, value));
                            } catch (Exception e) {
                                messageReporter.report("Failed to process metadata row: " + e, dataSource.getLineNumber());
                            }
                        }
                    } else {
                        messageReporter.report("Badly formed metadata row", dataSource.getLineNumber());
                    }
                } else {
                    messageReporter.report("Unrecognized preamble row: " + peekRow[0], dataSource.getLineNumber());
                }
            }
        }
    }
    
    private void close() {
        if (dataSource != null) {
            dataSource.close();
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
                p.evaluate(row, this, rowNumber);
            } catch (Exception e) {
                String msg = "Debug: Pattern " + p + " failed to match environment:\n" + row.toStringDeep();
//                log.warn(msg);
                getMessageReporter().report(msg, rowNumber);
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

    public ProgressMonitorReporter getMessageReporter() {
        return messageReporter;
    }

    public void setMessageReporter(ProgressMonitorReporter messageReporter) {
        this.messageReporter = messageReporter;
    }

    public String[] getHeaders() {
        return dataSource.getHeaders();
    }

    public CSVInput getDataSource() {
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
    

    /**
     * Fetch a remote (possibly cached) model from the given URI.
     * Return null if no data is found
     */
    
    public Model fetchModel(String uri) {
        Model model = (Model) fetchCache.get(uri);
        if (model == null) {
            try {
                log.info("fetching " + uri);  // TODO: TEMP
                long start = System.currentTimeMillis();
                model = RDFDataMgr.loadModel( uri );
                log.info("Fetched in " + NameUtils.formatDuration(System.currentTimeMillis() - start));
                if (model == null || model.isEmpty()) {
                    getMessageReporter().report("Warning: no data found at " + uri);
                } else {
                    fetchCache.put(uri, model);
                }
            } catch (Exception e) {
                getMessageReporter().report("Warning: exception fetching " + uri + ", " + e);
            }
        }
        return model;
    }

}
