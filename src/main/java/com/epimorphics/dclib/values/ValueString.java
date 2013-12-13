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

import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.MatchFailed;
import com.epimorphics.dclib.framework.NullResult;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.graph.Node;

public class ValueString extends ValueBase<String> implements Value {
    
    public ValueString(String value, DataContext dc) {
        super(value, dc);
    }

    public ValueStringArray split(String pattern) {
        return new ValueStringArray( value.split(pattern), dc );
    }
    
    @Override
    public String toString() {
        return value;
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
        return ValueFactory.asValue(value, dc);
    }
    
    public Object trim() {
        return new ValueString( value.trim(), dc );
    }
    
    public Object substring(int offset) {
        return new ValueString( value.substring(offset), dc );
    }
    
    public Object substring(int start, int end) {
        return new ValueString( value.substring(start, end), dc );
    }
    
    public Object regex(String regex) {
        Matcher m = Pattern.compile(regex).matcher(value);
        if (m.matches()) {
            if (m.groupCount() > 0) {
                return new ValueString( m.group(1), dc );
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
        return new ValueString( RDFUtil.getLocalname( value ), dc );
    }
    
    public Node map(String mapsource, boolean matchRequried) {
        Node n = dc.getSource(mapsource).lookup(value);
        if (n == null) {
            String msg = "Value '" + value + "' not found in source " + mapsource;
            if (matchRequried) {
                throw new MatchFailed(msg);
            } else {
                throw new NullResult(msg);
            }
        }
        return n;
    }
    
    public Node map(String mapsource) {
        Node n = dc.getSource(mapsource).lookup(value);
        if (n == null) {
            throw new MatchFailed("Value '" + value + "' not found in source " + mapsource);
        }
        return n;
    }
    
}
