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
        checkAgainstExpected("test/simple-skos-template.json", "test/test-map.csv", "test/test-map-result.ttl");
    }

    @Test
    public void testParameterized() throws IOException {
        checkAgainstExpected("test/parameterized-template.json", "test/test-parameterized.csv", "test/test-map-result.ttl");
        
        checkAgainstExpected("test/cond-template.json", "test/test-parameterized.csv", "test/test-map-result.ttl");
        checkAgainstExpected("test/cond-template.json", "test/test-map.csv", "test/test-map-result.ttl");
    }
    
    @Test
    public void testHierarchy() throws IOException {
        checkAgainstExpected("test/hierarchy.json", "test/hierarchy1.csv", "test/hierarchy-result.ttl");
        checkAgainstExpected("test/hierarchy2.json", "test/hierarchy2.csv", "test/hierarchy-result.ttl");
        checkAgainstExpected("test/hierarchy2.json", "test/hierarchy2b.csv", "test/hierarchy-result.ttl");
    }

    private void checkAgainstExpected(String templateFile, String dataFile, String resultFile) throws IOException {
        ConverterService service = new ConverterService();
        service.getDataContext().registerTemplate("test/simple-skos-template.json");
        service.put("$base", "http://example.com/");
        Model m = service.simpleConvert(templateFile, dataFile);
        assertNotNull(m);
        assertTrue(m.isIsomorphicWith( FileManager.get().loadModel(resultFile) ));
        
    }
}
