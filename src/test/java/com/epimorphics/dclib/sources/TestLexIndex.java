/******************************************************************
 * File:        TestLexIndex.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestLexIndex {
    
	LexIndex<String> table = new LexIndex<>();
    
    @BeforeEach
    public void setUp() {
    	table.put("theFirstKey", "first");
        table.put("first key", "second");
        table.put("anotherKey", "third");
    }

    @Test
    public void testNormalize() {
        assertEquals("", LexIndex.normalize(""));
        assertEquals("a aa bc", LexIndex.normalize("a aa and bc"));
        assertEquals("this is some punc", LexIndex.normalize("this-is:some_punc"));
        assertEquals("world ends", LexIndex.normalize("theWorldEnds"));
        assertEquals("you me", LexIndex.normalize("you and     me"));
        assertEquals("you me", LexIndex.normalize("you      \n & me"));
    }
    
    @Test
    public void testLookup() {
        assertNull( table.lookup("no match") );
        assertEquals("third", table.lookup("another key"));
        assertEquals("third", table.lookup("another-key"));
        assertEquals("third", table.lookup("anotherKey "));
        assertEquals("second", table.lookup("first key"));
        assertEquals("second", table.lookup("firstKey"));
        assertEquals("first", table.lookup("theFirstKey"));
    }
    
    @Test
    public void testLookupAll() {
        table.put("anotherKey", "fourth");
        Collection<String> results = table.lookupAll("another key");
        assertTrue(results.contains("third"));
        assertTrue(results.contains("fourth"));
        assertEquals(2, results.size());
    }
    
}
