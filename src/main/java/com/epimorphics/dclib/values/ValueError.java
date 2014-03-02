/******************************************************************
 * File:        ValueError.java
 * Created by:  Dave Reynolds
 * Created on:  2 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.hp.hpl.jena.graph.Node;

public class ValueError implements Value {
    protected String errorMessage;
    protected boolean warning = false;
    
    public ValueError(String msg) {
        errorMessage = msg;
    }
    
    public ValueError(String msg, boolean warning) {
        errorMessage = msg;
        this.warning = warning;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isFatal() {
        return !warning;
    }
    
    public boolean isWarning() {
        return warning;
    }
    
    public void setWarning(boolean warning) {
        this.warning = warning;
    }
    
    @Override
    public boolean isNull() {
        return false;
    }
    
    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public Value getString() {
        return this;
    }

    @Override
    public Value asString() {
        return this;
    }

    @Override
    public boolean isMulti() {
        return false;
    }

    @Override
    public Object[] getValues() {
        return null;
    }

    @Override
    public Node asNode() {
        return null;
    }

    @Override
    public String getDatatype() {
        return null;
    }

    @Override
    public Value append(Value val) {
        return this;
    }

}
