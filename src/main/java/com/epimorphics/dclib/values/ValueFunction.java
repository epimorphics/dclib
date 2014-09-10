/******************************************************************
 * File:        ValueFunction.java
 * Created by:  Dave Reynolds
 * Created on:  10 Sep 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import org.apache.commons.jexl2.Script;

import com.epimorphics.dclib.framework.BindingEnv;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * Wraps up a Jexl script as an executable function
 */
public class ValueFunction implements Value {
    public static final String ARG_NAME="$$";
    
    protected Script script;
    protected BindingEnv env;
    
    public ValueFunction(Script script) {
        this.script = script;
    }

    public void setBindingEnv(BindingEnv env) {
        this.env = env;
    }
    
    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public Object getValue() {
        return asString();
    }

    @Override
    public Value getString() {
        return asString();
    }

    @Override
    public Value asString() {
        return new ValueString("<function>");
    }

    @Override
    public boolean isMulti() {
        return false;
    }

    @Override
    public Value[] getValues() {
        return new Value[]{this};
    }

    @Override
    public Node asNode() {
        return NodeFactory.createAnon();
    }

    @Override
    public String getDatatype() {
        return null;
    }

    @Override
    public Value append(Value val) {
        return val;
    }

    public Object apply(Object arg) {
        BindingEnv call = new BindingEnv(env);
        call.put(ARG_NAME, arg);
        Object result = script.execute( call );
        return result;
    }
}
