/******************************************************************
 * File:        ValueNode.java
 * Created by:  Dave Reynolds
 * Created on:  2 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.hp.hpl.jena.graph.Node;

public class ValueNode extends ValueBase<Node> implements Value{

    public ValueNode(Node value) {
        super(value);
    }
    
    @Override
    public String toString() {
        if (value.isLiteral()) {
            return value.getLiteralLexicalForm();
        } else if (value.isURI()) {
            return value.getURI();
        } else {
            return value.getBlankNodeId().toString();
        }
    }

    @Override
    public Node asNode() {
        return value;
    }

    @Override
    public String getDatatype() {
        if (value.isLiteral()) {
            return value.getLiteralDatatypeURI();
        } else {
            return null;
        }
    }


}
