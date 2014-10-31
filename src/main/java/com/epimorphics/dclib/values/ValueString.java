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
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.XSD;

public class ValueString extends ValueBase<String> implements Value {
    
    public ValueString(String value) {
        super(value);
    }

    public ValueArray split(String pattern) {
        return new ValueArray( value.split(pattern) );
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
    
    // This strange test is because blindly applying LANGSTR to a large literal can take minutes
    // Technically BCP47 strings are unbounded (unlimited number of extensions) in practice they are not
    static int MAX_LANGSTR = 16;
    
    @Override
    public Node asNode() {
        int split = value.lastIndexOf("@");
        if (split != -1 && (value.length() - split < MAX_LANGSTR)) {
            String possLangTag = value.substring(split);
            Matcher matcher = LANGSTR.matcher(possLangTag);
            if (matcher.find()) {
                String lang = matcher.group(1);
                if (value.charAt(split-1) == '@') {
                    return NodeFactory.createLiteral( value.substring(0, split) + lang );
                } else if (value.charAt(split-1) == '\\') {
                    return NodeFactory.createLiteral( value.substring(0, split-1) + "@" + lang );
                } else {
                    return NodeFactory.createLiteral(value.substring(0, split), lang, false);
                }
            }
        }
        
        // Check for typed literal
        split = value.lastIndexOf("^^");
        if (split != -1) {
            if (value.charAt(split-1) != '\\') {
                String lex = value.substring(0, split);
                String dt = value.substring(split+2);
                if (dt.startsWith("<") && dt.endsWith(">")) {
                    dt = dt.substring(1, dt.length() - 1);
                }
                dt = ConverterProcess.getGlobalDataContext().expandURI(dt);
                return NodeFactory.createLiteral(lex, TypeMapper.getInstance().getSafeTypeByName(dt));
            } else {
                return NodeFactory.createLiteral(value.substring(0,split-1) + value.substring(split));
            }
        }

        return NodeFactory.createLiteral( value );
    }

    @Override
    public String getDatatype() {
        return XSD.xstring.getURI();
    }
    

}
