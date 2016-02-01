/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** 
 * Adapted from org.apache.jena.riot.system.stream.LocatorHTTP 
 * 
 * Removes "*\/*;q=0.5" from http accept header in order to avoid attracting
 * HTML responses when an RDF response is expected.
 * 
 */

package com.epimorphics.dclib.framework ;

import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.web.HttpOp ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Support for resources using the "http:" and "https" schemes */
public class LocatorHTTP extends org.apache.jena.riot.system.stream.LocatorURL {
    private static Logger         log         = LoggerFactory.getLogger(LocatorHTTP.class) ;
    private static final String[] schemeNames = {"http", "https"} ;
    private static final String   acceptHeader = "text/turtle,application/n-triples;q=0.9,application/ld+json;q=0.8,application/rdf+xml;q=0.7";

    public LocatorHTTP() {
        super(schemeNames) ;
    }

    @Override
    protected Logger log() { return log ; }

    @Override
    public TypedInputStream performOpen(String uri) {
        if ( uri.startsWith("http://") || uri.startsWith("https://") )
            return HttpOp.execHttpGet(uri, acceptHeader) ;
        return null ;
    }

    @Override
    public String getName() {
        return "DCLIB-LocatorHTTP" ;
    }

    @Override
    public int hashCode() {
        return 57 ;
    }
}