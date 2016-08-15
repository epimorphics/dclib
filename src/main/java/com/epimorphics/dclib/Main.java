/******************************************************************
 * File:        Main.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.ConverterService;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.tasks.LiveProgressMonitor;
import com.epimorphics.tasks.ProgressMessage;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.util.NameUtils;
import org.apache.jena.rdf.model.Model;

public class Main {
    public static final String DEBUG_FLAG = "--debug";
    public static final String STREAMING_FLAG = "--streaming";
    public static final String NTRIPLE_FLAG = "--ntriples";
    public static final String NULL_ROW_FLAG = "--abortIfRowFails";
    
    public static void main(String[] argsIn) throws IOException {
        boolean debug = false;
        boolean streaming = false;
        boolean ntriples = false;
        boolean nullRowAborts = false;
        
        // TODO proper command line parsing
        
        List<String> args = new ArrayList<>(argsIn.length);
        for (String arg : argsIn) args.add(arg);
        
        if (args.contains(DEBUG_FLAG)) {
            debug = true;
            args.remove(DEBUG_FLAG);
        }
        if (args.contains(STREAMING_FLAG)) {
            streaming = true;
            args.remove(STREAMING_FLAG);
        }
        if (args.contains(NTRIPLE_FLAG)) {
            ntriples = true;
            args.remove(NTRIPLE_FLAG);
        }
        if (args.contains(NULL_ROW_FLAG)) {
            nullRowAborts = true;
            args.remove(NULL_ROW_FLAG);
        }

        if (args.size() < 2) {
            System.err.println("Usage:  java -jar dclib.jar [--debug] [--streaming] [--ntriples] [--abortIfRowFails] template.json ... data.csv");
            System.exit(1);
        }
        String templateName = args.get(0);
        String dataFile = args.get( args.size() -1 );
        
        ConverterService service = new ConverterService();
        DataContext dc = service.getDataContext();
        for (int i = 1; i < args.size() - 1; i++) {
            Template aux = TemplateFactory.templateFrom(args.get(i), dc);
            dc.registerTemplate(aux);
        }
        
        SimpleProgressMonitor reporter = new LiveProgressMonitor();

        boolean succeeded = false;
        if (streaming) {
            Template template = TemplateFactory.templateFrom(templateName, dc);
            
            File dataFileF = new File(dataFile);
            String filename = dataFileF.getName();
            String filebasename = NameUtils.removeExtension(filename);
            dc.getGlobalEnv().put(ConverterProcess.FILE_NAME, filename);
            dc.getGlobalEnv().put(ConverterProcess.FILE_BASE_NAME, filebasename);
            InputStream is = new BOMInputStream( new FileInputStream(dataFileF) );
            
            ConverterProcess process = new ConverterProcess(dc, is);
            process.setDebug(debug);
            process.setTemplate( template );
            process.setMessageReporter( reporter );
            process.setAllowNullRows( !nullRowAborts );
            
            StreamRDF stream = StreamRDFWriter.getWriterStream(System.out,  ntriples ? Lang.NTRIPLES : Lang.TURTLE);
            process.setOutputStream( stream );
            
            succeeded = process.process();
            stream.finish();
            
        } else {
            Model m = service.simpleConvert(templateName, dataFile, reporter, debug, !nullRowAborts);
            if (m != null) {
                m.write(System.out, ntriples ? RDFLanguages.strLangNTriples : RDFLanguages.strLangTurtle);
                succeeded=true;
            } else {
                succeeded = false;
            }
        }
        if (!succeeded) {
            System.err.println("Failed to convert data");
            for (ProgressMessage message : reporter.getMessages()) {
                System.err.println(message.toString());
            }
            System.exit(1);
        }
    }
    
    
}
