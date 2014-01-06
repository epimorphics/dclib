/******************************************************************
 * File:        FileStore.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface for a storage facility where source data, templates and
 * so forth can be stored for access. Will want implementations for shared
 * storage services like S3 as well as simple file system.
 * <p>
 * All implementations support a path syntax with "/" as the separator.
 * </p>
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface FileStore {

    /**
     * List all files/objects in a named folder
     */
    public List<String> list(String folder);
    
    /**
     * Return true if a folder or file exists at the given location
     */
    public boolean exists(String path);
    
    /**
     * Open a file/object for reading
     */
    public InputStream read(String path) throws IOException;
    
    /**
     * Create a new folder 
     */
    public void makeFolder(String path) throws IOException;
    
    /**
     * Delete a folder
     */
    public void deleteFolder(String path) throws IOException;
    
    /**
     * Create a new file/object for writing. Replaces any existing file
     */
    public OutputStream write(String path) throws IOException;
    
    /**
     * Create a new file/object for writing
     * @param append if true then appends to an existing file
     */
    public OutputStream write(String path, boolean append) throws IOException;
}
