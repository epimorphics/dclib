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

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.MatchFailed;
import com.epimorphics.dclib.framework.NullResult;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.tasks.ProgressReporter;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * A simple packaged value.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class ValueBase<T> implements Value {
    protected ConverterProcess proc;
    protected T value;
    
    public ValueBase(T value, ConverterProcess config) {
        this.value = value;
        this.proc = config;
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
        String base = toString();
        if (val.isMulti()) {
            Object[] values = val.getValues();
            String[] results = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                results[i] = base + values[i];
            }
            return new ValueStringArray(results, proc);
        } else {
            return new ValueString(base + val.toString(), proc);
        }
    }

    @Override
    public boolean isMulti() {
        return false;
    }

    @Override
    public Object[] getValues() {
        return new Object[]{value};
    }
    
    protected void reportError(String msg) {
        ProgressReporter reporter = proc.getMessageReporter();
        reporter.report(msg);
        reporter.setFailed();
    }
    
    // Value methods applicable to any type
    
    public Object datatype(String typeURI) {
        return NodeFactory.createLiteral(toString(), typeFor(typeURI));
    }
    
    protected RDFDatatype typeFor(String typeURI) {
        return TypeMapper.getInstance().getSafeTypeByName( expandTypeURI(typeURI) );
    }
    
    protected String expandTypeURI(String typeURI) {
        if (proc != null) {
            typeURI = proc.getDataContext().expandURI(typeURI);
        }
        if (typeURI.startsWith("xsd:")) {
            // Hardwired xsd: even if the prefix mapping doesn't have it
            typeURI = typeURI.replace("xsd:", XSD.getURI());
        }
        return typeURI;
    }
    
    public Object format(String fmtstr) {
        return new ValueString(String.format(fmtstr, value), proc);
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
        ValueNumber v = new ValueNumber(toString(), proc);
        if (v.isNull()) {
            reportError("Could not convert " + value + " to a number");
        }
        return v;
    }
    
    public Boolean asBoolean() {
        return Boolean.valueOf(toString());
    }
    
    public Value map(String mapsource, boolean matchRequried) {
        Node n = proc.getDataContext().getSource(mapsource).lookup(toString());
        if (n == null) {
            String msg = "Value '" + value + "' not found in source " + mapsource;
            if (matchRequried) {
                return new ValueError(msg);
            } else {
                throw new NullResult(msg);
            }
        }
        return new ValueNode(n, proc);
    }
    
    public Value map(String mapsource) {
        Node n = proc.getDataContext().getSource(mapsource).lookup(toString());
        if (n == null) {
            throw new MatchFailed("Value '" + value + "' not found in source " + mapsource);
        }
        return new ValueNode(n, proc);
    }
    
    public Value asDate(String format, String typeURI) {
        return ValueDate.parse(toString(), format, expandTypeURI(typeURI), proc);
    }
    
    public Value asDate(String typeURI) {
        return ValueDate.parse(toString(), expandTypeURI(typeURI), proc);
    }

    
    protected ValueString wrap(String s) {
        return new ValueString(s, proc);
    }
    public ValueString toLowerCase() {
        return wrap(toString().toLowerCase());
    }
    
    public ValueString toUpperCase() {
        return wrap(toString().toUpperCase());
    }
    
    public ValueString toSegment() {
        return wrap( NameUtils.safeName(toString()) );
    }
    
    public ValueString trim() {
        return wrap( toString().trim() );
    }
    
    public ValueString substring(int offset) {
        return wrap( toString().substring(offset) );
    }
    
    public ValueString substring(int start, int end) {
        return new ValueString( toString().substring(start, end), proc );
    }
    
    public ValueString regex(String regex) {
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
    
    public ValueString lastSegment() {
        return new ValueString( RDFUtil.getLocalname( toString() ), proc );
    }
    
    public Node lang(String lang) {
        return NodeFactory.createLiteral(toString(), lang, false);
    }
}

