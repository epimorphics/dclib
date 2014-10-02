/******************************************************************
 * File:        RDFSparqlMapSource.java
 * Created by:  Dave Reynolds
 * Created on:  2 Oct 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.riot.system.StreamRDF;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.MapSource;
import com.epimorphics.dclib.templates.JSONConstants;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;

/**
 * MapSource in which:
 * <ul>
 *    <li>source data is a local RDF file </li>
 *    <li>a SPARQL query extracts a key and set of value bindings</li>
 *    <li>lookup is by exact match on the ?key, no lexical normalization</li>
 *    <li>default return value is the binding of ?value</li>
 *    <li>supports extended map call where other variable bindings can be looked up</li>
 * </ul>
 */
public class RDFSparqlMapSource extends MapSourceBaseBase implements MapSource {
    public static final String KEY_VARIABLE = "key";
    public static final String VALUE_VARIABLE = "value";
    
    protected Map<String, Map<String, Node>> table = new HashMap<String, Map<String,Node>>();
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        if (spec.hasKey(JSONConstants.SOURCE)) {
            if (spec.hasKey(JSONConstants.SOURCE_TYPE) && spec.hasKey(JSONConstants.QUERY)) {
                return spec.get(JSONConstants.SOURCE_TYPE).getAsString().value().equals(JSONConstants.RDF_SPARQL);
            }
        }
        return false;
    }

    public RDFSparqlMapSource(JsonObject spec, ConverterProcess proc) throws IOException {
        super(spec);
        String sourceFile = getRequiredField(JSONConstants.SOURCE);
        Model rdf = FileManager.get().loadModel( "file:" + findFile(sourceFile, proc) );
        
        String query = getRequiredField(JSONConstants.QUERY);
        query = PrefixUtils.expandQuery(query, proc.getDataContext().getPrefixes());
        
        QueryExecution qexec = QueryExecutionFactory.create(query, rdf);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution row = results.next();
                RDFNode keyn = row.get(KEY_VARIABLE);
                if (keyn == null || keyn.isAnon()) {
                    proc.getMessageReporter().report("Warning: ?key binding missing in RDFSparqlMapSource " + getName());
                } else {
                    String key = keyn.isURIResource() ? keyn.asResource().getURI() : keyn.asLiteral().getLexicalForm();
                    Map<String, Node> tableEntry = table.get(key);
                    if (tableEntry == null) {
                        tableEntry = new HashMap<>();
                        table.put(key, tableEntry);
                    }
                    for (Iterator<String> vars = row.varNames(); vars.hasNext();) {
                        String var = vars.next();
                        if ( ! var.equals(KEY_VARIABLE)) {
                            tableEntry.put(var, row.get(var).asNode());
                        }
                    }
                }
            }
        } finally {
            qexec.close();
        }
    }    
    

    @Override
    public Node lookup(String key) {
        return lookup(key, VALUE_VARIABLE);
    }

    @Override
    public Node lookup(String key, String valueToReturn) {
        Map<String, Node> tableEntry = table.get(key);
        if (tableEntry != null) {
            return tableEntry.get(valueToReturn);
        }
        return null;
    }

    @Override
    public void enrich(StreamRDF stream, Node match) {
        // No enrichment supported
    }



}
