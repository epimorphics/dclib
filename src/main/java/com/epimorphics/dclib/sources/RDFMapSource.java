/******************************************************************
 * File:        RDFMapSource.java
 * Created by:  Dave Reynolds
 * Created on:  15 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.riot.system.StreamRDF;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.MapSource;
import com.epimorphics.dclib.templates.JSONConstants;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.util.Closure;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A mapping source derived from an RDF file. The RDF will loaded into memory
 * during start up and then an in-memory mapping table created. So not
 * suitable for very large data sources.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RDFMapSource extends MapSourceBase implements MapSource {
    Model rdf;
    boolean enrichDescribe = false;
    List<Property> enrich = new ArrayList<>();
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        if (spec.hasKey(JSONConstants.SOURCE)) {
            if (spec.hasKey(JSONConstants.SOURCE_TYPE)) {
                return spec.get(JSONConstants.SOURCE_TYPE).getAsString().value().equals(JSONConstants.RDF);
            }
        }
        return false;
    }

    public RDFMapSource(JsonObject spec, ConverterProcess proc) throws IOException {
        super(spec);
        Property keyProp = asProperty( getField(JSONConstants.KEY), proc);
        Property valueProp = asProperty( getField(JSONConstants.VALUE), proc);
        Resource type = asResource( getField(JSONConstants.TYPE), proc);
        
        String sourceFile = getRequiredField(JSONConstants.SOURCE);
        rdf = FileManager.get().loadModel( "file:" + findFile(sourceFile, proc) );

        for (StmtIterator i = rdf.listStatements(null,  keyProp, (RDFNode)null); i.hasNext();) {
            Statement s = i.next();
            RDFNode keyNode = s.getObject();
            if (type == null ||  s.getSubject().hasProperty(RDF.type, type)) {
                String key = keyNode.isLiteral() ? keyNode.asLiteral().getLexicalForm() : keyNode.asResource().getURI();
                Node value = s.getSubject().asNode();
                if (valueProp != null) {
                    value = s.getSubject().getRequiredProperty(valueProp).getObject().asNode();
                }
                put(key, value);
            }
        }
        
        JsonValue enrichSpec = spec.get(JSONConstants.ENRICH);
        if (enrichSpec != null) {
            if (enrichSpec.isString()) {
                String enrichStr = enrichSpec.getAsString().value();
                if (enrichStr.equals("*")) {
                    enrichDescribe = true;
                } else {
                    enrich.add( asProperty(enrichStr, proc) );
                }
            } else if (enrichSpec.isArray()) {
                for (Iterator<JsonValue> i = enrichSpec.getAsArray().iterator(); i.hasNext();) {
                    JsonValue v = i.next();
                    if (v.isString()) {
                        enrich.add( asProperty(v.getAsString().value(), proc) );
                    } else {
                        throw new EpiException("Bad source configuration, enrich can only be a string or array of strings");
                    }
                }
            } else {
                throw new EpiException("Bad source configuration, enrich can only be a string or array of strings");
            }
        }
        
    }
    
    @Override
    public void enrich(StreamRDF stream, Node match) {
        if (match.isURI()) {
            Resource r = rdf.getResource( match.getURI() );
            if (enrichDescribe) {
                Model description = Closure.closure(r, false);
                ExtendedIterator<Triple> it = description.getGraph().find(null, null, null);
                while (it.hasNext()) {
                    stream.triple(it.next());
                }
            } else {
                for (Property p : enrich) {
                    for (StmtIterator si = r.listProperties(p); si.hasNext(); ) {
                        stream.triple( si.next().asTriple() ); 
                    }
                }
            }
        } 
    }
    
    private Property asProperty(String val, ConverterProcess proc) {
        return (val == null)  ? null : ResourceFactory.createProperty( asURI(val, proc) );
    }
    
    private Resource asResource(String val, ConverterProcess proc) {
        return (val == null)  ? null : ResourceFactory.createResource( asURI(val, proc) );
    }

    private String asURI(String val, ConverterProcess proc) {
        if (val == null) return null;
        if (val.startsWith("<") && val.endsWith(">")) {
            val = val.substring(1, val.length() - 1);
        }
        String uri = proc.getDataContext().expandURI(val);
        return uri;
    }
 
}