package com.epimorphics.dclib.sources;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class LineCount {
    public static int file(File f) throws IOException {
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(f))) {
            lnr.skip(Long.MAX_VALUE);
            return lnr.getLineNumber();
        }
    }
}
