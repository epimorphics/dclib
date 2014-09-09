/******************************************************************
 * File:        ValueBase.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.riot.system.StreamRDF;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.MapSource;
import com.epimorphics.dclib.framework.MatchFailed;
import com.epimorphics.dclib.framework.NullResult;
import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.tasks.ProgressReporter;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * A simple packaged value.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class ValueBase<T> implements Value {
    protected T value;
    
    public ValueBase(T value) {
        this.value = value;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }
    
    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Value getString() {
        return asString();
    }
    
    @Override
    public Value asString() {
        return wrap(toString());
    }
    
    @Override
    public Value append(Value val) {
        if (val.isMulti()) {
            Value[] values = val.getValues();
            Value[] results = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                results[i] = append( values[i] );
            }
            return new ValueArray(results);
        } else {
            return new ValueString(toString() + val.toString());
        }
    }
    
    public Value append(String str) {
        return append( new ValueString(str) );
    }

    @Override
    public boolean isMulti() {
        return false;
    }

    @Override
    public Value[] getValues() {
        return new Value[]{this};
    }
    
    protected void reportError(String msg) {
        ProgressReporter reporter = ConverterProcess.get().getMessageReporter();
        reporter.reportError(msg);
    }
    
    // Value methods applicable to any type
    
    public Object datatype(String typeURI) {
        return NodeFactory.createLiteral(toString(), typeFor(typeURI));
    }
    
    protected RDFDatatype typeFor(String typeURI) {
        return TypeMapper.getInstance().getSafeTypeByName( expandTypeURI(typeURI) );
    }
    
    protected String expandTypeURI(String typeURI) {
        typeURI = ConverterProcess.getGlobalDataContext().expandURI(typeURI);
        if (typeURI.startsWith("xsd:")) {
            // Hardwired xsd: even if the prefix mapping doesn't have it
            typeURI = typeURI.replace("xsd:", XSD.getURI());
        }
        return typeURI;
    }
    
    public Object format(String fmtstr) {
        return new ValueString(String.format(fmtstr, value));
    }

    public boolean isString() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isDate() {
        return false;
    }
    
    public Value asNumber() {
        ValueNumber v = new ValueNumber(toString());
        if (v.isNull()) {
            reportError("Could not convert " + value + " to a number");
        }
        return v;
    }
    
    public Boolean asBoolean() {
        return Boolean.valueOf(toString());
    }
    
    public Value map(String mapsource, boolean matchRequried) {
        ConverterProcess proc = ConverterProcess.get();
        MapSource source = proc.getDataContext().getSource(mapsource);
        Node n = source.lookup(toString());
        if (n == null) {
            String msg = "Value '" + value + "' not found in source " + mapsource;
            if (matchRequried) {
                return new ValueError(msg);
            } else {
                throw new NullResult(msg);
            }
        }
        source.enrich(proc.getOutputStream(), n);
        return new ValueNode(n);
    }
    
    public Value map(String mapsource) {
        ConverterProcess proc = ConverterProcess.get();
        MapSource source = proc.getDataContext().getSource(mapsource);
        Node n = source.lookup(toString());
        if (n == null) {
            throw new MatchFailed("Value '" + value + "' not found in source " + mapsource);
        }
        source.enrich(proc.getOutputStream(), n);
        return new ValueNode(n);
    }
    
    public Value map(String[] mapsources, Object deflt) {
        ConverterProcess proc = ConverterProcess.get();
        for (String mapsource : mapsources) { 
            MapSource source = proc.getDataContext().getSource(mapsource);
            Node n = source.lookup(toString());
            if (n != null) {
                source.enrich(proc.getOutputStream(), n);
                return new ValueNode(n);
            }
        }
        if (deflt instanceof Value) {
            return (Value)deflt;
        } else {
            return ValueFactory.asValue(deflt.toString());
        }
    }
    
    public Value asDate(String format, String typeURI) {
        return ValueDate.parse(toString(), format, expandTypeURI(typeURI));
    }
    
    public Value asDate(String typeURI) {
        return ValueDate.parse(toString(), expandTypeURI(typeURI));
    }

    
    protected ValueString wrap(String s) {
        return new ValueString(s);
    }
    public Value toLowerCase() {
        return wrap(toString().toLowerCase());
    }
    
    public Value toUpperCase() {
        return wrap(toString().toUpperCase());
    }
    
    public Value toSegment() {
        return wrap( NameUtils.safeName(toString()) );
    }
    
    public Value toCleanSegment() {
        String seg = toString().toLowerCase().replace("'", "");
        seg =  seg.replaceAll("[^@$a-zA-Z0-9\\.~]+", "-");
        if (seg.endsWith("-")) {
            seg = seg.substring(0, seg.length()-1);
        }
        return wrap( seg );
    }
    
    public Value toSegment(String repl) {
        return wrap( NameUtils.safeName(toString()).replaceAll("_", repl) );
    }
    
    public Value trim() {
        return wrap( toString().trim() );
    }
    
    public Value substring(int offset) {
        return wrap( toString().substring(offset) );
    }
    
    public Value substring(int start, int end) {
        return new ValueString( toString().substring(start, end) );
    }

    public Value replaceAll(String regex, String replacement) {
        return wrap( toString().replaceAll(regex, replacement) );
    }

    public Value regex(String regex) {
        Matcher m = Pattern.compile(regex).matcher(toString());
        if (m.matches()) {
            if (m.groupCount() > 0) {
                return wrap( m.group(1));
            } else {
                return wrap(toString());
            }
        } else {
            throw new NullResult("Regex " + regex + " did not match");
        }
    }
    
    public boolean matches(String regex) {
        return toString().matches(regex);
    }
    
    public Value lastSegment() {
        return new ValueString( RDFUtil.getLocalname( toString() ) );
    }
    
    public Node lang(String lang) {
        return NodeFactory.createLiteral(toString(), lang, false);
    }
    
    /**
     * If the node is a URI node then fetch any data
     * at that URI and merge that into the output model 
     * @return
     */
    public Value fetch() {
        ConverterProcess proc = ConverterProcess.get();
        String uri = asURI();
        Model model = proc.fetchModel(uri);
        if (model != null) {
            StreamRDF out = proc.getOutputStream();
            ExtendedIterator<Triple> it = model.getGraph().find(null, null, null);
            while (it.hasNext()) {
                out.triple(it.next());
            }
        }
        return this;
    }
    
    public Value fetch(String...strings) {
        ConverterProcess proc = ConverterProcess.get();
        String uri = asURI();
        Model model = proc.fetchModel(uri);
        if (model != null) {
            StreamRDF out = proc.getOutputStream();
            for (String puri : strings) {
                puri = proc.getDataContext().getPrefixes().expandPrefix(puri);
                Node p = NodeFactory.createURI(puri);
                Node s = NodeFactory.createURI(uri );
                ExtendedIterator<Triple> it = model.getGraph().find(s, p, null);
                while (it.hasNext()) {
                    out.triple(it.next());
                }
            }
        }
        return this;
    }
    
    /**
     * Return the value as a RDFNodeWrapper for RDF-specific scripting.
     * Only works if not streaming.
     */
    public RDFNodeWrapper asRDFNode() {
        ConverterProcess proc = ConverterProcess.get();
        Model model = proc.getModel();
        model.setNsPrefixes( proc.getDataContext().getPrefixes() );
        ModelWrapper wmodel = new ModelWrapper(model);
        if (value instanceof Node ) {
            return wmodel.getNode( model.asRDFNode((Node)value) );
        } else if (value instanceof RDFNode || value instanceof RDFNodeWrapper) {
            return wmodel.getNode( value );
        } else {
            return wmodel.getNode( value.toString() );
        }
    }
    
    protected String asURI() {
        if (value instanceof Node && ((Node)value).isURI()) {
            return ((Node)value).getURI();
        } else if (value instanceof Resource) {
            return ((Resource)value).getURI();
        } else {
            return value.toString();
        }
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof ValueBase<?>) {
            return value.equals( ((ValueBase<?>)other).value );
        } else {
            return value.equals(other);
        }
    }
}

