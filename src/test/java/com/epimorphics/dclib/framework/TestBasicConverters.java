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
        checkAgainstExpected("test/hierarchy/hierarchy.json", "test/hierarchy/hierarchy1.csv", "test/hierarchy/hierarchy-result.ttl");
        checkAgainstExpected("test/hierarchy/hierarchy2.json", "test/hierarchy/hierarchy2.csv", "test/hierarchy/hierarchy-result.ttl");
        checkAgainstExpected("test/hierarchy/hierarchy2.json", "test/hierarchy/hierarchy2b.csv", "test/hierarchy/hierarchy-result.ttl");
    }
    
    @Test
    public void testTemplateInclusion() throws IOException {
        checkAgainstExpected("test/hierarchy/hierarchy-complete.json", "test/hierarchy/hierarchy1.csv", "test/hierarchy/hierarchy-result.ttl");
    }
    
    @Test
    public void testDatasetReference() throws IOException {
        checkAgainstExpected("test/skos-collection.json", "test/test-map.csv", "test/skos-collection-result.ttl");
        checkAgainstExpected("test/skos-collection-with-member.json", "test/test-map.csv", "test/skos-collection-result-with-member.ttl");
        checkAgainstExpected("test/hierarchy/hierarchy-complete-top.json", "test/hierarchy/hierarchy1.csv", "test/hierarchy/hierarchy-top-result.ttl");
    }
    
    @Test
    public void testMetadata() throws IOException {
        checkAgainstExpected("test/hierarchy/hierarchy-complete-top-meta.json", "test/hierarchy/hierarchy1-meta.csv", "test/hierarchy/hierarchy-top-meta-result.ttl");
    }

    @Test
    public void testComposite() throws IOException {
        checkAgainstExpected("test/composite/nestedComposite.json", "test/test-map.csv", "test/top.ttl");
        checkAgainstExpected("test/top.json", "test/test-map.csv", "test/top.ttl");
    }
    
    @Test
    public void testNested() throws IOException {
        checkAgainstExpected("test/nesting/nested-lets.json", "test/test-map.csv", "test/nesting/nested.ttl");
        checkAgainstExpected("test/nesting/nested-composite.json", "test/test-map.csv", "test/nesting/nested.ttl");
        
        checkAgainstExpected("test/nesting/nested-bind.json", "test/test-map.csv", "test/nesting/nested-bind.ttl");
    }
    
    @Test
    public void testMapping() throws IOException {
        checkAgainstExpected("test/mapping/dept-type.json", "test/mapping/dept-type-data.csv", "test/mapping/dept-type-result.ttl");
        checkAgainstExpected("test/mapping/dept-type.json", "test/mapping/dept-type-data-error.csv", "test/mapping/dept-type-result-error.ttl");
        assertNull( convert("test/mapping/dept-type-required.json", "test/mapping/dept-type-data-error.csv") );
        
        // Check map error reporting
        ConverterService service = new ConverterService();
        service.getDataContext().registerTemplate("test/simple-skos-template.json");
        service.put("$base", "http://example.com/");
        SimpleProgressMonitor monitor = new SimpleProgressMonitor();
        service.simpleConvert("test/mapping/dept-type-required.json", "test/mapping/dept-type-data-error.csv", monitor);
        assertFalse( monitor.succeeded() );
        assertEquals(2, monitor.getMessages().size());
//        System.out.println(monitor.getMessages().get(0));

        // Non-sensical example to provide test of asNodeURI in situ
        convert("test/mapping/dept-type-inv.json", "test/mapping/dept-type-data.csv");
        
        checkAgainstExpected("test/mapping/multi-test.json", "test/mapping/multi-test.csv", "test/mapping/multi-test-expected.ttl");
    }
    
    @Test
    public void testMultiValued() throws IOException {
        checkAgainstExpected("test/bugCases/split.json", "test/bugCases/array-field.csv", "test/bugCases/split-expected.ttl");
        
    }
    
    @Test
    public void testNumericMapping() throws IOException {
        checkAgainstExpected("test/mapping/sampling-points.json", "test/mapping/sampling-points.csv", "test/mapping/sampling-points.ttl");
        checkAgainstExpected("test/mapping/sampling-points-chained.json", "test/mapping/sampling-points.csv", "test/mapping/sampling-points-chained.ttl");
    }
    
    @Test
    public void testRDFMapping() throws IOException {
        checkAgainstExpected("test/mapping/map-rdf-test-root.json", "test/mapping/map-rdf-test.csv", "test/mapping/map-rdf-root-result.ttl");
        checkAgainstExpected("test/mapping/map-rdf-test.json", "test/mapping/map-rdf-test.csv", "test/mapping/map-rdf-result.ttl");
        checkAgainstExpected("test/mapping/map-rdf-test-type.json", "test/mapping/map-rdf-test.csv", "test/mapping/map-rdf-result.ttl");
        checkAgainstExpected("test/mapping/map-rdf-test-type2.json", "test/mapping/map-rdf-test.csv", "test/mapping/map-rdf-result.ttl");
        
        checkAgainstExpected("test/mapping/map-rdf-test-root-enrich.json", "test/mapping/map-rdf-test.csv", "test/mapping/map-rdf-root-enrich-result.ttl");
        checkAgainstExpected("test/mapping/map-rdf-test-root-describe.json", "test/mapping/map-rdf-test.csv", "test/mapping/map-rdf-root-describe-result.ttl");
    }
    
    @Test
    public void testBadURIs() throws IOException {
        expectError("test/validation/baduris-template1.json", "test/test-map.csv");
        expectError("test/validation/baduris-template2.json", "test/test-map.csv");
        expectError("test/validation/baduris-template3.json", "test/test-map.csv");
    }
    
    @Test
    public void testConditionalComposites() throws IOException {
        checkAgainstExpected("test/composite/cond-composite.json", "test/test-map.csv", "test/test-map-result.ttl");
        checkAgainstExpected("test/composite/cond-composite-err.json", "test/test-map.csv", "test/test-map-result.ttl");
    }
    
    @Test
    public void testDateHandling() throws IOException {
        checkAgainstExpected("test/dates/date.json", "test/dates/date.csv", "test/dates/date.ttl");
        checkAgainstExpected("test/dates/date2.yaml", "test/dates/date.csv", "test/dates/date2.ttl");
    }
    
    @Test
    public void testPrefixDeclaration() throws IOException {
        checkAgainstExpected("test/composite/prefix-composite.json", "test/test-map.csv", "test/composite/prefix-composite-expected.ttl");
    }
    
    @Test
    public void testRawColumns() throws IOException {
        checkAgainstExpected("test/composite/cond-composite.json", "test/rawColumns/test1.csv", "test/rawColumns/expected.ttl");
    }
    
    @Test
    public void testLoadDirs() throws IOException {
        checkAgainstExpected("test/mapping/dept-type-rel.json", "test/mapping/dept-type-data.csv", ".,test/mapping", "test/mapping/dept-type-result.ttl");
    }
    
    @Test
    public void testBugs() throws IOException {
        checkAgainstExpected("test/bugCases/mailto.json", "test/test-ok.csv", "test/bugCases/mailto.ttl");
        checkAgainstExpected("test/bugCases/lang.json", "test/test-ok.csv", "test/bugCases/lang.ttl");
        checkAgainstExpected("test/bugCases/null.json", "test/test-ok.csv", "test/bugCases/null.ttl");
    }

    public static Model convert(String templateFile, String dataFile) throws IOException {
        return convert(templateFile, dataFile, null);
    }

    public static Model convert(String templateFile, String dataFile, String loadDirs) throws IOException {
        ConverterService service = new ConverterService();
        if (loadDirs != null) {
            service.setLoadDirectories(loadDirs);
        }
        service.getDataContext().registerTemplate("test/simple-skos-template.json");
        service.put("$base", "http://example.com/");
        SimpleProgressMonitor monitor = new SimpleProgressMonitor();
        Model m = service.simpleConvert(templateFile, dataFile, monitor);
//        for (ProgressMessage message : monitor.getMessages()) System.err.println(message.toString());
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
        checkAgainstExpected(templateFile, dataFile, null, resultFile);
    }
    
    public static void checkAgainstExpected(String templateFile, String dataFile, String loadDirs, String resultFile) throws IOException {
        Model m = convert(templateFile, dataFile, loadDirs);
        assertNotNull(m);
        String DUMMY = "http://example.com/DONOTUSE/";
        Model expected = FileManager.get().loadModel(resultFile, DUMMY, "Turtle");
        expected = RDFUtil.mapNamespace(expected, DUMMY, "");
        boolean same = m.isIsomorphicWith(expected);
//        boolean rev = expected.isIsomorphicWith(m);
        if (!same) {
            System.err.println("Result mismatch, result was:");
            m.write(System.err, "Turtle");
            System.err.println("Expected:");
            expected.write(System.err, "Turtle");
        }
        assertTrue( same );
    }
}
