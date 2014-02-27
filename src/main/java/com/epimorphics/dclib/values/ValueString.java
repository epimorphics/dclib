/******************************************************************
 * File:        ValueString.java
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
import com.hp.hpl.jena.graph.Node;

public class ValueString extends ValueBase<String> implements Value {
    
    public ValueString(String value, ConverterProcess proc) {
        super(value, proc);
    }

    public ValueStringArray split(String pattern) {
        return new ValueStringArray( value.split(pattern), proc );
    }
    
    @Override
    public String toString() {
        return value;
    }

    @Override
    public Value asString() {
        return this;
    }
    
    public Value asNumber() {
        return new ValueNumber(value, proc);
    }
    
    public String toLowerCase() {
        return value.toLowerCase();
    }
    
    public String toUpperCase() {
        return value.toUpperCase();
    }
    
    public String toSegment() {
        return NameUtils.safeName(value);
    }
    
    public Object toNumber() {
        return ValueFactory.asValue(value, proc);
    }
    
    public Object trim() {
        return new ValueString( value.trim(), proc );
    }
    
    public Object substring(int offset) {
        return new ValueString( value.substring(offset), proc );
    }
    
    public Object substring(int start, int end) {
        return new ValueString( value.substring(start, end), proc );
    }
    
    public Object regex(String regex) {
        Matcher m = Pattern.compile(regex).matcher(value);
        if (m.matches()) {
            if (m.groupCount() > 0) {
                return new ValueString( m.group(1), proc );
            } else {
                return this;
            }
        } else {
            throw new NullResult("Regex " + regex + " did not match");
        }
    }
    
    public boolean matches(String regex) {
        return value.matches(regex);
    }
    
    public Object lastSegment() {
        return new ValueString( RDFUtil.getLocalname( value ), proc );
    }
    
    public Node map(String mapsource, boolean matchRequried) {
        Node n = proc.getDataContext().getSource(mapsource).lookup(value);
        if (n == null) {
            String msg = "Value '" + value + "' not found in source " + mapsource;
            if (matchRequried) {
                // Can't thow an exception passed the execution context so have to report directly
                ProgressReporter reporter = proc.getMessageReporter();
                reporter.report(msg);
                reporter.failed();
            } else {
                throw new NullResult(msg);
            }
        }
        return n;
    }
    
    public Node map(String mapsource) {
        Node n = proc.getDataContext().getSource(mapsource).lookup(value);
        if (n == null) {
            throw new MatchFailed("Value '" + value + "' not found in source " + mapsource);
        }
        return n;
    }
    
}
