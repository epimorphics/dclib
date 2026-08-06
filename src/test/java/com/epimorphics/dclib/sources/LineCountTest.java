package com.epimorphics.dclib.sources;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class LineCountTest {

    @Test
    public void file_emptyFile_returnsZero() throws IOException {
        File f = Files.createTempFile("lines", "txt").toFile();
        assertEquals(0, LineCount.file(f));
    }

    @Test
    public void file_csvWithRows_returnsHeaderAndRowCount() throws IOException {
        File f = Files.createTempFile("lines", "txt").toFile();
        try (FileWriter w = new FileWriter(f)) {
            w.append("name,id\n");
            w.append("alice,1\n");
            w.append("bob,2");
        }
        assertEquals(3, LineCount.file(f));
    }

    @Test
    public void file_withoutNewLineEnding_returnsLineCount() throws IOException {
        File f = Files.createTempFile("lines", "txt").toFile();
        try (FileWriter w = new FileWriter(f)) {
            w.append("hi\n");
            w.append("hello\n");
            w.append("bonjour\n");
            w.append("hola");
        }
        assertEquals(4, LineCount.file(f));
    }

    @Test
    public void file_withNewLineEnding_returnsLineCount() throws IOException {
        File f = Files.createTempFile("lines", "txt").toFile();
        try (FileWriter w = new FileWriter(f)) {
            w.append("hi\n");
            w.append("hello\n");
            w.append("bonjour\n");
            w.append("hola\n");
        }
        assertEquals(4, LineCount.file(f));
    }
}