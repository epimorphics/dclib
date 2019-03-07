/******************************************************************
 * File:        LexIndex.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.util.OneToManyMap;

/**
 * Utility to provide in-memory lookups of values based on a 
 * normalized lexical form.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class LexIndex<T> {
    protected OneToManyMap<String, Record> table = new OneToManyMap<>();
    
    /**
     * Record a value in the index.
     */
    public void put(String key, T value) {
        Record r = new Record(key, value);
        table.put(r.normalized, r);
    }

    /**
     * Return the matching value or null if none is found. If there
     * are multiple values at the same normalized key then the closest match
     * to the un-normalized key will be used. Where the notion of "closest" is to be defined.
     */
    public T lookup(String key) {
        String nkey = normalize(key);
        Iterator<Record> i = table.getAll(nkey);
        if (i.hasNext()) {
            Record match = i.next();
            // There is a result but is there more than one
            if (i.hasNext()) {
                // Yes, so search for best
                int bestDist = StringUtils.getLevenshteinDistance(key, match.key);
                Record best = match;
                while (i.hasNext()) {
                    match = i.next();
                    int dist = StringUtils.getLevenshteinDistance(key, match.key);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = match;
                    }
                }
                return best.value;
            } else {
                return match.value;
            }
        }
        return null;
    }
    
    public Collection<T> lookupAll(String key) {
    	String nkey = normalize(key);
    	Iterator<Record> i = table.getAll(nkey);
    	List<T> ret = new ArrayList<T>();
    	while(i.hasNext()) {
    		ret.add(i.next().value);    		
    	}
    	return ret; 
	}
    
    /**
     * Normalize a string for more robust matching, assumes English text.
     * The normalizations are:
     * <ul>
     *   <li>split camel case (including internal digits) to separate tokens</li>
     *   <li>normalize white space to single spaces between all tokens</li>
     *   <li>map to lower case</li>
     *   <li>strip punctuation</li>
     *   <li>strip stop words "the", "and"</li>
     * </ul>
     * @param orig
     * @return
     */
    public static String normalize(String orig) {
        StringBuilder norm = new StringBuilder();
        boolean started = false;
        
        for (String token : StringUtils.splitByCharacterTypeCamelCase(orig)) {
            token = token.trim().toLowerCase();
            if ( ! token.isEmpty() ) {
                if (! STOP.matcher(token).matches()) {
                    if (started) {
                        norm.append(" ");
                    } else {
                        started = true;
                    }
                    norm.append(token);
                }
            }
        }
        return norm.toString();
    }
    private static final Pattern STOP = Pattern.compile("[\\p{Punct}]*|the|and");
    
    public final class Record {
        protected String key;
        protected String normalized;
        protected T value;
        
        public Record(String key, T value) {
            this.key = key;
            this.normalized = normalize(key);
            this.value = value;
        }
    }
}
