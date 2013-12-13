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

import com.epimorphics.rdfutil.RDFUtil;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class TestBasicConverters {
    
    @Test
    public void testResourceMap() throws IOException {
        checkAgainstExpected("test/simple-skos-template.json", "test/test-map.csv", "test/test-map-result.ttl");
        checkAgainstExpected("test/template-multi-value.json", "test/test-map.csv", "test/test-map-result-multi.ttl");
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
    
    @Test
    public void testTemplateInclusion() throws IOException {
        checkAgainstExpected("test/hierarchy-complete.json", "test/hierarchy1.csv", "test/hierarchy-result.ttl");
    }
    
    @Test
    public void testDatasetReference() throws IOException {
        checkAgainstExpected("test/skos-collection.json", "test/test-map.csv", "test/skos-collection-result.ttl");
        checkAgainstExpected("test/hierarchy-complete-top.json", "test/hierarchy1.csv", "test/hierarchy-top-result.ttl");
    }
    
    @Test
    public void testMetadata() throws IOException {
        checkAgainstExpected("test/hierarchy-complete-top-meta.json", "test/hierarchy1-meta.csv", "test/hierarchy-top-meta-result.ttl");
    }

    @Test
    public void testComposite() throws IOException {
        checkAgainstExpected("test/top.json", "test/test-map.csv", "test/top.ttl");
    }
    
    @Test
    public void testMapping() throws IOException {
        checkAgainstExpected("test/dept-type.json", "test/dept-type-data.csv", "test/dept-type-result.ttl");
        checkAgainstExpected("test/dept-type.json", "test/dept-type-data-error.csv", "test/dept-type-result-error.ttl");
        // Next one should fail but current doesn't
        checkAgainstExpected("test/dept-type-required.json", "test/dept-type-data-error.csv", "test/dept-type-result-error.ttl");
    }
    
    public static Model convert(String templateFile, String dataFile) throws IOException {
        ConverterService service = new ConverterService();
        service.getDataContext().registerTemplate("test/simple-skos-template.json");
        service.put("$base", "http://example.com/");
        Model m = service.simpleConvert(templateFile, dataFile);
        return m;
    }
    
    public static void checkAgainstExpected(String templateFile, String dataFile, String resultFile) throws IOException {
        Model m = convert(templateFile, dataFile);
        assertNotNull(m);
        String DUMMY = "http://example.com/DONOTUSE/";
        Model expected = FileManager.get().loadModel(resultFile, DUMMY, "Turtle");
        expected = RDFUtil.mapNamespace(expected, DUMMY, "");
        boolean same = m.isIsomorphicWith(expected);
        if (!same) {
            System.err.println("Result mismatch, result was:");
            m.write(System.err, "Turtle");
            System.err.println("Expected:");
            expected.write(System.err, "Turtle");
        }
        assertTrue( same );
        
    }
}
