/******************************************************************
 * File:        TestConverterProcess.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.epimorphics.dclib.sources.LineCount;
import org.junit.jupiter.api.Test;

import com.epimorphics.dclib.values.ValueNumber;
import com.epimorphics.tasks.ProgressMessage;
import com.epimorphics.tasks.ProgressMonitor;
import com.epimorphics.tasks.SimpleProgressMonitor;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class TestConverterProcess {
    final static String BASE = "http://example.com/";
    
    @Test
    public void testBaseCase() throws IOException {
        SimpleProgressMonitor monitor = new SimpleProgressMonitor();
        ConverterProcess process = setUp("test/test-ok.csv");
        process.setMessageReporter(monitor);
        process.setBatchSize(1);
        boolean ok = process.process();
        assertTrue(ok);
        assertTrue( contains(process, "1", "a", "10") );
        assertTrue( contains(process, "2", "b", "20") );

        List<ProgressMessage> msgs = monitor.getMessages();
        assertEquals(3, msgs.size());
        assertEquals("Processing row 1 of 2 (0%)", msgs.get(0).getMessage());
        assertEquals("Processing row 2 of 2 (50%)", msgs.get(1).getMessage());
        assertEquals("Processed 2 rows", msgs.get(2).getMessage());
    }
    
    @Test
    public void testFailureDetection() throws IOException {
        SimpleProgressMonitor monitor = new SimpleProgressMonitor();
        ConverterProcess process = setUp("test/test-fail.csv");
        process.setMessageReporter(monitor);
        process.setBatchSize(2);
        @SuppressWarnings("unused")
        boolean ok = process.process();
//        assertFalse(ok);  - failed row convert no longer itself fatal
        assertTrue( contains(process, "1", "a", "10") );
        assertTrue( contains(process, "2", "b", "20") );
        assertTrue( contains(process, "4", "d", "10") );

        List<ProgressMessage> msgs = monitor.getMessages();
        assertEquals(4, msgs.size());
        assertEquals("Processing row 1 of 4 (0%)", msgs.get(0).getMessage());
        assertEquals("Processing row 3 of 4 (50%)", msgs.get(1).getMessage());
        assertEquals("Warning: no templates matched line 4, com.epimorphics.dclib.framework.NullResult: Value exceeds test threshold of 20", msgs.get(2).getMessage());
        assertEquals(4, msgs.get(2).getLineNumber());
        assertEquals("Processed 4 rows", msgs.get(3).getMessage());
    }
    
    private ConverterProcess setUp(String file) throws IOException {
        int lineCount = LineCount.file(new File(file));
        InputStream is = new FileInputStream(file);
        ConverterProcess process = new ConverterProcess(new DataContext(), is);
        process.setTemplate( new TestTemplate() );
        process.setRowCount(lineCount - 1);
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
        System.err.println(String.format("State: %s (%d%%)", monitor.getState(), monitor.getProgress()));
        for (ProgressMessage message : monitor.getMessages()) {
            System.err.println("  " + message);
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
        public Node convertRow(ConverterProcess config, BindingEnv row, int rowNumber) {
            Node root = NodeFactory.createURI( BASE + rowNumber );
            for (String key : row.keySet()) {
                Node property = NodeFactory.createURI( BASE + key );
                Object value = row.get(key);
                if (value instanceof ValueNumber) {
                    if ( ((ValueNumber)value).toNumber().intValue() > 20) {
                        throw new NullResult("Value exceeds test threshold of 20");
                    }
                }
                Node vnode = NodeFactory.createLiteralString( value.toString() );
                config.getOutputStream().triple( Triple.create(root, property, vnode) );
            }
            return root;
        }

        @Override
        public String getName() {
            return "test-template";
        }

        @Override
        public void setName(String name) {
            // No-op
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public boolean isApplicableTo(ConverterProcess config, BindingEnv row, int rowNumber) {
            return true;
        }

        @Override
        public void preamble(ConverterProcess config, BindingEnv env) {
        }

        @Override
        public String getSource() {
            return null;
        }

        @Override
        public List<String> required() {
            return null;
        }

        @Override
        public List<String> optional() {
            return null;
        }

        @Override
        public Template deref() {
            return this;
        }

    }
}
