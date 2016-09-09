/******************************************************************
 * File:        Temp.java
 * Created by:  Dave Reynolds
 * Created on:  8 Sep 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.epimorphics.geo.GeoPoint;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Used for experimentation or one off hacks
 */
public class Temp {
    public static final String src = "/home/der/tmp/Data/merge.csv"; 
//    public static final String src = "/home/der/tmp/Data/testpc.csv"; 
    public static final String dest = "/home/der/tmp/Data/postcodes.csv"; 

    public static final Pattern PCMERGED = Pattern.compile( "([A-Z]{2}[0-9]{2})([0-9][A-Z]{2})" );
    public static void main(String[] args) throws IOException {
        Reader in = new FileReader( src );
        CSVReader reader = new CSVReader(in);
        Writer out = new FileWriter( dest );
        CSVWriter writer = new CSVWriter(out);
        String[] line = null;
        while ( (line = reader.readNext()) != null ) {
            long easting = Long.parseLong( line[1] );
            long northing = Long.parseLong( line[2] );
            GeoPoint point = GeoPoint.fromEastingNorthing(easting, northing);
            
            String postcode = line[0];
            Matcher matcher = PCMERGED.matcher(postcode);
            if (matcher.matches()) {
                postcode = matcher.group(1) + " " + matcher.group(2);
            }
            postcode = postcode.replaceAll(" +", " ");
            
            String[] lineout = new String[4];
            lineout[0] = postcode;
            lineout[1] = line[1];
            lineout[2] = line[2];
            lineout[3] = point.getGridRefString().replace(" ", "");
            writer.writeNext( lineout );
        }
        reader.close();
        writer.close();
    }
}
