/******************************************************************
 * File:        TestBasicConverters.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class TestBasicConverters {
    
    @Test
    public void testResourceMap() throws IOException {
        checkAgainstExpected("test/simple-skos-template.json", "test/test-map.csv", "test/test-map-result.ttl");
        checkAgainstExpected("test/template-multi-value.json", "test/test-map.csv", "test/test-map-result-multi.ttl");
    }

    
    // Multiline templates not supported
    // @Test
    public void testMultiLineTemplates() throws IOException {
        checkAgainstExpected("test/multi-line-skos-template.json", "test/test-map.csv", "test/test-map-result.ttl");
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
        checkAgainstExpected("test/skos-collection-with-member.json", "test/test-map.csv", "test/skos-collection-result-with-member.ttl");
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
        assertNull( convert("test/dept-type-required.json", "test/dept-type-data-error.csv") );
        
        // Check map error reporting
        ConverterService service = new ConverterService();
        service.getDataContext().registerTemplate("test/simple-skos-template.json");
        service.put("$base", "http://example.com/");
        SimpleProgressMonitor monitor = new SimpleProgressMonitor();
        service.simpleConvert("test/dept-type-required.json", "test/dept-type-data-error.csv", monitor);
        assertFalse( monitor.succeeded() );
        assertEquals(1, monitor.getMessages().size());
//        System.out.println(monitor.getMessages().get(0));

        // Non-sensical example to provide test of asNodeURI in situ
        convert("test/dept-type-inv.json", "test/dept-type-data.csv");

    }
    
    @Test
    public void testNumericMapping() throws IOException {
        checkAgainstExpected("test/sampling-points.json", "test/sampling-points.csv", "test/sampling-points.ttl");
        checkAgainstExpected("test/sampling-points-chained.json", "test/sampling-points.csv", "test/sampling-points-chained.ttl");
    }
    
    @Test
    public void testRDFMapping() throws IOException {
        checkAgainstExpected("test/map-rdf-test-root.json", "test/map-rdf-test.csv", "test/map-rdf-root-result.ttl");
        checkAgainstExpected("test/map-rdf-test.json", "test/map-rdf-test.csv", "test/map-rdf-result.ttl");
        checkAgainstExpected("test/map-rdf-test-type.json", "test/map-rdf-test.csv", "test/map-rdf-result.ttl");
    }
    
    @Test
    public void testBadURIs() throws IOException {
        expectError("test/baduris-template1.json", "test/test-map.csv");
        expectError("test/baduris-template2.json", "test/test-map.csv");
        expectError("test/baduris-template3.json", "test/test-map.csv");
    }
    

    public static Model convert(String templateFile, String dataFile) throws IOException {
        ConverterService service = new ConverterService();
        service.getDataContext().registerTemplate("test/simple-skos-template.json");
        service.put("$base", "http://example.com/");
        SimpleProgressMonitor monitor = new SimpleProgressMonitor();
        Model m = service.simpleConvert(templateFile, dataFile, monitor);
        return m;
    }

    public static void expectError(String templateFile, String dataFile) throws IOException {
        ConverterService service = new ConverterService();
        service.getDataContext().registerTemplate("test/simple-skos-template.json");
        service.put("$base", "http://example.com/");
        SimpleProgressMonitor monitor = new SimpleProgressMonitor();
        Model m = service.simpleConvert(templateFile, dataFile, monitor);
        assertNull(m);
        assertTrue(!monitor.succeeded());
        assertTrue(monitor.getMessages().size() > 1);
//        System.out.println(monitor.getMessages().get(0));
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
