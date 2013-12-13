/******************************************************************
 * File:        TestLexIndex.java
 * Created by:  Dave Reynolds
 * Created on:  13 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.sources;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestLexIndex {
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
        LexIndex<String> table = new LexIndex<>();
        table.put("theFirstKey", "first");
        table.put("first key", "second");
        table.put("anotherKey", "third");
        assertNull( table.lookup("no match") );
        assertEquals("third", table.lookup("another key"));
        assertEquals("third", table.lookup("another-key"));
        assertEquals("third", table.lookup("anotherKey "));
        assertEquals("second", table.lookup("first key"));
        assertEquals("second", table.lookup("firstKey"));
        assertEquals("first", table.lookup("theFirstKey"));
    }
    
}
