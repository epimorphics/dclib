/******************************************************************
 * File:        TestDataContext.java
 * Created by:  Dave Reynolds
 * Created on:  12 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.dclib.templates.TemplateBase;

public class TestDataContext {

    @Test
    public void testBasicDataContext() {
        DataContext base = new DataContext();
        base.getGlobalEnv().put("key1", "value1");
        base.getGlobalEnv().put("key2", "value2");
        Template t1 = new TestTemplate("t1");
        base.registerTemplate(t1);
        
        DataContext child = new DataContext( base );
        Template t2 = new TestTemplate("t2");
        child.registerTemplate(t2);
        child.getGlobalEnv().put("key2", "value2new");
        child.getGlobalEnv().put("key3", "value3");
        
        assertEquals("value3", child.getGlobalEnv().get("key3"));
        assertEquals("value2new", child.getGlobalEnv().get("key2"));
        assertEquals("value1", child.getGlobalEnv().get("key1"));
        
        assertNotNull( child.getTemplate("t2") );
        assertNotNull( child.getTemplate("t1") );
    }
    
    static public class TestTemplate extends TemplateBase implements Template {

        public TestTemplate(String name) {
            super(mkJSON(name));
        }
        
        private static JsonObject mkJSON(String name) {
            JsonObject json = new JsonObject();
            json.put("name", name);
            return json;
        }
        
    }
    
}
