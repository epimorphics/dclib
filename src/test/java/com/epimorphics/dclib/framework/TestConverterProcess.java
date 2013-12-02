/******************************************************************
 * File:        TestConverterProcess.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import com.epimorphics.tasks.ProgressMessage;
import com.epimorphics.tasks.ProgressMonitor;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class TestConverterProcess {
    final static String BASE = "http://example.com/";
    
    @Test
    public void testBaseCase() throws IOException {
        ConverterProcess process = setUp("test/test-ok.csv");
        boolean ok = process.process();
        assertTrue(ok);
        assertTrue( contains(process, "1", "a", "10") );
        assertTrue( contains(process, "2", "b", "20") );
    }
    
    @Test
    public void testFailureDetection() throws IOException {
        SimpleProgressMonitor monitor = new SimpleProgressMonitor();
        ConverterProcess process = setUp("test/test-fail.csv");
        process.setMessageReporter(monitor);
        boolean ok = process.process();
        assertFalse(ok);
        assertTrue( contains(process, "1", "a", "10") );
        assertTrue( contains(process, "2", "b", "20") );
        assertTrue( contains(process, "4", "d", "10") );
        List<ProgressMessage> messages = monitor.getMessages();
        assertEquals(1, messages.size());
        ProgressMessage message = messages.get(0);
        assertEquals(3, message.getLineNumber());
        assertTrue( message.getMessage().contains("Value exceeds test threshold") );
    }
    
    private ConverterProcess setUp(String file) throws IOException {
        InputStream is = new FileInputStream(file);
        ConverterProcess process = new ConverterProcess(new DataContext(), is);
        process.setTemplate( new TestTemplate() );
        return process;
    }
    
    private Resource res(String suffix) {
        return ResourceFactory.createResource( BASE + suffix );
    }
    
    private Property p(String suffix) {
        return ResourceFactory.createProperty( BASE + suffix );
    }
    
    private boolean contains(ConverterProcess process, String row, String name, String value) {
        Model m = process.getModel();
        return m.contains(res(row), p("name"), name)
                && m.contains(res(row), p("value"), value);
    }
    
    public static void printMessages(ProgressMonitor monitor) {
        System.out.println(String.format("State: %s (%d%%)", monitor.getState(), monitor.getProgress()));
        for (ProgressMessage message : monitor.getMessages()) {
            System.out.println("  " + message);
        }
    }

    // Dummy template to test the calling harness
    // Will raise error if the value column is a number above 20
    public static final class TestTemplate implements Template {
        
        @Override
        public boolean isApplicableTo(String[] columnNames) {
            return true;
        }

        @Override
        public boolean convertRow(ConverterProcess config, BindingEnv row, int rowNumber) {
            Node root = NodeFactory.createURI( BASE + rowNumber );
            for (String key : row.keySet()) {
                Node property = NodeFactory.createURI( BASE + key );
                Object value = row.get(key);
                if (value instanceof Number) {
                    if ( ((Number)value).intValue() > 20) {
                        throw new EpiException("Value exceeds test threshold of 20");
                    }
                }
                Node vnode = NodeFactory.createLiteral( value.toString() );
                config.getOutputStream().triple( new Triple(root, property, vnode) );
            }
            return true;
        }

        @Override
        public String getName() {
            return "test-template";
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public boolean isApplicableTo(BindingEnv row) {
            return true;
        }
        
    }
}
