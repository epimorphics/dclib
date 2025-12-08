/******************************************************************
 * File:        TestBindingEnv.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestBindingEnv {
    
    @Test
    public void testInheritance() {
        BindingEnv parent = new BindingEnv();
        
        parent.put("key1", "foo");
        parent.put("key2", "bar");
        
        BindingEnv child = new BindingEnv(parent);
        child.put("key1", "child");
        
        assertEquals("child", child.get("key1"));
        assertEquals("bar",   child.get("key2"));
        assertEquals("foo",   parent.get("key1"));
    }
 

}
