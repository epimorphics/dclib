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
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.XSD;

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
        
    @Override
    public boolean isString() {
        return true;
    }
    
    static Pattern LANGSTR =  Pattern.compile(".*@([a-z\\-]+)$");

    @Override
    public Node asNode() {
        Matcher matcher = LANGSTR.matcher(value);
        if (matcher.matches()) {
            String lang = matcher.group(1);
            int split = value.length() - lang.length() - 1;
            char pre = value.charAt(split-1);
            if (value.charAt(split-1) == '@') {
                return NodeFactory.createLiteral( value.substring(0, split) + lang );
            } else {
                return NodeFactory.createLiteral(value.substring(0, split), lang, false);
            }
        }
        return NodeFactory.createLiteral( value );
    }

    @Override
    public String getDatatype() {
        return XSD.xstring.getURI();
    }
    

}
