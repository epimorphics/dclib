/******************************************************************
 * File:        ValueBase.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.tasks.ProgressReporter;
import com.hp.hpl.jena.datatypes.TypeMapper;
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
    public Object getValue() {
        return value;
    }

    @Override
    public Value getString() {
        return asString();
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
        reporter.failed();
    }
    
    // Value methods applicable to any type
    
    public Object datatype(String typeURI) {
        typeURI = proc.getDataContext().expandURI(typeURI);
        if (typeURI.startsWith("xsd:")) {
            // Hardwired xsd: even if the prefix mapping doesn't have it
            typeURI = typeURI.replace("xsd:", XSD.getURI());
        }
        return NodeFactory.createLiteral(toString(), TypeMapper.getInstance().getSafeTypeByName(typeURI));
    }
    
    public Object format(String fmtstr) {
        return new ValueString(String.format(fmtstr, value), proc);
    }

}
