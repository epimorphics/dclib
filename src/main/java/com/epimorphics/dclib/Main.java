/******************************************************************
 * File:        Main.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static final String DEBUG_FLAG = "--debug";
    public static final String STREAMING_FLAG = "--streaming";
    public static final String NTRIPLE_FLAG = "--ntriples";
    public static final String NULL_ROW_FLAG = "--abortIfRowFails";
    public static final String NTHREADS_FLAG = "--nThreads";
    public static final String BATCH_FLAG = "--batch";
    
    public static void main(String[] argsIn) throws IOException {
        CommandArgs cargs = new CommandArgs();
        String batchFile = null;
        
        List<String> args = new ArrayList<>(argsIn.length);
        for (String arg : argsIn) args.add(arg);
        
        if (args.contains(DEBUG_FLAG)) {
            cargs.setDebug(true);
            args.remove(DEBUG_FLAG);
        }
        if (args.contains(STREAMING_FLAG)) {
            cargs.setStreaming(true);
            args.remove(STREAMING_FLAG);
        }
        if (args.contains(NTRIPLE_FLAG)) {
            cargs.setNtriples(true);
            args.remove(NTRIPLE_FLAG);
        }
        if (args.contains(NULL_ROW_FLAG)) {
            cargs.setNullRowAborts(true);
            args.remove(NULL_ROW_FLAG);
        }
        if (args.contains(NTHREADS_FLAG)) {
            int i = args.indexOf(NTHREADS_FLAG);
            try {
                cargs.setnThreads(  Integer.parseInt(args.get(i+1)) );
                args.remove(i);   // Flag
                args.remove(i);   // Argument to flag (removing flag shunts it down)
            } catch (Exception e) {
                System.err.println("No legal argument for --nThreads");
                System.exit(1);
            }
        }
        if (args.contains(BATCH_FLAG)) {
            int i = args.indexOf(BATCH_FLAG);
            if (i == args.size()) {
                System.err.println("No legal argument for --batch");
                System.exit(1);
            }
            batchFile = args.get(i+1);
            args.remove(i);   // Flag
            args.remove(i);   // Argument to flag (removing flag shunts it down)
        }

        if (batchFile == null && args.size() < 2) {
            System.err.println("Usage:  java -jar dclib.jar [--debug] [--streaming] [--ntriples] [--abortIfRowFails] template.json ... data.csv");
            System.err.println("   or:  java -jar dclib.jar [--debug] [--streaming] [--ntriples] [--abortIfRowFails] [--nThreads 4] --batch batchFile");
            System.exit(1);
        }
        
        if (batchFile == null) {
            String templateName = args.get(0);
            String dataFile = args.get( args.size() -1 );
            for (int i = 1; i < args.size() - 1; i++) {
                cargs.addAuxTemplate(args.get(i));
            }
            Command command = new Command(cargs, templateName, dataFile, System.out);
            if (!command.call()) {
                System.exit(1);
            }
        } else {
            ExecutorService exec = Executors.newFixedThreadPool( cargs.getnThreads() );
            BufferedReader in = new BufferedReader(new FileReader(batchFile));
            List<Future<Boolean>> jobs = new ArrayList<>();
            String line = null;
            while( (line = in.readLine()) != null ) {
                if (line.trim().startsWith("#")) continue;   // Skip as comment line
                String[] batch = line.trim().split("\\s+");
                if (batch.length != 2) {
                    System.err.println("Illegal line in batch file: " + line);
                    System.exit(1);
                }
                jobs.add( exec.submit( new Command(cargs, batch[0], batch[1]) ) );
            }
            in.close();
            try {
                boolean success = true;
                for (Future<Boolean> job: jobs) {
                    success = success & job.get();
                }
                exec.shutdown();
                if (!success) {
                    System.exit(1);
                }
            } catch (Exception e) {
                System.err.println("Batch execution interrupted: " + e);
                System.exit(1);
            }
        }
    }
    
    public static class Command implements Callable<Boolean>{
        CommandArgs args;
        String dataFile;
        String templateName;
        OutputStream out;
        
        public Command(CommandArgs args, String templateName, String dataFile, OutputStream out) {
            this.args = args;
            this.templateName = templateName;
            this.dataFile = dataFile;
            this.out = out;
        }
        
        public Command(CommandArgs args, String templateName, String dataFile) {
            this(args, templateName, dataFile, null);
        }
        
        private void openOutputStream() throws IOException {
            if (out == null) {
                String outf = dataFile.replaceFirst("\\.csv$", "");
                outf += args.isNtriples() ? ".nt" : ".ttl";
                out = new FileOutputStream(outf);
                System.err.println("Processing " + dataFile + " to " + outf);
            }
        }
        
        @Override
        public Boolean call()  {
            try {
                openOutputStream();
                
                ConverterService service = new ConverterService();
                DataContext dc = service.getDataContext();
                for(String template : args.getAuxTemplates()) {
                    Template aux = TemplateFactory.templateFrom(template, dc);
                    dc.registerTemplate(aux);
                }
                
                SimpleProgressMonitor reporter = new LiveProgressMonitor();
    
                boolean succeeded = false;
                if (args.isStreaming()) {
                    Template template = TemplateFactory.templateFrom(templateName, dc);
                    
                    File dataFileF = new File(dataFile);
                    String filename = dataFileF.getName();
                    String filebasename = NameUtils.removeExtension(filename);
                    dc.getGlobalEnv().put(ConverterProcess.FILE_NAME, filename);
                    dc.getGlobalEnv().put(ConverterProcess.FILE_BASE_NAME, filebasename);
                    InputStream is = new BOMInputStream( new FileInputStream(dataFileF) );
                    
                    ConverterProcess process = new ConverterProcess(dc, is);
                    process.setDebug( args.isDebug() );
                    process.setTemplate( template );
                    process.setMessageReporter( reporter );
                    process.setAllowNullRows( !args.isNullRowAborts() );
                    
                    StreamRDF stream = StreamRDFWriter.getWriterStream(out,  args.isNtriples() ? Lang.NTRIPLES : Lang.TURTLE);
                    process.setOutputStream( stream );
                    
                    succeeded = process.process();
                    stream.finish();
                    
                } else {
                    Model m = service.simpleConvert(templateName, dataFile, reporter, args.isDebug(), !args.isNullRowAborts());
                    if (m != null) {
                        m.write(out, args.isNtriples() ? RDFLanguages.strLangNTriples : RDFLanguages.strLangTurtle);
                        succeeded=true;
                    } else {
                        succeeded = false;
                    }
                }
                out.close();
                if (!succeeded) {
                    System.err.println("Failed to convert data");
                    for (ProgressMessage message : reporter.getMessages()) {
                        System.err.println(message.toString());
                    }
                    return false;
                }
                return true;
            } catch (Exception e) {
                System.err.println("Failed: " + e);
                return false;
            }
        }
    }
    
    public static class CommandArgs {
        boolean debug = false;
        boolean streaming = false;
        boolean ntriples = false;
        boolean nullRowAborts = false;
        int nThreads = 4;
        List<String> auxTemplates = new ArrayList<>();
        
        public boolean isDebug() {
            return debug;
        }
        public void setDebug(boolean debug) {
            this.debug = debug;
        }
        public boolean isStreaming() {
            return streaming;
        }
        public void setStreaming(boolean streaming) {
            this.streaming = streaming;
        }
        public boolean isNtriples() {
            return ntriples;
        }
        public void setNtriples(boolean ntriples) {
            this.ntriples = ntriples;
        }
        public boolean isNullRowAborts() {
            return nullRowAborts;
        }
        public void setNullRowAborts(boolean nullRowAborts) {
            this.nullRowAborts = nullRowAborts;
        }
        public int getnThreads() {
            return nThreads;
        }
        public void setnThreads(int nThreads) {
            this.nThreads = nThreads;
        }
        public void addAuxTemplate(String template) {
            auxTemplates.add(template);
        }
        public List<String> getAuxTemplates() {
            return auxTemplates;
        }
        
    }
    
    
}
