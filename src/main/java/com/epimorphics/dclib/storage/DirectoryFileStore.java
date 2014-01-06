/******************************************************************
 * File:        DirectoryFileStore.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.epimorphics.util.EpiException;
import com.epimorphics.util.FileUtil;

/**
 * File store implementation that uses simple local directories.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DirectoryFileStore implements FileStore {
    protected File root;
    
    public void setRoot(String rootDir) {
        root = new File(rootDir);
        if ( ! (root.exists() && root.canRead() && root.canWrite()) ) {
            throw new EpiException("Can't access file store root: " + rootDir);
        }
    }
    
    @Override
    public List<String> list(String folder) {
        String[] files = root.list();
        List<String> results = new ArrayList<>( files.length );
        for (String file : files) {
            results.add(file);
        }
        return results;
    }

    @Override
    public boolean exists(String path) {
        return new File(root, path).exists();
    }

    @Override
    public InputStream read(String path) throws IOException {
        return new FileInputStream( new File(root, path) );
    }

    @Override
    public void makeFolder(String path) throws IOException {
        FileUtil.ensureDir( new File(root, path).getPath() );
    }

    @Override
    public OutputStream write(String path) throws IOException {
        return new FileOutputStream(new File(root, path), false);
    }

    @Override
    public OutputStream write(String path, boolean append) throws IOException {
        return new FileOutputStream(new File(root, path), append);
    }

    @Override
    public void deleteFolder(String path) throws IOException {
        FileUtil.deleteDirectory( new File(root,path) );
    }

}
