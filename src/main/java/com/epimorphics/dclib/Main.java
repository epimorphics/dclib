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
import com.hp.hpl.jena.rdf.model.Model;

public class Main {
    
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage:  java -jar dclib.jar  template.json data.ttl");
            System.exit(1);
        }
        String templateName = args[0];
        String dataFile = args[1];
        
        ConverterService service = new ConverterService();
        service.setSilent(true);
        Model m = service.simpleConvert(templateName, dataFile);
        if (m != null) {
            m.write(System.out, "Turtle");
        } else {
            System.err.println("Failed to convert data");
            System.exit(1);
        }
        
    }
}
