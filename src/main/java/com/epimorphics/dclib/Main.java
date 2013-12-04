/******************************************************************
 * File:        Main.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib;

import java.io.FileOutputStream;
import java.io.IOException;

import com.epimorphics.dclib.framework.ConverterService;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.tasks.ProgressMessage;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.util.FileUtil;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.Closure;
import com.hp.hpl.jena.vocabulary.RDF;

public class Main {
    
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage:  java -jar dclib.jar  template.json data.ttl [split-directory]");
            System.exit(1);
        }
        String templateName = args[0];
        String dataFile = args[1];
        String splitDir = null;
        if (args.length > 2) {
            splitDir = args[2];
        }
        
        ConverterService service = new ConverterService();
        SimpleProgressMonitor reporter = new SimpleProgressMonitor();
        Model m = service.simpleConvert(templateName, dataFile, reporter);
        if (m != null) {
            if (splitDir == null) {
                m.write(System.out, "Turtle");
            } else {
                FileUtil.ensureDir(splitDir);
                for (ResIterator ri = m.listSubjectsWithProperty(RDF.type); ri.hasNext(); ) {
                    Resource root = ri.next();
                    Model split = Closure.closure(root, false);
                    String fname = root.getURI().contains("/") ? RDFUtil.getLocalname(root) : NameUtils.safeName(root.getURI());
                    FileOutputStream out = new FileOutputStream(splitDir + "/" + fname + ".ttl"); 
                    split.write(out, "Turtle");
                    out.close();
                }
            }
        } else {
            System.err.println("Failed to convert data");
            for (ProgressMessage message : reporter.getMessages()) {
                System.err.println(message.toString());
            }
            System.exit(1);
        }
        
    }
}
