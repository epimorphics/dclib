/******************************************************************
 * File:        Main.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib;

import java.io.IOException;

import com.epimorphics.dclib.framework.ConverterService;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.tasks.LiveProgressMonitor;
import com.epimorphics.tasks.ProgressMessage;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.hp.hpl.jena.rdf.model.Model;

public class Main {
    
    public static void main(String[] args) throws IOException {
        boolean debug = false;
        
        // TODO proper command line parsing
        int argsOffset = 0;
        if (args.length >= 3 && args[0].equals("--debug")) {
            debug = true;
            argsOffset = 1;
        }
        if (args.length - argsOffset < 2) {
            System.err.println("Usage:  java -jar dclib.jar [--debug] template.json ... data.csv");
            System.exit(1);
        }
        String templateName = args[argsOffset++];
        String dataFile = args[args.length - 1];
        
        ConverterService service = new ConverterService();
        DataContext dc = service.getDataContext();
        for (int i = argsOffset; i < args.length - 1; i++) {
            Template aux = TemplateFactory.templateFrom(args[i], dc);
            dc.registerTemplate(aux);
        }
        
        SimpleProgressMonitor reporter = new LiveProgressMonitor();
        Model m = service.simpleConvert(templateName, dataFile, reporter, debug);
        if (m != null) {
            m.write(System.out, "Turtle");
        } else {
            System.err.println("Failed to convert data");
            for (ProgressMessage message : reporter.getMessages()) {
                System.err.println(message.toString());
            }
            System.exit(1);
        }
        
    }
}
