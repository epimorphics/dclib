/******************************************************************
 * File:        TestBasicConverters.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class TestBasicConverters {
    
    @Test
    public void testResourceMap() throws IOException {
        ConverterService service = new ConverterService();
        service.put("$base", "http://example.com/");
        Model m = service.simpleConvert("test/simple-skos-template.json", "test/test-map.csv");
        assertNotNull(m);
        assertTrue(m.isIsomorphicWith( FileManager.get().loadModel("test/test-map-result.ttl") ));
    }

}
