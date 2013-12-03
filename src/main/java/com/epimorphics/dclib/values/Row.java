/******************************************************************
 * File:        row.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * Represent information on the current row.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Row {
    protected int number;
    protected String uuid;
    protected Node bNode;
    protected Map<Object, Node> bNodes;
    
    public Row(int lineNumber) {
        this.number = lineNumber;
    }
    
    public int getNumber() {
        return number;
    }
    
    public String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }
    
    public Node getBnode() {
        if (bNode == null) {
            bNode = NodeFactory.createAnon();
        }
        return bNode;
    }
    
    public Node bnodeFor(Object key) {
        if (bNodes == null) {
            bNodes = new HashMap<Object, Node>();
        }
        Node bnode = bNodes.get(key);
        if (bnode == null) {
            bnode = NodeFactory.createAnon();
            bNodes.put(key, bnode);
        }
        return bnode;
    }
}
